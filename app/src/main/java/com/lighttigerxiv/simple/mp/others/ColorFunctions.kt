package com.lighttigerxiv.simple.mp.others

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.lighttigerxiv.simple.mp.R

class ColorFunctions {

    companion object {

        fun getTheme(context: Context): Int{

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val theme = prefs.getString("setting_theme", "blue")
            var themeID = 0


            when(theme){

                "purple"-> themeID = R.style.PurpleTheme
                "blue"-> themeID = R.style.BlueTheme
                "red"-> themeID = R.style.RedTheme
                "yellow"-> themeID = R.style.YellowTheme
                "orange"-> themeID = R.style.OrangeTheme
                "mono"-> themeID = R.style.MonoTheme
                "green"-> themeID = R.style.GreenTheme
            }

            return themeID
        }

        fun getThemeColor(context: Context, intensity: Int ): Int{

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val theme = prefs.getString("setting_theme", "blue")
            var color = 0


            when(theme){

                "blue"->{
                    when(intensity){
                        1-> color = ContextCompat.getColor(context, R.color.blue1)
                        2-> color = ContextCompat.getColor(context, R.color.blue2)
                        3-> color = ContextCompat.getColor(context, R.color.blue3)
                        5-> color = ContextCompat.getColor(context, R.color.blue5)
                    }
                }
                "red"->{
                    when(intensity){
                        1-> color = ContextCompat.getColor(context, R.color.red1)
                        2-> color = ContextCompat.getColor(context, R.color.red2)
                        3-> color = ContextCompat.getColor(context, R.color.red3)
                        5-> color = ContextCompat.getColor(context, R.color.red5)
                    }
                }
                "purple"->{
                    when(intensity){
                        1-> color = ContextCompat.getColor(context, R.color.purple1)
                        2-> color = ContextCompat.getColor(context, R.color.purple2)
                        3-> color = ContextCompat.getColor(context, R.color.purple3)
                        5-> color = ContextCompat.getColor(context, R.color.purple5)
                    }
                }
                "yellow"->{
                    when(intensity){
                        1-> color = ContextCompat.getColor(context, R.color.yellow1)
                        2-> color = ContextCompat.getColor(context, R.color.yellow2)
                        3-> color = ContextCompat.getColor(context, R.color.yellow3)
                        5-> color = ContextCompat.getColor(context, R.color.yellow5)
                    }
                }
                "orange"->{
                    when(intensity){
                        1-> color = ContextCompat.getColor(context, R.color.orange1)
                        2-> color = ContextCompat.getColor(context, R.color.orange2)
                        3-> color = ContextCompat.getColor(context, R.color.orange3)
                        5-> color = ContextCompat.getColor(context, R.color.orange5)
                    }
                }
                "mono"->{
                    when(intensity){
                        1-> color = ContextCompat.getColor(context, R.color.mono1)
                        2-> color = ContextCompat.getColor(context, R.color.mono2)
                        3-> color = ContextCompat.getColor(context, R.color.mono3)
                        5-> color = ContextCompat.getColor(context, R.color.mono5)
                    }
                }
                "green"->{
                    when(intensity){
                        1-> color = ContextCompat.getColor(context, R.color.green1)
                        2-> color = ContextCompat.getColor(context, R.color.green2)
                        3-> color = ContextCompat.getColor(context, R.color.green3)
                        5-> color = ContextCompat.getColor(context, R.color.green5)
                    }
                }
            }

            return color
        }

        fun getEditTextBackground(context: Context): Drawable{

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val theme = prefs.getString("setting_theme", "blue")
            lateinit var background: Drawable


            when(theme){

                "blue"-> background = ContextCompat.getDrawable(context, R.drawable.card_blue_background)!!
                "red"-> background = ContextCompat.getDrawable(context, R.drawable.card_red_background)!!
                "purple"-> background = ContextCompat.getDrawable(context, R.drawable.card_purple_background)!!
                "yellow"-> background = ContextCompat.getDrawable(context, R.drawable.card_yellow_background)!!
                "orange"-> background = ContextCompat.getDrawable(context, R.drawable.card_orange_background)!!
                "mono"-> background = ContextCompat.getDrawable(context, R.drawable.card_mono_background)!!
                "green"-> background = ContextCompat.getDrawable(context, R.drawable.card_green_background)!!
            }

            return background
        }


        fun getPopupMenuTheme(context: Context): Int{

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val theme = prefs.getString("setting_theme", "blue")
            var themeID = 0


            when(theme){

                "blue"-> themeID = R.style.BluePopupMenu
                "red"-> themeID = R.style.RedPopupMenu
                "purple"-> themeID = R.style.PurplePopupMenu
                "yellow"-> themeID = R.style.YellowPopupMenu
                "orange"-> themeID = R.style.OrangePopupMenu
                "mono"-> themeID = R.style.MonoPopupMenu
                "green"-> themeID = R.style.GreenPopupMenu
            }

            return themeID
        }


        fun getDialogTheme(context: Context): Int{

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val theme = prefs.getString("setting_theme", "blue")
            var themeID = 0

            when(theme){

                "blue"-> themeID = R.style.BlueDialog
                "red"-> themeID = R.style.RedDialog
                "purple"-> themeID = R.style.PurpleDialog
                "yellow"-> themeID = R.style.YellowDialog
                "orange"-> themeID = R.style.OrangeDialog
                "mono"-> themeID = R.style.MonoDialog
                "green"-> themeID = R.style.GreenDialog
            }

            return themeID
        }
    }
}