package com.lighttigerxiv.simple.mp.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.others.ColorFunctions


class FragmentSetupTwo : Fragment() {

    private lateinit var fragmentContext: Context
    private lateinit var clMain: ConstraintLayout
    private lateinit var tvGrant: TextView
    private lateinit var btNext: Button
    private lateinit var btBack: Button


    private var storagePermissionGranted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.fragment_setup_two, container, false)
        view.context.setTheme(ColorFunctions.getTheme(view.context))
        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        assignVariables(view)
        setupTheme()


        btBack.setOnClickListener { onBtBackClickListener?.onButtonClicked() }
        btNext.setOnClickListener { onBtNextClickListener?.onButtonClicked() }

        checkPermissions()

        tvGrant.setOnClickListener {

            val permission = ContextCompat.checkSelfPermission( fragmentContext, Manifest.permission.READ_EXTERNAL_STORAGE )

            if(permission != PackageManager.PERMISSION_GRANTED){

                requestStoragePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }


    private fun assignVariables(view: View){

        fragmentContext = view.context
        clMain = view.findViewById(R.id.clMain_FragmentSetupTwo)
        tvGrant = view.findViewById(R.id.tvGrant_FragmentSetupTwo)
        btBack = view.findViewById(R.id.btBack_FragmentSetupTwo)
        btNext = view.findViewById(R.id.btNext_FragmentSetupTwo)
    }


    private fun setupTheme(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
    }


    private fun checkPermissions(){

        val permission = ContextCompat.checkSelfPermission( fragmentContext, Manifest.permission.READ_EXTERNAL_STORAGE )

        if( permission == PackageManager.PERMISSION_GRANTED ){

            storagePermissionGranted = true
            tvGrant.text = getString(R.string.Granted)
            tvGrant.setTextColor(ContextCompat.getColor(fragmentContext, R.color.subText))
            btNext.isEnabled = true
        }
    }


    private val requestStoragePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        when(it){
            true->{

                storagePermissionGranted = true
                tvGrant.text = getString(R.string.Granted)
                tvGrant.setTextColor(ContextCompat.getColor(fragmentContext, R.color.subText))
                btNext.isEnabled = true
            }
            false->{

                val spPreferences = PreferenceManager.getDefaultSharedPreferences(fragmentContext)
                var storagePermissionDeniedCount = spPreferences.getInt("storagePermissionDeniedCount", 0)
                storagePermissionDeniedCount++

                when(storagePermissionDeniedCount){

                    1-> Toast.makeText(fragmentContext, getString(R.string.PleaseEnableStoragePermission), Toast.LENGTH_SHORT).show()

                    2-> Toast.makeText(fragmentContext, getString(R.string.PleaseEnableStoragePermission), Toast.LENGTH_SHORT).show()

                    else->{

                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", fragmentContext.packageName, null)
                        startActivity(intent)
                    }
                }

                spPreferences.edit().putInt("storagePermissionDeniedCount", storagePermissionDeniedCount).apply()
            }
        }
    }


    // Listeners //////////////////////////////////////////////////////////////////////////////////////////////////////////

    interface OnButtonClickedListener{fun onButtonClicked()}
    var onBtNextClickListener: OnButtonClickedListener? = null
    var onBtBackClickListener: OnButtonClickedListener? = null
}