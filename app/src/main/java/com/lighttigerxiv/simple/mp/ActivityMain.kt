package com.lighttigerxiv.simple.mp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sothree.slidinguppanel.SlidingUpPanelLayout


class ActivityMain : AppCompatActivity() {

    //UI and Context
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var slidingLayout: SlidingUpPanelLayout
    private var frameLayout = R.id.frameLayout_ActivityMain
    private lateinit var fragmentManager: FragmentManager

    private var menuHome: Int? = null
    private var menuArtists: Int? = null
    private var menuAlbums: Int? = null
    private var menuPlaylists: Int? = null


    //Other
    private var fragmentHome = FragmentHome()
    private var fragmentArtists = FragmentArtists()
    private var fragmentAlbums = FragmentAlbums()
    private var fragmentPlaylists = FragmentPlaylists()



    private var selectedFragment = 0
    private var homeWasOpened = false
    private var artistsWasOpened = false
    private var albumsWasOpened = false
    private var playListsWasOpened = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try{

            initializeVariables()


            createNotificationChannel()



            checkPermissions()


            if( savedInstanceState == null ){

                selectedFragment = 0
                fragmentManager.beginTransaction().add( frameLayout, fragmentHome, "home" ).commit()
                homeWasOpened = true





            }


            handleFragmentChanges()
        }
        catch (exc: Exception) { println("Exception-> $exc") }
    }


    private fun initializeVariables(){


        frameLayout = R.id.frameLayout_ActivityMain
        bottomNav = findViewById(R.id.bottomNavigationView_ActivityMain)
        slidingLayout = findViewById(R.id.slidingLayout_ActivityMain)
        fragmentManager = supportFragmentManager

        menuHome = R.id.menuHome
        menuArtists = R.id.menuArtists
        menuAlbums = R.id.menuAlbums
        menuPlaylists = R.id.menuPlaylists
    }



    private fun handleFragmentChanges(){

        bottomNav.setOnItemSelectedListener { item ->

            if( selectedFragment == 0 )
                fragmentManager.beginTransaction().hide( fragmentHome ).commit()

            if( selectedFragment == 1 )
                fragmentManager.beginTransaction().hide( fragmentArtists ).commit()

            if( selectedFragment == 2 )
                fragmentManager.beginTransaction().hide( fragmentAlbums ).commit()

            if( selectedFragment == 3 )
                fragmentManager.beginTransaction().hide( fragmentPlaylists ).commit()


            when( item.itemId ){

                menuHome->{

                    selectedFragment = 0

                    if( !homeWasOpened ){

                        fragmentManager.beginTransaction().add( frameLayout, fragmentHome, "home" ).commit()
                        homeWasOpened = true
                    }

                    else
                        fragmentManager.beginTransaction().show( fragmentHome ).commit()
                }

                menuArtists->{

                    selectedFragment = 1

                    if( !artistsWasOpened ){

                        fragmentManager.beginTransaction().add( frameLayout, fragmentArtists, "artists" ).commit()
                        artistsWasOpened = true
                    }

                    else
                        fragmentManager.beginTransaction().show( fragmentArtists ).commit()
                }

                menuAlbums->{

                    selectedFragment = 2

                    if( !albumsWasOpened ){

                        fragmentManager.beginTransaction().add( frameLayout, fragmentAlbums, "albums" ).commit()
                        albumsWasOpened = true
                    }

                    else
                        fragmentManager.beginTransaction().show( fragmentAlbums ).commit()
                }

                menuPlaylists->{

                    selectedFragment = 3

                    if( !playListsWasOpened ){

                        fragmentManager.beginTransaction().add( frameLayout, fragmentPlaylists, "playlists" ).commit()
                        playListsWasOpened = true
                    }

                    else
                        fragmentManager.beginTransaction().show( fragmentPlaylists ).commit()
                }
            }
            true
        }
    }


    private fun checkPermissions(){


        val permission = ContextCompat.checkSelfPermission( applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE )


        if( permission != PackageManager.PERMISSION_GRANTED ){

            ActivityCompat.requestPermissions( this, arrayOf( Manifest.permission.READ_EXTERNAL_STORAGE ), 1 )
        }
    }


    private fun createNotificationChannel(){

        val channelName = getString(R.string.notificationChannelName)
        val channelDescription = getString(R.string.notificationChannelDescription)
        val channelImportance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel( "Playback", channelName, channelImportance )

        mChannel.description  = channelDescription

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

}