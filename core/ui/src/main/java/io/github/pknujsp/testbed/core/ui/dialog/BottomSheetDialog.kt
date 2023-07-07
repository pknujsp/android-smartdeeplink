package io.github.pknujsp.testbed.core.ui.dialog

import IGLSurfaceView
import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import io.github.pknujsp.testbed.core.ui.R

class BottomSheetDialog(
  dialog: Dialog, attributes: SimpleDialogGeneralAttributes,
  styleAttributes: SimpleDialogStyleAttributes,
  blurringViewLifeCycleListener: IGLSurfaceView? = null,
) : SimpleDialog(dialog, attributes, styleAttributes, blurringViewLifeCycleListener) {

  private val foldHistoryMap = mutableSetOf<FoldHistory>()

  private companion object {
    val minHeight = SimpleDialogStyleAttributes.modalIconHeight * 1.5f
  }

  private data class FoldHistory(
    @IdRes val viewId: Int,
    val show: Boolean,
  )

  init {
    init()
  }

  private fun init() {
    if (attributes.isFoldable) {
      val dialogView = dialog.findViewById<ViewGroup>(R.id.dialog_base_content)
      dialogView?.doOnPreDraw {

        val originalDialogViewTopInWindow = dialogView.y
        val originalDialogViewBottomInWindow = dialogView.y + dialogView.height
        val originalDialogViewHeight = dialogView.height

        var firstDownY = 0f
        var firstDialogViewY = 0f

        dialogView.setOnTouchListener(
          fun(view: View, event: MotionEvent): Boolean {
            when (event.action) {
              MotionEvent.ACTION_DOWN -> {
                firstDownY = event.rawY
                firstDialogViewY = view.y
              }

              MotionEvent.ACTION_MOVE -> {
                if (event.rawY < originalDialogViewTopInWindow) return true

                val moveY = event.rawY - firstDownY
                val newDialogViewTop = firstDialogViewY + moveY

                if (newDialogViewTop in (originalDialogViewTopInWindow..(originalDialogViewBottomInWindow - minHeight))) {
                  view.top = newDialogViewTop.toInt()
                }
              }
            }
            return true
          },
        )
      }

    }
  }

  fun fullyFold(animate: Boolean = true, animationDuration: Long = 100L, startDelayDuration: Long = 0L) {
    fold(0, animate, animationDuration, startDelayDuration)
  }

  fun fullExpand(animate: Boolean = true, animationDuration: Long = 100L, startDelayDuration: Long = 0L) {
    fold(100, animate, animationDuration, startDelayDuration)
  }

  fun fold(@IntRange(from = 0, to = 100) foldPercent: Int, animate: Boolean = true, animationDuration: Long = 100L, startDelayDuration: Long = 0L) {
    dialog.findViewById<ViewGroup>(R.id.dialog_base_content)?.let { dialogView ->
      dialogView.animate().scaleY(foldPercent / 100f).setDuration(animationDuration).setStartDelay(startDelayDuration).start()
    }
  }

  fun fold(show: Boolean, @IdRes viewId: Int, animate: Boolean = true, animationDuration: Long = 100L, startDelayDuration: Long = 0L) {
    if (foldHistoryMap.contains(FoldHistory(viewId = viewId, show = show))) return

    dialog.findViewById<ViewGroup>(R.id.dialog_base_content)?.let { dialogView ->
      val mainContentViewTop = dialogView.children.first().y
      val viewY = dialogView.findViewById<View>(viewId).run {
        y + if (show) height else 0
      }
      val scale = (viewY - mainContentViewTop) / dialogView.height

      foldHistoryMap.add(FoldHistory(viewId = viewId, show = show))

      dialogView.animate().scaleY(scale).setDuration(animationDuration).setStartDelay(startDelayDuration).start()
    }
  }
}
