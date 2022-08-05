package com.lighttigerxiv.simple.mp

import android.content.*

class ReceiverSkipSong: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {


        val notificationActionsServiceIntent = Intent( context, NotificationActionsService::class.java )

        notificationActionsServiceIntent.putExtra( "action", "skip" )

        context.startService( notificationActionsServiceIntent )
    }



}