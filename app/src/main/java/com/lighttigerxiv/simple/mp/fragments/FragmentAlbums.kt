package com.lighttigerxiv.simple.mp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lighttigerxiv.simple.mp.others.GetSongs
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.Song
import com.lighttigerxiv.simple.mp.adapters.AdapterRVAlbums
import com.lighttigerxiv.simple.mp.others.ColorFunctions


class FragmentAlbums : Fragment() {

    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var clMain: ConstraintLayout
    private lateinit var etSearch: EditText
    private lateinit var ibClearSearch: ImageButton
    private lateinit var rvAlbums: RecyclerView
    private lateinit var ivCricket: ImageView
    private lateinit var tvThisIsFeelingEmpty: TextView
    private lateinit var albumsList: ArrayList<Song>
    private lateinit var onAlbumOpenedListener: OnAlbumOpenedListener

    private lateinit var adapterRVAlbums: AdapterRVAlbums

    private var albumsLoaded = false


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

        if(savedInstanceState != null) restoreLifecycle(savedInstanceState)

        if(!albumsLoaded) loadAlbums()
    }

    override fun onResume() {
        super.onResume()

        handleSearch()

        if(etSearch.text.toString().trim().isNotEmpty())
            ibClearSearch.visibility = View.VISIBLE
    }

    private fun assignVariables(view: View){

        fragmentView = view
        fragmentContext = view.context
        clMain = view.findViewById(R.id.clMain_FragmentAlbums)
        etSearch = view.findViewById(R.id.etSearch_FragmentAlbums)
        ibClearSearch = view.findViewById(R.id.ibClearSearch_FragmentAlbums)
        rvAlbums = fragmentView.findViewById(R.id.rvAlbums_FragmentAlbums)
        ivCricket = view.findViewById(R.id.ivCricket_FragmentAlbums)
        tvThisIsFeelingEmpty = view.findViewById(R.id.tvThisIsFeelingEmpty_FragmentAlbums)


        val deviceOrientation = fragmentContext.resources.configuration.orientation

        if( deviceOrientation == Configuration.ORIENTATION_PORTRAIT )
            rvAlbums.layoutManager = GridLayoutManager( fragmentContext, 2 )

        else
            rvAlbums.layoutManager = GridLayoutManager( fragmentContext, 4 )
    }


    private fun restoreLifecycle(sis: Bundle) {

        albumsLoaded = sis.getBoolean("albumsLoaded", false)

        if(albumsLoaded){

            val adapterAlbumsListJson = sis.getString("adapterAlbumsList", "")
            val albumsListJson = sis.getString("albumsList", "")
            val jsonType = object : TypeToken<ArrayList<Song>>(){}.type

            val adapterAlbumsList = Gson().fromJson<ArrayList<Song>>(adapterAlbumsListJson, jsonType)
            albumsList = Gson().fromJson(albumsListJson, jsonType)

            adapterRVAlbums = AdapterRVAlbums(adapterAlbumsList)
            rvAlbums.adapter = adapterRVAlbums

            if(albumsList.size == 0){
                ivCricket.visibility = View.VISIBLE
                tvThisIsFeelingEmpty.visibility = View.VISIBLE
            }

            handleAlbumClicked()
        }
    }


    private fun setupTheming(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
        etSearch.background = ColorFunctions.getEditTextBackground(fragmentContext)
    }


    private fun loadAlbums(){

        val songsList = GetSongs.getSongsList(fragmentContext, false)
        albumsList = songsList.distinctBy { it.albumID } as ArrayList<Song>
        albumsList.sortBy { it.albumName }


        adapterRVAlbums = AdapterRVAlbums( albumsList )
        rvAlbums.adapter = adapterRVAlbums

        if(albumsList.size == 0){
            ivCricket.visibility = View.VISIBLE
            tvThisIsFeelingEmpty.visibility = View.VISIBLE
        }

        handleAlbumClicked()

        albumsLoaded = true
    }


    private fun handleAlbumClicked(){

        adapterRVAlbums.onAlbumOpenedListener = object : AdapterRVAlbums.OnAlbumClickedListener{
            override fun onAlbumOpened(albumID: Long) { onAlbumOpenedListener.onAlbumOpened( albumID ) }
        }
    }


    private fun handleSearch(){

        etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                val searchText = p0.toString().trim()

                if(searchText.isNotEmpty())
                    ibClearSearch.visibility = View.VISIBLE

                else
                    ibClearSearch.visibility = View.GONE


                val filteredList = ArrayList(albumsList)
                filteredList.removeIf{ !it.albumName.trim().lowercase().contains(searchText) }


                adapterRVAlbums.albumsList = filteredList
                adapterRVAlbums.notifyDataSetChanged()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        ibClearSearch.setOnClickListener {

            etSearch.setText("")
            ibClearSearch.visibility = View.GONE
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("albumsLoaded", albumsLoaded)

        if(albumsLoaded){

            outState.putString("albumsList", Gson().toJson(albumsList))
            outState.putString("adapterAlbumsList", Gson().toJson(adapterRVAlbums.albumsList))
        }
    }
}