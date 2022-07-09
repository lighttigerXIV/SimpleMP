package com.lighttigerxiv.simple.mp

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size

class GetSongs {

    companion object{


        @SuppressLint("Range")
        fun getSongsList(context: Context, sort : Boolean ): ArrayList<Song>{

            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val songsList = ArrayList<Song>()


            if( cursor != null ){
                if( cursor.moveToNext() ){
                    do{

                        val id = cursor.getLong(cursor.getColumnIndex( MediaStore.Audio.Media._ID) )
                        val songPath = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Media.DATA) )
                        val songUri = ContentUris.withAppendedId( uri, id )
                        val title = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Media.TITLE ) )
                        val albumName = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM))
                        val albumID = cursor.getLong( cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID) )
                        val duration = cursor.getInt( cursor.getColumnIndex(MediaStore.Audio.Media.DURATION) )
                        val artistName = cursor.getString( cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST) )
                        val artistID = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID) )
                        val year = cursor.getInt( cursor.getColumnIndex(MediaStore.Audio.Media.YEAR) )



                        val song = Song( id, songPath, songUri, title, albumName , albumID , null, "", duration, artistName, artistID, year )

                        if( duration > 60000 )
                            songsList.add( song )
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

        @Suppress("DEPRECATION")
        fun getSongAlbumArt( context: Context, songUri: Uri, albumID: Long ): Bitmap{

            val albumArt: Bitmap = try{

                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

                    context.contentResolver.loadThumbnail( songUri, Size(500,500), null )
                } else{

                    val sArtWorkUri = Uri.parse( "content://media/external/audio/albumart" )
                    val albumArtUri = ContentUris.withAppendedId(sArtWorkUri, albumID)
                    MediaStore.Images.Media.getBitmap( context.contentResolver, albumArtUri )
                }

            } catch (ignore: Exception){

                BitmapFactory.decodeResource( context.resources, R.drawable.icon_music_record )
            }

            return albumArt
        }
    }
}