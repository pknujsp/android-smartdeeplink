package com.pknujsp.feature.holographic

import android.graphics.Outline
import android.graphics.Rect
import android.view.View
import android.view.ViewOutlineProvider

internal class OutlineProvider(
    var shiftX: Int = 0, var shiftY: Int = 0
) : ViewOutlineProvider() {

    val rect: Rect = Rect()

    override fun getOutline(view: View, outline: Outline?) {
        rect.set(-10, -10, view.width+10, view.height-5)
        if (shiftX > 0) {
            rect.right += shiftX
        } else {
            rect.left += shiftX
        }

        if (shiftY > 0) {
            rect.bottom += shiftY
        } else {
            rect.top += shiftY
        }

        outline?.setRect(rect)
        outline?.alpha = 1f
    }
}