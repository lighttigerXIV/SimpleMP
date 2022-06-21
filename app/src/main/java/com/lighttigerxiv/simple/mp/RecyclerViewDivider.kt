package com.lighttigerxiv.simple.mp

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewDivider(context: Context): RecyclerView.ItemDecoration() {

    private var mDivider: Drawable = ContextCompat.getDrawable(context, R.drawable.rv_divider)!!

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        val dividerLeft = 0
        val dividerRight: Int = parent.width

        if( parent.childCount > 1 ){

            for (i in 0 until parent.childCount){

                if (i != parent.childCount - 1) {
                    val child: View = parent.getChildAt(i)

                    val params = child.layoutParams as RecyclerView.LayoutParams

                    val dividerTop: Int = child.bottom + params.bottomMargin
                    val dividerBottom: Int = dividerTop + mDivider.intrinsicHeight

                    mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                    mDivider.draw(c)
                }
            }
        }
    }
}