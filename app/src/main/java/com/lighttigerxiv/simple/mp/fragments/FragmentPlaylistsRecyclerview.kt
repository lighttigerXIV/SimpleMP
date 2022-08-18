package com.lighttigerxiv.simple.mp.fragments

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lighttigerxiv.simple.mp.Playlist
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.RVSpacerHorizontal
import com.lighttigerxiv.simple.mp.Song
import com.lighttigerxiv.simple.mp.others.ColorFunctions
import com.lighttigerxiv.simple.mp.others.GetSongs


class FragmentPlaylistsRecyclerview : Fragment() {

    //Main
    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var clMain: ConstraintLayout
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


    var onGenrePlaylistClickListener: OnGenrePlaylistClickListener?= null
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
        val view = inflater.inflate(R.layout.fragment_playlists_recyclerview, container, false)
        view.context.setTheme(ColorFunctions.getTheme(view.context))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assignVariables(view)
        setupTheme()

        if( page == "genres" ){

            clAddPlaylists.visibility = View.GONE
            val genresList = ArrayList(GetSongs.getSongsList(fragmentContext, false)).distinctBy{ it.genreID } as ArrayList<Song>


            adapterRVGenrePlaylists = AdapterRVGenrePlaylists(genresList)
            rvPlaylists.adapter = adapterRVGenrePlaylists


            adapterRVGenrePlaylists.onGenrePlaylistClickListener = object : AdapterRVGenrePlaylists.OnGenrePlaylistClickListener {
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
        clMain = view.findViewById(R.id.clMain_FragmentPlaylistsRecyclerview)
        clAddPlaylists = view.findViewById(R.id.clAddPlaylist_FragmentPlaylistsRecyclerview)
        btCreatePlaylist = view.findViewById(R.id.btCreatePlaylist_FragmentPlaylistsRecyclerview)
        rvPlaylists = view.findViewById(R.id.rvPlaylists_FragmentPlaylistsRecyclerview)
        ivInfo = view.findViewById(R.id.ivInfo_FragmentPlaylistsRecyclerview)
        tvNoPlaylists = view.findViewById(R.id.tvNoPlaylistFound_FragmentPlaylistsRecyclerview)

        page = requireArguments().getString("page", "")


        val deviceOrientation = fragmentContext.resources.configuration.orientation

        if( deviceOrientation == Configuration.ORIENTATION_PORTRAIT )
            rvPlaylists.layoutManager = GridLayoutManager(fragmentContext, 2)

        else
            rvPlaylists.layoutManager = GridLayoutManager(fragmentContext, 4)

        rvPlaylists.addItemDecoration(RVSpacerHorizontal(20))
    }


    private fun setupTheme(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
        ViewCompat.setBackgroundTintList(btCreatePlaylist, ColorStateList.valueOf(ColorFunctions.getThemeColor(fragmentContext, 5)))
    }


    private fun handleCreatePlaylist(){

        btCreatePlaylist.setOnClickListener{

            val bottomSheetAddPlaylist = BottomSheetDialog(fragmentContext)
            bottomSheetAddPlaylist.setContentView(R.layout.bottom_sheet_add_playlist)

            val clBS = bottomSheetAddPlaylist.findViewById<ConstraintLayout>(R.id.clMain_BottomSheetAddPlaylist)!!
            val etPlaylistName = bottomSheetAddPlaylist.findViewById<EditText>(R.id.etPlaylistName_BottomSheetAddPlaylist)!!
            val btAddPlaylistBS = bottomSheetAddPlaylist.findViewById<Button>(R.id.btAddPlaylist_BottomSheetAddPlaylist)!!


            clBS.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
            etPlaylistName.background = ColorFunctions.getEditTextBackground(fragmentContext)
            ViewCompat.setBackgroundTintList(btAddPlaylistBS, ColorStateList.valueOf(ColorFunctions.getThemeColor(fragmentContext, 5)))

            btAddPlaylistBS.setOnClickListener {

                val newPlaylistName = etPlaylistName.text.toString()

                if(newPlaylistName.trim().isEmpty())
                    Toast.makeText(fragmentContext, getString(R.string.FillPlaylistName), Toast.LENGTH_LONG).show()

                else{

                    val newPlaylistID = if( userPlaylists.size == 0 ) 1 else userPlaylists.maxOf { it.id } + 1 //Gives an id to playlist

                    userPlaylists.add( Playlist(newPlaylistID, newPlaylistName, null, ArrayList()) )

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
            bottomSheetAddPlaylist.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun handlePlaylistsClick(){

        adapterUserPlaylists.onUserPlaylistClickListener = object : AdapterUserPlaylists.OnUserPlaylistClickListener {
            override fun onUserPlaylistClicked(id: Int) {

                onUserPlaylistClickListener?.onUserPlaylistClicked(id)
            }
        }
    }



    // Adapters ////////////////////////////////////////////////////////////////////////////////////

    class AdapterRVGenrePlaylists(private var genresList: ArrayList<Song>): RecyclerView.Adapter<AdapterRVGenrePlaylists.ViewHolder>(){

        var onGenrePlaylistClickListener: OnGenrePlaylistClickListener?= null
        interface OnGenrePlaylistClickListener{ fun onGenrePlaylistClicked(genreID: Long ) }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_playlists, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val context = holder.itemView.context
            val playlist = genresList[position]
            val genreID = playlist.genreID
            val playlistName = playlist.genre


            val playlistIcon = ContextCompat.getDrawable(context, R.drawable.icon_playlists)!!.toBitmap()
            holder.sivPlaylistArt.setColorFilter(ColorFunctions.getThemeColor(context, 5))
            holder.sivPlaylistArt.setPadding(80)
            holder.sivPlaylistArt.setImageBitmap(playlistIcon)

            holder.tvName.text = playlistName
            holder.clMain.setOnClickListener { onGenrePlaylistClickListener?.onGenrePlaylistClicked(genreID) }
        }

        override fun getItemCount(): Int {return genresList.size}

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

            var clMain: ConstraintLayout = itemView.findViewById(R.id.clMain_RVPlaylists)
            var tvName: TextView = itemView.findViewById(R.id.playlistName_RVPlaylists)
            var sivPlaylistArt: ShapeableImageView = itemView.findViewById(R.id.playlistArt_RVPlaylists)
        }
    }


    class AdapterUserPlaylists(private var userPlaylists: ArrayList<Playlist>): RecyclerView.Adapter<AdapterUserPlaylists.ViewHolder>(){

        var onUserPlaylistClickListener: OnUserPlaylistClickListener?= null
        interface OnUserPlaylistClickListener{ fun onUserPlaylistClicked(id: Int ) }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_playlists, parent, false))
        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val context = holder.itemView.context
            val playlist = userPlaylists[holder.absoluteAdapterPosition]
            val playListID = playlist.id
            val playlistImagePath = playlist.imagePath
            val playlistName = playlist.name


            if( playlistImagePath == null ){

                val playlistIcon = ContextCompat.getDrawable(context, R.drawable.icon_playlists)!!.toBitmap()
                holder.ivArt.setColorFilter(ColorFunctions.getThemeColor(context, 5))
                holder.ivArt.setPadding(80)
                holder.ivArt.setImageBitmap(playlistIcon)
            }
            else{

                try{

                    holder.ivArt.setImageURI(Uri.parse(playlistImagePath))
                }
                catch(exc: Exception){

                    val playlistIcon = ContextCompat.getDrawable(context, R.drawable.icon_playlists)!!.toBitmap()
                    holder.ivArt.setColorFilter(ColorFunctions.getThemeColor(context, 5))
                    holder.ivArt.setPadding(80)
                    holder.ivArt.setImageBitmap(playlistIcon)

                    playlist.imagePath = null
                    context.getSharedPreferences("playlists", MODE_PRIVATE).edit().putString("playlists", Gson().toJson(userPlaylists)).apply()
                }
            }


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