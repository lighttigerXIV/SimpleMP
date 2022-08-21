package com.lighttigerxiv.simple.mp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.lighttigerxiv.simple.mp.fragments.FragmentSettings
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.others.ColorFunctions

class ActivitySettings : AppCompatActivity() {

    private lateinit var clMain: ConstraintLayout
    private lateinit var btBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ColorFunctions.getTheme(applicationContext))
        setContentView(R.layout.activity_settings)

        clMain = findViewById(R.id.clMain_ActivitySettings)
        btBack = findViewById(R.id.btBack_Toolbar)


        clMain.setBackgroundColor(ColorFunctions.getThemeColor(this, 1))
        btBack.setOnClickListener { onBackPressed() }


        if(savedInstanceState != null){

            val fragmentSettings = supportFragmentManager.findFragmentByTag("settings") as FragmentSettings
            supportFragmentManager.beginTransaction().show(fragmentSettings).commit()
        }
        else
            supportFragmentManager.beginTransaction().add(R.id.frameLayout_ActivitySettings, FragmentSettings(), "settings").commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("restore", true)
    }
}