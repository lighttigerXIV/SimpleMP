package com.lighttigerxiv.simple.mp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.ByteArrayOutputStream

class ActivityAddToPlaylist : AppCompatActivity() {

    //Main
    private lateinit var btBack: Button
    private lateinit var tvTitle: TextView
    private lateinit var btCreatePlaylist: Button
    private lateinit var rvPlaylists: RecyclerView

    //Adapters and Arraylists
    private lateinit var adapterRVPlaylists: AdapterRVPlaylists
    private var userPlaylists = ArrayList<Playlist>()

    //Others
    private lateinit var selectedSong: Song


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_add_to_playlist)

        getSharedPreferences("playlists", MODE_PRIVATE)
            .edit()
            .clear()

        assignVariables()
        btBack.setOnClickListener { onBackPressed() }
        tvTitle.text = getString(R.string.SelectPlaylist)



        loadUserPlaylists()
        handleBtCreatePlaylist()
    }


    private fun assignVariables() {

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

        adapterRVPlaylists.onPlaylistSelectedListener = object : AdapterRVPlaylists.OnPlaylistSelectedListener{
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

            val etPlaylistName = bottomSheetAddPlaylist.findViewById<EditText>(R.id.etPlaylistName_BottomSheetAddPlaylist)
            val btAddPlaylistBS = bottomSheetAddPlaylist.findViewById<Button>(R.id.btAddPlaylist_BottomSheetAddPlaylist)

            btAddPlaylistBS?.setOnClickListener {

                val newPlaylistName = etPlaylistName?.text.toString()

                if(newPlaylistName.trim().isEmpty())
                    Toast.makeText(applicationContext, getString(R.string.FillPlaylistName), Toast.LENGTH_LONG).show()

                else{

                    val playlistIcon = ContextCompat.getDrawable( applicationContext, R.drawable.icon_playlist) as BitmapDrawable
                    val iconBitmap = playlistIcon.bitmap
                    val baos = ByteArrayOutputStream()
                    iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val iconString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)


                    val newPlaylistID = if( userPlaylists.size == 0 ) 1 else userPlaylists.maxOf { it.id } + 1 //Gives an id to playlist

                    userPlaylists.add( Playlist(newPlaylistID, newPlaylistName, iconString, ArrayList()) )

                    getSharedPreferences( "playlists", MODE_PRIVATE )
                        .edit()
                        .putString("playlists", Gson().toJson(userPlaylists))
                        .apply()


                    adapterRVPlaylists = AdapterRVPlaylists(userPlaylists)
                    rvPlaylists.adapter = adapterRVPlaylists

                    handlePlaylistClicked()

                    bottomSheetAddPlaylist.dismiss()
                }
            }

            bottomSheetAddPlaylist.show()
        }
    }


    // Adapters ////////////////////////////////////////////////////////////////////////////////////

    class AdapterRVPlaylists(private var userPlaylists: ArrayList<Playlist>): RecyclerView.Adapter<AdapterRVPlaylists.ViewHolder>(){

        interface OnPlaylistSelectedListener{fun onItemSelected(position: Int)}
        var onPlaylistSelectedListener: OnPlaylistSelectedListener ?= null


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_add_to_playlist, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val playlist = userPlaylists[holder.bindingAdapterPosition]
            val playlistArtString = playlist.image
            val playlistArtBytes = Base64.decode(playlistArtString, 0)
            val playlistArtBitmap = BitmapFactory.decodeByteArray(playlistArtBytes, 0, playlistArtBytes.size)
            val playlistName = playlist.name


            holder.tvPlaylistName.text = playlistName
            holder.sivPlaylistArt.background = playlistArtBitmap.toDrawable(holder.itemView.context.resources)

            holder.clMain.setOnClickListener { onPlaylistSelectedListener?.onItemSelected(holder.bindingAdapterPosition) }
        }

        override fun getItemCount(): Int {return userPlaylists.size}

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

            var clMain = itemView.findViewById<ConstraintLayout>(R.id.clMain_RVAddToPlaylist)
            var sivPlaylistArt = itemView.findViewById<ShapeableImageView>(R.id.playlistArt_RVAddToPlaylist)
            var tvPlaylistName = itemView.findViewById<TextView>(R.id.playlistName_RVAddToPlaylist)
        }
    }
}