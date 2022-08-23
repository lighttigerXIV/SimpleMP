package com.lighttigerxiv.simple.mp.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lighttigerxiv.simple.mp.*
import com.lighttigerxiv.simple.mp.adapters.AdapterRVAlbums
import com.lighttigerxiv.simple.mp.adapters.AdapterRVSongs
import com.lighttigerxiv.simple.mp.others.ColorFunctions
import com.lighttigerxiv.simple.mp.others.GetSongs
import com.lighttigerxiv.simple.mp.services.SimpleMPService


class FragmentArtistRecyclerView : Fragment() {

    //User Interface
    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var clMain: ConstraintLayout
    private lateinit var rvContent: RecyclerView


    //Others
    private var page = ""
    private var artistID: Long? = 0
    private lateinit var songsList: ArrayList<Song>
    private lateinit var artistSongList: ArrayList<Song>
    private lateinit var adapterRVSongs: AdapterRVSongs
    private lateinit var adapterRVAlbums: AdapterRVAlbums



    //Service
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.fragment_recycler_view, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assignVariables(view)
        setupColors()

        if( page == "songs" ){


            rvContent.layoutManager = LinearLayoutManager(fragmentContext)
            rvContent.addItemDecoration(RecyclerViewSpacer(10))

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
        clMain = view.findViewById(R.id.clMain_FragmentRecyclerView)
        rvContent = view.findViewById(R.id.rvContent_FragmentRecyclerView)


        page = arguments?.getString("page").toString()
        artistID = arguments?.getLong("artistID")
        songsList = GetSongs.getSongsList(fragmentContext, false)


        rvContent.itemAnimator?.changeDuration = 0
    }


    private fun setupColors(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
    }


    private fun loadArtistSongs(){

        artistSongList = ArrayList(songsList)
        artistSongList.removeIf { it.artistID != artistID }

        adapterRVSongs = AdapterRVSongs(artistSongList, requireActivity().supportFragmentManager,
            showViewAlbum = true,
            showViewArtist = false)
        rvContent.adapter = adapterRVSongs
    }


    private fun loadArtistAlbums(){

        var artistAlbumsList = ArrayList(songsList)
        artistAlbumsList.removeIf { it.artistID != artistID }
        artistAlbumsList = artistAlbumsList.distinctBy { it.albumID } as ArrayList<Song>
        artistAlbumsList.sortBy { it.albumName }

        adapterRVAlbums = AdapterRVAlbums(artistAlbumsList)
        rvContent.adapter = adapterRVAlbums


        adapterRVAlbums.onAlbumOpenedListener = object : AdapterRVAlbums.OnAlbumClickedListener{
            override fun onAlbumOpened(albumID: Long) {
                onAlbumOpenedListener?.onAlbumOpened(albumID)
            }
        }
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

    var onAlbumOpenedListener: OnAlbumOpenedListener? = null
    interface OnAlbumOpenedListener { fun onAlbumOpened( albumID: Long ) }

    fun updateCurrentSong(){

        adapterRVSongs.currentSongPath = smpService.getCurrentSongPath()
        adapterRVSongs.notifyItemRangeChanged( 0, adapterRVSongs.getPlayListSize() )
    }

    fun resetRecyclerView(){

        adapterRVSongs.currentSongPath = ""
        adapterRVSongs.notifyItemRangeChanged( 0, adapterRVSongs.getPlayListSize() )
    }


    private fun handleSongSelected(){

        adapterRVSongs.onItemClickListener = object : AdapterRVSongs.OnItemClickListener{
            override fun onItemClick(position: Int) {

                if( serviceBounded ){

                    SimpleMPService.startService(fragmentContext)

                    smpService.setPlaylist(artistSongList)

                    if( smpService.musicShuffled )
                        smpService.playSongAndEnableShuffle(fragmentContext, position)

                    else{

                        smpService.currentSongPosition = position
                        smpService.playSong(fragmentContext)
                    }
                }
            }
        }
    }


    private fun handleAlbumSelected(){

        adapterRVAlbums.onAlbumOpenedListener = object : AdapterRVAlbums.OnAlbumClickedListener{
            override fun onAlbumOpened(albumID: Long) { onAlbumOpenedListener?.onAlbumOpened( albumID ) }
        }
    }
}