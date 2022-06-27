package com.lighttigerxiv.simple.mp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class FragmentRecyclerView : Fragment() {

    //User Interface
    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var rvContent: RecyclerView


    //Others
    private var page = ""
    private var artistID: Long? = 0
    private lateinit var songsList: ArrayList<Song>
    private lateinit var adapterRVSongs: AdapterRVSongs
    private lateinit var adapterRVAlbums: AdapterRVAlbums
    private var onAlbumOpenedListener: OnAlbumOpenedListener? = null


    //Service
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assignVariables(view)



        if( page == "songs" ){


            rvContent.layoutManager = LinearLayoutManager(fragmentContext)

            loadArtistSongs()

            val serviceIntent = Intent( fragmentContext, SimpleMPService::class.java )
            fragmentContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )
        }
        else{

            val deviceOrientation = fragmentContext.resources.configuration.orientation

            if( deviceOrientation == Configuration.ORIENTATION_PORTRAIT )
                rvContent.layoutManager = GridLayoutManager( fragmentContext, 2 )

            else
                rvContent.layoutManager = GridLayoutManager( fragmentContext, 4 )


            loadArtistAlbums()
        }
    }


    private fun assignVariables(view: View){

        fragmentView = view
        fragmentContext = view.context
        rvContent = view.findViewById(R.id.rvContent_FragmentRecyclerView)


        page = arguments?.getString("page").toString()
        artistID = arguments?.getLong("artistID")
        songsList = GetSongs.getSongsList( fragmentContext )


        rvContent.itemAnimator?.changeDuration = 0
    }


    private fun loadArtistSongs(){

        val artistSongList = ArrayList(songsList)
        artistSongList.removeIf { it.artistID != artistID }

        adapterRVSongs = AdapterRVSongs(artistSongList)
        rvContent.adapter = adapterRVSongs
    }


    private fun loadArtistAlbums(){

        val artistAlbumsList = ArrayList(songsList)
        artistAlbumsList.removeIf { it.artistID != artistID }
        artistAlbumsList.distinctBy { it.albumID }
        artistAlbumsList.sortBy { it.albumName }

        adapterRVAlbums = AdapterRVAlbums(artistAlbumsList)
        rvContent.adapter = adapterRVAlbums


        adapterRVAlbums.setOnAlbumClickedListener(object : AdapterRVAlbums.OnAlbumClickedListener{
            override fun onAlbumOpened(albumID: Long) {
                onAlbumOpenedListener?.onAlbumOpened(albumID)
            }
        })
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Service

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val binder = service as SimpleMPService.LocalBinder
            smpService = binder.getService()
            serviceBounded = true


            if( page == "songs" ){

                handleSongSelected()
                updateCurrentSong()
            }

            else{

                handleAlbumSelected()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            serviceBounded = false
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Listeners



    interface OnAlbumOpenedListener { fun onAlbumOpened( albumID: Long ) }
    fun setOnAlbumOpenedListener( listener: OnAlbumOpenedListener){onAlbumOpenedListener = listener }

    fun updateCurrentSong(){

        if( serviceBounded ){

            adapterRVSongs.setCurrentSongPath( smpService.getCurrentSongPath() )
            adapterRVSongs.notifyItemRangeChanged( 0, adapterRVSongs.getPlayListSize() )
        }
    }

    fun resetRecyclerView(){

        adapterRVSongs.setCurrentSongPath( "" )
        adapterRVSongs.notifyItemRangeChanged( 0, adapterRVSongs.getPlayListSize() )
    }


    private fun handleSongSelected(){

        adapterRVSongs.setOnItemClickListener(object : AdapterRVSongs.OnItemClickListener{
            override fun onItemClick(position: Int) {

                SimpleMPService.startService(fragmentContext)


                smpService.setPlaylist( adapterRVSongs.getPlaylist() )
                smpService.setInitialSongPosition( position )
                smpService.playSong( fragmentContext )
                updateCurrentSong()
            }
        })
    }


    private fun handleAlbumSelected(){

        adapterRVAlbums.setOnAlbumClickedListener( object : AdapterRVAlbums.OnAlbumClickedListener{
            override fun onAlbumOpened(albumID: Long) { onAlbumOpenedListener?.onAlbumOpened( albumID ) }
        })
    }


    override fun onResume() {
        super.onResume()

        fragmentView.requestLayout()
    }
}