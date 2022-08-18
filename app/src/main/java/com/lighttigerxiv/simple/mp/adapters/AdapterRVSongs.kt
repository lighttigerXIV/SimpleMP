package com.lighttigerxiv.simple.mp.adapters

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.lighttigerxiv.simple.mp.activities.ActivityAddToPlaylist
import com.lighttigerxiv.simple.mp.others.GetSongs
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.Song
import com.lighttigerxiv.simple.mp.fragments.*
import com.lighttigerxiv.simple.mp.others.ColorFunctions

class AdapterRVSongs(var songsList : ArrayList<Song>, private val fragmentManager: FragmentManager, private val showViewAlbum: Boolean, private val showViewArtist: Boolean ): RecyclerView.Adapter<AdapterRVSongs.ViewHolder>() {

    var currentSongPath = ""


    var onItemClickListener:  OnItemClickListener?= null
    interface OnItemClickListener{ fun onItemClick( position: Int ) }


    var onAddPlaylistClickListener: OnAddPlaylistClickListener? = null
    interface OnAddPlaylistClickListener{fun onAddPlaylistClicked()}


    fun getPlayListSize(): Int{ return songsList.size }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(LayoutInflater.from( parent.context ).inflate(R.layout.rv_songs, parent, false ))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val context = holder.itemView.context
        val song = songsList[position]
        val songPath = song.path
        val songID = song.id
        val songAlbumID = song.albumID
        val songAlbumArt = GetSongs.getSongAlbumArt(context, songID, songAlbumID)
        val songTitle = song.title
        val songArtist = song.artistName
        val songArtistID = song.artistID

        val fragmentAlbum = try{ fragmentManager.findFragmentByTag("floatingAlbum") as FragmentAlbum }
        catch (exc: Exception){ FragmentAlbum() }

        var fragmentArtist = try{ fragmentManager.findFragmentByTag("floatingArtist") as FragmentArtist }
        catch (exc: Exception){ FragmentArtist() }

        var fragmentArtistAlbum = try{ fragmentManager.findFragmentByTag("floatingArtistAlbum") as FragmentAlbum }
        catch (exc: Exception){ FragmentAlbum() }




        holder.albumArt.setImageBitmap(songAlbumArt)
        holder.title.text = songTitle
        holder.artist.text = songArtist



        if( currentSongPath == songPath ) {
            holder.title.setTypeface(null, Typeface.BOLD)
            holder.title.setTextColor(ColorFunctions.getThemeColor(holder.itemView.context, 5))
        }

        else {
            holder.title.setTypeface(null, Typeface.NORMAL)
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text))
        }


        holder.clMain.setOnClickListener{ onItemClickListener?.onItemClick( position ) }

        holder.ibMore.setOnClickListener{


            val popupView = holder.itemView.findViewById<View>(R.id.ibMore_RVSongs)
            val wrapper = ContextThemeWrapper(context, ColorFunctions.getPopupMenuTheme(context))
            val popupMenu = PopupMenu( wrapper, popupView )
            popupMenu.menuInflater.inflate(R.menu.menu_more_rv_songs, popupMenu.menu )

            if(!showViewAlbum)  popupMenu.menu.removeItem(R.id.menuViewAlbum)
            if(!showViewArtist) popupMenu.menu.removeItem(R.id.menuViewArtist)


            popupMenu.setOnMenuItemClickListener {

                when (it.itemId) {

                    R.id.menuAddToPlaylist -> {

                        val intent = Intent(context, ActivityAddToPlaylist::class.java)
                        val bundle = Bundle()

                        bundle.putString("selectedSongID", Gson().toJson(song.id))
                        intent.putExtras(bundle)

                        context.startActivity(intent)
                    }

                    R.id.menuViewAlbum ->{

                        val bundle = Bundle()
                        bundle.putLong("albumID", songAlbumID)
                        fragmentAlbum.arguments = bundle


                        fragmentManager.beginTransaction().add( R.id.frameLayout_ActivityMain, fragmentAlbum, "floatingAlbum").commit()
                    }

                    R.id.menuViewArtist ->{

                        if(fragmentArtist.isAdded) fragmentArtist = FragmentArtist() //Very Important

                        val bundle = Bundle()
                        bundle.putLong("artistID", songArtistID)
                        bundle.putString("artistName", songArtist)
                        fragmentArtist.arguments = bundle

                        fragmentManager.beginTransaction().add( R.id.frameLayout_ActivityMain, fragmentArtist, "floatingArtist").commit()
                    }
                }

                true
            }

            popupMenu.show()
        }


        fragmentAlbum.setOnBackPressed(object : FragmentAlbum.OnBackPressed{
            override fun onBackPressed() {

                val selectedFragment = context.getSharedPreferences("selectedFragment", MODE_PRIVATE).getInt("selectedFragment", 0)

                lateinit var fragment: Fragment

                when(selectedFragment){

                    0-> fragment = fragmentManager.findFragmentByTag("home") as FragmentHome
                    1-> fragment = fragmentManager.findFragmentByTag("artists") as FragmentArtists
                    2-> fragment = fragmentManager.findFragmentByTag("albums") as FragmentAlbums
                    3-> fragment = fragmentManager.findFragmentByTag("playlists") as FragmentPlaylists
                }

                fragmentManager.beginTransaction().remove(fragmentAlbum).commit()
                fragmentManager.beginTransaction().show(fragment).commit()
            }
        })



        fragmentArtist.onBackPressedListener = object : FragmentArtist.OnBackPressedListener{
            override fun onBackPressed() {

                val selectedFragment = context.getSharedPreferences("selectedFragment", MODE_PRIVATE).getInt("selectedFragment", 0)

                lateinit var fragment: Fragment

                when(selectedFragment){

                    0-> fragment = fragmentManager.findFragmentByTag("home") as FragmentHome
                    1-> fragment = fragmentManager.findFragmentByTag("artists") as FragmentArtists
                    2-> fragment = fragmentManager.findFragmentByTag("albums") as FragmentAlbums
                    3-> fragment = fragmentManager.findFragmentByTag("playlists") as FragmentPlaylists
                }

                fragmentManager.beginTransaction().hide(fragmentArtist).commit()
                fragmentManager.beginTransaction().show(fragment).commit()
            }
        }

        fragmentArtist.onAlbumOpenedListener = object : FragmentArtist.OnAlbumOpenedListener{
            override fun onAlbumOpened(albumID: Long) {

                if(fragmentArtistAlbum.isAdded) fragmentArtistAlbum = FragmentAlbum()

                val bundle = Bundle()
                bundle.putLong( "albumID", albumID )
                fragmentArtistAlbum.arguments = bundle


                fragmentManager.beginTransaction().hide( fragmentArtist ).commit()
                fragmentManager.beginTransaction().add( R.id.frameLayout_ActivityMain, fragmentArtistAlbum, "floatingArtistAlbum" ).commit()
            }
        }

        fragmentArtistAlbum.setOnBackPressed(object : FragmentAlbum.OnBackPressed{
            override fun onBackPressed() {

                fragmentManager.beginTransaction().hide(fragmentArtistAlbum).commit()
                fragmentManager.beginTransaction().show(fragmentArtist).commit()
            }
        })
    }

    override fun getItemCount(): Int { return songsList.size }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val clMain: ConstraintLayout = itemView.findViewById(R.id.clMain_RVSongs)
        val albumArt: ShapeableImageView = itemView.findViewById(R.id.albumArt_RVSongs)
        val title: TextView = itemView.findViewById(R.id.title_RVSongs)
        val artist: TextView = itemView.findViewById(R.id.artist_RVSongs)
        val ibMore: ImageButton = itemView.findViewById(R.id.ibMore_RVSongs)
    }
}