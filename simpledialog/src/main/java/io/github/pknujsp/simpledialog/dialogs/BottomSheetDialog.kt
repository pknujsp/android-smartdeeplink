package io.github.pknujsp.simpledialog.dialogs

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
import io.github.pknujsp.simpledialog.R
import io.github.pknujsp.simpledialog.attrs.SimpleDialogGeneralAttributes
import io.github.pknujsp.simpledialog.attrs.SimpleDialogStyleAttributes
import io.github.pknujsp.simpledialog.blur.view.IGLSurfaceView
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class BottomSheetDialog(
  dialog: Dialog, attributes: SimpleDialogGeneralAttributes,
  styleAttributes: SimpleDialogStyleAttributes,
  blurringViewLifeCycleListener: IGLSurfaceView? = null,
) : SimpleDialog(dialog, attributes, styleAttributes, blurringViewLifeCycleListener) {

  private val foldHistoryMap = mutableSetOf<FoldHistory>()

  private val dialogDiffHeight = MutableStateFlow(0f)

  private var job: Job? = null

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
        updateDialogDiffHeight(dialogView.y)
      }

      override fun onAnimationRepeat(animation: Animation?) {
        updateDialogDiffHeight(dialogView.y)
      }

      override fun onAnimationStart(animation: Animation?) {
        updateDialogDiffHeight(dialogView.y)
      }
    }

    attributes.viewMethodsInteractWith?.run {
      job = MainScope().launch {
        dialogDiffHeight.collect {
          setTranslationY(it)
        }
      }
    }

    dialogView?.doOnPreDraw {
      //updateDialogDiffHeight(-dialogView.height.toFloat())
    }

    attributes.viewMethodsInteractWith?.run {
      setOnDismissListener {
        attributes.viewMethodsInteractWith?.run {
          //job?.cancel(CancellationException("Dialog dismissed"))
          //setTranslationY(-dialogDiffHeight.value)
        }
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
                  //updateDialogDiffHeight(newDialogViewTop - originalDialogViewTopInWindow)
                }
              }
            }
            return true
          },
        )
      }

    }


  }

  private fun updateDialogDiffHeight(diff: Float) {
    dialogDiffHeight.value = diff
  }

  private fun fullyFold(animate: Boolean = true, animationDuration: Long = 100L, startDelayDuration: Long = 0L) {
    fold(0, animate, animationDuration, startDelayDuration)
  }

  private fun fullExpand(animate: Boolean = true, animationDuration: Long = 100L, startDelayDuration: Long = 0L) {
    fold(100, animate, animationDuration, startDelayDuration)
  }

  private fun fold(
    @IntRange(from = 0, to = 100) foldPercent: Int, animate: Boolean = true, animationDuration: Long = 100L,
    startDelayDuration:
    Long = 0L,
  ) {
    dialog.findViewById<ViewGroup>(R.id.dialog_base_content)?.let { dialogView ->
      dialogView.animate().scaleY(foldPercent / 100f).setDuration(animationDuration).setStartDelay(startDelayDuration).start()
    }
  }

  private fun fold(show: Boolean, @IdRes viewId: Int, animate: Boolean = true, animationDuration: Long = 100L, startDelayDuration: Long = 0L) {
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
