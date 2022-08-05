package com.lighttigerxiv.simple.mp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.sothree.slidinguppanel.PanelSlideListener
import com.sothree.slidinguppanel.PanelState
import com.sothree.slidinguppanel.SlidingUpPanelLayout


class ActivityMain : AppCompatActivity(){

    //UI and Context
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var slidingPanel: SlidingUpPanelLayout
    private var frameLayout = R.id.frameLayout_ActivityMain
    private lateinit var fragmentManager: FragmentManager
    private lateinit var clSlideContent: ConstraintLayout

    //Mini Player
    private lateinit var ivSongAlbumArtMiniPlayer: ShapeableImageView
    private lateinit var tvSongNameMiniPlayer: TextView
    private lateinit var tvSongArtistMiniPlayer: TextView
    private lateinit var ivPlayPauseMiniPlayer: ImageView
    private lateinit var clMiniPlayer: ConstraintLayout

    //Slide Player
    private lateinit var ivAlbumArtSlidePlayer: ShapeableImageView
    private lateinit var ivClosePlayerSlidePlayer: ImageView
    private lateinit var tvSongTitleSlidePlayer: TextView
    private lateinit var tvSongArtistSlidePlayer: TextView
    private lateinit var seekbarSongSlidePlayer: SeekBar
    private lateinit var tvCurrentSongSeconds: TextView
    private lateinit var tvSongSeconds: TextView
    private lateinit var ivShuffleSlidePlayer: ImageView
    private lateinit var ivPlayPauseSlidePlayer: ImageView
    private lateinit var ivNextSongSlidePlayer: ImageView
    private lateinit var ivPreviousSongSlidePlayer: ImageView
    private lateinit var ivLoopSongSlidePlayer: ImageView


    //Other
    private var permissionsGranted = false
    private var menuHome: Int? = null
    private var menuArtists: Int? = null
    private var menuAlbums: Int? = null
    private var menuPlaylists: Int? = null


    private var fragmentHome = FragmentHome()
    private var fragmentArtists = FragmentArtists()
    private var fragmentArtist = FragmentArtist()
    private var fragmentArtistAlbum = FragmentAlbum()
    private var fragmentAlbums = FragmentAlbums()
    private var fragmentAlbum = FragmentAlbum()
    private var fragmentPlaylists = FragmentPlaylists()
    private var fragmentGenrePlaylist = FragmentGenrePlaylist()
    private var fragmentUserPlaylist = FragmentUserPlaylist()


    private var selectedFragment = 0
    private var homeWasOpened = false
    private var artistsWasOpened = false
    private var artistIsOpen = false
    private var artistAlbumIsOpen = false
    private var albumsWasOpened = false
    private var albumIsOpen = false
    private var playlistsWasOpened = false
    private var genrePlaylistIsOpen = false
    private var userPlaylistIsOpen = false


    private lateinit var smpService: SimpleMPService
    var serviceBounded = false
    private var musicWasSelected = false


    /////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        assignVariables()
        createNotificationChannel()
        checkPermissions()



        //Service Bounding
        val serviceIntent = Intent( applicationContext, SimpleMPService::class.java )
        applicationContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )


        if( savedInstanceState == null ){

            if( permissionsGranted ) loadFragmentHome()

        }
        else restoreLifecycle( savedInstanceState )


        handleFragmentChanges()
        handleSlidePlayerListeners()
        handleArtistOpened()
        handleArtistBackPressed()
        handleArtistAlbumOpened()
        handleArtistAlbumBackPressed()
        handleAlbumOpened()
        handleAlbumBackPressed()
        handleGenrePlaylistOpened()
        handleGenrePlaylistBackPressed()
        handleUserPlaylistOpened()
        handleUserPlaylistBackPressed()
        handleUserPlaylistDeleted()
    }


    private fun assignVariables(){


        frameLayout = R.id.frameLayout_ActivityMain
        bottomNav = findViewById(R.id.bottomNavigationView_ActivityMain)
        slidingPanel = findViewById(R.id.slidingLayout_ActivityMain)
        fragmentManager = supportFragmentManager
        clMiniPlayer = findViewById(R.id.clMiniPlayer)
        ivSongAlbumArtMiniPlayer = findViewById(R.id.ivAlmbumArt_MiniPanel)
        tvSongNameMiniPlayer = findViewById(R.id.songName_MiniPanel)
        tvSongArtistMiniPlayer = findViewById(R.id.songArtist_MiniPanel)
        ivPlayPauseMiniPlayer = findViewById(R.id.ivPlayPause_MiniPanel)
        clSlideContent = findViewById(R.id.clSlideContent_ActivityMain)

        ivClosePlayerSlidePlayer = findViewById(R.id.ivClosePlayer_SlidePlayer)
        ivAlbumArtSlidePlayer = findViewById(R.id.ivAlbumArt_SlidePlayer)
        tvSongTitleSlidePlayer = findViewById(R.id.title_SlidePlayer)
        tvSongArtistSlidePlayer = findViewById(R.id.artist_SlidePlayer)
        seekbarSongSlidePlayer = findViewById(R.id.songSeekBar_SlidePlayer)
        tvCurrentSongSeconds = findViewById(R.id.currentSongSeconds_SlidePlayer)
        tvSongSeconds = findViewById(R.id.songSeconds_SlidePlayer)
        ivPlayPauseSlidePlayer = findViewById(R.id.ivPlayPause_SlidePlayer)
        ivShuffleSlidePlayer = findViewById(R.id.ivShuffle_SlidePlayer)
        ivPreviousSongSlidePlayer = findViewById(R.id.ivPreviousSong_SlidePlayer)
        ivNextSongSlidePlayer = findViewById(R.id.ivNextSong_SlidePlayer)
        ivLoopSongSlidePlayer = findViewById(R.id.ivLoop_SlidePlayer)

        menuHome = R.id.menuHome
        menuArtists = R.id.menuArtists
        menuAlbums = R.id.menuAlbums
        menuPlaylists = R.id.menuPlaylists
    }


    private fun restoreLifecycle( sis: Bundle ){

        selectedFragment = sis.getInt( "selectedFragment")
        homeWasOpened = sis.getBoolean( "homeWasOpened" )
        artistsWasOpened = sis.getBoolean( "artistsWasOpened" )
        artistIsOpen = sis.getBoolean("artistIsOpen")
        artistAlbumIsOpen = sis.getBoolean("artistAlbumIsOpen")
        albumsWasOpened = sis.getBoolean( "albumsWasOpened" )
        albumIsOpen = sis.getBoolean( "albumIsOpen" )
        playlistsWasOpened = sis.getBoolean( "playlistsWasOpened")
        genrePlaylistIsOpen = sis.getBoolean("genrePlaylistIsOpen")
        userPlaylistIsOpen = sis.getBoolean( "userPlaylistIsOpen" )
        musicWasSelected = sis.getBoolean( "musicWasSelected" )


        fragmentHome = if( homeWasOpened )
            fragmentManager.findFragmentByTag("home") as FragmentHome else FragmentHome()


        fragmentArtists = if( artistsWasOpened )
            fragmentManager.findFragmentByTag("artists") as FragmentArtists else FragmentArtists()


        fragmentArtist = if( artistIsOpen )
            fragmentManager.findFragmentByTag("artist") as FragmentArtist else FragmentArtist()


        fragmentArtistAlbum = if( artistAlbumIsOpen )
            fragmentManager.findFragmentByTag("artistAlbum") as FragmentAlbum else FragmentAlbum()


        fragmentAlbums = if( albumsWasOpened )
            fragmentManager.findFragmentByTag( "albums" ) as FragmentAlbums else FragmentAlbums()


        fragmentAlbum = if( albumIsOpen )
            fragmentManager.findFragmentByTag("album") as FragmentAlbum else FragmentAlbum()


        fragmentPlaylists = if( playlistsWasOpened )
            fragmentManager.findFragmentByTag("playlists") as FragmentPlaylists else FragmentPlaylists()


        fragmentGenrePlaylist = if( genrePlaylistIsOpen )
            fragmentManager.findFragmentByTag("genrePlaylist") as FragmentGenrePlaylist else FragmentGenrePlaylist()


        fragmentUserPlaylist = if( userPlaylistIsOpen )
            fragmentManager.findFragmentByTag("userPlaylist") as FragmentUserPlaylist else FragmentUserPlaylist()
    }


    private fun loadFragmentHome(){

        slidingPanel.panelHeight = 0

        selectedFragment = 0
        fragmentManager.beginTransaction().add( frameLayout, fragmentHome, "home" ).commit()
        homeWasOpened = true
    }


    private fun handlePanelSliding(){

        slidingPanel.addPanelSlideListener( object : PanelSlideListener{
            override fun onPanelSlide(panel: View, slideOffset: Float) {}

            override fun onPanelStateChanged( panel: View, previousState: PanelState, newState: PanelState ) {

                if( newState == PanelState.DRAGGING ){

                    clMiniPlayer.visibility = View.GONE

                }
                if( newState == PanelState.COLLAPSED ){

                    clMiniPlayer.visibility = View.VISIBLE
                    slidingPanel.isTouchEnabled = true

                }
                if( newState == PanelState.EXPANDED ){

                    slidingPanel.isTouchEnabled = false
                }
            }
        })
    }


    private fun handleSongSelected(){

        smpService.onMusicSelectedListener = object: SimpleMPService.OnMusicSelectedListener{
            override fun onMusicSelected(playList: ArrayList<Song>, position: Int) {

                if( homeWasOpened ) fragmentHome.updateCurrentSong()
                if( albumIsOpen ) fragmentAlbum.updateCurrentSong()
                if( artistIsOpen ) fragmentArtist.updateCurrentSong()
                if( artistAlbumIsOpen ) fragmentArtistAlbum.updateCurrentSong()
                if( genrePlaylistIsOpen ) fragmentGenrePlaylist.updateCurrentSong()
                if( userPlaylistIsOpen ) fragmentUserPlaylist.updateCurrentSong()


                musicWasSelected = true
                slidingPanel.panelHeight = 165

                val songID = playList[position].id
                val songAlbumID = playList[position].albumID
                val songAlbumArt = GetSongs.getSongAlbumArt( applicationContext, songID, songAlbumID )
                val songTitle = playList[position].title
                val songArtist = playList[position].artistName
                val songDuration = playList[position].duration

                setSlidingPanelData( songAlbumArt, songTitle, songArtist, songDuration )


                val iconPause = ContextCompat.getDrawable( applicationContext, R.drawable.icon_pause )
                val iconPauseRound = ContextCompat.getDrawable( applicationContext, R.drawable.icon_pause_round )

                ivPlayPauseMiniPlayer.setImageDrawable( iconPause )
                ivPlayPauseSlidePlayer.setImageDrawable( iconPauseRound )
            }
        }
    }


    private fun setSlidingPanelData(albumArt: Bitmap?, title: String?, artist: String?, duration: Int ){

        ivSongAlbumArtMiniPlayer.setImageBitmap( albumArt )
        tvSongNameMiniPlayer.text = title
        tvSongNameMiniPlayer.isSelected = true
        tvSongArtistMiniPlayer.text = artist
        tvSongArtistMiniPlayer.isSelected = true
        ivAlbumArtSlidePlayer.setImageBitmap( albumArt )
        tvSongTitleSlidePlayer.text = title
        tvSongArtistSlidePlayer.text = artist


        val seekBarSeconds = duration.rem(1000 * 60 *60).div(1000)
        val songMinutes = seekBarSeconds.div(60)
        val songSeconds = seekBarSeconds.rem(60)

        seekbarSongSlidePlayer.max = seekBarSeconds

        var stringSongSeconds = "$songSeconds"

        if( songSeconds < 10 )
            stringSongSeconds = "0$songSeconds"


        val durationInSeconds = "$songMinutes:$stringSongSeconds"


        val initialSeconds = "0:00"
        tvCurrentSongSeconds.text = initialSeconds
        tvSongSeconds.text = durationInSeconds


        slidingPanel.panelHeight = 165
    }


    private fun handleSeekBar(){

        smpService.onMusicSecondPassedListener = object : SimpleMPService.OnSecondPassedListener{
            override fun onSecondPassed(position: Int) {

                val seekBarNewPosition = position.div(1000)

                val currentPositionInSeconds = position.div(1000)
                val currentSongMinutes = (currentPositionInSeconds.div(60))%60
                val currentSongSeconds = currentPositionInSeconds%60

                var stringCurrentSongSeconds = "$currentSongSeconds"

                if( currentSongSeconds < 10 )
                    stringCurrentSongSeconds = "0$currentSongSeconds"


                val finalMinutesAndSeconds = "$currentSongMinutes:$stringCurrentSongSeconds"

                tvCurrentSongSeconds.text = finalMinutesAndSeconds

                seekbarSongSlidePlayer.progress = seekBarNewPosition

            }
        }


        seekbarSongSlidePlayer.setOnSeekBarChangeListener( object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if( fromUser ){

                    val currentSongMinutes = (progress.div(60))%60
                    val currentSongSeconds = progress%60

                    var stringCurrentSongSeconds = "$currentSongSeconds"

                    if( currentSongSeconds < 10 )
                        stringCurrentSongSeconds = "0$currentSongSeconds"

                    val finalMinutesAndSeconds = "$currentSongMinutes:$stringCurrentSongSeconds"

                    tvCurrentSongSeconds.text = finalMinutesAndSeconds
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {


                smpService.seekTo( seekBar.progress )

                ivPlayPauseSlidePlayer.setImageDrawable( ContextCompat.getDrawable(applicationContext, R.drawable.icon_pause_round) )
            }
        } )
    }


    private fun handlePauseResumeMusic(){

        ivPlayPauseMiniPlayer.setOnClickListener{ smpService.pauseResumeMusic( applicationContext ) }
    }


    private fun handleFragmentChanges(){

        bottomNav.setOnItemSelectedListener { item ->

            hideAllFragments()

            when( item.itemId ){

                menuHome->{

                    selectedFragment = 0

                    if( !homeWasOpened ){

                        fragmentManager.beginTransaction().add( frameLayout, fragmentHome, "home" ).commit()
                        homeWasOpened = true
                    }

                    else
                        fragmentManager.beginTransaction().show( fragmentHome ).commit()
                }

                menuArtists->{

                    selectedFragment = 1

                    if( !artistsWasOpened ){

                        fragmentManager.beginTransaction().add( frameLayout, fragmentArtists, "artists" ).commit()
                        artistsWasOpened = true
                    }

                    else{

                        if( artistIsOpen )
                            fragmentManager.beginTransaction().show( fragmentArtist ).commit()

                        else if( artistAlbumIsOpen )
                            fragmentManager.beginTransaction().show( fragmentArtistAlbum ).commit()

                        else
                            fragmentManager.beginTransaction().show( fragmentArtists ).commit()
                    }
                }

                menuAlbums->{

                    selectedFragment = 2

                    if( !albumsWasOpened ){

                        fragmentManager.beginTransaction().add( frameLayout, fragmentAlbums, "albums" ).commit()
                        albumsWasOpened = true
                    }

                    else{

                        if( albumIsOpen )
                            fragmentManager.beginTransaction().show( fragmentAlbum ).commit()

                        else
                            fragmentManager.beginTransaction().show( fragmentAlbums ).commit()
                    }


                    handleAlbumOpened()
                }

                menuPlaylists->{

                    selectedFragment = 3

                    if( !playlistsWasOpened ){

                        fragmentManager.beginTransaction().add( frameLayout, fragmentPlaylists, "playlists" ).commit()
                        playlistsWasOpened = true
                    }

                    else{

                        if( genrePlaylistIsOpen )
                            fragmentManager.beginTransaction().show(fragmentGenrePlaylist).commit()

                        else if( userPlaylistIsOpen )
                            fragmentManager.beginTransaction().show(fragmentUserPlaylist).commit()

                        else
                            fragmentManager.beginTransaction().show( fragmentPlaylists ).commit()
                    }

                }
            }
            true
        }
    }


    private fun hideAllFragments(){

        if( homeWasOpened ) fragmentManager.beginTransaction().hide( fragmentHome ).commit()
        if( artistsWasOpened ) fragmentManager.beginTransaction().hide( fragmentArtists ).commit()
        if( artistIsOpen ) fragmentManager.beginTransaction().hide( fragmentArtist ).commit()
        if( artistAlbumIsOpen ) fragmentManager.beginTransaction().hide( fragmentArtistAlbum ).commit()
        if( albumsWasOpened ) fragmentManager.beginTransaction().hide(fragmentAlbums).commit()
        if( albumIsOpen ) fragmentManager.beginTransaction().hide( fragmentAlbum ).commit()
        if( playlistsWasOpened ) fragmentManager.beginTransaction().hide( fragmentPlaylists ).commit()
        if( genrePlaylistIsOpen ) fragmentManager.beginTransaction().hide( fragmentGenrePlaylist ).commit()
        if( userPlaylistIsOpen ) fragmentManager.beginTransaction().hide( fragmentUserPlaylist ).commit()
    }


    private fun checkPermissions(){


        val permission = ContextCompat.checkSelfPermission( applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE )


        if( permission != PackageManager.PERMISSION_GRANTED ){

            ActivityCompat.requestPermissions( this, arrayOf( Manifest.permission.READ_EXTERNAL_STORAGE ), 1 )
        }
        else{

            permissionsGranted = true
        }
    }


    private fun handleMusicPaused(){

        smpService.onMusicPausedListener = object: SimpleMPService.OnMusicPausedListener{

            override fun onMusicPaused() {

                val iconPlay = ContextCompat.getDrawable( applicationContext, R.drawable.icon_play )
                val iconPlayRound = ContextCompat.getDrawable( applicationContext, R.drawable.icon_play_round )

                ivPlayPauseMiniPlayer.setImageDrawable( iconPlay )
                ivPlayPauseSlidePlayer.setImageDrawable( iconPlayRound )
            }
        }
    }


    private fun handleMusicResumed(){

        smpService.onMusicResumedListener = object: SimpleMPService.OnMusicResumedListener{

            override fun onMusicResumed() {

                val iconPause = ContextCompat.getDrawable( applicationContext, R.drawable.icon_pause )
                val iconPauseRound = ContextCompat.getDrawable( applicationContext, R.drawable.icon_pause_round )

                ivPlayPauseMiniPlayer.setImageDrawable( iconPause )
                ivPlayPauseSlidePlayer.setImageDrawable( iconPauseRound )
            }
        }
    }


    private fun handleMusicStopped(){

        smpService.onMediaPlayerStoppedListener = object : SimpleMPService.OnMediaPlayerStoppedListener{
            override fun onMediaPlayerStopped() {

                slidingPanel.panelState = PanelState.HIDDEN
                slidingPanel.panelHeight = 0


                if( homeWasOpened ) fragmentHome.resetRecyclerView()
                if( albumIsOpen ) fragmentAlbum.resetRecyclerView()
                if( artistIsOpen ) fragmentArtist.resetRecyclerView()
                if( artistAlbumIsOpen ) fragmentArtistAlbum.resetRecyclerView()
            }
        }
    }


    private fun handleMusicShuffled(){

        smpService.onMusicShuffleToggledListener = object : SimpleMPService.OnMusicShuffleToggledListener{
            override fun onMusicShuffleToggled(state: Boolean) {

                when ( state ){

                    true-> ivShuffleSlidePlayer.setColorFilter( ContextCompat.getColor( applicationContext, R.color.mainPurple ) )
                    false-> ivShuffleSlidePlayer.setColorFilter( ContextCompat.getColor( applicationContext, R.color.icon ) )
                }
            }
        }
    }


    private fun handleShuffleMusic(){

        ivShuffleSlidePlayer.setOnClickListener { smpService.toggleShuffle() }
    }


    private fun handleSlidePlayerListeners(){

        ivClosePlayerSlidePlayer.setOnClickListener{ slidingPanel.panelState = PanelState.COLLAPSED }

        ivPreviousSongSlidePlayer.setOnClickListener{ smpService.previousSong( applicationContext ) }

        ivPlayPauseSlidePlayer.setOnClickListener{ smpService.pauseResumeMusic( applicationContext ) }

        ivNextSongSlidePlayer.setOnClickListener { smpService.skipSong( applicationContext ) }
    }


    private fun handleLoopMusic(){

        ivLoopSongSlidePlayer.setOnClickListener {

            smpService.toggleLoop()

            if( smpService.onRepeatMode )
                ivLoopSongSlidePlayer.setColorFilter( ContextCompat.getColor( applicationContext, R.color.mainPurple ) )

            else
                ivLoopSongSlidePlayer.setColorFilter( ContextCompat.getColor( applicationContext, R.color.icon ) )

        }
    }


    private fun createNotificationChannel(){

        val channelName = getString(R.string.notificationChannelName)
        val channelDescription = getString(R.string.notificationChannelDescription)
        val channelImportance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel( "Playback", channelName, channelImportance )

        mChannel.description  = channelDescription

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }


    private fun restorePlayer(){

        val playList = smpService.getCurrentPlaylist()
        val position: Int = smpService.currentSongPosition


        if( position == -1 ){

            slidingPanel.panelHeight = 0
        }
        else{

            val songID = playList[position].id
            val albumID = playList[position].albumID
            val albumArt = GetSongs.getSongAlbumArt( applicationContext, songID, albumID )
            val title = playList[position].title
            val artist = playList[position].artistName
            val duration = playList[position].duration


            setSlidingPanelData( albumArt, title, artist, duration )

            if( smpService.musicShuffled ){

                ivShuffleSlidePlayer.setColorFilter( ContextCompat.getColor( applicationContext, R.color.mainPurple ) )
            }

            if( smpService.onRepeatMode ){

                ivLoopSongSlidePlayer.setColorFilter( ContextCompat.getColor( applicationContext, R.color.mainPurple ) )
            }

            if( slidingPanel.panelState == PanelState.EXPANDED ){

                clMiniPlayer.visibility = View.GONE
            }

            if( smpService.isMusicPlaying() ){

                val iconPause = ContextCompat.getDrawable( applicationContext, R.drawable.icon_pause )
                val iconPauseRound = ContextCompat.getDrawable( applicationContext, R.drawable.icon_pause_round )

                ivPlayPauseMiniPlayer.setImageDrawable( iconPause )
                ivPlayPauseSlidePlayer.setImageDrawable( iconPauseRound )
            }
            else{

                val iconPlay = ContextCompat.getDrawable( applicationContext, R.drawable.icon_play )
                val iconPlayRound = ContextCompat.getDrawable( applicationContext, R.drawable.icon_play_round )

                ivPlayPauseMiniPlayer.setImageDrawable( iconPlay )
                ivPlayPauseSlidePlayer.setImageDrawable( iconPlayRound )
            }
        }
    }


    private fun handleArtistOpened(){

        fragmentArtists.setOnArtistOpenedListener(object : FragmentArtists.OnArtistOpenedListener{
            override fun onArtistOpened(artistID: Long, artistName: String) {

                val bundle = Bundle()
                bundle.putString("artistName", artistName)
                bundle.putLong("artistID", artistID)

                fragmentArtist = FragmentArtist()
                fragmentArtist.arguments = bundle
                artistIsOpen = true

                fragmentManager.beginTransaction().hide(fragmentArtists).commit()
                fragmentManager.beginTransaction().add( frameLayout, fragmentArtist, "artist" ).commit()

                handleArtistBackPressed()
                handleArtistAlbumOpened()
                handleArtistAlbumBackPressed()
            }
        })
    }


    private fun handleArtistBackPressed(){

        fragmentArtist.onBackPressedListener = object : FragmentArtist.OnBackPressedListener{
            override fun onBackPressed() {

                fragmentManager.beginTransaction().remove( fragmentArtist ).commit()
                fragmentManager.beginTransaction().show( fragmentArtists ).commit()

                artistIsOpen = false
            }
        }
    }


    private fun handleArtistAlbumOpened(){

        fragmentArtist.onAlbumOpenedListener = object : FragmentArtist.OnAlbumOpenedListener{
            override fun onAlbumOpened(albumID: Long) {

                val bundle = Bundle()
                bundle.putLong( "albumID", albumID )
                fragmentArtistAlbum = FragmentAlbum()
                fragmentArtistAlbum.arguments = bundle
                artistAlbumIsOpen = true


                fragmentManager.beginTransaction().hide( fragmentArtist ).commit()
                fragmentManager.beginTransaction().add( frameLayout, fragmentArtistAlbum, "artistAlbum" ).commit()

                handleArtistAlbumBackPressed()
            }
        }
    }


    private fun handleArtistAlbumBackPressed(){

        fragmentArtistAlbum.setOnBackPressed(object : FragmentAlbum.OnBackPressed{
            override fun onBackPressed() {

                fragmentManager.beginTransaction().remove( fragmentArtistAlbum ).commit()
                fragmentManager.beginTransaction().show( fragmentArtist ).commit()

                artistAlbumIsOpen = false
            }
        })
    }


    private fun handleAlbumOpened(){

        fragmentAlbums.setOnAlbumOpenedListener( object : FragmentAlbums.OnAlbumOpenedListener{
            override fun onAlbumOpened(albumID: Long) {


                val bundle = Bundle()
                bundle.putLong( "albumID", albumID )
                fragmentAlbum = FragmentAlbum()
                fragmentAlbum.arguments = bundle
                albumIsOpen = true


                fragmentManager.beginTransaction().hide( fragmentAlbums ).commit()
                fragmentManager.beginTransaction().add( frameLayout, fragmentAlbum, "album" ).commit()

                handleAlbumBackPressed()
            }
        })
    }


    private fun handleAlbumBackPressed(){

        fragmentAlbum.setOnBackPressed(object : FragmentAlbum.OnBackPressed{
            override fun onBackPressed() {

                fragmentManager.beginTransaction().remove( fragmentAlbum ).commit()
                fragmentManager.beginTransaction().show( fragmentAlbums ).commit()

                albumIsOpen = false
            }
        })
    }


    private fun handleGenrePlaylistOpened() {

        fragmentPlaylists.onGenrePlaylistClickListener = object : FragmentPlaylists.OnGenrePlaylistClickListener{
            override fun onPlaylistClicked(genreID: Long) {

                val bundle = Bundle()
                bundle.putBoolean("isGenrePlaylist", true)
                bundle.putLong( "genreID", genreID )
                fragmentGenrePlaylist = FragmentGenrePlaylist()
                fragmentGenrePlaylist.arguments = bundle
                genrePlaylistIsOpen = true


                fragmentManager.beginTransaction().hide( fragmentPlaylists ).commit()
                fragmentManager.beginTransaction().add( frameLayout, fragmentGenrePlaylist, "genrePlaylist" ).commit()

                handleGenrePlaylistBackPressed()
            }
        }
    }


    private fun handleGenrePlaylistBackPressed(){

        fragmentGenrePlaylist.onBackPressedListener = object : FragmentGenrePlaylist.OnBackPressedListener{
            override fun onBackPressed() {

                fragmentManager.beginTransaction().remove( fragmentGenrePlaylist ).commit()
                fragmentManager.beginTransaction().show( fragmentPlaylists ).commit()

                genrePlaylistIsOpen = false
            }
        }
    }


    private fun handleUserPlaylistOpened() {

        fragmentPlaylists.onUserPlaylistClickListener = object : FragmentPlaylists.OnUserPlaylistClickListener{
            override fun onPlaylistClicked(id: Int) {

                val bundle = Bundle()
                bundle.putInt( "playlistID", id )
                fragmentUserPlaylist = FragmentUserPlaylist()
                fragmentUserPlaylist.arguments = bundle
                userPlaylistIsOpen = true


                fragmentManager.beginTransaction().hide( fragmentPlaylists ).commit()
                fragmentManager.beginTransaction().add( frameLayout, fragmentUserPlaylist, "userPlaylist" ).commit()

                handleUserPlaylistBackPressed()
                handleUserPlaylistDeleted()
            }
        }
    }


    private fun handleUserPlaylistBackPressed(){

        fragmentUserPlaylist.onBackPressedListener = object : FragmentUserPlaylist.OnBackPressedListener{
            override fun onBackPressed() {

                fragmentManager.beginTransaction().remove( fragmentUserPlaylist ).commit()
                fragmentManager.beginTransaction().show( fragmentPlaylists ).commit()

                userPlaylistIsOpen = false
            }
        }
    }


    private fun handleUserPlaylistDeleted(){

        fragmentUserPlaylist.onPlaylistDeletedListener = object : FragmentUserPlaylist.OnPlaylistDeletedListener{
            override fun onPlaylistDeleted() {

                fragmentManager.beginTransaction().remove( fragmentUserPlaylist ).commit()
                fragmentManager.beginTransaction().show( fragmentPlaylists ).commit()

                userPlaylistIsOpen = false

                fragmentPlaylists.resetUserPlaylists()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        if( serviceBounded )
            applicationContext.unbindService(connection)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)


        outState.putInt( "selectedFragment", selectedFragment )
        outState.putBoolean( "homeWasOpened", homeWasOpened )
        outState.putBoolean( "artistsWasOpened", artistsWasOpened )
        outState.putBoolean("artistIsOpen", artistIsOpen)
        outState.putBoolean("artistAlbumIsOpen", artistAlbumIsOpen)
        outState.putBoolean( "albumsWasOpened", albumsWasOpened )
        outState.putBoolean( "musicWasSelected", musicWasSelected )
        outState.putBoolean( "albumIsOpen", albumIsOpen )
        outState.putBoolean("playlistsWasOpened", playlistsWasOpened)
        outState.putBoolean("genrePlaylistIsOpen", genrePlaylistIsOpen)
        outState.putBoolean("userPlaylistIsOpen", userPlaylistIsOpen)
    }


    override fun onBackPressed() {

        if( slidingPanel.panelState == PanelState.EXPANDED )
            slidingPanel.panelState = PanelState.COLLAPSED

        else
            super.onBackPressed()
    }


    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if( grantResults[0] == PackageManager.PERMISSION_GRANTED )
            permissionsGranted = true; loadFragmentHome()

    }


    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val binder = service as SimpleMPService.LocalBinder
            smpService = binder.getService()
            serviceBounded = true


            handleSongSelected()
            handleSeekBar()
            handleMusicPaused()
            handleMusicResumed()
            handlePauseResumeMusic()
            handleMusicShuffled()
            handleMusicStopped()
            handlePanelSliding()
            handleShuffleMusic()
            handleLoopMusic()


            if( smpService.isMusicPlayingOrPaused() ) restorePlayer()

            else slidingPanel.panelHeight = 0
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            serviceBounded = false
        }
    }
}