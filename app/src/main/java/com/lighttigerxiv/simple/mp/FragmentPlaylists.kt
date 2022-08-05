package com.lighttigerxiv.simple.mp

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class FragmentPlaylists : Fragment() {

    //Main
    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    //Adapters
    private lateinit var adapterViewPager: AdapterViewPager

    //Others
    private var genrePlaylistsOpened = false
    private var userPlaylistsOpened = false


    var onGenrePlaylistClickListener: OnGenrePlaylistClickListener ?= null
    interface OnGenrePlaylistClickListener{fun onPlaylistClicked(genreID: Long) }

    var onUserPlaylistClickListener: OnUserPlaylistClickListener ?= null
    interface OnUserPlaylistClickListener{fun onPlaylistClicked(id: Int) }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        return inflater.inflate(R.layout.fragment_playlists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assignVariables(view)


        adapterViewPager = AdapterViewPager(childFragmentManager, lifecycle)
        viewPager.adapter = adapterViewPager


        if( savedInstanceState != null ){

            try {

                genrePlaylistsOpened = savedInstanceState.getBoolean("genrePlaylistsOpened", false)
                userPlaylistsOpened = savedInstanceState.getBoolean("userPlaylistsOpened", false)


                if( genrePlaylistsOpened ){

                    val fragmentGenrePlaylist = childFragmentManager.findFragmentByTag("f0") as FragmentPlaylistsRecyclerview

                    fragmentGenrePlaylist.onGenrePlaylistClickListener = object : FragmentPlaylistsRecyclerview.OnGenrePlaylistClickListener{
                        override fun onGenrePlaylistClicked(genreID: Long) { onGenrePlaylistClickListener?.onPlaylistClicked(genreID) }
                    }
                }

                if( userPlaylistsOpened ){

                    val fragmentUserPlaylists = childFragmentManager.findFragmentByTag("f1") as FragmentPlaylistsRecyclerview

                    fragmentUserPlaylists.onUserPlaylistClickListener = object :
                        FragmentPlaylistsRecyclerview.OnUserPlaylistClickListener {
                        override fun onUserPlaylistClicked(id: Int) { onUserPlaylistClickListener?.onPlaylistClicked(id) }
                    }
                }
            }
            catch (exc: Exception){}
        }


        TabLayoutMediator(tabLayout, viewPager){ tab, position->
            run{

                when (position) {

                    0->tab.text = getString(R.string.Genres)
                    1->tab.text = getString(R.string.YourPlaylists)
                }
            }
        }.attach()


        if( !genrePlaylistsOpened ){

            adapterViewPager.onGenresOpenedListener = object: AdapterViewPager.OnGenresOpenedListener{
                override fun onGenresPlaylistOpened() {

                    genrePlaylistsOpened = true
                }
            }
        }
        if( !userPlaylistsOpened ){

            adapterViewPager.onUserPlaylistsOpenedListener = object : AdapterViewPager.OnUserPlaylistsOpenedListener{
                override fun onUserPlaylistsOpened() {

                    userPlaylistsOpened = true
                }
            }
        }


        adapterViewPager.onGenrePlaylistClickListener = object : AdapterViewPager.OnGenrePlaylistClickListener{
            override fun onPlaylistClicked(genreID: Long) { onGenrePlaylistClickListener?.onPlaylistClicked(genreID) }
        }

        adapterViewPager.onUserPlaylistClickListener = object : AdapterViewPager.OnUserPlaylistClickListener{
            override fun onPlaylistClicked(id: Int) { onUserPlaylistClickListener?.onPlaylistClicked(id) }
        }
    }


    private fun assignVariables(view: View){

        //Main
        fragmentView = view
        fragmentContext = view.context
        tabLayout = view.findViewById(R.id.tabLayout_FragmentPlaylists)
        viewPager = view.findViewById(R.id.vp_FragmentPlaylists)
    }


    fun resetUserPlaylists() { adapterViewPager.resetUserPlaylists() }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("genrePlaylistsOpened", genrePlaylistsOpened)
        outState.putBoolean("userPlaylistsOpened", userPlaylistsOpened)
    }

    class AdapterViewPager(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle){

        var onGenresOpenedListener: OnGenresOpenedListener ?= null
        interface OnGenresOpenedListener{fun onGenresPlaylistOpened()}

        var onUserPlaylistsOpenedListener: OnUserPlaylistsOpenedListener ?= null
        interface OnUserPlaylistsOpenedListener{ fun onUserPlaylistsOpened() }

        var onGenrePlaylistClickListener: OnGenrePlaylistClickListener ?= null
        interface OnGenrePlaylistClickListener{fun onPlaylistClicked(genreID: Long) }

        var onUserPlaylistClickListener: OnUserPlaylistClickListener ?= null
        interface OnUserPlaylistClickListener{fun onPlaylistClicked(id: Int) }


        private interface OnResetUserPlaylists{fun onResetUserPlaylists()}
        private var onResetUserPlaylists: OnResetUserPlaylists ?= null


        fun resetUserPlaylists(){ onResetUserPlaylists?.onResetUserPlaylists() }


        override fun getItemCount(): Int {return 2}

        override fun createFragment(position: Int): Fragment {

            val fragment = FragmentPlaylistsRecyclerview()
            val bundle = Bundle()

            when(position){

                0->{

                    bundle.putString("page", "genres")
                    fragment.arguments = bundle

                    onGenresOpenedListener?.onGenresPlaylistOpened()

                    fragment.onGenrePlaylistClickListener = object : FragmentPlaylistsRecyclerview.OnGenrePlaylistClickListener{
                        override fun onGenrePlaylistClicked(genreID: Long) {

                            onGenrePlaylistClickListener?.onPlaylistClicked(genreID)
                        }
                    }
                }
                1->{

                    bundle.putString("page", "yourPlaylists")
                    fragment.arguments = bundle

                    onUserPlaylistsOpenedListener?.onUserPlaylistsOpened()

                    fragment.onUserPlaylistClickListener = object : FragmentPlaylistsRecyclerview.OnUserPlaylistClickListener{
                        override fun onUserPlaylistClicked(id: Int) {

                            onUserPlaylistClickListener?.onPlaylistClicked(id)
                        }
                    }

                    onResetUserPlaylists = object: AdapterViewPager.OnResetUserPlaylists{
                        override fun onResetUserPlaylists() { fragment.resetPlaylists() }
                    }
                }
            }

            return fragment
        }
    }
}