package com.lighttigerxiv.simple.mp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.lighttigerxiv.simple.mp.fragments.FragmentSetupOne
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.fragments.FragmentSetupThree
import com.lighttigerxiv.simple.mp.others.ColorFunctions
import com.lighttigerxiv.simple.mp.fragments.FragmentSetupTwo

class ActivitySetup : AppCompatActivity() {

    private lateinit var clMain: ConstraintLayout
    private lateinit var fragmentManager: FragmentManager
    private var frameLayoutID = 0


    private var fragmentSetupOne = FragmentSetupOne()
    private var fragmentSetupTwo = FragmentSetupTwo()
    private var fragmentSetupThree = FragmentSetupThree()


    private var setupOneAdded = false
    private var setupTwoAdded = false
    private var setupThreeAdded = false


    private var selectedFragment = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(ColorFunctions.getTheme(this))
        setContentView(R.layout.activity_setup)
        assignVariables()


        if(savedInstanceState == null){

            selectedFragment = 0
            fragmentManager.beginTransaction().add(frameLayoutID, fragmentSetupOne, "setupOne").commit()
            setupOneAdded = true
        }
        else restoreLifecycle(savedInstanceState)


        handleFragmentChanges()
    }


    private fun assignVariables(){

        clMain = findViewById(R.id.clMain_ActivitySetup)
        fragmentManager = supportFragmentManager
        frameLayoutID = R.id.frameLayout_ActivitySetup
    }


    private fun restoreLifecycle(sis: Bundle){

        setupOneAdded = sis.getBoolean("setupOneAdded", false)
        setupTwoAdded = sis.getBoolean("setupTwoAdded", false)
        setupThreeAdded = sis.getBoolean("setupThreeAdded", false)


        fragmentSetupOne = when(setupOneAdded){
            true-> fragmentManager.findFragmentByTag("setupOne") as FragmentSetupOne
            false-> FragmentSetupOne()
        }

        fragmentSetupTwo = when(setupTwoAdded){
            true-> fragmentManager.findFragmentByTag("setupTwo") as FragmentSetupTwo
            false-> FragmentSetupTwo()
        }

        fragmentSetupThree = when(setupThreeAdded){
            true-> fragmentManager.findFragmentByTag("setupThree") as FragmentSetupThree
            false-> FragmentSetupThree()
        }
    }


    private fun handleFragmentChanges(){

        fragmentSetupOne.onBtNextClicked = object : FragmentSetupOne.OnBtNextClicked{
            override fun onButtonClicked() {

                fragmentManager.beginTransaction().hide(fragmentSetupOne).commit()

                when(setupTwoAdded){

                    true->{
                        fragmentManager.beginTransaction().show(fragmentSetupTwo).commit()
                    }
                    false->{
                        fragmentManager.beginTransaction().add(frameLayoutID, fragmentSetupTwo, "setupTwo").commit()
                        setupTwoAdded = true
                    }
                }

                selectedFragment = 1
            }
        }


        fragmentSetupTwo.onBtBackClickListener = object : FragmentSetupTwo.OnButtonClickedListener{
            override fun onButtonClicked() {

                fragmentManager.beginTransaction().hide(fragmentSetupTwo).commit()
                fragmentManager.beginTransaction().show(fragmentSetupOne).commit()
            }
        }

        fragmentSetupTwo.onBtNextClickListener = object : FragmentSetupTwo.OnButtonClickedListener{
            override fun onButtonClicked() {

                fragmentManager.beginTransaction().hide(fragmentSetupTwo).commit()

                when(setupThreeAdded){

                    true->{
                        fragmentManager.beginTransaction().show(fragmentSetupThree).commit()
                    }
                    false->{
                        fragmentManager.beginTransaction().add(frameLayoutID, fragmentSetupThree, "setupThree").commit()
                        setupThreeAdded = true
                    }
                }

                selectedFragment = 2
            }
        }

        fragmentSetupThree.onBtBackClickListener = object : FragmentSetupThree.OnButtonClickedListener{
            override fun onButtonClicked() {

                fragmentManager.beginTransaction().hide(fragmentSetupThree).commit()
                fragmentManager.beginTransaction().show(fragmentSetupTwo).commit()
            }
        }

        fragmentSetupThree.onBtFinishClickListener = object : FragmentSetupThree.OnButtonClickedListener{
            override fun onButtonClicked() {

                getSharedPreferences("firstTimeSetup", MODE_PRIVATE).edit().putBoolean("firstTimeSetup", false).apply()
                startActivity(Intent(applicationContext, ActivityMain::class.java))
                finish()
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("setupOneAdded", setupOneAdded)
        outState.putBoolean("setupTwoAdded", setupTwoAdded)
        outState.putBoolean("setupThreeAdded", setupThreeAdded)
        outState.putInt("selectedFragment", selectedFragment)
    }
}