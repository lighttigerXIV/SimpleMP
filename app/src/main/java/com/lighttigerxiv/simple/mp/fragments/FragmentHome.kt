package com.lighttigerxiv.simple.mp.fragments

import android.annotation.SuppressLint
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lighttigerxiv.simple.mp.*
import com.lighttigerxiv.simple.mp.activities.ActivitySettings
import com.lighttigerxiv.simple.mp.adapters.AdapterRVSongs
import com.lighttigerxiv.simple.mp.others.GetSongs
import com.lighttigerxiv.simple.mp.others.ColorFunctions


class FragmentHome : Fragment() {

    //Main
    private lateinit var fragmentContext: Context
    private lateinit var fragmentView: View
    private lateinit var clMain: ConstraintLayout
    private lateinit var etSearch: EditText
    private lateinit var ibClearSearch: ImageButton
    private lateinit var ivMenu: ImageView
    private lateinit var rvSongs: RecyclerView
    private lateinit var ivCricket: ImageView
    private lateinit var tvThisIsFeelingEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabShuffle: FloatingActionButton
    private lateinit var songsList: ArrayList<Song>



    //Others
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false
    private lateinit var adapterRVSongs: AdapterRVSongs


    //Lifecycle
    private var songsLoaded = false



    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun updateCurrentSong(){

        adapterRVSongs.currentSongPath = smpService.getCurrentSongPath()
        adapterRVSongs.notifyItemRangeChanged( 0, adapterRVSongs.getPlayListSize() )
    }


    fun resetRecyclerView(){

        adapterRVSongs.currentSongPath = ""
        adapterRVSongs.notifyItemRangeChanged( 0, adapterRVSongs.getPlayListSize() )
    }


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        view.context.setTheme(ColorFunctions.getTheme(view.context))

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try{
            assignVariables( view )

            setupThemes()

            if(savedInstanceState != null) restoreLifecycle(savedInstanceState)


            val serviceIntent = Intent( fragmentContext, SimpleMPService::class.java )
            fragmentContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )


            if(!songsLoaded) loadSongs()


            handleMenu()
            handleFabShuffle()
            handleMusicClicked()
            handleCricketClick()
        }
        catch ( exc: Exception ){}
    }


    override fun onResume() {
        super.onResume()

        handleSearch()

        if(etSearch.text.toString().trim().isNotEmpty())
            ibClearSearch.visibility = View.VISIBLE
    }

    private fun assignVariables(view: View ){

        fragmentView = view
        fragmentContext = fragmentView.context
        clMain = view.findViewById(R.id.clMain_FragmentHome)
        etSearch = view.findViewById(R.id.etSearch_FragmentHome)
        ibClearSearch = view.findViewById(R.id.ibClearSearch_FragmentHome)
        ivMenu = view.findViewById(R.id.ivMenu_FragmentHome)
        rvSongs = view.findViewById(R.id.rvSongs_FragmentHome)
        ivCricket = view.findViewById(R.id.ivCricket_FragmentHome)
        tvThisIsFeelingEmpty = view.findViewById(R.id.tvThisIsFeelingEmpty_FragmentHome)
        progressBar = view.findViewById(R.id.progressBar_FragmentHome)
        fabShuffle = view.findViewById(R.id.fabShuffle_FragmentHome)


        rvSongs.layoutManager = LinearLayoutManager(fragmentContext)
        rvSongs.addItemDecoration(RecyclerViewSpacer(10))
        rvSongs.itemAnimator?.changeDuration = 0
    }


    private fun restoreLifecycle(sis: Bundle){

        songsLoaded = sis.getBoolean("songsLoaded", false)

        if(songsLoaded){

            val adapterSongsListJson = sis.getString("adapterSongsList", "")
            val songsListJson = sis.getString("songsList", "")
            val jsonType = object : TypeToken<ArrayList<Song>>(){}.type
            val adapterSongsList = Gson().fromJson<ArrayList<Song>>(adapterSongsListJson, jsonType)

            songsList = Gson().fromJson(songsListJson, jsonType)

            adapterRVSongs = AdapterRVSongs(adapterSongsList, parentFragmentManager, showViewAlbum = true, showViewArtist = true)
            rvSongs.adapter = adapterRVSongs

            if(songsList.size == 0){
                ivCricket.visibility = View.VISIBLE
                tvThisIsFeelingEmpty.visibility = View.VISIBLE
                fabShuffle.visibility = View.GONE
            }

            handleMusicClicked()
        }
    }


    private fun setupThemes(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
        etSearch.background = ColorFunctions.getEditTextBackground(fragmentContext)
    }


    private fun loadSongs(){

        songsList = GetSongs.getSongsList(fragmentContext, true)

        adapterRVSongs = AdapterRVSongs(songsList, parentFragmentManager, showViewAlbum = true, showViewArtist = true)
        rvSongs.adapter = adapterRVSongs

        if(songsList.size == 0){
            ivCricket.visibility = View.VISIBLE
            tvThisIsFeelingEmpty.visibility = View.VISIBLE
            fabShuffle.visibility = View.GONE
        }

        songsLoaded = true
    }


    private fun handleCricketClick(){

        ivCricket.setOnClickListener {

            val mp = MediaPlayer.create(fragmentContext, R.raw.cricket_sound)
            mp.setVolume(0.06F, 0.06F)
            mp.start()
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun handleSearch(){

        etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val filteredSongsList = ArrayList(songsList)
                val searchText = s.toString().trim()


                //Shows/hides the clear search button
                if(searchText.isNotEmpty())
                    ibClearSearch.visibility = View.VISIBLE

                else
                    ibClearSearch.visibility = View.GONE


                filteredSongsList.removeIf { !it.title.trim().lowercase().contains(searchText) and !it.artistName.trim().lowercase().contains( searchText ) }


                adapterRVSongs.songsList = filteredSongsList
                adapterRVSongs.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        ibClearSearch.setOnClickListener {

            if( songsLoaded ){

                etSearch.setText("")
                ibClearSearch.visibility = View.GONE
            }
        }
    }


    private fun handleMenu(){

        val popupView = fragmentView.findViewById<View>(R.id.ivMenu_FragmentHome)
        val wrapper = ContextThemeWrapper(fragmentContext, ColorFunctions.getPopupMenuTheme(fragmentContext))
        val popupMenu = PopupMenu( wrapper, popupView )
        popupMenu.menuInflater.inflate(R.menu.menu_more_fragment_home, popupMenu.menu )

        ivMenu.setOnClickListener {

            popupMenu.setOnMenuItemClickListener {

                when (it.itemId) {

                    R.id.menuSortByRecent -> setSortMode( "Recent" )

                    R.id.menuSortByOldest -> setSortMode( "Oldest" )

                    R.id.menuSortByAscendent -> setSortMode( "Ascendent" )

                    R.id.menuSortByDescendent -> setSortMode( "Descendent" )

                    R.id.menuSettings -> startActivity(Intent(fragmentContext, ActivitySettings::class.java))
                }
                true
            }

            popupMenu.show()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setSortMode(sortMode: String ){

        if(songsLoaded){

            fragmentContext.getSharedPreferences( "Settings", MODE_PRIVATE ).edit().putString( "sort", sortMode ).apply()
            songsList = GetSongs.getSongsList(fragmentContext, true)
            adapterRVSongs.songsList = songsList
            adapterRVSongs.notifyDataSetChanged()
        }
    }


    private fun handleFabShuffle(){

        fabShuffle.setOnClickListener {

            if( songsList.size > 0 && serviceBounded ){

                SimpleMPService.startService(fragmentContext)


                smpService.playList = songsList
                smpService.enableShuffle()
                smpService.playSong(fragmentContext)
            }
        }
    }


    private fun handleMusicClicked(){

        adapterRVSongs.onItemClickListener = object : AdapterRVSongs.OnItemClickListener{
            override fun onItemClick(position: Int) {

                if( serviceBounded ){

                    SimpleMPService.startService(fragmentContext)

                    smpService.playList = adapterRVSongs.songsList

                    if( smpService.musicShuffled ){

                        smpService.playSongAndEnableShuffle(fragmentContext, position)
                    }
                    else{

                        smpService.currentSongPosition = position
                        smpService.playSong(fragmentContext)
                    }
                }
            }
        }
    }


    private val connection = object : ServiceConnection{

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val binder = service as SimpleMPService.LocalBinder
            smpService = binder.getService()
            serviceBounded = true
            updateCurrentSong()
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            serviceBounded = false
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("songsLoaded", songsLoaded)

        if(songsLoaded){

            outState.putString("songsList", Gson().toJson(songsList))
            outState.putString("adapterSongsList", Gson().toJson(adapterRVSongs.songsList))
        }
    }
}