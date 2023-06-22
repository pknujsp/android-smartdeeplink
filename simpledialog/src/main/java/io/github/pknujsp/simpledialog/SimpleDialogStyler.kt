package io.github.pknujsp.simpledialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.view.allViews
import androidx.core.view.updateLayoutParams

private class SimpleDialogStyler(private val simpleDialogAttributes: SimpleDialogAttributes, private val dialog: Dialog) {
  private companion object {
    private val density: Float = Resources.getSystem().displayMetrics.density

    @SuppressLint("InternalInsetResource", "DiscouragedApi") private val navigationBarHeight: Int = Resources.getSystem().run {
      getDimensionPixelSize(getIdentifier("navigation_bar_height", "dimen", "android"))
    }
    val maxBlurRadius: Float = 24 * density
  }

  operator fun invoke() {
    dialog.window?.apply {
      position(this)
      spacing(this)
      blur(this)
      dim(this)
      background(this)

      attributes = attributes.also { attr ->
        attr.copyFrom(attr)
        size(attr)
        blur(attr)
        dim(attr)
      }
    }
  }

  private fun blur(attributes: WindowManager.LayoutParams) {
    if (simpleDialogAttributes.dialogType == DialogType.Fullscreen) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && simpleDialogAttributes.blur) attributes.blurBehindRadius =
      (maxBlurRadius * (simpleDialogAttributes.blurIndensity / 100f)).toInt()
  }

  private fun blur(window: Window) {
    if (simpleDialogAttributes.dialogType == DialogType.Fullscreen) return

    window.apply {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && simpleDialogAttributes.blur) addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
      else clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    }
  }

  private fun dim(window: Window) {
    if (simpleDialogAttributes.dialogType == DialogType.Fullscreen) return

    window.apply {
      if (simpleDialogAttributes.dim) addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
      else clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
  }

  private fun dim(attributes: WindowManager.LayoutParams) {
    if (simpleDialogAttributes.dialogType == DialogType.Fullscreen) return

    if (simpleDialogAttributes.dim) attributes.dimAmount = simpleDialogAttributes.dimIndensity / 100f
  }

  private fun position(window: Window) {
    if (simpleDialogAttributes.dialogType == DialogType.Fullscreen) return

    if (simpleDialogAttributes.dialogType == DialogType.BottomSheet) {
      window.setGravity(android.view.Gravity.BOTTOM)
    }
  }

  private fun background(window: Window) {
    window.decorView.allViews.filter {
      it.id == android.R.id.content
    }.firstOrNull()?.also { parent ->
      if (simpleDialogAttributes.dialogType != DialogType.Fullscreen) parent.elevation = simpleDialogAttributes.elevation * density

      simpleDialogAttributes.backgroundResourceId?.run {
        parent.setBackgroundResource(this)
      } ?: run {
        parent.background = GradientDrawable().apply {
          shape = GradientDrawable.RECTANGLE
          setColor(simpleDialogAttributes.backgroundColor)
          if (simpleDialogAttributes.dialogType != DialogType.Fullscreen) cornerRadius = simpleDialogAttributes.cornerRadius * density
        }
      }
    }
  }

  private fun spacing(window: Window) {
    if (simpleDialogAttributes.dialogType == DialogType.Fullscreen) return

    window.decorView.allViews.filter {
      it.id == android.R.id.content
    }.firstOrNull()?.also { parent ->
      parent.updateLayoutParams<ViewGroup.MarginLayoutParams> {
        if (simpleDialogAttributes.dialogType == DialogType.BottomSheet) bottomMargin = simpleDialogAttributes.bottomMargin * density.toInt()
        leftMargin = simpleDialogAttributes.horizontalMargin * density.toInt()
        rightMargin = simpleDialogAttributes.horizontalMargin * density.toInt()
      }
    }

  }

  private fun size(attributes: WindowManager.LayoutParams) {
    attributes.apply {
      width =
        if (simpleDialogAttributes.dialogType == DialogType.Fullscreen) ViewGroup.LayoutParams.MATCH_PARENT else simpleDialogAttributes.layoutWidth
      height =
        if (simpleDialogAttributes.dialogType == DialogType.Fullscreen) ViewGroup.LayoutParams.MATCH_PARENT else simpleDialogAttributes.layoutHeight
    }
  }
}

internal fun Dialog.theme(simpleDialogAttributes: SimpleDialogAttributes) {
  SimpleDialogStyler(simpleDialogAttributes, this).invoke()
}
