package io.github.pknujsp.testbed.core.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference


internal class SimpleDialogStyler(
  val simpleDialogAttributes: SimpleDialogAttributes,
  @ActivityContext context: Context,
) {

  private val activityRoot = WeakReference((context as Activity).window).run {
    enqueue()
    get()!!
  }

  private companion object {
    private val density: Float = Resources.getSystem().displayMetrics.density

    private val maxBlurRadius: Float = 24 * density

    private val blurProcessor = BlurProcessor()
  }

  fun applyStyle(dialog: Dialog) {
    setOnDismissDialogListener(dialog)
    blurOrDim(dialog)

    dialog.window?.apply {
      position(this)
      spacing(this)
      //dim(this)
      background(this)

      attributes = attributes.also { attr ->
        attr.copyFrom(attr)
        size(attr)
        //blur(attr)
        //dim(attr)
      }
    }
  }

  private fun blurOrDim(attributes: WindowManager.LayoutParams) {
    if (simpleDialogAttributes.dialogType == DialogType.Fullscreen) return

    if (simpleDialogAttributes.blur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) attributes.blurBehindRadius =
      (maxBlurRadius * (simpleDialogAttributes.blurIndensity / 100f)).toInt()
  }

  private fun blurOrDim(dialog: Dialog) {
    if (simpleDialogAttributes.dialogType == DialogType.Fullscreen) return

    /**
    window.apply {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && simpleDialogAttributes.blur) addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    else clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
    }
     */

    // new
    if (simpleDialogAttributes.blur || simpleDialogAttributes.dim) {
      activityRoot.also { window ->
        /** Only available on Android 12 and above!
        window.addContentView(
        View(window.context).apply {
        id = R.id.dialog_custom_background
        background = reducedBitmap.toDrawable(window.context.resources)
        },
        ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
        )
         */
        println("MainScope")
        MainScope().launch(Dispatchers.Default) {
          println("MainScope.launched")
          val startTime = System.currentTimeMillis()
          val radius = (maxBlurRadius * (simpleDialogAttributes.blurIndensity / 100.0)).toInt()

          blurProcessor.nativeBlurOrDim(window, radius, 3.0, simpleDialogAttributes.dimIndensity).onSuccess {
            val end = "${System.currentTimeMillis() - startTime}MS 소요됨"

            if (dialog.isShowing) {
              val view = View(window.context).apply {
                id = R.id.dialog_custom_background
                background = it.toDrawable(resources)
              }
              println("${System.currentTimeMillis() - startTime}MS 소요됨")

              withContext(Dispatchers.Main) {
                //window.addContentView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
                window.decorView.draw(Canvas(it))
              }
            }
          }.onFailure {
            println("blurProcessor.blur.onFailure -> $it")
          }

          /**
          blurProcessor.blur(first, second, radius).onSuccess {
          withContext(Dispatchers.Main) {
          if (!dialog.isShowing) return@withContext

          second.addContentView(
          View(second.context).apply {
          id = R.id.dialog_custom_background
          background = it.toDrawable(resources)
          },
          ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
          )
          }
          }
           */

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
        activityRoot.run {
          (findViewById<ViewGroup>(androidx.appcompat.R.id.action_bar_root)?.parent as? ViewGroup)?.run {
            removeView(findViewById(R.id.dialog_custom_background))
          }
        }
      }
    }
  }
}
