package com.lighttigerxiv.simple.mp

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.lighttigerxiv.simple.mp.activities.ActivityMain
import com.lighttigerxiv.simple.mp.others.ColorFunctions

class FragmentSettings : PreferenceFragmentCompat() {

    interface OnSettingChangedListener{fun onSettingChanged(setting: String)}
    private var onSettingChangedListener: OnSettingChangedListener? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.preferences, rootKey)
        val themePreferences: ListPreference? = findPreference("setting_theme")


        themePreferences?.setOnPreferenceChangeListener { _, _ ->

            onSettingChangedListener?.onSettingChanged( "setting_theme" )

            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val themePreferences: ListPreference? = findPreference("setting_theme")


        themePreferences!!.icon = ContextCompat.getDrawable(view.context, R.drawable.icon_theme_regular)
        themePreferences.icon!!.setTintList(ColorStateList.valueOf(ColorFunctions.getThemeColor(view.context, 5)))


        onSettingChangedListener = object : OnSettingChangedListener{
            override fun onSettingChanged(setting: String) {

                when(setting){

                    "setting_theme"->{

                        Snackbar.make( view, getString(R.string.RestartToApplyTheme), Snackbar.LENGTH_LONG )
                            .setAction(getString(R.string.Restart)) {

                                val intent = Intent(view.context, ActivityMain::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            .show()
                    }
                }
            }
        }
    }
}