package io.github.pknujsp.blur

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.FrameLayout
import io.github.pknujsp.blur.BlurUtils.getCoordinatesInWindow
import io.github.pknujsp.blur.BlurUtils.toBitmap
import io.github.pknujsp.coroutineext.launchSafely
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlin.properties.Delegates

class BlurringView private constructor(context: Context) : View(context), DirectBlurListener {
  private var resizeRatio by Delegates.notNull<Double>()
  private var radius by Delegates.notNull<Int>()

  private var initialized = false

  private var contentView: View? = null
  private var blurredBitmap: Bitmap? = null
  private var window: Window? = null

  private val originalCoordinatesRect: Rect = Rect(0, 0, 0, 0)
  private val dstCoordinatesRect: Rect = Rect(0, 0, 0, 0)

  private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

  constructor(context: Context, resizeRatio: Double, radius: Int) : this(context) {
    this.resizeRatio = resizeRatio
    this.radius = radius
  }

  private companion object {
    @OptIn(DelicateCoroutinesApi::class) private val dispatcher = newSingleThreadContext("BlurringView")

    private val mainScope = MainScope()

    private val blurProcessor: BlurringViewProcessor = GlobalBlurProcessorImpl
  }

  init {
    layoutParams = FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT,
    )
  }

  private val onPreDrawListener: ViewTreeObserver.OnPreDrawListener = ViewTreeObserver.OnPreDrawListener {
    if (initialized) {
      mainScope.launch(dispatcher) {
        contentView?.toBitmap(window!!, originalCoordinatesRect)?.run {
          blurProcessor.blur(this)
        }
      }
    }
    true
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    (context as Activity).window.let { window ->
      this.window = window

      window.decorView.findViewById<View>(android.R.id.content).run {
        contentView = this
        viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
        originalCoordinatesRect.set(getCoordinatesInWindow(window))
        dstCoordinatesRect.set(0, 0, originalCoordinatesRect.width(), originalCoordinatesRect.height())

        blurProcessor.initBlur(this@BlurringView, Size(originalCoordinatesRect.width(), originalCoordinatesRect.height()), radius, resizeRatio)
        initialized = true
      }
    }
  }


  override fun onDetachedFromWindow() {
    mainScope.cancel()
    contentView?.viewTreeObserver?.removeOnPreDrawListener(onPreDrawListener)

    blurredBitmap?.recycle()
    blurredBitmap = null

    super.onDetachedFromWindow()
  }

  override fun onBlurred(bitmap: Bitmap?) {
    mainScope.launchSafely {
      blurredBitmap?.recycle()
      blurredBitmap = bitmap

    }.onException { _, _ ->
      
    }
  }

}
