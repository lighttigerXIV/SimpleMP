package com.lighttigerxiv.simple.mp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.lighttigerxiv.simple.mp.others.GetSongs
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.Song

class AdapterRVAlbums( var albumsList: ArrayList<Song> ): RecyclerView.Adapter<AdapterRVAlbums.ViewHolder>(){

    var onAlbumOpenedListener: OnAlbumClickedListener? = null
    interface OnAlbumClickedListener{ fun onAlbumOpened(albumID: Long) }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootView = LayoutInflater.from(parent.context).inflate(R.layout.rv_albums, parent, false)

        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val context = holder.itemView.context
        val song = albumsList[position]
        val songID = song.id
        val songAlbumID = song.albumID
        val songAlbumArt = GetSongs.getSongAlbumArt(context, songID, songAlbumID)
        val songAlbumName = song.albumName


        holder.albumArt.setImageBitmap( songAlbumArt )
        holder.albumName.text = songAlbumName
        holder.clMain.setOnClickListener{ onAlbumOpenedListener?.onAlbumOpened( songAlbumID ) }
    }

    override fun getItemCount(): Int { return albumsList.size }

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
