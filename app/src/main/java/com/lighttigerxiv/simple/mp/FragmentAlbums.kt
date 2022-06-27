package com.lighttigerxiv.simple.mp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class FragmentAlbums : Fragment() {

    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var rvAlbums: RecyclerView
    private lateinit var songsList: ArrayList<Song>
    private lateinit var onAlbumOpenedListener: OnAlbumOpenedListener


    interface OnAlbumOpenedListener { fun onAlbumOpened( albumID: Long ) }
    fun setOnAlbumOpenedListener( listener: OnAlbumOpenedListener){onAlbumOpenedListener = listener}


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        return inflater.inflate(R.layout.fragment_albums, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        fragmentView = view
        fragmentContext = view.context
        rvAlbums = fragmentView.findViewById(R.id.rvAlbums_FragmentAlbums)
        songsList = GetSongs.getSongsList( fragmentContext )
        songsList = songsList.distinctBy { it.albumID } as ArrayList<Song>
        songsList.sortBy { it.albumName }


        val deviceOrientation = fragmentContext.resources.configuration.orientation

        if( deviceOrientation == Configuration.ORIENTATION_PORTRAIT )
            rvAlbums.layoutManager = GridLayoutManager( fragmentContext, 2 )

        else
            rvAlbums.layoutManager = GridLayoutManager( fragmentContext, 4 )




        rvAlbums.addItemDecoration( RVSpacerHorizontal( 10 ) )
        val adapterSongsRV = AdapterRVAlbums( songsList )
        rvAlbums.adapter = adapterSongsRV


        adapterSongsRV.setOnAlbumClickedListener(object : AdapterRVAlbums.OnAlbumClickedListener{
            override fun onAlbumOpened(albumID: Long) { onAlbumOpenedListener.onAlbumOpened( albumID ) }
        })
    }
}