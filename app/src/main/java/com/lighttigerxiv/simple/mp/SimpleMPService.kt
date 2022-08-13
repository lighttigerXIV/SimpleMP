package com.lighttigerxiv.simple.mp

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.*
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import com.lighttigerxiv.simple.mp.activities.ActivityMain
import com.lighttigerxiv.simple.mp.others.GetSongs
import com.lighttigerxiv.simple.mp.services.ReceiverPlayPause
import com.lighttigerxiv.simple.mp.services.ReceiverPreviousSong
import com.lighttigerxiv.simple.mp.services.ReceiverSkipSong
import com.lighttigerxiv.simple.mp.services.ReceiverStop


class SimpleMPService: Service() {


    private val mBinder = LocalBinder()
    private lateinit var notification: Notification
    private lateinit var notificationManager: NotificationManager
    var playList = ArrayList<Song>()
    private var shuffledPlaylist = ArrayList<Song>()
    var currentSongPosition: Int = 0
    private var currentSongPath: String = ""
    var onRepeatMode = false
    private lateinit var audioManager: AudioManager


    //Listeners
    var onMusicSelectedListener: OnMusicSelectedListener? = null
    var onMusicSelectedListenerToQueue: OnMusicSelectedListenerToQueue? = null //Since there is no way to have two listeners at same time it needs another listener to the queue list
    var onMusicPausedListener: OnMusicPausedListener? = null
    var onPlaylistAdded: OnPlaylistsAdded? = null
    var onMusicResumedListener: OnMusicResumedListener? = null
    var onMusicSecondPassedListener: OnSecondPassedListener? = null
    var onMusicShuffleToggledListener: OnMusicShuffleToggledListener? = null
    var onMediaPlayerStoppedListener: OnMediaPlayerStoppedListener? = null


    //Player States
    private var serviceStarted = false
    var musicShuffled = false
    private var musicStarted = false


    //Others
    private lateinit var mediaButtonReceiver: ComponentName
    private lateinit var mediaSession: MediaSessionCompat


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

        val context = this
        mediaButtonReceiver = ComponentName(context, ReceiverPlayPause::class.java)
        mediaSession = MediaSessionCompat(context, "SessionTag")
        mediaSession.setCallback(object : MediaSessionCompat.Callback(){

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
                }

                return super.onMediaButtonEvent(mediaButtonIntent)
            }
        })

        return mBinder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }


    override fun onCreate() {
        super.onCreate()

        notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    fun getCurrentPlaylist(): ArrayList<Song>{

        return if(!musicShuffled) playList else shuffledPlaylist
    }



    fun isMusicPlayingOrPaused(): Boolean{ return musicStarted }


    fun toggleShuffle(){

        if( !musicShuffled ){

            musicShuffled = true

            shuffledPlaylist = ArrayList()
            val tempShuffledPlaylist = ArrayList<Song>()


            //Adds the current song to first position
            playList.forEach { song ->

                if (song.path != currentSongPath)
                    tempShuffledPlaylist.add(song)

                else
                    shuffledPlaylist.add( song )
            }

            //Shuffles the temp playlist and adds it to the one with just the current song
            tempShuffledPlaylist.shuffle()

            for( song in tempShuffledPlaylist )
                shuffledPlaylist.add( song )


            currentSongPosition = 0
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

        onMusicShuffleToggledListener?.onMusicShuffleToggled(musicShuffled)
    }


    fun enableShuffle(){

        musicShuffled = true

        shuffledPlaylist = ArrayList(playList)
        shuffledPlaylist.shuffle()

        onMusicShuffleToggledListener?.onMusicShuffleToggled(true)


        currentSongPosition = 0
    }


    fun setPlaylist( newPlaylist: ArrayList<Song> ){ playList = newPlaylist }


    fun playSongAndEnableShuffle(context: Context, position: Int){


        val selectedSong = playList[position]
        shuffledPlaylist = ArrayList(playList)


        shuffledPlaylist.shuffle()
        shuffledPlaylist.removeIf{ it.path == selectedSong.path }
        shuffledPlaylist.add(0, selectedSong )

        currentSongPosition = 0
        playSong(context)


        musicShuffled = true
    }


    fun isMusicPlaying(): Boolean{

        return mediaPlayer.isPlaying
    }


    fun getCurrentSongPath(): String{ return currentSongPath }


    private val audioFocusChangeListener = OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {}

            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT->{}

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


    fun playSong(context: Context){

        serviceStarted = true
        musicStarted = true

        val songPath: String
        val songTitle: String
        val songArtist: String
        val songID: Long
        val songAlbumID: Long
        val songAlbumArt: Bitmap
        val songDuration: Int


        println("Playlist Aleatória -> $shuffledPlaylist")


        if( !musicShuffled ) {

            songPath = playList[currentSongPosition].path
            songTitle = playList[currentSongPosition].title
            songArtist = playList[currentSongPosition].artistName
            songID = playList[currentSongPosition].id
            songAlbumID = playList[currentSongPosition].albumID
            songAlbumArt = GetSongs.getSongAlbumArt(context, songID, songAlbumID)
            songDuration = playList[currentSongPosition].duration
        }
        else{

            songPath = shuffledPlaylist[currentSongPosition].path
            songTitle = shuffledPlaylist[currentSongPosition].title
            songArtist = shuffledPlaylist[currentSongPosition].artistName
            songID = shuffledPlaylist[currentSongPosition].id
            songAlbumID = shuffledPlaylist[currentSongPosition].albumID
            songAlbumArt = GetSongs.getSongAlbumArt(context, songID, songAlbumID)
            songDuration = shuffledPlaylist[currentSongPosition].duration
        }

        currentSongPath = songPath


        mediaPlayer.reset()
        mediaPlayer.setDataSource(songPath)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {

            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager


            requestPlayWithFocus()
            mediaSession.isActive = true

            //Open App
            val openAppIntent = Intent( context, ActivityMain::class.java )
            val pendingOpenAppIntent = TaskStackBuilder.create( context ).run{

                addNextIntentWithParentStack(openAppIntent)
                getPendingIntent( 0, PendingIntent.FLAG_IMMUTABLE )
            }

            //Stop Service
            val stopIntent = Intent(context, ReceiverStop::class.java )
            val pendingStopIntent = PendingIntent.getBroadcast( context, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE )


            //Previous Music
            val previousSongIntent = Intent(context, ReceiverPreviousSong::class.java )
            val pendingPreviousSongIntent = PendingIntent.getBroadcast( context, 1, previousSongIntent, PendingIntent.FLAG_IMMUTABLE )


            //Pauses/Plays music
            val playPauseIntent = Intent(context, ReceiverPlayPause::class.java )
            val pendingPlayPauseIntent = PendingIntent.getBroadcast( context, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE )


            //Skips to next music
            val skipSongIntent = Intent(context, ReceiverSkipSong::class.java )
            val pendingSkipSongIntent = PendingIntent.getBroadcast( context, 1, skipSongIntent, PendingIntent.FLAG_IMMUTABLE )



            notification = NotificationCompat.Builder(context, "Playback")
                .setContentIntent( pendingOpenAppIntent )
                .setStyle( androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(1, 2, 3)
                )
                .setSmallIcon(R.drawable.icon)
                .addAction( R.drawable.icon_stop_notification, "Stop Player", pendingStopIntent )
                .addAction( R.drawable.icon_previous_notification, "Previous Music", pendingPreviousSongIntent )
                .addAction( R.drawable.icon_pause_notification, "Play Pause Music", pendingPlayPauseIntent )
                .addAction( R.drawable.icon_next_notification, "Next Music", pendingSkipSongIntent )
                .build()


            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()

                    .putString(MediaMetadata.METADATA_KEY_TITLE, songTitle)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, songArtist)
                    .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, songAlbumArt)
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, songDuration.toLong())
                    .build()
            )


            startForeground( 2, notification )
            notificationManager.notify( 2, notification )
        }


        handleSongFinished( context )


        if( !musicShuffled ) {
            onMusicSelectedListener?.onMusicSelected(playList, currentSongPosition)
            onMusicSelectedListenerToQueue?.onMusicSelected(playList, currentSongPosition)
        }

        else {
            onMusicSelectedListener?.onMusicSelected(shuffledPlaylist, currentSongPosition)
            onMusicSelectedListenerToQueue?.onMusicSelected(shuffledPlaylist, currentSongPosition)
        }




        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post( object : Runnable{
            override fun run() {

                if( onMusicSecondPassedListener != null )
                    onMusicSecondPassedListener?.onSecondPassed( mediaPlayer.currentPosition )
                    mainHandler.postDelayed( this,1000)
            }
        })
    }


    fun seekTo( position: Int){

        val newSongPosition = position * 1000

        mediaPlayer.seekTo(newSongPosition)

        if( !mediaPlayer.isPlaying ) mediaPlayer.start()
    }


    private fun handleSongFinished(context: Context) {

        mediaPlayer.setOnCompletionListener{

            //If loop mode is activated
            if( onRepeatMode ){

                playSong( context )
            }

            //Is it's the last song
            else if( (currentSongPosition + 1) == playList.size ){

                stopMediaPlayer()
            }
            else{

                currentSongPosition++

                playSong( context )
            }
        }
    }


    fun toggleLoop(){

        onRepeatMode = !onRepeatMode
    }



    fun stopMediaPlayer(){

        onMediaPlayerStoppedListener?.onMediaPlayerStopped()
        mediaPlayer.stop()
        currentSongPosition = -1
        currentSongPath = ""
        stopForeground(true)
        stopSelf()
    }


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


    @Suppress("DEPRECATION")
    private fun pauseMusic(context: Context ){


        val playPauseIcon = R.drawable.icon_play_notification
        mediaPlayer.pause()
        mediaSession.isActive = false


        if( onMusicPausedListener != null)
            onMusicPausedListener?.onMusicPaused()



        //Updates the notification
        val playPauseIntent = Intent(context, ReceiverPlayPause::class.java )
        playPauseIntent.putExtra( "action", "playPause" )
        val pendingPlayPauseIntent = PendingIntent.getBroadcast( context, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE )


        notification.actions[2] = Notification.Action( playPauseIcon, "Play Music", pendingPlayPauseIntent )


        startForeground( 2, notification )
        notificationManager.notify( 2, notification )
    }


    @Suppress("DEPRECATION")
    fun pauseResumeMusic(context: Context ){

        val playPauseIcon: Int

        if( mediaPlayer.isPlaying ) {

            playPauseIcon = R.drawable.icon_play_notification


            mediaPlayer.pause()

            if( onMusicPausedListener != null) onMusicPausedListener?.onMusicPaused()
        }
        else {

            playPauseIcon = R.drawable.icon_pause_notification


            if( onMusicResumedListener != null ) onMusicResumedListener?.onMusicResumed()

            requestPlayWithFocus()
        }


        //Updates the notification
        val playPauseIntent = Intent(context, ReceiverPlayPause::class.java )
        playPauseIntent.putExtra( "action", "playPause" )
        val pendingPlayPauseIntent = PendingIntent.getBroadcast( context, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE )


        notification.actions[2] = Notification.Action( playPauseIcon, "Play Music", pendingPlayPauseIntent )



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
                    onMusicResumedListener?.onMusicResumed()

                    true
                }
                else -> false
            }
        }
    }


    fun updatePlaylists(){ onPlaylistAdded?.OnPlaylistAdded() }

    //////////////////////////////////////////////////////////////////////////////////////////////////////

    interface OnMusicSelectedListener{ fun onMusicSelected( playList: ArrayList<Song>, position: Int ) }


    interface OnMusicSelectedListenerToQueue{ fun onMusicSelected( playList: ArrayList<Song>, position: Int ) }


    interface OnPlaylistsAdded{ fun OnPlaylistAdded() }


    interface OnMusicPausedListener{ fun onMusicPaused() }


    interface OnMusicResumedListener{ fun onMusicResumed() }


    interface OnSecondPassedListener{ fun onSecondPassed(position: Int ) }


    interface OnMusicShuffleToggledListener{ fun onMusicShuffleToggled(state: Boolean) }


    interface OnMediaPlayerStoppedListener{ fun onMediaPlayerStopped() }
}