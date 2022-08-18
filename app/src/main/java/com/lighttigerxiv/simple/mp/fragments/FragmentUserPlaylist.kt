@file:Suppress("DEPRECATION")

package com.lighttigerxiv.simple.mp.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lighttigerxiv.simple.mp.*
import com.lighttigerxiv.simple.mp.others.ColorFunctions
import com.lighttigerxiv.simple.mp.others.GetSongs


class FragmentUserPlaylist : Fragment() {

    //Main
    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var clMain: ConstraintLayout
    private lateinit var btBack: Button
    private lateinit var btCancel: Button
    private lateinit var btSave: Button
    private lateinit var ibMore: ImageButton
    private lateinit var sivPlaylistArt: ShapeableImageView
    private lateinit var tvPlaylistName: TextView
    private lateinit var etPlaylistName: EditText
    private lateinit var rvSongs: RecyclerView


    private var userPlaylists = ArrayList<Playlist>()
    private lateinit var playlist: Playlist
    private var playlistID = -1
    private var songsList = ArrayList<Song>()
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false
    private lateinit var adapterRVPlaylistSongs: AdapterRVPlaylistSongs

    //Lifecycle
    private var editMode = false
    private var playlistLoaded = false


    interface OnPlaylistDeletedListener{ fun onPlaylistDeleted() }
    var onPlaylistDeletedListener: OnPlaylistDeletedListener?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_playlist, container, false)
        view.context.setTheme(ColorFunctions.getTheme(view.context))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPressed()
        assignVariables(view)
        setupTheme()
        btBack.setOnClickListener{onBackPressedListener?.onBackPressed()}

        val serviceIntent = Intent( fragmentContext, SimpleMPService::class.java )
        fragmentContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )

        if(savedInstanceState != null) restoreLifecycle(savedInstanceState)
        if(!playlistLoaded) loadPlaylist()
        if(playlistLoaded) handleSongClicked()


        handleIBMore()
    }


    private fun assignVariables(view: View){

        fragmentView = view
        fragmentContext = view.context
        clMain = view.findViewById(R.id.clMain_FragmentUserPlaylist)
        btBack = view.findViewById(R.id.btBack_Toolbar)
        btCancel = view.findViewById(R.id.btCancel_Toolbar)
        btSave = view.findViewById(R.id.btAux_Toolbar)
        ibMore = view.findViewById(R.id.ibMore_FragmentUserPlaylists)
        sivPlaylistArt = view.findViewById(R.id.sivArt_FragmentUserPlaylist)
        tvPlaylistName = view.findViewById(R.id.tvName_FragmentUserPlaylist)
        etPlaylistName = view.findViewById(R.id.etPlaylistName_FragmentUserPlaylist)
        rvSongs = view.findViewById(R.id.rvSongs_FragmentUserPlaylist)



        rvSongs.layoutManager = LinearLayoutManager(fragmentContext)
        rvSongs.addItemDecoration(RecyclerViewSpacer(10))
        rvSongs.itemAnimator = null

        btCancel.text = getString(R.string.Cancel)
        btSave.text = getString(R.string.Save)
    }


    private fun setupTheme(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
        etPlaylistName.background = ColorFunctions.getEditTextBackground(fragmentContext)
    }


    private fun restoreLifecycle(sis: Bundle){

        editMode = sis.getBoolean("editMode", false)
        playlistLoaded = sis.getBoolean("playlistLoaded", false)

        if( playlistLoaded ){

            val userPlaylistsJson = fragmentContext.getSharedPreferences("playlists", MODE_PRIVATE).getString("playlists","" )
            var jsonType = object : TypeToken<ArrayList<Playlist>>(){}.type
            userPlaylists = Gson().fromJson(userPlaylistsJson, jsonType)


            val playlistJson = sis.getString("playlist", "")
            playlist = Gson().fromJson(playlistJson, Playlist::class.java)
            songsList = playlist.playlist

            val playlistImagePath = playlist.imagePath
            val playlistName = playlist.name
            playlistID = playlist.id

            if(playlist.imagePath != null)
                sivPlaylistArt.setImageURI(Uri.parse(playlistImagePath))

            else{
                val playlistIcon = ContextCompat.getDrawable(fragmentContext, R.drawable.icon_playlists)!!.toBitmap()
                sivPlaylistArt.setColorFilter(ColorFunctions.getThemeColor(fragmentContext, 5))
                sivPlaylistArt.setImageBitmap(playlistIcon)
            }


            tvPlaylistName.text = playlistName


            val currentSongsListJson = sis.getString("currentSongsList", "")
            jsonType = object : TypeToken<ArrayList<Song>>(){}.type
            val currentSongsList: ArrayList<Song> = Gson().fromJson(currentSongsListJson, jsonType)

            adapterRVPlaylistSongs = AdapterRVPlaylistSongs(currentSongsList, editMode)
            rvSongs.adapter = adapterRVPlaylistSongs

            if(editMode) enableEditMode()
        }
    }


    private fun loadPlaylist() {

        val userPlaylistsJson = fragmentContext.getSharedPreferences("playlists", MODE_PRIVATE).getString("playlists","" )
        val jsonType = object : TypeToken<ArrayList<Playlist>>(){}.type
        userPlaylists = Gson().fromJson(userPlaylistsJson, jsonType)

        playlistID = requireArguments().getInt("playlistID", 0)
        userPlaylists.forEach {
            if(it.id == playlistID)
                playlist = it
        }


        songsList = playlist.playlist
        fragmentContext.getSharedPreferences("playlistArt", MODE_PRIVATE).edit().putString("playlistArt", playlist.imagePath).apply()


        val playlistImagePath = playlist.imagePath


        if(playlistImagePath == null){

            val playlistIcon = ContextCompat.getDrawable(fragmentContext, R.drawable.icon_playlists)!!.toBitmap()
            sivPlaylistArt.setColorFilter(ColorFunctions.getThemeColor(fragmentContext, 5))
            sivPlaylistArt.setImageBitmap(playlistIcon)
        }
        else{

            sivPlaylistArt.setImageURI(Uri.parse(playlistImagePath))
        }


        tvPlaylistName.text = playlist.name

        fillPlaylist()
    }


    private fun fillPlaylist(){

        adapterRVPlaylistSongs = AdapterRVPlaylistSongs(songsList, editMode)
        rvSongs.adapter = adapterRVPlaylistSongs

        handleSongClicked()
        playlistLoaded = true
    }



    private fun handleSongClicked(){

        adapterRVPlaylistSongs.onItemClickListener = object : AdapterRVPlaylistSongs.OnItemClickListener {
            override fun onItemClick(position: Int) {

                if( serviceBounded ){

                    SimpleMPService.startService(fragmentContext)

                    smpService.playList = adapterRVPlaylistSongs.playList

                    if( smpService.musicShuffled ){

                        smpService.playSongAndEnableShuffle(fragmentContext, position)
                    }
                    else{

                        smpService.currentSongPosition = position
                        smpService.playSong(fragmentContext)
                    }
                }
            }
        }
    }

    private fun handleBackPressed(){

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    if (isEnabled) {
                        isEnabled = false
                        onBackPressedListener?.onBackPressed()
                    }
                }
            })
    }


    private fun handleIBMore(){

        ibMore.setOnClickListener {

            val popupView = fragmentView.findViewById<View>(R.id.ibMore_FragmentUserPlaylists)
            val wrapper = ContextThemeWrapper(fragmentContext, ColorFunctions.getPopupMenuTheme(fragmentContext))
            val popupMenu = PopupMenu( wrapper, popupView )
            popupMenu.menuInflater.inflate(R.menu.menu_more_playlist, popupMenu.menu )

            popupMenu.setOnMenuItemClickListener{ menuItem ->

                when(menuItem.itemId){


                    R.id.menuEditPLaylist ->{

                        enableEditMode()
                    }
                    R.id.menuDeletePlaylist ->{

                        val dialog = AlertDialog.Builder(fragmentContext, ColorFunctions.getDialogTheme(fragmentContext))
                            .setCancelable(false)
                            .setTitle(getString(R.string.DeletePlaylist))
                            .setMessage(getString(R.string.ConfirmDeleteString))
                            .setNegativeButton(getString(R.string.Cancel), null)
                            .setPositiveButton(getString(R.string.Delete) ) { dialog, _ ->

                                val newUserPlaylist = ArrayList(userPlaylists)


                                newUserPlaylist.forEach { currentPlaylist ->

                                    if (currentPlaylist.id == playlistID) {

                                        val newUserPlaylists = ArrayList(userPlaylists)
                                        newUserPlaylists.removeIf { it.id == playlistID }

                                        fragmentContext.getSharedPreferences("playlists", MODE_PRIVATE)
                                            .edit()
                                            .putString("playlists", Gson().toJson(newUserPlaylists) )
                                            .apply()


                                        onPlaylistDeletedListener?.onPlaylistDeleted()
                                    }
                                }

                                dialog.dismiss()
                            }

                        dialog.show()
                    }
                }

                true
            }

            popupMenu.show()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun enableEditMode(){

        editMode = true

        ibMore.visibility = View.GONE
        btBack.visibility = View.GONE
        btCancel.visibility = View.VISIBLE
        btSave.visibility = View.VISIBLE
        tvPlaylistName.visibility = View.INVISIBLE
        etPlaylistName.visibility = View.VISIBLE

        etPlaylistName.setText(playlist.name)

        adapterRVPlaylistSongs.editMode = true
        adapterRVPlaylistSongs.notifyDataSetChanged()

        handleEditMode()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun disableEditMode(){

        editMode = false

        ibMore.visibility = View.VISIBLE
        btBack.visibility = View.VISIBLE
        btCancel.visibility = View.GONE
        btSave.visibility = View.GONE
        tvPlaylistName.visibility = View.VISIBLE
        etPlaylistName.visibility = View.GONE

        etPlaylistName.setText(playlist.name)

        adapterRVPlaylistSongs.editMode = false
        adapterRVPlaylistSongs.notifyDataSetChanged()
    }


    private fun handleEditMode(){

        btCancel.setOnClickListener {

            loadPlaylist()
            disableEditMode()
        }

        btSave.setOnClickListener {

            val playlistName = etPlaylistName.text.toString()
            val playlistImagePath = playlist.imagePath
            songsList = adapterRVPlaylistSongs.playList

            userPlaylists.forEach {
                if( it.id == playlistID ){

                    it.name = playlistName
                    it.playlist = songsList
                    it.imagePath = playlistImagePath
                }
            }

            tvPlaylistName.text = playlistName
            fragmentContext.getSharedPreferences("playlists", MODE_PRIVATE).edit().putString("playlists", Gson().toJson(userPlaylists) ).apply()


            onPlaylistUpdatedListener?.onPlaylistUpdated()
            disableEditMode()
        }


        sivPlaylistArt.setOnClickListener {

            if(editMode){

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).also {
                    it.addCategory(Intent.CATEGORY_OPENABLE)
                    it.type = "image/*"
                    it.addFlags(FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    it.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivityForResult(intent, 1)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == 1){

            try{

                val uri = data!!.data!!
                fragmentContext.contentResolver.takePersistableUriPermission(uri, FLAG_GRANT_READ_URI_PERMISSION)
                val uriString = uri.toString()



                for(it in userPlaylists){
                    if(it.id == playlistID)
                        it.imagePath = uriString
                }

                sivPlaylistArt.colorFilter = null
                sivPlaylistArt.setImageURI( Uri.parse(uriString))

            }
            catch (exc: Exception){}
        }
    }



    // Service //////////////////////////////////////////////////////////////////////////////////

    @SuppressLint("NotifyDataSetChanged")
    fun updateCurrentSong(){

        adapterRVPlaylistSongs.currentSongPath = smpService.getCurrentSongPath()
        adapterRVPlaylistSongs.notifyDataSetChanged()
    }


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val binder = service as SimpleMPService.LocalBinder
            smpService = binder.getService()
            serviceBounded = true
            updateCurrentSong()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            serviceBounded = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("editMode", editMode)
        outState.putBoolean("playlistLoaded", playlistLoaded)
        outState.putString("playlist", Gson().toJson(playlist))



        if(playlistLoaded) outState.putString("currentSongsList", Gson().toJson(adapterRVPlaylistSongs.playList))
    }


    // Interfaces //////////////////////////////////////////////////////////////////////////////////

    var onBackPressedListener: OnBackPressedListener? = null
    interface OnBackPressedListener{ fun onBackPressed() }

    var onPlaylistUpdatedListener: OnPlaylistUpdatedListener? = null
    interface OnPlaylistUpdatedListener{ fun onPlaylistUpdated() }


    // Adapters ///////////////////////////////////////////////////////////////////////////////////

    private class AdapterRVPlaylistSongs(var playList : ArrayList<Song>, var editMode: Boolean ): RecyclerView.Adapter<AdapterRVPlaylistSongs.ViewHolder>() {

        var currentSongPath = ""


        var onItemClickListener:  OnItemClickListener? = null
        interface OnItemClickListener{ fun onItemClick( position: Int ) }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(LayoutInflater.from( parent.context ).inflate(R.layout.rv_songs, parent, false ))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val context = holder.itemView.context
            val song = playList[position]
            val songPath = song.path
            val songID = song.id
            val songAlbumID = song.albumID
            val songAlbumArt = GetSongs.getSongAlbumArt(context, songID, songAlbumID)
            val songTitle = song.title
            val songArtist = song.artistName



            holder.albumArt.setImageBitmap(songAlbumArt)
            holder.title.text = songTitle
            holder.artist.text = songArtist


            when(editMode){

                true->{

                    holder.ibMore.visibility = View.GONE
                    holder.ibRemove.visibility = View.VISIBLE


                    holder.ibRemove.setOnClickListener {

                        playList.removeAt(position)
                        this.notifyItemRemoved(position)
                    }
                }
                false->{

                    holder.ibMore.visibility = View.VISIBLE
                    holder.ibRemove.visibility = View.GONE

                    if( currentSongPath == songPath ) {
                        holder.title.setTypeface(null, Typeface.BOLD)
                        holder.title.setTextColor(ColorFunctions.getThemeColor(holder.itemView.context, 5))
                    }

                    else {
                        holder.title.setTypeface(null, Typeface.NORMAL)
                        holder.title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text))
                    }


                    holder.clMain.setOnClickListener{ onItemClickListener?.onItemClick( position ) }

                    holder.ibMore.setOnClickListener{

                        val popupView = holder.itemView.findViewById<View>(R.id.ibMore_RVSongs)
                        val wrapper = ContextThemeWrapper(context, ColorFunctions.getPopupMenuTheme(context))
                        val popupMenu = PopupMenu( wrapper, popupView )
                        popupMenu.menuInflater.inflate(R.menu.menu_more_rv_playlist_songs, popupMenu.menu )

                        popupMenu.setOnMenuItemClickListener {

                            when (it.itemId) {

                                R.id.menuViewAlbum ->{}
                                R.id.menuViewArtist ->{}
                            }

                            true
                        }

                        popupMenu.show()
                    }
                }
            }
        }

        override fun getItemCount(): Int { return playList.size }


        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

            val clMain: ConstraintLayout = itemView.findViewById(R.id.clMain_RVSongs)
            val albumArt: ShapeableImageView = itemView.findViewById(R.id.albumArt_RVSongs)
            val title: TextView = itemView.findViewById(R.id.title_RVSongs)
            val artist: TextView = itemView.findViewById(R.id.artist_RVSongs)
            val ibMore: ImageButton = itemView.findViewById(R.id.ibMore_RVSongs)
            val ibRemove: ImageButton = itemView.findViewById(R.id.ibRemove_RVSongs)
        }
    }
}