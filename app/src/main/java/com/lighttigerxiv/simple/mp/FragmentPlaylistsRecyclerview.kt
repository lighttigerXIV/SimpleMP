package com.lighttigerxiv.simple.mp

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream


class FragmentPlaylistsRecyclerview : Fragment() {

    //Main
    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var clAddPlaylists: ConstraintLayout
    private lateinit var btCreatePlaylist: Button
    private lateinit var rvPlaylists: RecyclerView
    private lateinit var ivInfo: ImageView
    private lateinit var tvNoPlaylists: TextView

    //Adapters
    private lateinit var adapterRVGenrePlaylists: AdapterRVGenrePlaylists
    private lateinit var adapterUserPlaylists: AdapterUserPlaylists

    private var userPlaylists = ArrayList<Playlist>()


    //Others
    private var page = ""


    var onGenrePlaylistClickListener: OnGenrePlaylistClickListener ?= null
    interface OnGenrePlaylistClickListener{ fun onGenrePlaylistClicked(genreID: Long ) }

    var onUserPlaylistClickListener: OnUserPlaylistClickListener?= null
    interface OnUserPlaylistClickListener{ fun onUserPlaylistClicked( id: Int ) }


    fun resetPlaylists() {

        val userPlaylistsJson = fragmentContext.getSharedPreferences("playlists", MODE_PRIVATE)
            .getString("playlists", null)


        //If there are no playlists
        if( userPlaylistsJson == null ){

            ivInfo.visibility = View.VISIBLE
            tvNoPlaylists.visibility = View.VISIBLE


            adapterUserPlaylists = AdapterUserPlaylists(ArrayList())
            rvPlaylists.adapter = adapterUserPlaylists
        }
        else{

            val jsonType = object : TypeToken<ArrayList<Playlist>>(){}.type
            userPlaylists = Gson().fromJson(userPlaylistsJson, jsonType)

            adapterUserPlaylists = AdapterUserPlaylists(userPlaylists)
            rvPlaylists.adapter = adapterUserPlaylists


            if(userPlaylists.size == 0){

                ivInfo.visibility = View.VISIBLE
                tvNoPlaylists.visibility = View.VISIBLE
            }
        }

        handlePlaylistsClick()
        handleCreatePlaylist()
    }


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_playlists_recyclerview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assignVariables(view)


        if( page == "genres" ){

            clAddPlaylists.visibility = View.GONE
            val genresList = ArrayList(GetSongs.getSongsList(fragmentContext, false)).distinctBy{ it.genreID } as ArrayList<Song>


            adapterRVGenrePlaylists = AdapterRVGenrePlaylists(genresList)
            rvPlaylists.adapter = adapterRVGenrePlaylists


            adapterRVGenrePlaylists.onGenrePlaylistClickListener = object : AdapterRVGenrePlaylists.OnGenrePlaylistClickListener{
                override fun onGenrePlaylistClicked(genreID: Long) {

                    onGenrePlaylistClickListener?.onGenrePlaylistClicked(genreID)
                }
            }
        }
        if( page == "yourPlaylists" ){

            val userPlaylistsJson = fragmentContext.getSharedPreferences("playlists", MODE_PRIVATE)
                .getString("playlists", null)


            //If there are no playlists
            if( userPlaylistsJson == null ){

                ivInfo.visibility = View.VISIBLE
                tvNoPlaylists.visibility = View.VISIBLE


                adapterUserPlaylists = AdapterUserPlaylists(ArrayList())
                rvPlaylists.adapter = adapterUserPlaylists
            }
            else{

                val jsonType = object : TypeToken<ArrayList<Playlist>>(){}.type
                userPlaylists = Gson().fromJson(userPlaylistsJson, jsonType)

                adapterUserPlaylists = AdapterUserPlaylists(userPlaylists)
                rvPlaylists.adapter = adapterUserPlaylists
            }

            handlePlaylistsClick()
            handleCreatePlaylist()
        }
    }


    private fun assignVariables(view: View){

        fragmentView = view
        fragmentContext = view.context
        clAddPlaylists = view.findViewById(R.id.clAddPlaylist_FragmentPlaylistsRecyclerview)
        btCreatePlaylist = view.findViewById(R.id.btCreatePlaylist_FragmentPlaylistsRecyclerview)
        rvPlaylists = view.findViewById(R.id.rvPlaylists_FragmentPlaylistsRecyclerview)
        ivInfo = view.findViewById(R.id.ivInfo_FragmentPlaylistsRecyclerview)
        tvNoPlaylists = view.findViewById(R.id.tvNoPlaylistFound_FragmentPlaylistsRecyclerview)

        page = arguments!!.getString("page", "")


        val deviceOrientation = fragmentContext.resources.configuration.orientation

        if( deviceOrientation == Configuration.ORIENTATION_PORTRAIT )
            rvPlaylists.layoutManager = GridLayoutManager(fragmentContext, 3)

        else
            rvPlaylists.layoutManager = GridLayoutManager(fragmentContext, 4)
    }


    private fun handleCreatePlaylist(){

        btCreatePlaylist.setOnClickListener{

            val bottomSheetAddPlaylist = BottomSheetDialog(fragmentContext)
            bottomSheetAddPlaylist.setContentView(R.layout.bottom_sheet_add_playlist)

            val etPlaylistName = bottomSheetAddPlaylist.findViewById<EditText>(R.id.etPlaylistName_BottomSheetAddPlaylist)
            val btAddPlaylistBS = bottomSheetAddPlaylist.findViewById<Button>(R.id.btAddPlaylist_BottomSheetAddPlaylist)

            btAddPlaylistBS?.setOnClickListener {

                val newPlaylistName = etPlaylistName?.text.toString()

                if(newPlaylistName.trim().isEmpty())
                    Toast.makeText(fragmentContext, getString(R.string.FillPlaylistName), Toast.LENGTH_LONG).show()

                else{

                    val playlistIcon = ContextCompat.getDrawable( fragmentContext, R.drawable.icon_playlist) as BitmapDrawable
                    val iconBitmap = playlistIcon.bitmap
                    val baos = ByteArrayOutputStream()
                    iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                    val iconString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)


                    val newPlaylistID = if( userPlaylists.size == 0 ) 1 else userPlaylists.maxOf { it.id } + 1 //Gives an id to playlist

                    userPlaylists.add( Playlist(newPlaylistID, newPlaylistName, iconString, ArrayList()) )

                    fragmentContext.getSharedPreferences( "playlists", MODE_PRIVATE )
                        .edit()
                        .putString("playlists", Gson().toJson(userPlaylists))
                        .apply()


                    adapterUserPlaylists = AdapterUserPlaylists(userPlaylists)
                    rvPlaylists.adapter = adapterUserPlaylists

                    handlePlaylistsClick()

                    ivInfo.visibility = View.GONE
                    tvNoPlaylists.visibility = View.GONE

                    bottomSheetAddPlaylist.dismiss()
                }
            }

            bottomSheetAddPlaylist.show()
        }
    }

    private fun handlePlaylistsClick(){

        adapterUserPlaylists.onUserPlaylistClickListener = object : AdapterUserPlaylists.OnUserPlaylistClickListener{
            override fun onUserPlaylistClicked(id: Int) {

                onUserPlaylistClickListener?.onUserPlaylistClicked(id)
            }
        }
    }



    // Adapters ////////////////////////////////////////////////////////////////////////////////////

    class AdapterRVGenrePlaylists(private var genresList: ArrayList<Song>): RecyclerView.Adapter<AdapterRVGenrePlaylists.ViewHolder>(){

        var onGenrePlaylistClickListener: OnGenrePlaylistClickListener ?= null
        interface OnGenrePlaylistClickListener{ fun onGenrePlaylistClicked(genreID: Long ) }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_playlists, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val playlist = genresList[position]
            val genreID = playlist.genreID
            val playlistName = playlist.genre


            holder.tvName.text = playlistName

            holder.clMain.setOnClickListener { onGenrePlaylistClickListener?.onGenrePlaylistClicked(genreID) }
        }

        override fun getItemCount(): Int {return genresList.size}

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

            var clMain: ConstraintLayout = itemView.findViewById(R.id.clMain_RVPlaylists)
            var tvName: TextView = itemView.findViewById(R.id.playlistName_RVPlaylists)
        }
    }


    class AdapterUserPlaylists(private var userPlaylists: ArrayList<Playlist>): RecyclerView.Adapter<AdapterUserPlaylists.ViewHolder>(){

        var onUserPlaylistClickListener: OnUserPlaylistClickListener?= null
        interface OnUserPlaylistClickListener{ fun onUserPlaylistClicked(id: Int ) }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_playlists, parent, false))
        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val playlist = userPlaylists[holder.absoluteAdapterPosition]
            val playListID = playlist.id
            val playlistArtString = playlist.image
            val playlistName = playlist.name


            val decodedPlaylistArt = Base64.decode(playlistArtString, 0)
            val playlistArtBitmap = BitmapFactory.decodeByteArray(decodedPlaylistArt, 0, decodedPlaylistArt.size)


            holder.ivArt.setImageBitmap(playlistArtBitmap)
            holder.tvName.text = playlistName

            holder.clMain.setOnClickListener { onUserPlaylistClickListener?.onUserPlaylistClicked(playListID) }
        }

        override fun getItemCount(): Int {return userPlaylists.size}

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

            var clMain: ConstraintLayout = itemView.findViewById(R.id.clMain_RVPlaylists)
            var ivArt: ShapeableImageView = itemView.findViewById(R.id.playlistArt_RVPlaylists)
            var tvName: TextView = itemView.findViewById(R.id.playlistName_RVPlaylists)
        }
    }

}