package com.lighttigerxiv.simple.mp.fragments

import android.annotation.SuppressLint
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var ivMenu: ImageView
    private lateinit var rvSongs: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var fabShuffle: FloatingActionButton

    private lateinit var songsList: ArrayList<Song>



    //Others
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false
    private lateinit var adapterRVSongs: AdapterRVSongs


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

            setupColors()


            val serviceIntent = Intent( fragmentContext, SimpleMPService::class.java )
            fragmentContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )


            songsList = GetSongs.getSongsList(fragmentContext, true)

            adapterRVSongs = AdapterRVSongs(songsList)
            rvSongs.adapter = adapterRVSongs


            handleSearch()
            handleMenu()
            handleFabShuffle()
            handleMusicClicked()
        }
        catch ( exc: Exception ){}
    }


    private fun assignVariables( view: View ){

        fragmentView = view
        fragmentContext = fragmentView.context
        clMain = view.findViewById(R.id.clMain_FragmentHome)
        etSearch = fragmentView.findViewById(R.id.etSearch_FragmentHome)
        ivMenu = fragmentView.findViewById(R.id.ivMenu_FragmentHome)
        rvSongs = fragmentView.findViewById(R.id.rvSongs_FragmentHome)
        progressBar = fragmentView.findViewById(R.id.progressBar_FragmentHome)
        fabShuffle = view.findViewById(R.id.fabShuffle_FragmentHome)


        rvSongs.layoutManager = LinearLayoutManager(fragmentContext)
        rvSongs.addItemDecoration(RecyclerViewSpacer(10))
        rvSongs.itemAnimator?.changeDuration = 0
    }


    private fun setupColors(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
        etSearch.background = ColorFunctions.getEditTextBackground(fragmentContext)
    }


    private fun handleSearch(){

        etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val filteredSongsList = ArrayList(songsList)
                val searchText = s.toString().trim()

                filteredSongsList.removeIf { !it.title.trim().lowercase().contains(searchText) and !it.artistName.trim().lowercase().contains( searchText ) }


                adapterRVSongs.songsList = filteredSongsList
                adapterRVSongs.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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

        fragmentContext.getSharedPreferences( "Settings", MODE_PRIVATE ).edit().putString( "sort", sortMode ).apply()
        songsList = GetSongs.getSongsList(fragmentContext, true)
        adapterRVSongs.songsList = songsList
        adapterRVSongs.notifyDataSetChanged()
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
}