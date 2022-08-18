package com.lighttigerxiv.simple.mp.others

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.Song

class GetSongs {

    companion object{

        @SuppressLint("Range")
        fun getSongsList(context: Context, sort : Boolean ): ArrayList<Song>{

            try{

                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                val songsList = ArrayList<Song>()


                if( cursor != null && cursor.count > 0){
                    if( cursor.moveToNext() ){
                        do{

                            val id = cursor.getLong(cursor.getColumnIndex( MediaStore.Audio.Media._ID) )
                            val songPath = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Media.DATA) )
                            val title = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Media.TITLE ) )
                            val albumName = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM))
                            val albumID = cursor.getLong( cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID) )
                            val duration = cursor.getInt( cursor.getColumnIndex(MediaStore.Audio.Media.DURATION) )
                            val artistName = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST) )
                            val artistID = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID) )


                            val genreID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.GENRE_ID))

                            else
                                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Genres._ID))


                            var genre = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.GENRE))

                            else
                                null


                            if( genre == null ) genre = context.getString(R.string.Undefined)
                            val year = cursor.getInt( cursor.getColumnIndex(MediaStore.Audio.Media.YEAR) )



                            val song = Song( id, songPath, title, albumName , albumID , duration, artistName, artistID, year,  genreID, genre )

                            val filterDuration = PreferenceManager.getDefaultSharedPreferences(context).getInt("setting_filterAudio", 60) * 1000
                            if( duration > filterDuration ) songsList.add( song )
                        }
                        while (cursor.moveToNext())
                    }
                    cursor.close()
                }

                if( sort ){

                    when ( context.getSharedPreferences( "Settings", Context.MODE_PRIVATE).getString( "sort", "Recent" ) ) {

                        "Recent"-> songsList.reverse()
                        "Ascendent" -> songsList.sortBy { it.title }
                        "Descendent" -> songsList.sortByDescending { it.title }
                    }
                }


                return songsList
            }
            catch (exc: Exception){println("Exception-> $exc")}

            return ArrayList()
        }

        @Suppress("DEPRECATION")
        fun getSongAlbumArt( context: Context, songID: Long, albumID: Long ): Bitmap{

            lateinit var albumArt: Bitmap

            try{

                albumArt = if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

                    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    val songUri = ContentUris.withAppendedId(uri, songID)

                    context.contentResolver.loadThumbnail( songUri, Size(500,500), null )
                } else{

                    val sArtWorkUri = Uri.parse( "content://media/external/audio/albumart" )
                    val albumArtUri = ContentUris.withAppendedId(sArtWorkUri, albumID)

                    MediaStore.Images.Media.getBitmap( context.contentResolver, albumArtUri )
                }

            } catch (ignore: Exception){


                val defaultAlbumArt = BitmapFactory.decodeResource( context.resources, R.drawable.icon_music_record).copy(Bitmap.Config.ARGB_8888, true)
                val paint = Paint()
                val filter = PorterDuffColorFilter(ColorFunctions.getThemeColor(context, 5), PorterDuff.Mode.SRC_IN)
                paint.colorFilter = filter
                val canvas = Canvas(defaultAlbumArt)
                canvas.drawBitmap(defaultAlbumArt, 0F, 0F, paint)

                albumArt = defaultAlbumArt
            }

            return albumArt
        }
    }
}