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
    private var fragmentAlbums = FragmentAlbums()
    private var fragmentAlbum = FragmentAlbum()
    private var fragmentPlaylists = FragmentPlaylists()



    private var selectedFragment = 0
    private var homeWasOpened = false
    private var artistsWasOpened = false
    private var albumsWasOpened = false
    private var albumIsOpen = false
    private var playListsWasOpened = false

    private lateinit var smpService: SimpleMPService
    var serviceBounded = false
    private var restorePlayer = false
    private var musicWasSelected = false


    /////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try{

            assignVariables()
            createNotificationChannel()
            checkPermissions()
            handleFragmentChanges()

            if( savedInstanceState == null ){

                if( permissionsGranted ) loadFragmentHome()

            }
            else{

                homeWasOpened = savedInstanceState.getBoolean( "homeWasOpened" )
                musicWasSelected = savedInstanceState.getBoolean( "musicWasSelected" )


                fragmentHome = if( homeWasOpened )
                    fragmentManager.findFragmentByTag("home") as FragmentHome else
                    FragmentHome()

                if( musicWasSelected )
                    restorePlayer = true

            }
        }
        catch (exc: Exception) { println("Exception-> $exc") }
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


    private fun loadFragmentHome(){

        slidingPanel.panelHeight = 0


        val serviceIntent = Intent( applicationContext, SimpleMPService::class.java )
        applicationContext.bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )

        selectedFragment = 0
        fragmentManager.beginTransaction().add( frameLayout, fragmentHome, "home" ).commit()
        homeWasOpened = true

        ivClosePlayerSlidePlayer.setOnClickListener{

            slidingPanel.panelState = PanelState.COLLAPSED
        }
    }


    private fun handleSliding(){

        slidingPanel.addPanelSlideListener( object : PanelSlideListener{
            override fun onPanelSlide(panel: View, slideOffset: Float) {}

            override fun onPanelStateChanged(
                panel: View,
                previousState: PanelState,
                newState: PanelState,
            ) {

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


        smpService.setOnMusicSelectedListener( object: SimpleMPService.OnMusicSelectedListener{
            override fun onMusicSelected(playList: ArrayList<Song>, position: Int) {

                if( selectedFragment == 0 ) fragmentHome.updateCurrentSong()
                if( albumIsOpen ) fragmentAlbum.updateCurrentSong()


                musicWasSelected = true
                slidingPanel.panelHeight = 165

                val songAlbumArt = playList[position].albumArt
                val songTitle = playList[position].title
                val songArtist = playList[position].artist
                val songDuration = playList[position].duration

                setSlidingPanelData( songAlbumArt, songTitle, songArtist, songDuration )


                val iconPause = ContextCompat.getDrawable( applicationContext, R.drawable.icon_pause )
                val iconPauseRound = ContextCompat.getDrawable( applicationContext, R.drawable.icon_pause_round )

                ivPlayPauseMiniPlayer.setImageDrawable( iconPause )
                ivPlayPauseSlidePlayer.setImageDrawable( iconPauseRound )
            }
        } )
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


        ivPlayPauseMiniPlayer.visibility = View.VISIBLE
    }


    private fun handleSeekBar(){

        smpService.setMusicSecondPassedListener(object : SimpleMPService.OnSecondPassedListener{
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
        })


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




    //Pauses/Plays the music when button is clicked
    private fun handlePauseResumeMusic(){

        ivPlayPauseMiniPlayer.setOnClickListener{ smpService.pauseResumeMusic( applicationContext ) }
    }


    private fun handleFragmentChanges(){

        bottomNav.setOnItemSelectedListener { item ->

            if( selectedFragment == 0 )
                fragmentManager.beginTransaction().hide( fragmentHome ).commit()

            if( selectedFragment == 1 )
                fragmentManager.beginTransaction().hide( fragmentArtists ).commit()

            if( selectedFragment == 2 ) {

                if( albumIsOpen )
                    fragmentManager.beginTransaction().hide( fragmentAlbum ).commit()
                else
                    fragmentManager.beginTransaction().hide(fragmentAlbums).commit()
            }

            if( selectedFragment == 3 )
                fragmentManager.beginTransaction().hide( fragmentPlaylists ).commit()


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

                    else
                        fragmentManager.beginTransaction().show( fragmentArtists ).commit()
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

                    if( !playListsWasOpened ){

                        fragmentManager.beginTransaction().add( frameLayout, fragmentPlaylists, "playlists" ).commit()
                        playListsWasOpened = true
                    }

                    else
                        fragmentManager.beginTransaction().show( fragmentPlaylists ).commit()
                }
            }
            true
        }
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

        smpService.setOnMusicPausedListener( object: SimpleMPService.OnMusicPausedListener{

            override fun onMusicPaused() {

                val iconPlay = ContextCompat.getDrawable( applicationContext, R.drawable.icon_play )
                val iconPlayRound = ContextCompat.getDrawable( applicationContext, R.drawable.icon_play_round )

                ivPlayPauseMiniPlayer.setImageDrawable( iconPlay )
                ivPlayPauseSlidePlayer.setImageDrawable( iconPlayRound )
            }
        } )
    }


    private fun handleMusicResumed(){

        smpService.setOnMusicResumedListener( object: SimpleMPService.OnMusicResumedListener{

            override fun onMusicResumed() {

                val iconPause = ContextCompat.getDrawable( applicationContext, R.drawable.icon_pause )
                val iconPauseRound = ContextCompat.getDrawable( applicationContext, R.drawable.icon_pause_round )

                ivPlayPauseMiniPlayer.setImageDrawable( iconPause )
                ivPlayPauseSlidePlayer.setImageDrawable( iconPauseRound )
            }
        } )
    }


    private fun handleShuffleMusic(){

        ivShuffleSlidePlayer.setOnClickListener {

            if( !smpService.isPlaylistShuffled() ){

                ivShuffleSlidePlayer.setColorFilter( ContextCompat.getColor( applicationContext, R.color.mainPurple ) )
            }
            else{

                ivShuffleSlidePlayer.setColorFilter( ContextCompat.getColor( applicationContext, R.color.icon ) )
            }

            smpService.toggleShuffle()
        }
    }


    private fun handleSlidePlayerListeners(){

        ivPreviousSongSlidePlayer.setOnClickListener{ smpService.previousSong( applicationContext ) }

        ivPlayPauseSlidePlayer.setOnClickListener{ smpService.pauseResumeMusic( applicationContext ) }

        ivNextSongSlidePlayer.setOnClickListener { smpService.skipSong( applicationContext ) }
    }


    private fun handleLoopMusic(){

        ivLoopSongSlidePlayer.setOnClickListener {

            smpService.toggleLoop()

            if( smpService.isLooping() )
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
            handleSliding()
            handleSlidePlayerListeners()
            handleShuffleMusic()
            handleLoopMusic()


            if( restorePlayer or smpService.isMusicPlayingOrPaused())
                restorePlayer()

        }

        override fun onServiceDisconnected(name: ComponentName?) {

            serviceBounded = false
        }
    }


    private fun restorePlayer(){

        val playList = smpService.getCurrentPlaylist()
        val position: Int = smpService.getCurrentPosition()

        val albumArt = playList[position].albumArt
        val title = playList[position].title
        val artist = playList[position].artist
        val duration = playList[position].duration


        slidingPanel.panelHeight = 165

        if( homeWasOpened ) fragmentHome.updateCurrentSong()
        if( albumIsOpen ) fragmentAlbum.updateCurrentSong()


        setSlidingPanelData( albumArt, title, artist, duration )

        if( smpService.isPlaylistShuffled() ){

            ivShuffleSlidePlayer.setColorFilter( ContextCompat.getColor( applicationContext, R.color.mainPurple ) )
        }

        if( smpService.isLooping() ){

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

                fragmentAlbum.setOnBackPressed(object : FragmentAlbum.OnBackPressed{
                    override fun onBackPressed() {

                        fragmentManager.beginTransaction().remove( fragmentAlbum ).commit()
                        fragmentManager.beginTransaction().show( fragmentAlbums ).commit()

                        albumIsOpen = false
                    }
                })
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()

        if( serviceBounded )
            applicationContext.unbindService(connection)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)


        outState.putBoolean( "homeWasOpened", homeWasOpened )
        outState.putBoolean( "musicWasSelected", musicWasSelected )
    }


    override fun onBackPressed() {


        if( slidingPanel.panelState == PanelState.EXPANDED )
            slidingPanel.panelState = PanelState.COLLAPSED

        else
            super.onBackPressed()
    }


    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray, ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if( grantResults[0] == PackageManager.PERMISSION_GRANTED )
            permissionsGranted = true; loadFragmentHome()

    }
}