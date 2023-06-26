package io.github.pknujsp.testbed.core.ui

import android.annotation.SuppressLint
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
import androidx.core.view.allViews
import androidx.core.view.updateLayoutParams
import java.lang.ref.WeakReference


internal class SimpleDialogStyler(
  val simpleDialogAttributes: SimpleDialogAttributes,
  @ActivityContext context: Context,
) {

  private val activityRoot: WeakReference<Pair<View, Window>>? = (context as? Activity)?.let { activity ->
    val window = activity.window
    val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
    WeakReference(contentView to activity.window)
  }

  private companion object {
    private val density: Float = Resources.getSystem().displayMetrics.density

    @SuppressLint("InternalInsetResource", "DiscouragedApi") private val navigationBarHeight: Int = Resources.getSystem().run {
      getDimensionPixelSize(getIdentifier("navigation_bar_height", "dimen", "android"))
    }
    private val maxBlurRadius: Float = 33 * density

    private val blurProcessor = BlurProcessor()
  }

  fun applyStyle(dialog: Dialog) {
    dialog.window?.apply {
      position(this)
      spacing(this)
      blur(this)
      dim(this)
      background(this)

      attributes = attributes.also { attr ->
        attr.copyFrom(attr)
        size(attr)
        //blur(attr)
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

    /**
    window.apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && simpleDialogAttributes.blur) addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    else clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    }
     */

    // new
    if (simpleDialogAttributes.blur) {
      activityRoot?.get()?.also {
        blurProcessor.blur(it.first, it.second, (maxBlurRadius * (simpleDialogAttributes.blurIndensity / 100.0)).toInt())
      }
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


  private fun compatBlur(window: Window) {

  }
}
