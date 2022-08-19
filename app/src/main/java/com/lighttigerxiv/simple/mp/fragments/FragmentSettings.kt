package com.lighttigerxiv.simple.mp.fragments

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.google.android.material.snackbar.Snackbar
import com.lighttigerxiv.simple.mp.activities.ActivityAboutPage
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.activities.ActivityMain
import com.lighttigerxiv.simple.mp.others.ColorFunctions
import java.util.*

class FragmentSettings : PreferenceFragmentCompat() {

    /*
    interface OnSettingChangedListener{fun onSettingChanged(setting: String)}
    private var onSettingChangedListener: OnSettingChangedListener? = null

     */

    interface OnSettingClickedListener{fun onSettingClicked(setting: String)}
    private var onSettingClickedListener: OnSettingClickedListener? = null




    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.preferences, rootKey)
        val themeModePreference: ListPreference? = findPreference("setting_themeMode")
        val themePreferences: Preference? = findPreference("setting_theme")
        val filterAudioBelowPreference: Preference? = findPreference("setting_filterAudio")
        val aboutPreference: Preference? = findPreference("setting_about")


        themeModePreference?.setOnPreferenceChangeListener { _, newValue ->

            when(newValue.toString()){

                "system"-> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                "light"-> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "dark"-> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            true
        }

        themePreferences?.onPreferenceClickListener = Preference.OnPreferenceClickListener{

            onSettingClickedListener?.onSettingClicked("setting_theme")
            true
        }

        filterAudioBelowPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {

            onSettingClickedListener?.onSettingClicked("setting_filterAudio")
            true
        }

        aboutPreference?.onPreferenceClickListener = Preference.OnPreferenceClickListener {

            onSettingClickedListener?.onSettingClicked("setting_about")
            true
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context
        val themeModePreference: ListPreference? = findPreference("setting_themeMode")
        val themePreference: Preference? = findPreference("setting_theme")
        val filterAudioPreference: Preference? = findPreference("setting_filterAudio")
        val limitAudioVolumePreference: SwitchPreference? = findPreference("setting_limitAudioVolume")
        val aboutPreference: Preference? = findPreference("setting_about")


        //Inserts summary in the preferences
        val themeValue = PreferenceManager.getDefaultSharedPreferences(context).getString("setting_theme", "blue")
        themePreference?.summary = themeValue?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

        val filterAudioValue = PreferenceManager.getDefaultSharedPreferences( context ).getInt("setting_filterAudio", 60)
        filterAudioPreference?.summary = "$filterAudioValue ${getString(R.string.Seconds)}"


        //Inserts an icon to the preferences
        themeModePreference!!.icon = ContextCompat.getDrawable(context, R.drawable.icon_theme_mode_regular)
        themeModePreference.icon!!.setTintList(ColorStateList.valueOf(ColorFunctions.getThemeColor(context, 5)))

        themePreference!!.icon = ContextCompat.getDrawable( context, R.drawable.icon_theme_regular)
        themePreference.icon!!.setTintList(ColorStateList.valueOf(ColorFunctions.getThemeColor(context, 5)))

        filterAudioPreference!!.icon = ContextCompat.getDrawable(context, R.drawable.icon_filter_regular)
        filterAudioPreference.icon!!.setTintList(ColorStateList.valueOf(ColorFunctions.getThemeColor(context, 5)))

        limitAudioVolumePreference!!.icon = ContextCompat.getDrawable(context, R.drawable.icon_volume_regular)
        limitAudioVolumePreference.icon!!.setTintList(ColorStateList.valueOf(ColorFunctions.getThemeColor(context, 5)))

        aboutPreference!!.icon = ContextCompat.getDrawable(context, R.drawable.icon_about_regular)
        aboutPreference.icon!!.setTintList(ColorStateList.valueOf(ColorFunctions.getThemeColor(context, 5)))


        onSettingClickedListener = object : OnSettingClickedListener{
            override fun onSettingClicked(setting: String) {

                when(setting){

                    "setting_theme"->{

                        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_preference_theme, ConstraintLayout(context), false)
                        val ivBlueTheme: ImageView = dialogView.findViewById(R.id.ivBlueTheme_DialogPreferenceTheme)
                        val ivRedTheme: ImageView = dialogView.findViewById(R.id.ivRedTheme_DialogPreferenceTheme)
                        val ivPurpleTheme: ImageView = dialogView.findViewById(R.id.ivPurpleTheme_DialogPreferenceTheme)
                        val ivOrangeTheme: ImageView = dialogView.findViewById(R.id.ivOrangeTheme_DialogPreferenceTheme)
                        val ivYellowTheme: ImageView = dialogView.findViewById(R.id.ivYellowTheme_DialogPreferenceTheme)
                        val ivGreenTheme: ImageView = dialogView.findViewById(R.id.ivGreenTheme_DialogPreferenceTheme)
                        val ivMonoTheme: ImageView = dialogView.findViewById(R.id.ivMonoTheme_DialogPreferenceTheme)


                        val dialog = AlertDialog.Builder(context, ColorFunctions.getDialogTheme(context))
                            .setView(dialogView)
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.OK), null)
                            .show()


                        ivBlueTheme.setOnClickListener {
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("setting_theme", "blue").apply()
                            dialog.dismiss()
                            showRestartSnack(view)
                        }
                        ivRedTheme.setOnClickListener {
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("setting_theme", "red").apply()
                            dialog.dismiss()
                            showRestartSnack(view)
                        }
                        ivPurpleTheme.setOnClickListener {
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("setting_theme", "purple").apply()
                            dialog.dismiss()
                            showRestartSnack(view)
                        }
                        ivOrangeTheme.setOnClickListener {
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("setting_theme", "orange").apply()
                            dialog.dismiss()
                            showRestartSnack(view)
                        }
                        ivYellowTheme.setOnClickListener {
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("setting_theme", "yellow").apply()
                            dialog.dismiss()
                            showRestartSnack(view)
                        }
                        ivGreenTheme.setOnClickListener {
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("setting_theme", "green").apply()
                            dialog.dismiss()
                            showRestartSnack(view)
                        }
                        ivMonoTheme.setOnClickListener {
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("setting_theme", "mono").apply()
                            dialog.dismiss()
                            showRestartSnack(view)
                        }
                    }

                    "setting_filterAudio"->{

                        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_preference_filter_audio, ConstraintLayout(context), false)
                        val etSeconds = dialogView.findViewById<EditText>(R.id.etSeconds_DialogFilterAudio)

                        etSeconds.setText(PreferenceManager.getDefaultSharedPreferences( context ).getInt("setting_filterAudio", 60).toString())
                        etSeconds.background = ColorFunctions.getEditTextBackground(context)

                        AlertDialog.Builder(context, ColorFunctions.getDialogTheme(context))
                            .setView(dialogView)
                            .setCancelable(false)
                            .setNegativeButton(getString(R.string.Cancel), null)
                            .setPositiveButton(getString(R.string.OK)
                            ) { dialog, _ ->

                                val etSecondsText = etSeconds.text.toString().trim()

                                if(etSecondsText.isNotEmpty()){

                                    PreferenceManager.getDefaultSharedPreferences( context ).edit().putInt("setting_filterAudio", etSecondsText.toInt()).apply()
                                    filterAudioPreference.summary = "$etSecondsText ${getString(R.string.Seconds)}"
                                    dialog.dismiss()


                                    showRestartSnack(view)
                                }
                            }
                            .show()
                    }

                    "setting_about"-> startActivity(Intent(context, ActivityAboutPage::class.java ))
                }
            }
        }
    }


    private fun showRestartSnack(view: View){

        Snackbar.make( view, getString(R.string.RestartToTakeEffect), Snackbar.LENGTH_LONG )
            .setAction(getString(R.string.Restart)) {

                val intent = Intent(view.context, ActivityMain::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .show()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //TO-DO: Restore opened dialogs

    }
}