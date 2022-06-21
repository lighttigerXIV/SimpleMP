package com.lighttigerxiv.simple.mp

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class FragmentHome : Fragment() {

    //UI
    private lateinit var fragmentContext: Context
    private lateinit var fragmentView: View
    private lateinit var rvSongs: RecyclerView
    private lateinit var originalSongsList: ArrayList<Song>



    //Others
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false
    private lateinit var adapterSongsRV: AdapterSongsRV


    ////////////////////////////////////////////////////////////////////////////////////////////


    fun setSelectedMusic( position: Int){

        adapterSongsRV.setPlayingMusic(position)
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


                        adapterSongsRV.setPlayingMusic(position)
                        smpService.setPlaylist( updatedSongsList )
                        smpService.setInitialSongPosition( position )
                        smpService.playSong( fragmentContext )


                    }
                }
            })
        }
        catch ( exc: Exception ){}
    }


    private fun assignVariables(){

        fragmentContext = fragmentView.context
        rvSongs = fragmentView.findViewById(R.id.rvSongs_FragmentHome)


        rvSongs.layoutManager = LinearLayoutManager(fragmentContext)
        rvSongs.addItemDecoration( RecyclerViewDivider( fragmentContext ) )
    }


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

                    val albumArt: Bitmap = try{

                        fragmentContext.contentResolver.loadThumbnail( songUri, Size(500,500), null )

                    } catch (ignore: Exception){

                        BitmapFactory.decodeResource( resources, R.drawable.icon_music_record )
                    }


                    val duration = cursor.getInt( cursor.getColumnIndex(MediaStore.Audio.Media.DURATION) )


                    val artist = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST) )

                    val song = Song()

                    song.id = id
                    song.path = songPath
                    song.uri = songUri
                    song.title = title
                    song.albumArt = albumArt
                    song.duration = duration
                    song.artist = artist

                    if( duration > 60000 )
                        songsList.add( song )
                }
                while (cursor.moveToNext())
            }
            cursor.close()
        }

        return songsList
    }


    private val connection = object : ServiceConnection{

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