package com.lighttigerxiv.simple.mp

import android.app.AlertDialog
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class FragmentUserPlaylist : Fragment() {

    //Main
    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var btBack: Button
    private lateinit var ibMore: ImageButton
    private lateinit var sivPlaylistArt: ShapeableImageView
    private lateinit var tvPlaylistName: TextView
    private lateinit var rvSongs: RecyclerView


    private lateinit var userPlaylists: ArrayList<Playlist>
    private lateinit var playlist: Playlist
    private var playlistID = -1
    private lateinit var songsList: ArrayList<Song>
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false
    private lateinit var adapterRVSongs: AdapterRVSongs


    interface OnPlaylistDeletedListener{ fun onPlaylistDeleted() }
    var onPlaylistDeletedListener: OnPlaylistDeletedListener ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPressed()
        assignVariables(view)
        btBack.setOnClickListener{onBackPressedListener?.onBackPressed()}


        val serviceIntent = Intent( fragmentContext, SimpleMPService::class.java )
        fragmentContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )

        loadPlaylist()
        handleSongClicked()
        handleIBMore()
    }


    private fun assignVariables(view: View){

        fragmentView = view
        fragmentContext = view.context
        btBack = view.findViewById(R.id.btBack_Toolbar)
        ibMore = view.findViewById(R.id.ibMore_FragmentUserPlaylists)
        sivPlaylistArt = view.findViewById(R.id.sivArt_FragmentUserPlaylist)
        tvPlaylistName = view.findViewById(R.id.tvName_FragmentUserPlaylist)
        rvSongs = view.findViewById(R.id.rvSongs_FragmentUserPlaylist)


        rvSongs.layoutManager = LinearLayoutManager(fragmentContext)
        rvSongs.addItemDecoration(RecyclerViewSpacer(10))
        rvSongs.itemAnimator = null
    }


    private fun loadPlaylist() {

        val userPlaylistsJson = fragmentContext.getSharedPreferences("playlists", MODE_PRIVATE).getString("playlists","" )
        val jsonType = object : TypeToken<ArrayList<Playlist>>(){}.type
        userPlaylists = Gson().fromJson(userPlaylistsJson, jsonType)

        playlistID = arguments!!.getInt("playlistID", 0)
        userPlaylists.forEach {
            if(it.id == playlistID)
                playlist = it
        }


        songsList = playlist.playlist



        val playlistImageString = playlist.image
        val playlistImageBytes = Base64.decode(playlistImageString, Base64.DEFAULT)
        val playlistImageBitmap = BitmapFactory.decodeByteArray(playlistImageBytes, 0, playlistImageBytes.size)
        val playlistName = playlist.name

        sivPlaylistArt.setImageBitmap(playlistImageBitmap)
        tvPlaylistName.text = playlistName


        adapterRVSongs = AdapterRVSongs(songsList)
        rvSongs.adapter = adapterRVSongs
    }


    private fun handleSongClicked(){

        adapterRVSongs.onItemClickListener = object : AdapterRVSongs.OnItemClickListener{
            override fun onItemClick(position: Int) {

                if( serviceBounded ){

                    SimpleMPService.startService(fragmentContext)

                    smpService.playList = adapterRVSongs.songsList

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

            val popupMenu = PopupMenu(fragmentContext, ibMore)
            popupMenu.inflate(R.menu.menu_more_playlist)

            popupMenu.setOnMenuItemClickListener{ menuItem ->

                when(menuItem.itemId){

                    R.id.menuAddSongs->{


                    }
                    R.id.menuSelectImage->{


                    }
                    R.id.menuDeletePlaylist->{

                        val dialog = AlertDialog.Builder(fragmentContext, R.style.Custom_Dialog)
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

    // Service //////////////////////////////////////////////////////////////////////////////////

    fun updateCurrentSong(){

        adapterRVSongs.currentSongPath = smpService.getCurrentSongPath()
        adapterRVSongs.notifyItemRangeChanged( 0, adapterRVSongs.getPlayListSize() )
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


    // Interfaces //////////////////////////////////////////////////////////////////////////////////

    var onBackPressedListener: OnBackPressedListener ?= null
    interface OnBackPressedListener{ fun onBackPressed() }
}