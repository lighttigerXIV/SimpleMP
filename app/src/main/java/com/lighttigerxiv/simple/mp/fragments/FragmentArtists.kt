package com.lighttigerxiv.simple.mp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
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
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lighttigerxiv.simple.mp.others.GetSongs
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.Song
import com.lighttigerxiv.simple.mp.others.ColorFunctions


class FragmentArtists : Fragment() {

    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var clMain: ConstraintLayout
    private lateinit var etSearch: EditText
    private lateinit var ibClearSearch: ImageButton
    private lateinit var rvArtists: RecyclerView
    private lateinit var ivCricket: ImageView
    private lateinit var tvThisIsFeelingEmpty: TextView


    //Others
    private var artistList = ArrayList<Song>()
    private lateinit var adapterRVArtists: AdapterRVArtists

    //Lifecycle
    private var artistsLoaded = false

    //Interfaces
    private lateinit var onArtistOpenedListener: OnArtistOpenedListener

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_artists, container, false)
        view.context.setTheme(ColorFunctions.getTheme(view.context))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assignVariables(view)
        setupColors()

        if(savedInstanceState != null) restoreLifecycle(savedInstanceState)

        if(!artistsLoaded) loadRVArtists()
    }

    override fun onResume() {
        super.onResume()

        handleSearch()

        if(etSearch.text.toString().trim().isNotEmpty())
            ibClearSearch.visibility = View.VISIBLE
    }

    private fun assignVariables(view: View ){

        fragmentView = view
        fragmentContext = view.context
        clMain = view.findViewById(R.id.clMain_FragmentArtists)
        etSearch = view.findViewById(R.id.etSearch_FragmentArtists)
        ibClearSearch = view.findViewById(R.id.ibClearSearch_FragmentArtists)
        rvArtists = view.findViewById(R.id.rvArtists_FragmentArtists)
        ivCricket = view.findViewById(R.id.ivCricket_FragmentArtists)
        tvThisIsFeelingEmpty = view.findViewById(R.id.tvThisIsFeelingEmpty_FragmentArtists)


        val deviceOrientation = fragmentContext.resources.configuration.orientation
        if( deviceOrientation == Configuration.ORIENTATION_PORTRAIT )
            rvArtists.layoutManager = GridLayoutManager( fragmentContext, 2 )

        else
            rvArtists.layoutManager = GridLayoutManager( fragmentContext, 4 )
    }


    private fun restoreLifecycle(sis: Bundle){

        artistsLoaded = sis.getBoolean("artistsLoaded", false)

        if(artistsLoaded){

            val adapterArtistListJson = sis.getString("adapterArtistList", "")
            val artistListJson = sis.getString("artistList", "")
            val jsonType = object : TypeToken<ArrayList<Song>>(){}.type

            val adapterArtistList = Gson().fromJson<ArrayList<Song>>(adapterArtistListJson, jsonType)
            artistList = Gson().fromJson(artistListJson, jsonType)


            adapterRVArtists = AdapterRVArtists(adapterArtistList)
            rvArtists.adapter = adapterRVArtists

            if(artistList.size == 0){
                ivCricket.visibility = View.VISIBLE
                tvThisIsFeelingEmpty.visibility = View.VISIBLE
            }

            handleArtistClicked()
        }
    }


    private fun setupColors(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
        etSearch.background = ColorFunctions.getEditTextBackground(fragmentContext)
    }


    //Loads the artists RecyclerView
    private fun loadRVArtists(){

        artistList = GetSongs.getSongsList(fragmentContext, false)
        artistList = artistList.distinctBy { it.artistID } as ArrayList<Song>
        artistList.sortBy { it.artistName }


        adapterRVArtists = AdapterRVArtists(artistList)
        rvArtists.adapter = adapterRVArtists
        handleArtistClicked()

        if(artistList.size == 0){
            ivCricket.visibility = View.VISIBLE
            tvThisIsFeelingEmpty.visibility = View.VISIBLE
        }

        artistsLoaded = true
    }


    private fun handleArtistClicked(){

        adapterRVArtists.onArtistOpenedListener = object : AdapterRVArtists.OnArtistOpenedListener {
            override fun onArtistOpened(artistID: Long, artistName: String) {
                onArtistOpenedListener.onArtistOpened( artistID, artistName )
            }
        }
    }


    //Handles the search bar
    private fun handleSearch(){

        etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val searchText = s.toString().lowercase().trim()

                if(searchText.isNotEmpty())
                    ibClearSearch.visibility = View.VISIBLE

                else
                    ibClearSearch.visibility = View.GONE


                val filteredList = ArrayList(artistList)
                filteredList.removeIf { !it.artistName.lowercase().contains(searchText) }

                adapterRVArtists.artistsList = filteredList
                adapterRVArtists.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        ibClearSearch.setOnClickListener {

            etSearch.setText("")
            ibClearSearch.visibility = View.GONE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)


        outState.putBoolean("artistsLoaded", artistsLoaded)

        if( artistsLoaded ){

            outState.putString("adapterArtistList", Gson().toJson(adapterRVArtists.artistsList))
            outState.putString("artistList", Gson().toJson(artistList))
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Listeners

    interface OnArtistOpenedListener{ fun onArtistOpened(artistID: Long, artistName: String) }
    fun setOnArtistOpenedListener( listener: OnArtistOpenedListener){ onArtistOpenedListener = listener }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Adapters
    class AdapterRVArtists(var artistsList: ArrayList<Song>): RecyclerView.Adapter<AdapterRVArtists.ViewHolder>(){

        var onArtistOpenedListener: OnArtistOpenedListener? = null
        interface OnArtistOpenedListener{ fun onArtistOpened(artistID: Long, artistName: String) }


        ////////////////////////////////////////////////////////////////////////////////////////////
        //Main

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val rootView = LayoutInflater.from(parent.context).inflate(R.layout.rv_artists, parent, false)
            return ViewHolder(rootView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val context = holder.itemView.context
            val artist = artistsList[position]
            val artistSongID = artist.id
            val artistAlbumID = artist.albumID
            val artistAlbum = GetSongs.getSongAlbumArt(context, artistSongID, artistAlbumID)
            val artistName = artist.artistName
            val artistID = artist.artistID


            holder.clMain.setOnClickListener{ onArtistOpenedListener?.onArtistOpened( artistID , artistName) }
            holder.ivArtistAlbum.setImageBitmap( artistAlbum )
            holder.tvArtistName.text = artistName
        }

        override fun getItemCount(): Int { return artistsList.size }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            var clMain: ConstraintLayout
            var ivArtistAlbum: ShapeableImageView
            var tvArtistName: TextView

            init {

                clMain = itemView.findViewById(R.id.clMain_RVPlaylists)
                ivArtistAlbum = itemView.findViewById(R.id.playlistArt_RVPlaylists)
                tvArtistName = itemView.findViewById(R.id.playlistName_RVPlaylists)
            }
        }
    }
}