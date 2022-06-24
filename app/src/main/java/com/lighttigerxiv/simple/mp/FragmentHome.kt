package com.lighttigerxiv.simple.mp

import android.annotation.SuppressLint
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class FragmentHome : Fragment() {

    //UI
    private lateinit var fragmentContext: Context
    private lateinit var fragmentView: View
    private lateinit var ivMenu: ImageView
    private lateinit var rvSongs: RecyclerView
    private lateinit var originalSongsList: ArrayList<Song>



    //Others
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false
    private lateinit var adapterSongsRV: AdapterSongsRV


    ////////////////////////////////////////////////////////////////////////////////////////////

    fun updateCurrentSong(){

        if( serviceBounded ){

            adapterSongsRV.setCurrentSongPath( smpService.getCurrentSongPath() )
            adapterSongsRV.notifyItemRangeChanged( 0, adapterSongsRV.getPlayListSize() )
        }
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try{

            fragmentView = view


            assignVariables()

            val serviceIntent = Intent( fragmentContext, SimpleMPService::class.java )
            fragmentContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )


            originalSongsList = getSongsList()


            if(savedInstanceState != null){

                serviceBounded = savedInstanceState.getBoolean( "serviceBounded" )
            }


            adapterSongsRV = AdapterSongsRV(originalSongsList)

            rvSongs.adapter = adapterSongsRV


            adapterSongsRV.setOnItemClickListener( object : AdapterSongsRV.OnItemClickListener{
                override fun onItemClick(position: Int) {

                    if( serviceBounded ){

                        SimpleMPService.startService(fragmentContext)

                        val updatedSongsList = ArrayList(originalSongsList)

                        for( song in originalSongsList )
                            updatedSongsList.add(song)


                        if( smpService.isPlaylistShuffled() )
                            smpService.toggleShuffle()


                        smpService.setPlaylist( updatedSongsList )
                        smpService.setInitialSongPosition( position )
                        smpService.playSong( fragmentContext )
                    }
                }
            })



            val popupView = fragmentView.findViewById<View>( R.id.ivMenu_FragmentHome )
            val popupMenu = PopupMenu( fragmentContext, popupView )
            popupMenu.menuInflater.inflate( R.menu.menu_more_fragment_home, popupMenu.menu )

            ivMenu.setOnClickListener {

                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId) {

                        R.id.menuDefault-> setSortMode( "Default" )

                        R.id.menuSortByDate-> setSortMode( "Date" )

                        R.id.menuSortAZ-> setSortMode( "AZ" )

                        R.id.menuSortZA-> setSortMode( "ZA" )

                        R.id.menuSortByArtist-> setSortMode( "Artist" )
                    }
                    true
                }

                popupMenu.show()
            }
        }
        catch ( exc: Exception ){}
    }


    private fun setSortMode( sortMode: String ){

        fragmentContext.getSharedPreferences( "Settings", MODE_PRIVATE )
            .edit()
            .putString( "sort", sortMode )
            .apply()


        originalSongsList = getSongsList()

        adapterSongsRV.setPlaylist( originalSongsList )
        adapterSongsRV.notifyItemRangeChanged( 0, adapterSongsRV.getPlayListSize() )
    }


    private fun assignVariables(){

        fragmentContext = fragmentView.context
        ivMenu = fragmentView.findViewById(R.id.ivMenu_FragmentHome)
        rvSongs = fragmentView.findViewById(R.id.rvSongs_FragmentHome)


        rvSongs.layoutManager = LinearLayoutManager(fragmentContext)
        rvSongs.addItemDecoration( RecyclerViewDivider( fragmentContext ) )
        rvSongs.itemAnimator?.changeDuration = 0
    }


    @Suppress("DEPRECATION")
    @SuppressLint("Range")
    private fun getSongsList(): ArrayList<Song>{

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = fragmentContext.contentResolver.query(uri, null, null, null, null)
        val songsList = ArrayList<Song>()


        if( cursor != null ){
            if( cursor.moveToNext() ){
                do{

                    val id = cursor.getLong(cursor.getColumnIndex( MediaStore.Audio.Media._ID) )

                    val songPath = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Media.DATA) )

                    val songUri = ContentUris.withAppendedId( uri, id )

                    val title = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Media.TITLE ) )

                    lateinit var albumArt: Bitmap
                    val albumID = cursor.getLong( cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID) )

                    albumArt = try{

                        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

                            fragmentContext.contentResolver.loadThumbnail( songUri, Size(500,500), null )
                        } else{

                            val sArtWorkUri = Uri.parse( "content://media/external/audio/albumart" )
                            val albumArtUri = ContentUris.withAppendedId(sArtWorkUri, albumID)
                            MediaStore.Images.Media.getBitmap( fragmentContext.contentResolver, albumArtUri )
                        }

                    } catch (ignore: Exception){

                        BitmapFactory.decodeResource( resources, R.drawable.icon_music_record )
                    }


                    val duration = cursor.getInt( cursor.getColumnIndex(MediaStore.Audio.Media.DURATION) )
                    val year = cursor.getInt( cursor.getColumnIndex(MediaStore.Audio.Media.YEAR) )
                    val artist = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST) )


                    val song = Song( id, songPath, songUri, title, albumArt, duration, artist, year )

                    if( duration > 60000 )
                        songsList.add( song )
                }
                while (cursor.moveToNext())
            }
            cursor.close()
        }


        when ( fragmentContext.getSharedPreferences( "Settings", MODE_PRIVATE ).getString( "sort", "default" ) ) {

            "Date" -> songsList.sortByDescending { it.year }
            "AZ" -> songsList.sortBy { it.title }
            "ZA" -> songsList.sortByDescending { it.title }
            "Artist"-> songsList.sortBy { it.artist }
        }

        return songsList
    }


    private val connection = object : ServiceConnection{

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
}