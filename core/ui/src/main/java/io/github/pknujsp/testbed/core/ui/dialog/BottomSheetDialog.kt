package io.github.pknujsp.testbed.core.ui.dialog

import IGLSurfaceView
import android.app.Dialog
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import io.github.pknujsp.testbed.core.ui.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BottomSheetDialog(
  dialog: Dialog, attributes: SimpleDialogGeneralAttributes,
  styleAttributes: SimpleDialogStyleAttributes,
  blurringViewLifeCycleListener: IGLSurfaceView? = null,
) : SimpleDialog(dialog, attributes, styleAttributes, blurringViewLifeCycleListener) {

  private val foldHistoryMap = mutableSetOf<FoldHistory>()

  private val _dialogTopInWindow = MutableStateFlow(0f)
  val dialogTopInWindow = _dialogTopInWindow.asStateFlow()

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
    val dialogView = dialog.findViewById<ViewGroup>(R.id.dialog_base_content)
    dialogView.layoutAnimationListener = object : AnimationListener {
      override fun onAnimationEnd(animation: Animation?) {
        updateDialogTopInWindow(dialogView.y)
      }

      override fun onAnimationRepeat(animation: Animation?) {
        updateDialogTopInWindow(dialogView.y)
      }

      override fun onAnimationStart(animation: Animation?) {
        updateDialogTopInWindow(dialogView.y)
      }
    }

    attributes.viewMethodsInteractWith?.run {
      dialogView?.doOnPreDraw {
        //setBottom(dialogView.y.toInt())
      }

      setOnDismissListener {
        //setBottom((originalHeight + originalY).toInt())
      }
    }

    if (attributes.isFoldable) {
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
                  updateDialogTopInWindow(newDialogViewTop)
                }
              }
            }
            return true
          },
        )
      }

    }

    MainScope().launch {
      dialogTopInWindow.collect {
        attributes.viewMethodsInteractWith?.run {
          // setBottom(it.toInt())
        }
      }
    }
  }

  private fun updateDialogTopInWindow(top: Float) {
    _dialogTopInWindow.update {
      top
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
