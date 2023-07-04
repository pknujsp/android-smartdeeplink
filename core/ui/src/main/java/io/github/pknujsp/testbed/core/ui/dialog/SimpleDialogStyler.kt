package io.github.pknujsp.testbed.core.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.LruCache
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.allViews
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import io.github.pknujsp.blur.view.BlurringView
import io.github.pknujsp.testbed.core.ui.R
import kotlin.properties.Delegates


internal class SimpleDialogStyler(
  val simpleDialogStyleAttributes: SimpleDialogStyleAttributes,
  @ActivityContext context: Context,
) {

  private val activityWindow = (context as Activity).window
  private var mainContentView: FrameLayout by Delegates.notNull()
  private var compatContentView: FrameLayout by Delegates.notNull()

  private companion object {
    private val density: Float = Resources.getSystem().displayMetrics.density

    private val maxBlurRadius: Float = 16 * density

    private val drawableCache = LruCache<BackgroundDrawableInfo, Drawable>(10)
  }

  private data class BackgroundDrawableInfo(
    val leftCornerRadius: Float,
    val rightCornerRadius: Float,
    val topCornerRadius: Float,
    val bottomCornerRadius: Float,
    val isShowModal: Boolean,
    @ColorInt val backgroundColor: Int,
    @DrawableRes val customBackgroundDrawableId: Int?,
  )

  fun applyStyle(dialog: Dialog) {
    setOnDismissDialogListener(dialog)
    dialog.window?.apply {
      compatContentView = getContentView(this, R.id.dialog_custom_background)
      mainContentView = getContentView(this, android.R.id.content)
      setBlur(dialog)

      ((decorView as ViewGroup).children.first() as LinearLayout).gravity = Gravity.CENTER
      setBackgroundAndModal(compatContentView)
      setMainContentViewLayout()
      setCompatContentViewLayout()
      setDim(this)

      attributes = attributes.also { attr ->
        attr.copyFrom(attr)
        setWindowLayout(attr)
        setDim(attr)
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.S)
  private fun setBlur(attributes: WindowManager.LayoutParams) {
    if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) return

    if (simpleDialogStyleAttributes.blur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) attributes.blurBehindRadius =
      (maxBlurRadius * (simpleDialogStyleAttributes.blurIndensity / 100f)).toInt()
  }

  private fun setBlur(dialog: Dialog) {
    if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) return

    if (simpleDialogStyleAttributes.blur) {
      val radius = (maxBlurRadius * (simpleDialogStyleAttributes.blurIndensity / 100.0)).toInt()
      val blurringView = BlurringView(activityWindow.context, 1.0, radius)
      dialog.setOnShowListener {
        blurringView.onResume()
      }

      mainContentView.addView(blurringView, 0, blurringView.layoutParams)

    }
  }

  private fun setDim(window: Window) {
    if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) return
    if (simpleDialogStyleAttributes.dim) window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
  }

  private fun setDim(attributes: WindowManager.LayoutParams) {
    if (simpleDialogStyleAttributes.dialogType == DialogType.Fullscreen) return
    if (simpleDialogStyleAttributes.dim) attributes.dimAmount = simpleDialogStyleAttributes.dimIndensity / 100f
  }

  private fun getContentView(window: Window, @IdRes id: Int): FrameLayout = window.decorView.allViews.filter {
    it.id == id
  }.first() as FrameLayout


  private fun setBackgroundAndModal(contentView: FrameLayout) {
    contentView.apply {
      val backgroundDrawableInfo = BackgroundDrawableInfo(
        leftCornerRadius = simpleDialogStyleAttributes.cornerRadius * density,
        rightCornerRadius = simpleDialogStyleAttributes.cornerRadius * density,
        topCornerRadius = simpleDialogStyleAttributes.cornerRadius * density,
        bottomCornerRadius = simpleDialogStyleAttributes.cornerRadius * density,
        isShowModal = simpleDialogStyleAttributes.isShowModalPoint,
        backgroundColor = simpleDialogStyleAttributes.backgroundColor,
        customBackgroundDrawableId = simpleDialogStyleAttributes.backgroundResourceId,
      )

      drawableCache.get(backgroundDrawableInfo)?.let { drawable ->
        background = drawable
      } ?: run {
        if (simpleDialogStyleAttributes.backgroundResourceId == null) {
          val drawables = mutableListOf<Drawable>()

          drawables.add(
            CornersDrawable(
              simpleDialogStyleAttributes.backgroundColor,
              simpleDialogStyleAttributes.cornerRadius * density, simpleDialogStyleAttributes.cornerRadius * density,
              simpleDialogStyleAttributes.cornerRadius * density, simpleDialogStyleAttributes.cornerRadius * density,
            ).apply {
              this.setBounds(0, (12 * density).toInt(), width, height)
            },
          )

          var iconHeight = 0
          if (simpleDialogStyleAttributes.isShowModalPoint) {
            iconHeight = (12 * density).toInt()

            val icon =
              ResourcesCompat.getDrawable(context.resources, simpleDialogStyleAttributes.customModalViewId ?: R.drawable.icon_more_edited, null)!!
            val diffPx = icon.intrinsicHeight - iconHeight
            val iconWidth = icon.intrinsicWidth - diffPx

            drawables.add(icon)
          }

          background = LayerDrawable(drawables.toTypedArray()).apply {
            if (simpleDialogStyleAttributes.isShowModalPoint) setLayerGravity(1, Gravity.CENTER_HORIZONTAL or Gravity.TOP)
            setLayerInsetTop(0, iconHeight)
            drawableCache.put(backgroundDrawableInfo, this)
          }
        } else {
          setBackgroundResource(simpleDialogStyleAttributes.backgroundResourceId!!)
        }
      }

      if (simpleDialogStyleAttributes.dialogType != DialogType.Fullscreen) {
        elevation = simpleDialogStyleAttributes.elevation * density
      }
    }
  }

  private fun setMainContentViewLayout() {
    mainContentView.apply {
      updateLayoutParams<ViewGroup.MarginLayoutParams> {
        width = ViewGroup.LayoutParams.MATCH_PARENT
        height = ViewGroup.LayoutParams.MATCH_PARENT
      }
      updateLayoutParams<LinearLayout.LayoutParams> {
        gravity = Gravity.CENTER
      }
    }
  }

  private fun setCompatContentViewLayout() {
    compatContentView.apply {
      updateLayoutParams<ViewGroup.MarginLayoutParams> {
        if (simpleDialogStyleAttributes.dialogType != DialogType.Fullscreen) {
          if (simpleDialogStyleAttributes.dialogType == DialogType.BottomSheet) bottomMargin =
            simpleDialogStyleAttributes.bottomMargin * density.toInt()
          leftMargin = simpleDialogStyleAttributes.horizontalMargin * density.toInt()
          rightMargin = simpleDialogStyleAttributes.horizontalMargin * density.toInt()
        }

        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
      }
    }
  }

  private fun setWindowLayout(attributes: WindowManager.LayoutParams) {
    attributes.apply {
      width = ViewGroup.LayoutParams.MATCH_PARENT
      height = ViewGroup.LayoutParams.MATCH_PARENT
    }
  }


  private fun setOnDismissDialogListener(dialog: Dialog) {

  }


}
