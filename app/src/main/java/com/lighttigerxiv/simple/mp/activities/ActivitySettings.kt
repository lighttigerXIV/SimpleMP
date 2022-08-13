package com.lighttigerxiv.simple.mp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.lighttigerxiv.simple.mp.FragmentSettings
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


        clMain.setBackgroundColor(ColorFunctions.getThemeColor(applicationContext, 1))
        btBack.setOnClickListener { onBackPressed() }
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout_ActivitySettings, FragmentSettings()).commit()
    }
}