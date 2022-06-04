package com.lighttigerxiv.simple.mp

import android.annotation.SuppressLint
import android.content.*
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.session.MediaSessionCompat
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.concurrent.thread


class FragmentHome : Fragment() {

    //UI
    private lateinit var fragmentContext: Context
    private lateinit var fragmentView: View
    private lateinit var rvSongs: RecyclerView
    private lateinit var songsList: ArrayList<Song>


    private lateinit var smpService: SimpleMPService

    var serviceStarted = false
    var serviceBounded = false

    /////////////////////////////////////////////////////////////////////////



    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try{

            fragmentView = view


            initializeVariables()

            val serviceIntent = Intent( fragmentContext, SimpleMPService::class.java )
            fragmentContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )


            songsList = getSongsList()


            if(savedInstanceState != null){

                serviceStarted = savedInstanceState.getBoolean( "serviceStarted" )
            }


            val adapterSongsRV = AdapterSongsRV(songsList)

            rvSongs.adapter = adapterSongsRV


            adapterSongsRV.setOnItemClickListener( object : AdapterSongsRV.OnItemClickListener{
                override fun onItemClick(position: Int) {


                    if( serviceBounded ){

                        SimpleMPService.startService(fragmentContext)
                        smpService.playSong( fragmentContext, songsList, position )
                    }



                }
            })
        }
        catch ( exc: Exception ){ println("Exception-> $exc") }
    }




    private fun initializeVariables(){

        fragmentContext = fragmentView.context

        rvSongs = fragmentView.findViewById(R.id.rvSongs_FragmentHome)
        rvSongs.layoutManager = LinearLayoutManager(fragmentContext)
        rvSongs.addItemDecoration( RVSpacer(10) )
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

                    val albumArt = fragmentContext.contentResolver.loadThumbnail( songUri, Size(500,500), null )

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


    override fun onStop() {
        super.onStop()

        fragmentContext.unbindService( connection )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean( "serviceStarted", serviceStarted )
    }
}