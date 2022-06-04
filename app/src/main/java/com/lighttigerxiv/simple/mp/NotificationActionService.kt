package com.lighttigerxiv.simple.mp

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class NotificationActionService: Service() {

    var serviceBounded = false
    private lateinit var action: String

    private lateinit var smpService: SimpleMPService

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        action = intent?.getStringExtra("action").toString()

        val serviceIntent = Intent( this, SimpleMPService::class.java )
        bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )

        return super.onStartCommand(intent, flags, startId)
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val binder = service as SimpleMPService.LocalBinder
            smpService = binder.getService()

            if( action == "playPause" )
                smpService.pauseResumeMusic( applicationContext )


            unbindService( this )
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            serviceBounded = false
        }
    }

}