package io.github.pknujsp.testbed.core.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

abstract class SimpleDialog(
  val dialog: Dialog,
  val attributes: SimpleDialogGeneralAttributes, val styleAttributes: SimpleDialogStyleAttributes,
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

  override fun cancel() {
    dialog.cancel()
  }

  override fun dismiss() {
    dialog.dismiss()
  }

  protected open fun initDrag() {
    dialog.window?.decorView?.doOnPreDraw {
      (dialog.window?.decorView as? ViewGroup)?.also { decorView ->
        (decorView.children.first() as? ViewGroup)?.children?.filter { it is FrameLayout }?.first()?.also { dialogView ->

          _dialogView = dialogView

          _draggablePixelsRangeRect = RectF().apply {
            left = (decorView.left + dialogView.marginLeft).toFloat()
            top = (decorView.top - dialogView.marginTop).toFloat()
            right = (decorView.right - dialogView.width - dialogView.marginRight).toFloat()
            bottom = (decorView.bottom - dialogView.height - dialogView.marginBottom).toFloat()
          }
          _dialogViewFirstPixels = Pair(dialogView.x, dialogView.y)

          if (!attributes.isDraggable) return@doOnPreDraw

          val decorViewWidth = decorView.width
          val decorViewHeight = decorView.height

          dialogView.isClickable = true

          var firstDownX: Float = 0f
          var firstDownY: Float = 0f
          var firstDialogViewX = 0f
          var firstDialogViewY = 0f

          var newX: Float = 0f
          var newY: Float = 0f

          dialogView.setOnTouchListener(
            fun(view: View, event: MotionEvent): Boolean {
              when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                  firstDownX = event.rawX
                  firstDownY = event.rawY
                  firstDialogViewX = view.x
                  firstDialogViewY = view.y
                }

                MotionEvent.ACTION_MOVE -> {
                  val xy = filter(firstDialogViewX + (-firstDownX + event.rawX), firstDialogViewY + (-firstDownY + event.rawY))
                  view.animate().x(xy.first).y(xy.second).setDuration(0).start()
                }
              }
              return true
            },
          )
        }
      }
    }
  }

  private fun init() {
    dialog.run {
      setCancelable(attributes.isCancelable)
      setCanceledOnTouchOutside(attributes.isCancelable)
    }
    initDrag()
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
