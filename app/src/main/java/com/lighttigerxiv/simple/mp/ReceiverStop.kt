package com.lighttigerxiv.simple.mp

import android.content.*

class ReceiverStop: BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent?) {

        val notificationActionsServiceIntent = Intent( context, NotificationActionsService::class.java )

        notificationActionsServiceIntent.putExtra( "action", "stop" )

        context.startService( notificationActionsServiceIntent )
    }



}