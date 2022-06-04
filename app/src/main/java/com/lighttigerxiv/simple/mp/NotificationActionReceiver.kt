package com.lighttigerxiv.simple.mp

import android.content.*
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.IBinder
import androidx.core.content.ContextCompat.startActivity
import com.lighttigerxiv.simple.mp.SimpleMPService.Companion.startService

class NotificationActionReceiver: BroadcastReceiver() {





    override fun onReceive(context: Context, intent: Intent?) {

        val action = intent?.getStringExtra("action")

        val notificationActionsServiceIntent = Intent( context, NotificationActionService::class.java )

        if( action == "playPause" ){

            notificationActionsServiceIntent.putExtra( "action", action )
        }

        context.startService( notificationActionsServiceIntent )
    }



}