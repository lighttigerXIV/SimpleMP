package com.lighttigerxiv.simple.mp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class AdapterRVSongs(private var songsList : ArrayList<Song> ): RecyclerView.Adapter<AdapterRVSongs.ViewHolder>() {

    private lateinit var clickListener:  OnItemClickListener
    private var currentSongPath = ""

    interface OnItemClickListener{ fun onItemClick( position: Int ) }

    fun setOnItemClickListener( listener: OnItemClickListener){ clickListener = listener }

    fun setCurrentSongPath( path: String){ currentSongPath = path }

    fun getPlayListSize(): Int{ return songsList.size }

    fun setPlaylist( songsList: ArrayList<Song> ){ this.songsList = songsList }

    fun getPlaylist(): ArrayList<Song>{ return this.songsList }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from( parent.context ).inflate( R.layout.rv_songs, parent, false ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val context = holder.itemView.context
        val songUri = songsList[position].uri
        val songAlbumID = songsList[position].albumID
        val songAlbumArt = GetSongs.getSongAlbumArt( context, songUri, songAlbumID )
        val songTitle = songsList[position].title
        val songArtist = songsList[position].artistName
        val songPath = songsList[position].path


        holder.albumArt.setImageBitmap(songAlbumArt)
        holder.title.text = songTitle
        holder.artist.text = songArtist



        if( currentSongPath == songPath ){

            holder.title.setTextColor( ContextCompat.getColor( holder.itemView.context, R.color.mainPurple) )
        }

        else{

            holder.title.setTextColor( ContextCompat.getColor( holder.itemView.context, R.color.text) )
        }



        holder.clMain.setOnClickListener{


            clickListener.onItemClick( position )
        }
    }

    override fun getItemCount(): Int { return songsList.size }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var clMain: ConstraintLayout
        var albumArt: ShapeableImageView
        var title: TextView
        var artist: TextView


        init {

            clMain = itemView.findViewById(R.id.clMain_RVSongs)
            albumArt = itemView.findViewById(R.id.albumArt_RVSongs)
            title = itemView.findViewById(R.id.title_RVSongs)
            artist = itemView.findViewById(R.id.artist_RVSongs)
        }
    }
}