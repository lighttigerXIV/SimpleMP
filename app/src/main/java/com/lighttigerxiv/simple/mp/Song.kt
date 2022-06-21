package com.lighttigerxiv.simple.mp

import android.graphics.Bitmap
import android.net.Uri

class Song(){

    var id: Long = 0
    var path: String = ""
    var uri: Uri? = null
    var title: String = ""
    var albumArt: Bitmap? = null
    var duration: Int = 0
    var artist: String = ""
    var selected: Boolean = false
}