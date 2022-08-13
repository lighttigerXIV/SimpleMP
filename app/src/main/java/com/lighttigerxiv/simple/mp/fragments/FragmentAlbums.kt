package com.lighttigerxiv.simple.mp.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lighttigerxiv.simple.mp.others.GetSongs
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.RVSpacerHorizontal
import com.lighttigerxiv.simple.mp.Song
import com.lighttigerxiv.simple.mp.adapters.AdapterRVAlbums
import com.lighttigerxiv.simple.mp.others.ColorFunctions


class FragmentAlbums : Fragment() {

    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var clMain: ConstraintLayout
    private lateinit var etSearch: EditText
    private lateinit var rvAlbums: RecyclerView
    private lateinit var songsList: ArrayList<Song>
    private lateinit var onAlbumOpenedListener: OnAlbumOpenedListener


    interface OnAlbumOpenedListener { fun onAlbumOpened( albumID: Long ) }
    fun setOnAlbumOpenedListener( listener: OnAlbumOpenedListener){onAlbumOpenedListener = listener}


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.fragment_albums, container, false)
        view.context.setTheme(ColorFunctions.getTheme(view.context))
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assignVariables(view)
        setupTheming()



        val adapterSongsRV = AdapterRVAlbums( songsList )
        rvAlbums.adapter = adapterSongsRV


        adapterSongsRV.setOnAlbumClickedListener(object : AdapterRVAlbums.OnAlbumClickedListener{
            override fun onAlbumOpened(albumID: Long) { onAlbumOpenedListener.onAlbumOpened( albumID ) }
        })
    }

    private fun assignVariables(view: View){

        fragmentView = view
        fragmentContext = view.context
        clMain = view.findViewById(R.id.clMain_FragmentAlbums)
        etSearch = view.findViewById(R.id.etSearch_FragmentAlbums)
        rvAlbums = fragmentView.findViewById(R.id.rvAlbums_FragmentAlbums)
        songsList = GetSongs.getSongsList(fragmentContext, false)
        songsList = songsList.distinctBy { it.albumID } as ArrayList<Song>
        songsList.sortBy { it.albumName }


        val deviceOrientation = fragmentContext.resources.configuration.orientation

        if( deviceOrientation == Configuration.ORIENTATION_PORTRAIT )
            rvAlbums.layoutManager = GridLayoutManager( fragmentContext, 2 )

        else
            rvAlbums.layoutManager = GridLayoutManager( fragmentContext, 4 )

        rvAlbums.addItemDecoration( RVSpacerHorizontal( 10 ) )
    }


    private fun setupTheming(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
        etSearch.background = ColorFunctions.getEditTextBackground(fragmentContext)
    }
}