package com.lighttigerxiv.simple.mp

import android.annotation.SuppressLint
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class FragmentHome : Fragment() {

    //UI
    private lateinit var fragmentContext: Context
    private lateinit var fragmentView: View
    private lateinit var etSearch: EditText
    private lateinit var ivMenu: ImageView
    private lateinit var rvSongs: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var songsList: ArrayList<Song>



    //Others
    private lateinit var smpService: SimpleMPService
    private var serviceBounded = false
    private lateinit var adapterSongsRV: AdapterSongsRV


    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun updateCurrentSong(){

        adapterSongsRV.setCurrentSongPath( smpService.getCurrentSongPath() )
        adapterSongsRV.notifyItemRangeChanged( 0, adapterSongsRV.getPlayListSize() )
    }


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try{
            assignVariables( view )


            val serviceIntent = Intent( fragmentContext, SimpleMPService::class.java )
            fragmentContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )



            songsList = GetSongs.getSongsList( fragmentContext )


            adapterSongsRV = AdapterSongsRV(songsList)
            rvSongs.adapter = adapterSongsRV


            handleSearch()
            handleMenu()
            handleMusicClicked()
        }
        catch ( exc: Exception ){}
    }


    private fun assignVariables( view: View ){

        fragmentView = view
        fragmentContext = fragmentView.context
        etSearch = fragmentView.findViewById(R.id.etSearch_FragmentHome)
        ivMenu = fragmentView.findViewById(R.id.ivMenu_FragmentHome)
        rvSongs = fragmentView.findViewById(R.id.rvSongs_FragmentHome)
        progressBar = fragmentView.findViewById(R.id.progressBar_FragmentHome)


        rvSongs.layoutManager = LinearLayoutManager(fragmentContext)
        rvSongs.itemAnimator?.changeDuration = 0
    }


    private fun handleSearch(){

        etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("NotifyDataSetChanged")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val tempSongsList = ArrayList<Song>()
                val searchText = s.toString().trim()


                for( song in songsList )
                    if( song.title.trim().lowercase().contains(searchText) or song.artist.trim().lowercase().contains( searchText ) )
                        tempSongsList.add( song )


                adapterSongsRV.setPlaylist( tempSongsList )
                adapterSongsRV.notifyDataSetChanged()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun handleMenu(){

        val popupView = fragmentView.findViewById<View>( R.id.ivMenu_FragmentHome )
        val popupMenu = PopupMenu( fragmentContext, popupView )
        popupMenu.menuInflater.inflate( R.menu.menu_more_fragment_home, popupMenu.menu )

        ivMenu.setOnClickListener {

            popupMenu.setOnMenuItemClickListener {

                when (it.itemId) {

                    R.id.menuDefault-> setSortMode( "Default" )

                    R.id.menuSortByDate-> setSortMode( "Date" )

                    R.id.menuSortAZ-> setSortMode( "AZ" )

                    R.id.menuSortZA-> setSortMode( "ZA" )

                    R.id.menuSortByArtist-> setSortMode( "Artist" )
                }
                true
            }

            popupMenu.show()
        }
    }


    private fun setSortMode( sortMode: String ){

        fragmentContext.getSharedPreferences( "Settings", MODE_PRIVATE ).edit().putString( "sort", sortMode ).apply()
        songsList = GetSongs.getSongsList(fragmentContext)
        adapterSongsRV.setPlaylist( songsList )
        adapterSongsRV.notifyDataSetChanged()
    }


    private fun handleMusicClicked(){

        adapterSongsRV.setOnItemClickListener( object : AdapterSongsRV.OnItemClickListener{
            override fun onItemClick(position: Int) {

                if( serviceBounded ){

                    SimpleMPService.startService(fragmentContext)

                    if( smpService.isPlaylistShuffled() )
                        smpService.toggleShuffle()


                    smpService.setPlaylist( adapterSongsRV.getPlaylist() )
                    smpService.setInitialSongPosition( position )
                    smpService.playSong( fragmentContext )
                }
            }
        })
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