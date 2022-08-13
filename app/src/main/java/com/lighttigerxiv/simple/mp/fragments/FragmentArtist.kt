package com.lighttigerxiv.simple.mp.fragments

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.lighttigerxiv.simple.mp.others.CheckInternet
import com.lighttigerxiv.simple.mp.others.MakeRequest
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.others.ColorFunctions
import com.lighttigerxiv.simple.mp.others.ResponseAudioDB
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors


class FragmentArtist : Fragment() {

    //User interface
    private lateinit var fragmentView: View
    private lateinit var fragmentContext: Context
    private lateinit var clMain: ConstraintLayout
    private lateinit var btBack: Button
    private lateinit var ivArtistImage: ShapeableImageView
    private lateinit var tvArtistName: TextView
    private lateinit var tabLayoutContent: TabLayout
    private lateinit var vpContent: ViewPager2


    //Others
    private var artistName: String = ""
    private var artistID: Long = 0
    private var songsOpened = false
    private var albumsOpened = false
    private lateinit var adapterViewPager: AdapterViewPager


    //Lifecycle
    private var artistImageLoaded = false


    //Listeners

    var onBackPressedListener: OnBackPressedListener?= null
    interface OnBackPressedListener{ fun onBackPressed() }

    var onAlbumOpenedListener: OnAlbumOpenedListener?= null
    interface OnAlbumOpenedListener { fun onAlbumOpened( albumID: Long ) }


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.fragment_artist, container, false)
        view.context.setTheme(ColorFunctions.getTheme(view.context))
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleBackPressed()
        assignVariables(view)
        setupColors()
        btBack.setOnClickListener { onBackPressedListener?.onBackPressed() }

        try{

            tvArtistName.text = artistName

            adapterViewPager = AdapterViewPager( childFragmentManager, lifecycle, artistID)
            vpContent.adapter = adapterViewPager


            if (savedInstanceState != null){

                songsOpened = savedInstanceState.getBoolean("songsOpened")
                albumsOpened = savedInstanceState.getBoolean( "albumsOpened" )

                if( songsOpened ){

                    handleSongsFragmentListener()
                }

                if( albumsOpened ){

                    val fragment = childFragmentManager.findFragmentByTag("f1") as FragmentArtistRecyclerView

                    fragment.onAlbumOpenedListener = object : FragmentArtistRecyclerView.OnAlbumOpenedListener {
                        override fun onAlbumOpened(albumID: Long) {

                            onAlbumOpenedListener?.onAlbumOpened(albumID)
                        }
                    }
                }
            }


            if( !songsOpened ) handleSongsFragmentListener()
            if( !albumsOpened ) handleAlbumsFragmentListener()


            TabLayoutMediator(tabLayoutContent, vpContent ) { tab, position ->
                run {

                    when (position) {

                        0-> tab.text = getString(R.string.Songs)
                        1-> tab.text = getString(R.string.Albums)
                    }
                }
            }.attach()

            //Loads artist image asyncronously
            val executor = Executors.newSingleThreadExecutor()
            val handler = Handler(Looper.getMainLooper())

            executor.execute {
                handler.post{

                    if( !artistImageLoaded ) loadArtistImage()
                }
            }
        }
        catch (exc: Exception){}
    }



    private fun handleSongsFragmentListener(){

        adapterViewPager.setOnSongsFragmentOpened(object : AdapterViewPager.OnSongsFragmentOpened {
            override fun onSongsOpened(fragment: FragmentArtistRecyclerView) { songsOpened = true }
        })
    }


    private fun handleAlbumsFragmentListener(){

        adapterViewPager.setOnAlbumsFragmentOpened(object : AdapterViewPager.OnAlbumsFragmentOpened {
            override fun onAlbumsOpened(fragment: FragmentArtistRecyclerView) {

                albumsOpened = true

                fragment.onAlbumOpenedListener = object : FragmentArtistRecyclerView.OnAlbumOpenedListener {
                    override fun onAlbumOpened(albumID: Long) {

                        onAlbumOpenedListener?.onAlbumOpened(albumID)
                    }
                }
            }
        })
    }


    private fun assignVariables(view: View){

        fragmentView = view
        fragmentContext = view.context
        clMain = view.findViewById(R.id.clMain_FragmentArtist)
        btBack = view.findViewById(R.id.btBack_Toolbar)
        ivArtistImage = view.findViewById(R.id.ivArtistImage_FragmentArtist)
        tvArtistName = view.findViewById(R.id.artistName_FragmentArtist)
        tabLayoutContent = view.findViewById(R.id.tabLayout_FragmentArtist)
        vpContent = view.findViewById(R.id.vp_FragmentArtist)


        artistName = arguments!!.getString("artistName").toString()
        artistID = arguments!!.getLong("artistID")
    }


    private fun setupColors(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
    }


    private fun loadArtistImage(){

        val spArtists = fragmentContext.getSharedPreferences( "artists", MODE_PRIVATE )
        val imageSaved = spArtists.getString(artistID.toString(), null)

        if( imageSaved != null ){

            try{

                val encodeByte = Base64.decode(imageSaved, Base64.DEFAULT)
                val artistImageBitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)

                ivArtistImage.setImageBitmap( artistImageBitmap )

                artistImageLoaded = true
            }
            catch (exc: Exception){}
        }
        else{

            if(CheckInternet.isNetworkAvailable(fragmentContext)){

                val url = "https://www.theaudiodb.com/api/v1/json/2/search.php?s=$artistName"

                MakeRequest( fragmentContext, url, object : MakeRequest.OnResponseListener {
                    override fun onResponse(responseCode: Int, responseJson: String) {

                        if( responseCode == 200 ){

                            val responseAudioDB = Gson().fromJson( responseJson, ResponseAudioDB::class.java )

                            if( responseAudioDB.artists == null ){

                                val defaultArtistDrawable = ContextCompat.getDrawable(fragmentContext, R.drawable.icon_person_regular)
                                ivArtistImage.setColorFilter(ContextCompat.getColor(fragmentContext, R.color.icon))
                                ivArtistImage.setImageDrawable(defaultArtistDrawable)

                                artistImageLoaded = true
                            }
                            else{

                                try{
                                    val artistImageURL = responseAudioDB.artists[0].strArtistThumb
                                    Glide.with(fragmentContext)
                                        .asBitmap()
                                        .load(artistImageURL)
                                        .into(object : CustomTarget<Bitmap>(){
                                            override fun onResourceReady( resource: Bitmap, transition: Transition<in Bitmap>? ) {

                                                ivArtistImage.setImageBitmap( resource )

                                                val baos = ByteArrayOutputStream()
                                                resource.compress(Bitmap.CompressFormat.PNG, 50, baos)
                                                val b = baos.toByteArray()
                                                val encodedImage = Base64.encodeToString(b, Base64.DEFAULT)

                                                spArtists.edit().putString(artistID.toString(), encodedImage).apply()

                                                artistImageLoaded = true
                                            }

                                            override fun onLoadCleared(placeholder: Drawable?) {}
                                        })
                                }
                                catch (exc: Exception){}
                            }
                        }
                    }
                }).get()
            }
            else{

                val defaultArtistDrawable = ContextCompat.getDrawable(fragmentContext, R.drawable.icon_person_regular)
                ivArtistImage.setColorFilter(ContextCompat.getColor(fragmentContext, R.color.icon))
                ivArtistImage.setImageDrawable(defaultArtistDrawable)

                artistImageLoaded = true
            }
        }
    }


    private fun handleBackPressed(){

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    if (isEnabled) {
                        isEnabled = false
                        onBackPressedListener?.onBackPressed()
                    }
                }
            })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("songsOpened", songsOpened)
        outState.putBoolean("albumsOpened", albumsOpened)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Listeners

    fun updateCurrentSong(){

        try{

            if( songsOpened ) {
                val fragment = childFragmentManager.findFragmentByTag("f0") as FragmentArtistRecyclerView
                fragment.updateCurrentSong()
            }
        }
        catch (exc: Exception){}

    }

    fun resetRecyclerView(){

        try{

            if( songsOpened ){

                val fragment = childFragmentManager.findFragmentByTag("f0") as FragmentArtistRecyclerView
                fragment.resetRecyclerView()
            }
        }
        catch (exc: Exception){}
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Adapters

    class AdapterViewPager(fragmentManager: FragmentManager, lifecycle: Lifecycle, private val artistID: Long) :
        FragmentStateAdapter(fragmentManager, lifecycle) {

        private lateinit var onSongsFragmentOpened: OnSongsFragmentOpened
        private lateinit var onAlbumsFragmentOpened: OnAlbumsFragmentOpened


        interface OnSongsFragmentOpened{ fun onSongsOpened( fragment: FragmentArtistRecyclerView) }
        fun setOnSongsFragmentOpened(listener: OnSongsFragmentOpened){onSongsFragmentOpened = listener}

        interface OnAlbumsFragmentOpened{ fun onAlbumsOpened( fragment: FragmentArtistRecyclerView) }
        fun setOnAlbumsFragmentOpened(listener: OnAlbumsFragmentOpened){onAlbumsFragmentOpened = listener}


        override fun getItemCount(): Int { return 2 }

        override fun createFragment(position: Int): Fragment {


            val fragment = FragmentArtistRecyclerView()
            val bundle = Bundle()
            bundle.putLong( "artistID", artistID )

            try{

                when(position){

                    0-> {
                        bundle.putString("page", "songs")
                        onSongsFragmentOpened.onSongsOpened(fragment)
                    }
                    1-> {
                        bundle.putString("page", "albums")
                        onAlbumsFragmentOpened.onAlbumsOpened(fragment)
                    }
                }

                fragment.arguments = bundle

                return fragment
            }
            catch (exc: Exception){return fragment }

        }
    }
}