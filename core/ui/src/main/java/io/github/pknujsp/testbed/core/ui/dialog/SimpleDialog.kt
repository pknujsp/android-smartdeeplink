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

  private var _draggablePixelsRangeRect: Rect? = null
  private val draggablePixelsRangeRect: Rect get() = _draggablePixelsRangeRect!!

  init {
    init()
  }

  override fun cancel() {
    alertDialog.cancel()
  }

  override fun dismiss() {
    alertDialog.dismiss()
  }

  protected open fun initDrag() {
    alertDialog.window?.let { window ->
      _draggablePixelsRangeRect = Resources.getSystem().run {
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

      val dragDirection = attributes.dragDirection

      val decorView = window.decorView

      var dx: Float = 0f
      var dy: Float = 0f

      var newX: Float = 0f
      var newY: Float = 0f

      decorView.setOnTouchListener(
        fun(view: View, event: MotionEvent): Boolean {
          when (event.action) {
            MotionEvent.ACTION_DOWN -> {
              dx = view.x
              dy = view.y
            }

            MotionEvent.ACTION_MOVE -> {
              val xy = filter(dx, dy, dx + event.rawX, dy + event.rawY, view.width, view.height)
              view.animate().x(xy.first).y(xy.second).setDuration(0).start()
            }
          }
          return true
        },
      )
    }
  }

  private fun init() {
    alertDialog.run {
      setCancelable(attributes.isCancelable)
      setCanceledOnTouchOutside(attributes.isCancelable)
    }

    if (attributes.isDraggable) initDrag()
  }

  private fun filter(originalX: Float, originalY: Float, newX: Float, newY: Float, width: Int, height: Int): Pair<Float, Float> {
    when (attributes.dragDirection) {
      is DragDirection.Both -> {
        val x = if (originalX < draggablePixelsRangeRect.left) draggablePixelsRangeRect.left
        else if (originalX + width > draggablePixelsRangeRect.right) draggablePixelsRangeRect.right - width
        else originalX

        val y = if (originalY < draggablePixelsRangeRect.top) draggablePixelsRangeRect.top
        else if (originalY + height > draggablePixelsRangeRect.bottom) draggablePixelsRangeRect.bottom - height
        else originalY

        return Pair(x.toFloat(), y.toFloat())
      }

      is DragDirection.Horizontal -> {
        val x = if (originalX < draggablePixelsRangeRect.left) draggablePixelsRangeRect.left
        else if (originalX + width > draggablePixelsRangeRect.right) draggablePixelsRangeRect.right - width
        else originalX

        return Pair(x.toFloat(), originalY)
      }

      is DragDirection.Vertical -> {
        val y = if (originalY < draggablePixelsRangeRect.top) draggablePixelsRangeRect.top
        else if (originalY + height > draggablePixelsRangeRect.bottom) draggablePixelsRangeRect.bottom - height
        else originalY

        return Pair(originalX, y.toFloat())
      }
    }
  }
}
