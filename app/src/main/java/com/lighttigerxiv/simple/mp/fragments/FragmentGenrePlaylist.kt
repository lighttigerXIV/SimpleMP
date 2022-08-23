package com.lighttigerxiv.simple.mp.fragments

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.setPadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.lighttigerxiv.simple.mp.*
import com.lighttigerxiv.simple.mp.adapters.AdapterRVSongs
import com.lighttigerxiv.simple.mp.others.ColorFunctions
import com.lighttigerxiv.simple.mp.others.GetSongs
import com.lighttigerxiv.simple.mp.services.SimpleMPService


class FragmentGenrePlaylist : Fragment() {

    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var clMain: ConstraintLayout
    private lateinit var btBack: Button
    private lateinit var sivPlaylistImage: ShapeableImageView
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
        val view = inflater.inflate(R.layout.fragment_genre_playlist, container, false)
        view.context.setTheme(ColorFunctions.getTheme(view.context))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPressed()
        assignVariables(view)
        setupTheme()
        btBack.setOnClickListener { onBackPressedListener?.onBackPressed() }


        val serviceIntent = Intent( fragmentContext, SimpleMPService::class.java )
        fragmentContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )


        if( isGenrePlaylist ){

            val songsList = GetSongs.getSongsList(fragmentContext, false)
            playlistSongsList = songsList.filter { it.genreID == genreID } as ArrayList<Song>


            val playlistIcon = ContextCompat.getDrawable(fragmentContext, R.drawable.icon_playlists)!!.toBitmap()
            sivPlaylistImage.setColorFilter(ColorFunctions.getThemeColor(fragmentContext, 5))
            sivPlaylistImage.setPadding(80)
            sivPlaylistImage.setImageBitmap(playlistIcon)


            tvPlaylistName.text = playlistSongsList[0].genre

            adapterRVSongs = AdapterRVSongs(playlistSongsList, parentFragmentManager, showViewAlbum = true, showViewArtist = true)
            rvSongs.adapter = adapterRVSongs
        }

        handleMusicClicked()
    }

    private fun assignVariables(view: View) {

        fragmentView = view
        fragmentContext = view.context
        clMain = view.findViewById(R.id.clMain_FragmentGenrePlaylist)
        btBack = view.findViewById(R.id.btBack_Toolbar)
        sivPlaylistImage = view.findViewById(R.id.sivArt_FragmentGenrePlaylist)
        tvPlaylistName = view.findViewById(R.id.tvName_FragmentGenrePlaylist)
        rvSongs = view.findViewById(R.id.rvSongs_FragmentGenrePlaylist)


        isGenrePlaylist = requireArguments().getBoolean("isGenrePlaylist", false)
        genreID = requireArguments().getLong("genreID", 0)


        rvSongs.layoutManager = LinearLayoutManager(fragmentContext)
        rvSongs.addItemDecoration(RecyclerViewSpacer(10))
        rvSongs.itemAnimator = null
    }


    private fun setupTheme(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
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

    var onBackPressedListener: OnBackPressedListener?= null
    interface OnBackPressedListener{ fun onBackPressed() }
}