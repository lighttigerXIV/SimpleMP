package com.lighttigerxiv.simple.mp.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lighttigerxiv.simple.mp.*
import com.lighttigerxiv.simple.mp.others.ColorFunctions
import com.lighttigerxiv.simple.mp.others.GetSongs
class ActivityAddToPlaylist : AppCompatActivity() {

    //Main
    private lateinit var clMain: ConstraintLayout
    private lateinit var btBack: Button
    private lateinit var tvTitle: TextView
    private lateinit var btCreatePlaylist: Button
    private lateinit var rvPlaylists: RecyclerView

    //Adapters and Arraylists
    private lateinit var adapterRVPlaylists: AdapterRVPlaylists
    private var userPlaylists = ArrayList<Playlist>()

    //Others
    private lateinit var selectedSong: Song

    //Service
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ColorFunctions.getTheme(applicationContext))
        setContentView(R.layout.activity_add_to_playlist)
        assignVariables()
        setupTheme()

        val serviceIntent = Intent( applicationContext, SimpleMPService::class.java )
        bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )

        getSharedPreferences("playlists", MODE_PRIVATE).edit().clear()


        btBack.setOnClickListener { onBackPressed() }
        tvTitle.text = getString(R.string.SelectPlaylist)



        loadUserPlaylists()
        handleBtCreatePlaylist()
    }


    private fun assignVariables() {

        clMain = findViewById(R.id.clMain_ActivityAddToPlaylist)
        btBack = findViewById(R.id.btBack_Toolbar)
        tvTitle = findViewById(R.id.tvTitle_Toolbar)
        btCreatePlaylist = findViewById(R.id.btCreatePlaylist_ActivityAddToPlaylist)
        rvPlaylists = findViewById(R.id.rvPlaylists_ActivityAddToPlaylist)

        val selectedSongID = intent.extras!!.getString("selectedSongID", "")
        val songsList = GetSongs.getSongsList(applicationContext, false)

        songsList.forEach {
            if( it.id.toString() == selectedSongID )
                selectedSong = it
        }


        rvPlaylists.layoutManager = LinearLayoutManager(applicationContext)
    }


    private fun setupTheme(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(applicationContext, 1))
        btCreatePlaylist.setTextColor(ColorFunctions.getThemeColor(applicationContext, 2))
        ViewCompat.setBackgroundTintList(btCreatePlaylist, ColorStateList.valueOf(ColorFunctions.getThemeColor(applicationContext, 5)))
    }


    private fun loadUserPlaylists(){

        val userPLaylistsJson = getSharedPreferences("playlists", MODE_PRIVATE).getString("playlists", null)

        if( userPLaylistsJson != null ){

            val jsonType = object : TypeToken<ArrayList<Playlist>>(){}.type
            userPlaylists = Gson().fromJson(userPLaylistsJson, jsonType)


            adapterRVPlaylists = AdapterRVPlaylists(userPlaylists)
            rvPlaylists.adapter = adapterRVPlaylists

            handlePlaylistClicked()
        }
    }


    private fun handlePlaylistClicked(){

        adapterRVPlaylists.onPlaylistSelectedListener = object : AdapterRVPlaylists.OnPlaylistSelectedListener {
            override fun onItemSelected(position: Int) {

                val playlist = userPlaylists[position]
                val playlistSongs = playlist.playlist
                var ableToAddSong = true

                playlistSongs.forEach {

                    if(it.path == selectedSong.path){

                        ableToAddSong = false
                        Toast.makeText(applicationContext, getString(R.string.SongAlreadyAdded), Toast.LENGTH_LONG).show()
                    }
                }

                if(ableToAddSong){

                    playlistSongs.add(selectedSong)
                    getSharedPreferences("playlists", MODE_PRIVATE)
                        .edit()
                        .putString( "playlists", Gson().toJson(userPlaylists) )
                        .apply()

                    finish()
                }
            }
        }
    }


    private fun handleBtCreatePlaylist(){

        btCreatePlaylist.setOnClickListener{

            val bottomSheetAddPlaylist = BottomSheetDialog(this)
            bottomSheetAddPlaylist.setContentView(R.layout.bottom_sheet_add_playlist)

            val clBS = bottomSheetAddPlaylist.findViewById<ConstraintLayout>(R.id.clMain_BottomSheetAddPlaylist)
            val etPlaylistName = bottomSheetAddPlaylist.findViewById<EditText>(R.id.etPlaylistName_BottomSheetAddPlaylist)
            val btAddPlaylistBS = bottomSheetAddPlaylist.findViewById<Button>(R.id.btAddPlaylist_BottomSheetAddPlaylist)


            clBS!!.setBackgroundColor(ColorFunctions.getThemeColor(applicationContext, 1))
            etPlaylistName!!.background = ColorFunctions.getEditTextBackground(applicationContext)
            ViewCompat.setBackgroundTintList(btAddPlaylistBS!!, ColorStateList.valueOf(ColorFunctions.getThemeColor(applicationContext, 5)))


            btAddPlaylistBS.setOnClickListener {

                val newPlaylistName = etPlaylistName.text.toString()

                if(newPlaylistName.trim().isEmpty())
                    Toast.makeText(applicationContext, getString(R.string.FillPlaylistName), Toast.LENGTH_LONG).show()

                else{

                    val newPlaylistID = if( userPlaylists.size == 0 ) 1 else userPlaylists.maxOf { it.id } + 1 //Gives an id to playlist

                    userPlaylists.add( Playlist(newPlaylistID, newPlaylistName, null, ArrayList()) )

                    getSharedPreferences( "playlists", MODE_PRIVATE )
                        .edit()
                        .putString("playlists", Gson().toJson(userPlaylists))
                        .apply()


                    adapterRVPlaylists = AdapterRVPlaylists(userPlaylists)
                    rvPlaylists.adapter = adapterRVPlaylists


                    smpService.updatePlaylists()

                    handlePlaylistClicked()

                    bottomSheetAddPlaylist.dismiss()
                }
            }

            bottomSheetAddPlaylist.show()
            bottomSheetAddPlaylist.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }


    // Adapters ////////////////////////////////////////////////////////////////////////////////////

    class AdapterRVPlaylists(private var userPlaylists: ArrayList<Playlist>): RecyclerView.Adapter<AdapterRVPlaylists.ViewHolder>(){

        interface OnPlaylistSelectedListener{fun onItemSelected(position: Int)}
        var onPlaylistSelectedListener: OnPlaylistSelectedListener?= null


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_add_to_playlist, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val context = holder.itemView.context
            val playlist = userPlaylists[holder.bindingAdapterPosition]
            val playlistImagePath = playlist.imagePath
            val playlistName = playlist.name


            if( playlist.imagePath == null ){

                val playlistIcon = ContextCompat.getDrawable(context, R.drawable.icon_playlists)!!.toBitmap()
                holder.sivPlaylistArt.setColorFilter(ColorFunctions.getThemeColor(context, 5))
                holder.sivPlaylistArt.setImageBitmap(playlistIcon)
            }
            else{

                holder.sivPlaylistArt.setImageURI(Uri.parse(playlistImagePath))
            }

            holder.tvPlaylistName.text = playlistName
            holder.clMain.setOnClickListener { onPlaylistSelectedListener?.onItemSelected(holder.bindingAdapterPosition) }
        }

        override fun getItemCount(): Int {return userPlaylists.size}

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

            var clMain : ConstraintLayout = itemView.findViewById(R.id.clMain_RVAddToPlaylist)
            var sivPlaylistArt : ShapeableImageView = itemView.findViewById(R.id.playlistArt_RVAddToPlaylist)
            var tvPlaylistName : TextView = itemView.findViewById(R.id.playlistName_RVAddToPlaylist)
        }
    }


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val binder = service as SimpleMPService.LocalBinder
            smpService = binder.getService()
            serviceBounded = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            serviceBounded = false
        }
    }
}