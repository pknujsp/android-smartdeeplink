package io.github.pknujsp.testbed.core.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.allViews
import androidx.core.view.updateLayoutParams
import io.github.pknujsp.blur.BlurProcessor
import io.github.pknujsp.testbed.core.ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


internal class SimpleDialogStyler(
  val simpleDialogStyleAttributes: SimpleDialogStyleAttributes,
  @ActivityContext context: Context,
) {

  private val activityWindow = (context as Activity).window

  private companion object {
    private val density: Float = Resources.getSystem().displayMetrics.density

    private val maxBlurRadius: Float = 16 * density

    private val blurProcessor = BlurProcessor()
  }

  fun applyStyle(dialog: Dialog) {
    setOnDismissDialogListener(dialog)
    blur(dialog)

    dialog.window?.apply {
      position(this)
      spacing(this)
      dim(this)
      background(this)

      attributes = attributes.also { attr ->
        attr.copyFrom(attr)
        size(attr)
        dim(attr)
      }
    }
  }

  private fun blur(attributes: WindowManager.LayoutParams) {
    if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) return

    if (simpleDialogStyleAttributes.blur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) attributes.blurBehindRadius =
      (maxBlurRadius * (simpleDialogStyleAttributes.blurIndensity / 100f)).toInt()
  }

  private fun blur(dialog: Dialog) {
    if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) return

    /**
    window.apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && simpleDialogAttributes.blur) addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    else clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    }
     */

    // new
    if (simpleDialogStyleAttributes.blur) {
      activityWindow.also { window ->
        /** Only available on Android 12 and above!
        window.addContentView(
        View(window.context).apply {
        id = R.id.dialog_custom_background
        background = reducedBitmap.toDrawable(window.context.resources)
        },
        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
         */


        MainScope().launch(Dispatchers.Main.immediate) {
          val radius = (maxBlurRadius * (simpleDialogStyleAttributes.blurIndensity / 100.0)).toInt()
          blurProcessor.nativeBlur(window, radius, 2.5).onSuccess {
            if (dialog.isShowing) {
              val view = View(window.context).apply {
                id = R.id.dialog_custom_background
                background = it.toDrawable(resources)
              }
              window.addContentView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
          }.onFailure {

          }
        }
      }
    }
  }

  private fun dim(window: Window) {
    if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) return

    window.apply {
      if (simpleDialogStyleAttributes.dim) addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
      else clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
  }

  private fun dim(attributes: WindowManager.LayoutParams) {
    if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) return

    if (simpleDialogStyleAttributes.dim) attributes.dimAmount = simpleDialogStyleAttributes.dimIndensity / 100f
  }

  private fun position(window: Window) {
    if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) return

    if (simpleDialogStyleAttributes.dialogType == DialogType.BottomSheet) {
      window.setGravity(android.view.Gravity.BOTTOM)
    }
  }

  private fun background(window: Window) {
    window.decorView.allViews.filter {
      it.id == android.R.id.content
    }.firstOrNull()?.also { parent ->
      if (simpleDialogStyleAttributes.dialogType != DialogType.Fullscreen) {
        window.setElevation((simpleDialogStyleAttributes.elevation - 1) * density)
        parent.elevation = simpleDialogStyleAttributes.elevation * density
      }

      simpleDialogStyleAttributes.backgroundResourceId?.run {
        parent.setBackgroundResource(this)
      } ?: run {
        parent.background = GradientDrawable().apply {
          shape = GradientDrawable.RECTANGLE
          setColor(simpleDialogStyleAttributes.backgroundColor)
          if (simpleDialogStyleAttributes.dialogType != DialogType.Fullscreen) cornerRadius = simpleDialogStyleAttributes.cornerRadius * density
        }
      }
    }
  }

  private fun spacing(window: Window) {
    if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) return

    window.decorView.allViews.filter {
      it.id == android.R.id.content
    }.firstOrNull()?.also { parent ->
      parent.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        if (simpleDialogStyleAttributes.dialogType == DialogType.BottomSheet) bottomMargin =
          simpleDialogStyleAttributes.bottomMargin * density.toInt()
        leftMargin = simpleDialogStyleAttributes.horizontalMargin * density.toInt()
        rightMargin = simpleDialogStyleAttributes.horizontalMargin * density.toInt()
      }
    }

  }

  private fun size(attributes: WindowManager.LayoutParams) {
    attributes.apply {
      width =
        if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) ViewGroup.LayoutParams.MATCH_PARENT else simpleDialogStyleAttributes.layoutWidth
      height =
        if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) ViewGroup.LayoutParams.MATCH_PARENT else simpleDialogStyleAttributes.layoutHeight
    }
  }


  private fun setOnDismissDialogListener(dialog: Dialog) {
    dialog.setOnDismissListener {
      if (simpleDialogStyleAttributes.blur && simpleDialogStyleAttributes.dialogType != DialogType.Fullscreen) {
        blurProcessor.cancel()
        activityWindow.run {
          (findViewById<ViewGroup>(androidx.appcompat.R.id.action_bar_root)?.parent as? ViewGroup)?.run {
            removeView(findViewById(R.id.dialog_custom_background))
          }
        }
      }
    }
  }
}
