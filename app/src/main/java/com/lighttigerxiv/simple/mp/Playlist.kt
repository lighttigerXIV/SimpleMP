package com.lighttigerxiv.simple.mp

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class Playlist(

    val id: Int,
    var name: String,
    var imagePath: String?,
    var playlist: ArrayList<Song>
)
