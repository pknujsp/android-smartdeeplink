package io.github.pknujsp.testbed.core.ui.dialog

import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog

abstract class SimpleDialog(
  val alertDialog: AlertDialog,
  val attributes: SimpleDialogGeneralAttributes, val styleAttributes: SimpleDialogStyleAttributes,
) : DialogInterface by alertDialog {

  private val draggablePixelsRangeRect = Resources.getSystem().run {
    val displayMetrics = displayMetrics
    val widthPixels = displayMetrics.widthPixels
    val heightPixels = displayMetrics.heightPixels

    Rect(
      0 + styleAttributes.horizontalMargin,
      0 + styleAttributes.bottomMargin,
      widthPixels - styleAttributes.horizontalMargin,
      heightPixels - styleAttributes.bottomMargin,
    )
  }

  init {
    setDrag()
  }

  override fun cancel() {
    alertDialog.cancel()
  }

  override fun dismiss() {
    alertDialog.dismiss()
  }

  private fun setDrag() {
    if (attributes.isDraggable) {
      initDrag()
    }
  }

  protected open fun initDrag() {
    alertDialog.window?.let { window ->
      val decorView = window.decorView

      var dx: Float = 0f
      var dy: Float = 0f

      var newX: Float = 0f
      var newY: Float = 0f

      decorView.setOnTouchListener(
        fun(view: View, event: MotionEvent): Boolean {
          when (event.action) {
            MotionEvent.ACTION_DOWN -> {
              dx = view.x - event.rawX.toInt()
              dy = view.y - event.rawY.toInt()
            }

            MotionEvent.ACTION_MOVE -> {
              newX = event.rawX + dx
              newY = event.rawY + dy

              if (!isOutSideFromWindow(newX, newY, view.width, view.height)) view.animate().x(newX).y(newY).setDuration(0).start()
            }
          }
          return true
        },
      )
    }
  }

  private fun isOutSideFromWindow(dx: Float, dy: Float, width: Int, height: Int): Boolean =
    (dx < draggablePixelsRangeRect.left) || (dx + width > draggablePixelsRangeRect.right) || (dy < draggablePixelsRangeRect.top) || (dy + height > draggablePixelsRangeRect.bottom)

}
