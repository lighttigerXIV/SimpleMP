package com.lighttigerxiv.simple.mp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class FragmentGenrePlaylist : Fragment() {

    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var btBack: Button
    private lateinit var tvPlaylistName: TextView
    private lateinit var rvSongs: RecyclerView

    private lateinit var playlistSongsList: ArrayList<Song>
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false
    private lateinit var adapterRVSongs: AdapterRVSongs

    //Others
    private var isGenrePlaylist = false
    private var genreID: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_genre_playlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPressed()
        assignVariables(view)
        btBack.setOnClickListener { onBackPressedListener?.onBackPressed() }


        val serviceIntent = Intent( fragmentContext, SimpleMPService::class.java )
        fragmentContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )


        if( isGenrePlaylist ){

            val songsList = GetSongs.getSongsList(fragmentContext, false)
            playlistSongsList = songsList.filter { it.genreID == genreID } as ArrayList<Song>

            tvPlaylistName.text = playlistSongsList[0].genre

            adapterRVSongs = AdapterRVSongs(playlistSongsList)
            rvSongs.adapter = adapterRVSongs
        }

        handleMusicClicked()
    }

    private fun assignVariables(view: View) {

        fragmentView = view
        fragmentContext = view.context
        btBack = view.findViewById(R.id.btBack_Toolbar)
        tvPlaylistName = view.findViewById(R.id.tvName_FragmentPlaylist)
        rvSongs = view.findViewById(R.id.rvSongs_FragmentPlaylist)


        isGenrePlaylist = arguments!!.getBoolean("isGenrePlaylist", false)
        genreID = arguments!!.getLong("genreID", 0)


        rvSongs.layoutManager = LinearLayoutManager(fragmentContext)
        rvSongs.addItemDecoration(RecyclerViewSpacer(10))
        rvSongs.itemAnimator = null
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

    fun updateCurrentSong(){

        adapterRVSongs.currentSongPath = smpService.getCurrentSongPath()
        adapterRVSongs.notifyItemRangeChanged( 0, adapterRVSongs.getPlayListSize() )
    }


    fun resetRecyclerView(){

        adapterRVSongs.currentSongPath = ""
        adapterRVSongs.notifyItemRangeChanged( 0, adapterRVSongs.getPlayListSize() )
    }


    private fun handleMusicClicked(){

        adapterRVSongs.onItemClickListener = object : AdapterRVSongs.OnItemClickListener{
            override fun onItemClick(position: Int) {

                if( serviceBounded ){

                    SimpleMPService.startService(fragmentContext)

                    smpService.playList = playlistSongsList

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


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Interfaces

    var onBackPressedListener: OnBackPressedListener ?= null
    interface OnBackPressedListener{ fun onBackPressed() }
}