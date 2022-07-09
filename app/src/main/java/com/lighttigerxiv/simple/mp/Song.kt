package com.lighttigerxiv.simple.mp

import android.graphics.Bitmap
import android.net.Uri

data class Song(

    val id: Long,
    val path: String,
    val uri: Uri,
    val title: String,
    val albumName: String,
    val albumID: Long,
    val albumArt: Bitmap? = null,
    val albumArtUri: String?= "",
    val duration: Int,
    val artistName: String,
    val artistID: Long,
    val year: Int,
    var selected: Boolean = false
)