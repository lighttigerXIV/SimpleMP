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
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class FragmentAlbum : Fragment() {

    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var btBack: Button
    private lateinit var ivAlbumArt: ImageView
    private lateinit var tvAlbumName: TextView
    private lateinit var tvAlbumArtist: TextView
    private lateinit var rvSongs: RecyclerView
    private var albumID: Long = 0
    private var onBackListener: OnBackPressed? = null
    private var songsList = ArrayList<Song>()


    private lateinit var adapterRVSongs: AdapterRVSongs
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false


    fun updateCurrentSong(){

        if( serviceBounded ){

            adapterRVSongs.currentSongPath = smpService.getCurrentSongPath()
            adapterRVSongs.notifyItemRangeChanged( 0, adapterRVSongs.getPlayListSize() )
        }
    }

    fun resetRecyclerView(){

        adapterRVSongs.currentSongPath = ""
        adapterRVSongs.notifyItemRangeChanged( 0, adapterRVSongs.getPlayListSize() )
    }


    interface OnBackPressed{ fun onBackPressed() }
    fun setOnBackPressed(listener: OnBackPressed){onBackListener = listener}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_album, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assignVariables(view)
        btBack.setOnClickListener { onBackListener?.onBackPressed() }

        handleBackPressed()


        val serviceIntent = Intent( fragmentContext, SimpleMPService::class.java )
        fragmentContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )


        songsList = GetSongs.getSongsList( fragmentContext, false )
        songsList.removeIf { it.albumID != albumID }


        adapterRVSongs = AdapterRVSongs(songsList)
        rvSongs.adapter = adapterRVSongs



        val songID = songsList[0].id
        val albumArt = GetSongs.getSongAlbumArt( fragmentContext, songID, albumID )
        val albumName = songsList[0].albumName
        val albumArtist = songsList[0].artistName


        ivAlbumArt.setImageBitmap( albumArt )
        tvAlbumName.text = albumName
        tvAlbumArtist.text = albumArtist


        adapterRVSongs.onItemClickListener = object : AdapterRVSongs.OnItemClickListener{
            override fun onItemClick(position: Int) {

                if( serviceBounded ){

                    SimpleMPService.startService(fragmentContext)

                    smpService.playList = songsList

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


    private fun assignVariables( view: View ) {

        fragmentView = view
        fragmentContext = view.context
        btBack = fragmentView.findViewById(R.id.btBack_Toolbar)
        tvAlbumName = fragmentView.findViewById(R.id.albumName_FragmentAlbum)
        tvAlbumArtist = fragmentView.findViewById(R.id.albumArtist_FragmentAlbum)
        ivAlbumArt = fragmentView.findViewById(R.id.albumArt_FragmentAlbum)
        rvSongs = fragmentView.findViewById(R.id.rvSongs_FragmentAlbum)

        albumID = arguments!!.getLong( "albumID" )


        rvSongs.layoutManager = LinearLayoutManager(fragmentContext)
        rvSongs.addItemDecoration(RecyclerViewSpacer(10))
        rvSongs.itemAnimator?.changeDuration = 0
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


    private fun handleBackPressed(){

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    if (isEnabled) {
                        isEnabled = false
                        onBackListener?.onBackPressed()
                    }
                }
            }
        )
    }
}