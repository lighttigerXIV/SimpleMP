package com.lighttigerxiv.simple.mp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MusicIntentReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        println("Entrou no receiver")
    }
}