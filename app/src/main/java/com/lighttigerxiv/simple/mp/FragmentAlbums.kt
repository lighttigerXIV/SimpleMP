package com.lighttigerxiv.simple.mp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class FragmentAlbums : Fragment() {


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {

        return inflater.inflate(R.layout.fragment_albums, container, false)
    }


}