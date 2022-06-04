package com.lighttigerxiv.simple.mp

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Base64.*
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.io.ByteArrayOutputStream


class SimpleMPService: Service() {


    private val mBinder = LocalBinder()
    private lateinit var notification: Notification


    inner class LocalBinder : Binder() {
        fun getService(): SimpleMPService = this@SimpleMPService
    }


    companion object {

        private val mediaPlayer = MediaPlayer()

        fun startService(context: Context) {
            val startIntent = Intent(context, SimpleMPService::class.java)
            context.startForegroundService(startIntent)

        }
        fun stopService(context: Context) {
            val stopIntent = Intent(context, SimpleMPService::class.java)
            context.stopService(stopIntent)
        }


        /*
        private fun handleMusicStopped(context: Context, playList: ArrayList<Song>, position: Int) {

            mediaPlayer.setOnCompletionListener{

                //Is it's the last song
                if( position == playList.size ){


                }
                else{

                    val newPosition = position + 1

                    playSong( context, playList, newPosition )
                }
            }
        }



         */
    }



    override fun onBind(intent: Intent?): IBinder {

        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }


    fun playSong(context: Context, playList: ArrayList<Song>, position: Int){

        val songPath = playList[position].path
        val songTitle = playList[position].title
        val songArtist = playList[position].artist
        val songAlbumArt = playList[position].albumArt

        val spTemp = context.getSharedPreferences( "temp", MODE_PRIVATE )
        val spTempEditor = spTemp.edit()


        val baos = ByteArrayOutputStream()
        songAlbumArt?.compress( Bitmap.CompressFormat.PNG, 100, baos )
        val b = baos.toByteArray()
        val encodedAlbumArt = encodeToString(b, DEFAULT)


        spTempEditor
            .putString( "songTitle", songTitle )
            .putString( "songArtist", songArtist )
            .putString( "encodedAlbumArt", encodedAlbumArt )
            .apply()



        mediaPlayer.reset()
        mediaPlayer.setDataSource(songPath)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {

            mediaPlayer.start()
        }


        val intentAction = Intent(context, NotificationActionReceiver::class.java )


        intentAction.putExtra( "action", "playPause" )

        val pendingIntentAction = PendingIntent.getBroadcast( context, 1, intentAction, PendingIntent.FLAG_IMMUTABLE )




        val mediaSession = MediaSessionCompat(context, "mediaSession")

        val notificationBuilder = NotificationCompat.Builder(context, "Playback")

        notification = notificationBuilder
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(songTitle)
            .setContentText(songArtist)
            .setLargeIcon( songAlbumArt )
            .addAction( R.drawable.icon_pause, "Pause Music", pendingIntentAction )
            .setStyle( androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0)
            )
            .setPriority( NotificationCompat.PRIORITY_LOW )
            .build()

        startForeground( 2, notification )
        NotificationManagerCompat.from( context ).notify( 2, notification )

        //handleMusicStopped( context, playList, position )
    }


    fun pauseResumeMusic( context: Context ){

        val spTemp = context.getSharedPreferences( "temp", MODE_PRIVATE )
        val songTitle = spTemp.getString( "songTitle", null )
        val songArtist = spTemp.getString( "songArtist", null )
        val encodedAlbumArt = spTemp.getString( "encodedAlbumArt", null)


        val albumBytes = decode( encodedAlbumArt, 0 )
        val songAlbumArt = BitmapFactory.decodeByteArray(albumBytes, 0, albumBytes.size)


        val playPauseIcon: Int

        if( mediaPlayer.isPlaying ) {

            playPauseIcon = R.drawable.icon_play
            mediaPlayer.pause()
        }
        else {

            playPauseIcon = R.drawable.icon_pause
            mediaPlayer.start()
        }


        val intentAction = Intent(context, NotificationActionReceiver::class.java )
        intentAction.putExtra( "action", "playPause" )

        val pendingIntentAction = PendingIntent.getBroadcast( context, 1, intentAction, PendingIntent.FLAG_IMMUTABLE )

        val mediaSession = MediaSessionCompat(context, "mediaSession")

        val notificationBuilder = NotificationCompat.Builder(context, "Playback")

        val notification = notificationBuilder
            .setSmallIcon(R.drawable.icon)
            .setContentTitle(songTitle)
            .setContentText(songArtist)
            .setLargeIcon( songAlbumArt )
            .addAction( playPauseIcon, "Play Music", pendingIntentAction )
            .setStyle( androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        startForeground( 2, notification )
        NotificationManagerCompat.from( context ).notify( 2, notification )
    }
}