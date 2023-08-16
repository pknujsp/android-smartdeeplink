package io.github.pknujsp.simpledialog.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnCancelListener
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.core.view.allViews
import androidx.core.view.doOnPreDraw
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import io.github.pknujsp.simpledialog.R
import io.github.pknujsp.simpledialog.attrs.DragDirection
import io.github.pknujsp.simpledialog.attrs.SimpleDialogGeneralAttributes
import io.github.pknujsp.simpledialog.attrs.SimpleDialogStyleAttributes
import io.github.pknujsp.simpledialog.blur.view.IGLSurfaceView
import io.github.pknujsp.simpledialog.interfaces.IDialogMover

open class SimpleDialog(
  context: Context,
  @StyleRes themeResId: Int,
  private val attributes: SimpleDialogGeneralAttributes,
  private val styleAttributes: SimpleDialogStyleAttributes,
  private var blurringViewLifeCycleListener: IGLSurfaceView? = null,
) : Dialog(context, themeResId), IDialogMover {

  private var _draggablePixelsRangeRect: RectF? = null
  private val draggablePixelsRangeRect: RectF get() = _draggablePixelsRangeRect!!

  private var _dialogViewFirstPixels: Pair<Float, Float>? = null
  private val dialogViewFirstPixels: Pair<Float, Float> get() = _dialogViewFirstPixels!!

  private var _dialogView: View? = null
  private val dialogView: View get() = _dialogView!!

  private val onShowListener: MutableList<DialogInterface.OnShowListener> = mutableListOf()
  private val onCancelListener: MutableList<OnCancelListener> = mutableListOf()
  private val onDismissListener: MutableList<DialogInterface.OnDismissListener> = mutableListOf()

  init {
    init()
  }

  private fun init() {
    initTouchEvent()

    super.setOnShowListener { onShowListener.forEach { action -> action.onShow(it) } }
    super.setOnCancelListener { onCancelListener.forEach { action -> action.onCancel(it) } }
    super.setOnDismissListener {
      onDismissListener.forEach { action -> action.onDismiss(it) }
      blurringViewLifeCycleListener?.onPause()
      _dialogView = null
      _draggablePixelsRangeRect = null
      _dialogViewFirstPixels = null
    }
  }

  override fun create() {
    super.create()
  }

  override fun show() {
    super.show()
  }

  override fun setOnShowListener(listener: DialogInterface.OnShowListener?) {
    if (listener != null) onShowListener.add(listener)
  }

  override fun setOnCancelListener(listener: OnCancelListener?) {
    if (listener != null) onCancelListener.add(listener)
  }

  override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
    if (listener != null) onDismissListener.add(listener)
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun initTouchEvent() {
    (window?.decorView as? ViewGroup)?.let { decorView ->
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

        setCancelable(attributes.isCancelable)
        setCanceledOnTouchOutside(attributes.isCanceledOnTouchOutside)

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
