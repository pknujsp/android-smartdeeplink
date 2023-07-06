package io.github.pknujsp.testbed.core.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.allViews
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import io.github.pknujsp.testbed.core.ui.R

abstract class SimpleDialog(
  protected val dialog: Dialog,
  protected val attributes: SimpleDialogGeneralAttributes,
  protected val styleAttributes: SimpleDialogStyleAttributes,
) : DialogInterface by dialog, IDialogMover {

  private var _draggablePixelsRangeRect: RectF? = null
  private val draggablePixelsRangeRect: RectF get() = _draggablePixelsRangeRect!!

  private var _dialogViewFirstPixels: Pair<Float, Float>? = null
  private val dialogViewFirstPixels: Pair<Float, Float> get() = _dialogViewFirstPixels!!

  private var _dialogView: View? = null
  private val dialogView: View get() = _dialogView!!

  init {
    init()
  }

  private fun init() {
    initTouchEvent()
  }

  override fun cancel() {
    dialog.cancel()
  }

  override fun dismiss() {
    dialog.dismiss()
  }

  protected open fun initTouchEvent() {
    (dialog.window?.decorView as? ViewGroup)?.let { decorView ->
      decorView.doOnPreDraw {
        _dialogView = decorView.allViews.firstOrNull { it.id == R.id.dialog_base_content }
        _draggablePixelsRangeRect = RectF().apply {
          left = (decorView.left + dialogView.marginLeft).toFloat()
          top = (decorView.top - dialogView.marginTop).toFloat()
          right = (decorView.right - dialogView.width - dialogView.marginRight).toFloat()
          bottom = (decorView.bottom - dialogView.height - dialogView.marginBottom).toFloat()
        }
        dialogView.isClickable = true
        _dialogViewFirstPixels = Pair(dialogView.x, dialogView.y)

        var firstDownX: Float = 0f
        var firstDownY: Float = 0f
        var firstDialogViewX = 0f
        var firstDialogViewY = 0f

        val isDraggable = attributes.isDraggable
        val isCancelable = attributes.isCancelable

        dialogView.setOnTouchListener(
          fun(view: View, event: MotionEvent): Boolean {
            when (event.action) {
              MotionEvent.ACTION_DOWN -> {
                firstDownX = event.rawX
                firstDownY = event.rawY
                firstDialogViewX = view.x
                firstDialogViewY = view.y
              }

              MotionEvent.ACTION_UP -> {
                if (isCancelable && !(event.rawX > view.x && event.rawX < view.x + view.width && event.rawY > view.y && event.rawY < view.y + view.height)) dismiss()
              }

              MotionEvent.ACTION_MOVE -> {
                if (isDraggable) {
                  val xy = filter(firstDialogViewX + (-firstDownX + event.rawX), firstDialogViewY + (-firstDownY + event.rawY))
                  view.animate().x(xy.first).y(xy.second).setDuration(0).start()
                }
              }
            }
            return true
          },
        )
      }

    }
  }


  private fun filter(newX: Float, newY: Float): Pair<Float, Float> = when (attributes.dragDirection) {
    is DragDirection.Both -> Pair(filterX(newX), filterY(newY))
    is DragDirection.Horizontal -> Pair(filterX(newX), dialogViewFirstPixels.second)
    is DragDirection.Vertical -> Pair(dialogViewFirstPixels.first, filterY(newY))
  }


  private fun filterX(newX: Float): Float = if (newX < draggablePixelsRangeRect.left) draggablePixelsRangeRect.left
  else if (newX > draggablePixelsRangeRect.right) draggablePixelsRangeRect.right
  else newX

  private fun filterY(newY: Float): Float = if (newY < draggablePixelsRangeRect.top) draggablePixelsRangeRect.top
  else if (newY > draggablePixelsRangeRect.bottom) draggablePixelsRangeRect.bottom
  else newY

  override fun move(targetX: Float, targetY: Float, animationDuration: Long, startDelayDuration: Long) {
    dialogView.animate().x(filterX(dialogView.x + (-dialogView.x + targetX))).y(
      filterY(dialogView.y + (-dialogView.y + targetY)),
    ).setDuration(animationDuration).setStartDelay(startDelayDuration).start()
  }

  override fun moveToFirstPosition(animationDuration: Long, startDelayDuration: Long) {
    dialogView.animate().x(dialogViewFirstPixels.first).y(dialogViewFirstPixels.second).setDuration(animationDuration)
      .setStartDelay(startDelayDuration).start()
  }

  override fun moveX(x: Float, animationDuration: Long, startDelayDuration: Long) {
    dialogView.animate().x(filterX(x)).setDuration(animationDuration).setStartDelay(startDelayDuration).start()
  }

  override fun moveY(y: Float, animationDuration: Long, startDelayDuration: Long) {
    dialogView.animate().y(filterY(y)).setDuration(animationDuration).setStartDelay(startDelayDuration).start()
  }
  
}
