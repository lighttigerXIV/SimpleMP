package com.lighttigerxiv.simple.mp.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.others.ColorFunctions

class ActivityAboutPage : AppCompatActivity() {

    private lateinit var clMain: ConstraintLayout
    private lateinit var btBack: Button
    private lateinit var clGitHub: ConstraintLayout
    private lateinit var clGitLab: ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ColorFunctions.getTheme(applicationContext))
        setContentView(R.layout.activity_about_page)
        assignVariables()


        clMain.setBackgroundColor(ColorFunctions.getThemeColor(this, 1))


        btBack.setOnClickListener { onBackPressed() }
        clGitHub.setOnClickListener{ startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/lighttigerXIV/SimpleMP"))) }
        clGitLab.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/lighttigerxiv/SimpleMP"))) }
    }


    private fun assignVariables(){

        clMain = findViewById(R.id.clMain_ActivityAboutPage)
        btBack = findViewById(R.id.btBack_Toolbar)
        clGitHub = findViewById(R.id.clGitHub_ActivityAboutPage)
        clGitLab = findViewById(R.id.clGitLab_ActivityAboutPage)
    }
}