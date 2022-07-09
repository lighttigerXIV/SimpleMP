package com.lighttigerxiv.simple.mp

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
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView


class FragmentArtists : Fragment() {

    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var etSearch: EditText
    private lateinit var rvArtists: RecyclerView


    //Others
    private var artistList = ArrayList<Song>()
    private lateinit var adapterRVArtists: AdapterRVArtists

    //Interfaces
    private lateinit var onArtistOpenedListener: OnArtistOpenedListener

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_artists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assignVariables(view)


        handleSearch()
        loadRVArtists()
    }


    private fun assignVariables( view: View ){

        fragmentView = view
        fragmentContext = view.context
        etSearch = view.findViewById(R.id.etSearch_FragmentArtists)
        rvArtists = view.findViewById(R.id.rvArtists_FragmentArtists)


        val deviceOrientation = fragmentContext.resources.configuration.orientation
        if( deviceOrientation == Configuration.ORIENTATION_PORTRAIT )
            rvArtists.layoutManager = GridLayoutManager( fragmentContext, 2 )

        else
            rvArtists.layoutManager = GridLayoutManager( fragmentContext, 4 )
    }


    //Loads the artists RecyclerView
    private fun loadRVArtists(){

        artistList = GetSongs.getSongsList( fragmentContext, false )
        artistList = artistList.distinctBy { it.artistID } as ArrayList<Song>
        artistList.sortBy { it.artistName }


        adapterRVArtists = AdapterRVArtists(artistList)
        rvArtists.adapter = adapterRVArtists


        adapterRVArtists.setOnArtistOpenedListener(object : AdapterRVArtists.OnArtistOpenedListener{
            override fun onArtistOpened(artistID: Long, artistName: String) { onArtistOpenedListener.onArtistOpened( artistID, artistName ) }
        })
    }


    //Handles the search bar
    private fun handleSearch(){

        etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val filteredList = ArrayList(artistList)
                filteredList.removeIf { !it.artistName.lowercase().contains(s.toString().lowercase().trim()) }

                adapterRVArtists.setArtistList( filteredList )
                adapterRVArtists.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Listeners

    interface OnArtistOpenedListener{ fun onArtistOpened(artistID: Long, artistName: String) }
    fun setOnArtistOpenedListener( listener: OnArtistOpenedListener ){ onArtistOpenedListener = listener }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Adapters
    class AdapterRVArtists(private var artistsList: ArrayList<Song>): RecyclerView.Adapter<AdapterRVArtists.ViewHolder>(){

        private lateinit var onArtistOpenedListener: OnArtistOpenedListener

        ////////////////////////////////////////////////////////////////////////////////////////////
        //Listeners

        fun setArtistList(list: ArrayList<Song>){ artistsList = list }

        interface OnArtistOpenedListener{ fun onArtistOpened(artistID: Long, artistName: String) }
        fun setOnArtistOpenedListener(listener: OnArtistOpenedListener){ onArtistOpenedListener = listener }


        ////////////////////////////////////////////////////////////////////////////////////////////
        //Main

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val rootView = LayoutInflater.from(parent.context).inflate(R.layout.rv_artists, parent, false)
            return ViewHolder(rootView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val context = holder.itemView.context
            val artist = artistsList[position]
            val artistUri = artist.uri
            val artistAlbumID = artist.albumID
            val artistAlbum = GetSongs.getSongAlbumArt( context, artistUri, artistAlbumID )
            val artistName = artist.artistName
            val artistID = artist.artistID


            holder.clMain.setOnClickListener{ onArtistOpenedListener.onArtistOpened( artistID , artistName) }
            holder.ivArtistAlbum.setImageBitmap( artistAlbum )
            holder.tvArtistName.text = artistName
        }

        override fun getItemCount(): Int { return artistsList.size }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            var clMain: ConstraintLayout
            var ivArtistAlbum: ShapeableImageView
            var tvArtistName: TextView

            init {

                clMain = itemView.findViewById(R.id.clMain_RVArtists)
                ivArtistAlbum = itemView.findViewById(R.id.artistAlbum_RVArtists)
                tvArtistName = itemView.findViewById(R.id.artistName_RVArtists)
            }
        }
    }
}