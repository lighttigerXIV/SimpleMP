package com.lighttigerxiv.simple.mp

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat


class SimpleMPService: Service() {


    private val mBinder = LocalBinder()
    private lateinit var notification: Notification
    private lateinit var notificationManager: NotificationManager
    private var playList = ArrayList<Song>()
    private var shuffledPlaylist = ArrayList<Song>()
    private var currentSongPosition: Int = 0
    private var currentSongPath: String? = null
    private var loop = false
    private lateinit var audioManager: AudioManager


    //Listeners
    private var musicSelectedListener: OnMusicSelectedListener ?= null
    private var musicPausedListener: OnMusicPausedListener? = null
    private var musicResumedListener: OnMusicResumedListener? = null
    private var musicSecondPassedListener: OnSecondPassedListener? = null


    //Player States
    private var musicShuffled = false


    //Others
    private lateinit var mediaSession: MediaSession


    inner class LocalBinder : Binder() {
        fun getService(): SimpleMPService = this@SimpleMPService
    }


    companion object {

        private val mediaPlayer = MediaPlayer()

        fun startService(context: Context) {
            val startIntent = Intent(context, SimpleMPService::class.java)
            context.startForegroundService(startIntent)

        }
    }


    override fun onBind(intent: Intent?): IBinder {

        mediaSession = MediaSession(this, "SessionTag")

        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }




    fun setPlaylist( pPlaylist: ArrayList<Song> ){

        playList = pPlaylist
    }


    fun setInitialSongPosition( pCurrentSongPosition: Int){

        currentSongPosition = pCurrentSongPosition
    }


    fun isPlaylistShuffled(): Boolean{ return musicShuffled }


    fun toggleShuffle(){

        if( !musicShuffled ){

            musicShuffled = true

            shuffledPlaylist = ArrayList()
            val tempShuffledPlaylist = ArrayList<Song>()


            for( song in playList ) {

                if (song.path != currentSongPath)
                    tempShuffledPlaylist.add(song)

                else
                    shuffledPlaylist.add( song )
            }


            tempShuffledPlaylist.shuffle()

            for( song in tempShuffledPlaylist )
                shuffledPlaylist.add( song )



            currentSongPosition = 1
        }
        else{

            musicShuffled = false


            for( i in playList.indices ){

                if( playList[i].path == currentSongPath ){

                    currentSongPosition = i
                    break
                }
            }
        }
    }


    fun getCurrentPlaylist(): ArrayList<Song>{

        return if( !musicShuffled ) playList
        else shuffledPlaylist
    }


    fun isMusicPlaying(): Boolean{

        return mediaPlayer.isPlaying
    }

   ///////////////////////////////////////////    Focus    /////////////////////////////////////////////


    private val audioFocusChangeListener = OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {



            }
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT->{


            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {

                if( mediaPlayer.isPlaying )
                    pauseMusic(this )
            }
            AudioManager.AUDIOFOCUS_LOSS -> {

                if( mediaPlayer.isPlaying )
                    pauseMusic(this )
            }
        }
    }



    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
        setAudioAttributes(AudioAttributes.Builder().run {
            setUsage(AudioAttributes.USAGE_MEDIA)
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            build()
        })
        setAcceptsDelayedFocusGain(true)
        setOnAudioFocusChangeListener(audioFocusChangeListener)
        build()
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////


    fun playSong(context: Context){

        val songPath: String
        val songTitle: String
        val songArtist: String
        val songAlbumArt: Bitmap?


        if( !musicShuffled ) {

            songPath = playList[currentSongPosition].path
            songTitle = playList[currentSongPosition].title
            songArtist = playList[currentSongPosition].artist
            songAlbumArt = playList[currentSongPosition].albumArt
        }
        else{

            songPath = shuffledPlaylist[currentSongPosition].path
            songTitle = shuffledPlaylist[currentSongPosition].title
            songArtist = shuffledPlaylist[currentSongPosition].artist
            songAlbumArt = shuffledPlaylist[currentSongPosition].albumArt
        }

        currentSongPath = songPath


        mediaPlayer.reset()
        mediaPlayer.setDataSource(songPath)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {

            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager


            requestPlayWithFocus()


            //Open App
            val openAppIntent = Intent( context, ActivityMain::class.java )
            val pendingOpenAppIntent = TaskStackBuilder.create( context ).run{

                addNextIntentWithParentStack(openAppIntent)
                getPendingIntent( 0, PendingIntent.FLAG_IMMUTABLE )
            }

            //Previous Music
            val previousSongIntent = Intent(context, ReceiverPreviousSong::class.java )
            val pendingPreviousSongIntent = PendingIntent.getBroadcast( context, 1, previousSongIntent, PendingIntent.FLAG_IMMUTABLE )


            //Pauses/Plays music
            val playPauseIntent = Intent(context, ReceiverPlayPause::class.java )
            val pendingPlayPauseIntent = PendingIntent.getBroadcast( context, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE )


            //Skips to next music
            val skipSongIntent = Intent(context, ReceiverSkipSong::class.java )
            val pendingSkipSongIntent = PendingIntent.getBroadcast( context, 1, skipSongIntent, PendingIntent.FLAG_IMMUTABLE )


            val mediaSession = MediaSessionCompat(context, "mediaSession")

            val notificationBuilder = NotificationCompat.Builder(context, "Playback")

            notification = notificationBuilder
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(songTitle)
                .setContentIntent( pendingOpenAppIntent )
                .setContentText(songArtist)
                .setLargeIcon( songAlbumArt )
                .addAction( R.drawable.icon_previous_notification, "Previous Music", pendingPreviousSongIntent )
                .addAction( R.drawable.icon_pause, "Play Pause Music", pendingPlayPauseIntent )
                .addAction( R.drawable.icon_next_notification, "Next Music", pendingSkipSongIntent )
                .setStyle( androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
                )
                .setPriority( NotificationCompat.PRIORITY_LOW )
                .build()


            startForeground( 2, notification )
            notificationManager.notify( 2, notification )

        }



        handleMediaButtonsClick( context )


        handleSongFinished( context )

        if( musicSelectedListener != null ){

            if( !musicShuffled )
                musicSelectedListener?.onMusicSelected( playList, currentSongPosition )

            else
                musicSelectedListener?.onMusicSelected( shuffledPlaylist, currentSongPosition )
        }


        val mainHandler = Handler(Looper.getMainLooper())

        mainHandler.post( object : Runnable{
            override fun run() {

                if( musicSecondPassedListener != null )
                    musicSecondPassedListener?.onSecondPassed( mediaPlayer.currentPosition )
                    mainHandler.postDelayed( this,1000)
            }
        })
    }


    private fun handleMediaButtonsClick( context: Context ){

        mediaSession.setCallback( object : MediaSession.Callback() {

            override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {

                val ke = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

                if( ke?.action == KeyEvent.ACTION_DOWN ){

                    if( ke.keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS )
                        previousSong( context )

                    if( ke.keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE )
                        pauseResumeMusic( context )

                    if( ke.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY )
                        pauseResumeMusic( context )

                    if( ke.keyCode == KeyEvent.KEYCODE_MEDIA_NEXT )
                        skipSong( context )


                    println( "Media Button Action-> $ke" )
                }


                return super.onMediaButtonEvent(mediaButtonIntent)
            }
        })

        mediaSession.isActive = true
    }

    fun seekTo( position: Int){

        val newSongPosition = position * 1000

        mediaPlayer.seekTo(newSongPosition)

        if( !mediaPlayer.isPlaying )
            mediaPlayer.start()


    }


    private fun handleSongFinished(context: Context) {

        mediaPlayer.setOnCompletionListener{

            //If loop mode is activated
            if( loop ){

                playSong( context )
            }

            //Is it's the last song
            else if( (currentSongPosition + 1) == playList.size ){

                mediaPlayer.stop()
            }
            else{

                currentSongPosition++

                playSong( context )
            }
        }
    }


    fun toggleLoop(){

        loop = !loop
    }


    fun isLooping(): Boolean{ return loop }


    fun skipSong(context: Context){

        if( (currentSongPosition + 1) < playList.size ){

            currentSongPosition ++
            playSong( context )
        }
    }


    fun previousSong(context: Context){

        if( (currentSongPosition - 1) >= 0 ){

            currentSongPosition--
            playSong( context )
        }

    }


    private fun pauseMusic( context: Context ){


        val playPauseIcon = R.drawable.icon_play
        mediaPlayer.pause()


        if( musicPausedListener != null)
            musicPausedListener?.onMusicPaused()



        //Updates the notification
        val playPauseIntent = Intent(context, ReceiverPlayPause::class.java )
        playPauseIntent.putExtra( "action", "playPause" )
        val pendingPlayPauseIntent = PendingIntent.getBroadcast( context, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE )


        notification.actions[1] = Notification.Action( playPauseIcon, "Play Music", pendingPlayPauseIntent )


        startForeground( 2, notification )
        notificationManager.notify( 2, notification )


    }


    fun getCurrentPosition(): Int {return currentSongPosition}

    @Suppress("DEPRECATION")
    fun pauseResumeMusic(context: Context ){

        val playPauseIcon: Int

        if( mediaPlayer.isPlaying ) {

            playPauseIcon = R.drawable.icon_play
            mediaPlayer.pause()

            if( musicPausedListener != null)
                musicPausedListener?.onMusicPaused()

        }
        else {

            playPauseIcon = R.drawable.icon_pause


            if( musicResumedListener != null )
                musicResumedListener?.onMusicResumed()

            requestPlayWithFocus()
        }


        //Updates the notification
        val playPauseIntent = Intent(context, ReceiverPlayPause::class.java )
        playPauseIntent.putExtra( "action", "playPause" )
        val pendingPlayPauseIntent = PendingIntent.getBroadcast( context, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE )


        notification.actions[1] = Notification.Action( playPauseIcon, "Play Music", pendingPlayPauseIntent )



        startForeground( 2, notification )
        notificationManager.notify( 2, notification )
    }


    private fun requestPlayWithFocus(){

        val focusLock = Any()
        val res = audioManager.requestAudioFocus(focusRequest)


        synchronized(focusLock) {
            when (res) {
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {

                    mediaPlayer.start()
                    musicResumedListener?.onMusicResumed()

                    true
                }
                else -> false
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////

    interface OnMusicSelectedListener{ fun onMusicSelected( playList: ArrayList<Song>, position: Int ) }


    interface OnMusicPausedListener{ fun onMusicPaused() }


    interface OnMusicResumedListener{ fun onMusicResumed() }


    interface OnSecondPassedListener{ fun onSecondPassed(position: Int ) }



    fun setOnMusicSelectedListener( listener: OnMusicSelectedListener ){ musicSelectedListener = listener }


    fun setOnMusicPausedListener( listener: OnMusicPausedListener ){ musicPausedListener = listener }


    fun setOnMusicResumedListener( listener: OnMusicResumedListener ){ musicResumedListener = listener }


    fun setMusicSecondPassedListener( listener: OnSecondPassedListener ){ musicSecondPassedListener = listener }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////

}