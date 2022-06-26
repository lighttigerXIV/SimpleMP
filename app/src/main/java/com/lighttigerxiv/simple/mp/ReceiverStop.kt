package com.lighttigerxiv.simple.mp

import android.content.*
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.media.session.MediaButtonReceiver
import com.lighttigerxiv.simple.mp.SimpleMPService.Companion.startService

class ReceiverStop: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {

        val notificationActionsServiceIntent = Intent( context, NotificationActionService::class.java )

        notificationActionsServiceIntent.putExtra( "action", "stop" )

        context.startService( notificationActionsServiceIntent )
    }



}