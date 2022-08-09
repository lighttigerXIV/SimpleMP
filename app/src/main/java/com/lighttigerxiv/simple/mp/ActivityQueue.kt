package com.lighttigerxiv.simple.mp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class ActivityQueue : AppCompatActivity() {

    //Main
    private lateinit var ibCloseQueue: ImageButton
    private lateinit var ivCurrentSongAlbumArt: ShapeableImageView
    private lateinit var tvCurrentSongName: TextView
    private lateinit var tvCurrentSongArtist: TextView
    private lateinit var rvQueue: RecyclerView

    //Service
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false
    private lateinit var adapterRVSongs: AdapterRVSongs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue)


        assignVariables()


        val serviceIntent = Intent( applicationContext, SimpleMPService::class.java )
        bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )


        ibCloseQueue.setOnClickListener { finish() }
    }


    private fun assignVariables(){

        ibCloseQueue = findViewById(R.id.ibCloseQueue_ActivityQueue)
        ivCurrentSongAlbumArt = findViewById(R.id.ivCurrentSongAlbumArt_ActivityQueue)
        tvCurrentSongName = findViewById(R.id.tvCurrentSongName_ActivityQueue)
        tvCurrentSongArtist = findViewById(R.id.tvCurrentSongArtist_ActivityQueue)
        rvQueue = findViewById(R.id.rvQueue_ActivityQueue)


        rvQueue.layoutManager = LinearLayoutManager(applicationContext)
        rvQueue.addItemDecoration(RecyclerViewSpacer(10))
        rvQueue.itemAnimator = null
    }


    private fun updateQueueList( queueList: ArrayList<Song> ){

        val filteredQueueList = ArrayList(queueList)

        /*
        run loop@{

            queueList.forEachIndexed { index, song->

                if( index < smpService.currentSongPosition ){

                    filteredQueueList.remove(song)
                }
                else{

                    filteredQueueList.remove(song)
                    ivCurrentSongAlbumArt.setImageBitmap(GetSongs.getSongAlbumArt( applicationContext, song.id, song.albumID ))
                    tvCurrentSongName.text = song.title
                    tvCurrentSongArtist.text = song.artistName
                    return@loop
                }
            }
        }

         */

        for(song in queueList){

            if(song.path == smpService.getCurrentSongPath()){

                ivCurrentSongAlbumArt.setImageBitmap(GetSongs.getSongAlbumArt( applicationContext, song.id, song.albumID ))
                tvCurrentSongName.text = song.title
                tvCurrentSongArtist.text = song.artistName
                filteredQueueList.remove(song)
                break
            }
            else{

                filteredQueueList.remove(song)
            }
        }

        adapterRVSongs = AdapterRVSongs(filteredQueueList)
        rvQueue.adapter = adapterRVSongs
    }


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val binder = service as SimpleMPService.LocalBinder
            smpService = binder.getService()
            serviceBounded = true


            updateQueueList( smpService.getCurrentPlaylist() )


            smpService.onMusicSelectedListenerToQueue = object : SimpleMPService.OnMusicSelectedListenerToQueue{
                override fun onMusicSelected(playList: ArrayList<Song>, position: Int) {

                    updateQueueList(playList)
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            serviceBounded = false
        }
    }
}