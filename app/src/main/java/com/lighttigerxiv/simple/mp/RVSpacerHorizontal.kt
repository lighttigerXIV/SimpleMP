package com.lighttigerxiv.simple.mp

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RVSpacerHorizontal(private val distance: Int ): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect.left = distance
        outRect.right = distance
    }
}