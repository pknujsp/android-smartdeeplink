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
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.allViews
import androidx.core.view.updateLayoutParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import java.lang.ref.SoftReference


internal class SimpleDialogStyler(
  val simpleDialogAttributes: SimpleDialogAttributes,
  @ActivityContext context: Context,
) {

  private val activityRoot: SoftReference<Pair<View, Window>>? = (context as? Activity)?.let { activity ->
    val window = activity.window
    val contentView = activity.findViewById<ViewGroup>(android.R.id.content)
    SoftReference(contentView to activity.window)
  }

  private companion object {
    private val density: Float = Resources.getSystem().displayMetrics.density

    @SuppressLint("InternalInsetResource", "DiscouragedApi") private val navigationBarHeight: Int = Resources.getSystem().run {
      getDimensionPixelSize(getIdentifier("navigation_bar_height", "dimen", "android"))
    }
    private val maxBlurRadius: Float = 28 * density

    private val blurProcessor = BlurProcessor()
  }

  fun applyStyle(dialog: Dialog) {
    setOnDismissDialogListener(dialog)
    dialog.window?.apply {
      position(this)
      spacing(this)
      blur(dialog)
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

  private fun blur(dialog: Dialog) {
    if (simpleDialogAttributes.dialogType == DialogType.Fullscreen) return

    /**
    window.apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && simpleDialogAttributes.blur) addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    else clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    }
     */

    // new
    if (simpleDialogAttributes.blur) {
      activityRoot?.get()?.run {

        /**
         * window.addContentView(
        View(window.context).apply {
        id = R.id.dialog_custom_background
        background = reducedBitmap.toDrawable(window.context.resources)
        },
        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
         */

        (MainScope() + Job()).launch(Dispatchers.Default) {
          blurProcessor.blur(first, second, (maxBlurRadius * (simpleDialogAttributes.blurIndensity / 100.0)).toInt()).onSuccess {
            withContext(Dispatchers.Main) {
              second.addContentView(
                View(second.context).apply {
                  id = R.id.dialog_custom_background
                  background = it.toDrawable(resources)
                },
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
              )
            }
          }
        }
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


  private fun setOnDismissDialogListener(dialog: Dialog) {
    dialog.setOnDismissListener {
      if (simpleDialogAttributes.blur && simpleDialogAttributes.dialogType != DialogType.Fullscreen) {
        blurProcessor.cancel()
        activityRoot?.get()?.run {
          (second.findViewById<ViewGroup>(androidx.appcompat.R.id.action_bar_root)?.parent as? ViewGroup)?.run {
            removeView(findViewById(R.id.dialog_custom_background))
          }
        }

        activityRoot?.clear()
      }
    }
  }
}
