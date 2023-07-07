package io.github.pknujsp.testbed.core.ui.dialog

import IGLSurfaceView
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnCancelListener
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
  protected var blurringViewLifeCycleListener: IGLSurfaceView? = null,
) : DialogInterface by dialog, IDialogMover {


  private var _draggablePixelsRangeRect: RectF? = null
  private val draggablePixelsRangeRect: RectF get() = _draggablePixelsRangeRect!!

  private var _dialogViewFirstPixels: Pair<Float, Float>? = null
  private val dialogViewFirstPixels: Pair<Float, Float> get() = _dialogViewFirstPixels!!

  private var _dialogView: View? = null
  private val dialogView: View get() = _dialogView!!

  private var onShowListener: MutableList<DialogInterface.OnShowListener> = mutableListOf()
  private var onCancelListener: MutableList<OnCancelListener> = mutableListOf()
  private var onDismissListener: MutableList<DialogInterface.OnDismissListener> = mutableListOf()

  init {
    init()
  }

  private fun init() {
    initTouchEvent()

    dialog.setOnShowListener {
      onShowListener.forEach { it.onShow(dialog) }
    }
    dialog.setOnCancelListener {
      onCancelListener.forEach { it.onCancel(dialog) }
    }
    dialog.setOnDismissListener {
      blurringViewLifeCycleListener?.onPause()
      onDismissListener.forEach { it.onDismiss(dialog) }
      _dialogView = null
      _draggablePixelsRangeRect = null
      _dialogViewFirstPixels = null
    }
  }

  override fun cancel() {
    dialog.cancel()
  }

  override fun dismiss() {
    dialog.dismiss()
  }

  fun setOnShowListener(listener: DialogInterface.OnShowListener) {
    onShowListener.add(listener)
  }

  fun setOnCancelListener(listener: OnCancelListener) {
    onCancelListener.add(listener)
  }

  fun setOnDismissListener(listener: DialogInterface.OnDismissListener) {
    onDismissListener.add(listener)
  }
  

  fun isShowing(): Boolean = dialog.isShowing

  @SuppressLint("ClickableViewAccessibility")
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

        dialog.setCancelable(attributes.isCancelable)
        dialog.setCanceledOnTouchOutside(attributes.isCanceledOnTouchOutside)

        if (attributes.isDraggable) dialogView.setOnTouchListener(
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

        val dialogOnTouchListener = View.OnTouchListener { _, event ->
          when (event.action) {
            MotionEvent.ACTION_DOWN -> {
              dismiss()
            }
          }
          true
        }

        if (attributes.isCanceledOnTouchOutside) {
          blurringViewLifeCycleListener?.setOnTouchListener(dialogOnTouchListener) ?: (dialogView.parent as View).setOnTouchListener(
            dialogOnTouchListener,
          )
        }
      }

    }
  }

  private fun isTouchedOutside(event: MotionEvent): Boolean =
    event.rawX < dialogView.x || event.rawX > dialogView.x + dialogView.width || event.rawY < dialogView.y || event.rawY > dialogView.y + dialogView.height


  private fun filter(newX: Float, newY: Float): Pair<Float, Float> = when (attributes.dragDirection) {
    is DragDirection.Both -> Pair(filterX(newX), filterY(newY))
    is DragDirection.Horizontal -> Pair(filterX(newX), dialogViewFirstPixels.second)
    is DragDirection.Vertical -> Pair(dialogViewFirstPixels.first, filterY(newY))
  }


  private fun filterX(newX: Float): Float = if (!attributes.isRestrictViewsFromOffWindow) newX
  else if (newX < draggablePixelsRangeRect.left) draggablePixelsRangeRect.left
  else if (newX > draggablePixelsRangeRect.right) draggablePixelsRangeRect.right
  else newX

  private fun filterY(newY: Float): Float = if (!attributes.isRestrictViewsFromOffWindow) newY
  else if (newY < draggablePixelsRangeRect.top) draggablePixelsRangeRect.top
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
