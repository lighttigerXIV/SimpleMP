package com.lighttigerxiv.simple.mp

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView


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


    class AdapterRVAlbums( private var songsList: ArrayList<Song> ): RecyclerView.Adapter<AdapterRVAlbums.ViewHolder>(){

        private lateinit var onAlbumOpenedListener: OnAlbumClickedListener

        interface OnAlbumClickedListener{ fun onAlbumOpened(albumID: Long) }
        fun setOnAlbumClickedListener(listener: OnAlbumClickedListener){ onAlbumOpenedListener = listener }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val rootView = LayoutInflater.from(parent.context).inflate(R.layout.rv_albums, parent, false)

            return ViewHolder(rootView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val song = songsList[position]
            val songAlbumID = song.albumID
            val songAlbumArt = song.albumArt
            val songAlbumName = song.albumName


            holder.albumArt.setImageBitmap( songAlbumArt )
            holder.albumName.text = songAlbumName
            holder.clMain.setOnClickListener{ onAlbumOpenedListener.onAlbumOpened( songAlbumID ) }
        }

        override fun getItemCount(): Int { return songsList.size }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

            var clMain: ConstraintLayout
            var albumArt: ShapeableImageView
            var albumName: TextView

            init{

                clMain = itemView.findViewById(R.id.clMain_RVAlbums)
                albumArt = itemView.findViewById(R.id.albumArt_RVAlbums)
                albumName = itemView.findViewById(R.id.albumName_RVAlbums)
            }
        }
    }



}