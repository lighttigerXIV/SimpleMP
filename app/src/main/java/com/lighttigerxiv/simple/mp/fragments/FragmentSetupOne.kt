package com.lighttigerxiv.simple.mp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import com.lighttigerxiv.simple.mp.R
import com.lighttigerxiv.simple.mp.others.ColorFunctions


class FragmentSetupOne : Fragment() {

    private lateinit var fragmentContext: Context
    private lateinit var clMain: ConstraintLayout
    private lateinit var btNext: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_setup_one, container, false)
        view.context.setTheme(ColorFunctions.getTheme(view.context))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        assignVariables(view)
        setupTheme()


        btNext.setOnClickListener { onBtNextClicked?.onButtonClicked() }
    }


    private fun assignVariables(view: View){

        fragmentContext = view.context
        clMain = view.findViewById(R.id.clMain_FragmentSetupOne)
        btNext = view.findViewById(R.id.btNext_FragmentSetupOne)
    }


    private fun setupTheme(){

        clMain.setBackgroundColor(ColorFunctions.getThemeColor(fragmentContext, 1))
    }


    // Listeners //////////////////////////////////////////////////////////////////////////////////////////////////////////

    interface OnBtNextClicked{fun onButtonClicked()}
    var onBtNextClicked: OnBtNextClicked? = null
}