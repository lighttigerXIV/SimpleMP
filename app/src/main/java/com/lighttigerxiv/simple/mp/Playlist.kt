package com.lighttigerxiv.simple.mp

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class Playlist(

    val id: Int,
    val name: String,
    val image: String,
    val playlist: ArrayList<Song>
)
