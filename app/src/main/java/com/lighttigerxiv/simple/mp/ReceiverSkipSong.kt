package com.lighttigerxiv.simple.mp

import android.content.*
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.IBinder
import androidx.core.content.ContextCompat.startActivity
import com.lighttigerxiv.simple.mp.SimpleMPService.Companion.startService

class ReceiverSkipSong: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {


        val notificationActionsServiceIntent = Intent( context, NotificationActionService::class.java )

        notificationActionsServiceIntent.putExtra( "action", "skip" )

        context.startService( notificationActionsServiceIntent )
    }



}