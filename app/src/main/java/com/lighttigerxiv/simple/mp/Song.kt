package com.lighttigerxiv.simple.mp

import android.graphics.Bitmap
import android.net.Uri

data class Song(

    val id: Long,
    val path: String,
    val uri: Uri?,
    val title: String,
    val albumArt: Bitmap?,
    val duration: Int,
    val artist: String,
    val year: Int,
    var selected: Boolean = false
)