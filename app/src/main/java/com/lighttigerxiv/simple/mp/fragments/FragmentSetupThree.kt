package com.lighttigerxiv.simple.mp.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.others.ColorFunctions


class FragmentSetupThree : Fragment() {

    //Main
    private lateinit var fragmentContext: Context
    private lateinit var clMain: ConstraintLayout
    private lateinit var clBlueTheme: ConstraintLayout
    private lateinit var ivBlueTheme: ImageView
    private lateinit var clRedTheme: ConstraintLayout
    private lateinit var ivRedTheme: ImageView
    private lateinit var clPurpleTheme: ConstraintLayout
    private lateinit var ivPurpleTheme: ImageView
    private lateinit var clOrangeTheme: ConstraintLayout
    private lateinit var ivOrangeTheme: ImageView
    private lateinit var clYellowTheme: ConstraintLayout
    private lateinit var ivYellowTheme: ImageView
    private lateinit var clGreenTheme: ConstraintLayout
    private lateinit var ivGreenTheme: ImageView
    private lateinit var clMonoTheme: ConstraintLayout
    private lateinit var ivMonoTheme: ImageView
    private lateinit var btFinish: Button
    private lateinit var btBack: Button

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {

        val view = inflater.inflate(R.layout.fragment_setup_three, container, false)
        view.context.setTheme(ColorFunctions.getTheme(view.context))
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assignVariables(view)

        if(savedInstanceState != null) restoreLifecycle(savedInstanceState)

        setupTheme()

        btBack.setOnClickListener { onBtBackClickListener?.onButtonClicked() }
        btFinish.setOnClickListener { onBtFinishClickListener?.onButtonClicked() }

        handleThemesClicked()
    }

    private fun assignVariables(view: View){

        fragmentContext = view.context
        clMain = view.findViewById(R.id.clMain_FragmentSetupThree)
        clBlueTheme = view.findViewById(R.id.clBlueTheme_FragmentSetupThree)
        ivBlueTheme = view.findViewById(R.id.ivBlueTheme_FragmentSetupThree)
        clRedTheme = view.findViewById(R.id.clRedTheme_FragmentSetupThree)
        ivRedTheme = view.findViewById(R.id.ivRedTheme_FragmentSetupThree)
        clPurpleTheme = view.findViewById(R.id.clPurpleTheme_FragmentSetupThree)
        ivPurpleTheme = view.findViewById(R.id.ivPurpleTheme_FragmentSetupThree)
        clOrangeTheme = view.findViewById(R.id.clOrangeTheme_FragmentSetupThree)
        ivOrangeTheme = view.findViewById(R.id.ivOrangeTheme_FragmentSetupThree)
        clYellowTheme = view.findViewById(R.id.clYellowTheme_FragmentSetupThree)
        ivYellowTheme = view.findViewById(R.id.ivYellowTheme_FragmentSetupThree)
        clGreenTheme = view.findViewById(R.id.clGreenTheme_FragmentSetupThree)
        ivGreenTheme = view.findViewById(R.id.ivGreenTheme_FragmentSetupThree)
        clMonoTheme = view.findViewById(R.id.clMonoTheme_FragmentSetupThree)
        ivMonoTheme = view.findViewById(R.id.ivMonoTheme_FragmentSetupThree)
        btBack = view.findViewById(R.id.btBack_FragmentSetupThree)
        btFinish = view.findViewById(R.id.btFinish_FragmentSetupThree)
    }


    private fun restoreLifecycle(sis: Bundle){

        btFinish.isEnabled = sis.getBoolean("isBtFinishEnabled", false)

        if(btFinish.isEnabled){

            when(PreferenceManager.getDefaultSharedPreferences(fragmentContext).getString("setting_theme", "blue")){

                "blue"-> ivBlueTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_blue_selected))
                "red"-> ivRedTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_red_selected))
                "purple"-> ivPurpleTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_purple_selected))
                "orange"-> ivOrangeTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_orange_selected))
                "yellow"-> ivYellowTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_yellow_selected))
                "green"-> ivGreenTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_green_selected))
                "mono"-> ivMonoTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_mono_selected))
            }
        }
    }


    private fun setupTheme(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
        btBack.backgroundTintList = ColorStateList.valueOf(ColorFunctions.getThemeColor(fragmentContext, 5))

        if(btFinish.isEnabled) btFinish.backgroundTintList = ColorStateList.valueOf(ColorFunctions.getThemeColor(fragmentContext, 5))
    }


    private fun handleThemesClicked(){

        clBlueTheme.setOnClickListener {

            PreferenceManager.getDefaultSharedPreferences(fragmentContext).edit().putString("setting_theme", "blue").apply()
            btFinish.isEnabled = true
            btFinish.backgroundTintList = ColorStateList.valueOf(ColorFunctions.getThemeColor(fragmentContext, 5))
            ivBlueTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_blue_selected))
            requireActivity().recreate()
        }

        clRedTheme.setOnClickListener {

            PreferenceManager.getDefaultSharedPreferences(fragmentContext).edit().putString("setting_theme", "red").apply()
            btFinish.isEnabled = true
            btFinish.backgroundTintList = ColorStateList.valueOf(ColorFunctions.getThemeColor(fragmentContext, 5))
            ivRedTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_red_selected))
            requireActivity().recreate()
        }

        clPurpleTheme.setOnClickListener {

            PreferenceManager.getDefaultSharedPreferences(fragmentContext).edit().putString("setting_theme", "purple").apply()
            btFinish.isEnabled = true
            btFinish.backgroundTintList = ColorStateList.valueOf(ColorFunctions.getThemeColor(fragmentContext, 5))
            ivPurpleTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_purple_selected))
            requireActivity().recreate()
        }

        clOrangeTheme.setOnClickListener {

            PreferenceManager.getDefaultSharedPreferences(fragmentContext).edit().putString("setting_theme", "orange").apply()
            btFinish.isEnabled = true
            btFinish.backgroundTintList = ColorStateList.valueOf(ColorFunctions.getThemeColor(fragmentContext, 5))
            ivOrangeTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_orange_selected))
            requireActivity().recreate()
        }

        clYellowTheme.setOnClickListener {

            PreferenceManager.getDefaultSharedPreferences(fragmentContext).edit().putString("setting_theme", "yellow").apply()
            btFinish.isEnabled = true
            btFinish.backgroundTintList = ColorStateList.valueOf(ColorFunctions.getThemeColor(fragmentContext, 5))
            ivYellowTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_yellow_selected))
            requireActivity().recreate()
        }

        clGreenTheme.setOnClickListener {

            PreferenceManager.getDefaultSharedPreferences(fragmentContext).edit().putString("setting_theme", "green").apply()
            btFinish.isEnabled = true
            btFinish.backgroundTintList = ColorStateList.valueOf(ColorFunctions.getThemeColor(fragmentContext, 5))
            ivGreenTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_green_selected))
            requireActivity().recreate()
        }

        clMonoTheme.setOnClickListener {

            PreferenceManager.getDefaultSharedPreferences(fragmentContext).edit().putString("setting_theme", "mono").apply()
            btFinish.isEnabled = true
            btFinish.backgroundTintList = ColorStateList.valueOf(ColorFunctions.getThemeColor(fragmentContext, 5))
            ivMonoTheme.setImageDrawable(ContextCompat.getDrawable(fragmentContext, R.drawable.theme_mono_selected))
            requireActivity().recreate()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("isBtFinishEnabled", btFinish.isEnabled)
    }


    // Listeners //////////////////////////////////////////////////////////////////////////////////////////////////////////

    interface OnButtonClickedListener{fun onButtonClicked()}
    var onBtFinishClickListener: OnButtonClickedListener? = null
    var onBtBackClickListener: OnButtonClickedListener? = null
}