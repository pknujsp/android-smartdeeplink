package io.github.pknujsp.testbed.feature.holographic

import android.graphics.Outline
import android.graphics.Rect
import android.view.View
import android.view.ViewOutlineProvider

internal class OutlineProvider(
  var shiftX: Int = 0, var shiftY: Int = 0,
) : ViewOutlineProvider() {

  private val rect: Rect = Rect()

  override fun getOutline(view: View, outline: Outline?) {
    rect.set(-10, 10, view.width + 10, view.height)
    rect.right += shiftX
    rect.left += shiftX

    rect.bottom += shiftY
    rect.top += shiftY
    outline?.setRect(rect)
  }
}
