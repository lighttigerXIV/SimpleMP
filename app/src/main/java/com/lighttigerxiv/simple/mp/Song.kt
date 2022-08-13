package com.lighttigerxiv.simple.mp

import android.graphics.Bitmap
import android.net.Uri

data class Song(

    val id: Long,
    val path: String,
    val title: String,
    val albumName: String,
    val albumID: Long,
    val duration: Int,
    val artistName: String,
    val artistID: Long,
    val year: Int,
    val genreID: Long,
    val genre: String,
    var selected: Boolean = false
)