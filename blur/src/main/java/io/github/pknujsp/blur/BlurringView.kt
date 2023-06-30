package io.github.pknujsp.blur

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.FrameLayout
import io.github.pknujsp.blur.BitmapUtils.getLocationRectInWindow
import io.github.pknujsp.blur.BitmapUtils.toBitmap
import io.github.pknujsp.coroutineext.launchSafely
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.properties.Delegates

class BlurringView private constructor(context: Context) : View(context), BlurManager {
  override var onWindowAttachListener: ViewTreeObserver.OnWindowAttachListener? = null
  override var onWindowDetachListener: ViewTreeObserver.OnWindowAttachListener? = null

  @OptIn(DelicateCoroutinesApi::class) private val dispatcher = newSingleThreadContext("BlurringView")

  private var iBlurRequest: IBlurRequest by Delegates.notNull()
  private var resizeRatio by Delegates.notNull<Double>()
  private var radius by Delegates.notNull<Int>()

  private var initialized = false
  private var rendering = false
  private val mutex = Mutex()
  private var contentView: View? = null
  private var blurredBitmap: Bitmap? = null
  private var lastSrcBitmap: Bitmap? = null
  private var window: Window? = null

  private val sizeRect: Rect = Rect(0, 0, 0, 0)
  private val blurredSizeRect: Rect = Rect(0, 0, 0, 0)
  private val dstSizeRect: Rect = Rect(0, 0, 0, 0)

  private val paint = Paint().apply {
    isAntiAlias = false
    setHasTransientState(false)
  }

  private val nativeBlurProcessor = NativeImageProcessor()

  constructor(context: Context, iBlurRequest: IBlurRequest, resizeRatio: Double, radius: Int) : this(context) {
    this.iBlurRequest = iBlurRequest
    this.resizeRatio = resizeRatio
    this.radius = radius
  }

  private companion object {
    private val mainScope = MainScope()
  }

  init {
    layoutParams = FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT,
    ).apply {
      gravity = android.view.Gravity.CENTER
    }
  }

  private val onPreDrawListener: ViewTreeObserver.OnPreDrawListener = ViewTreeObserver.OnPreDrawListener {
    if (initialized && !rendering) {
      mainScope.launch(dispatcher) {
        if (window != null) {
          mutex.withLock {
            rendering = true
          }
          println("onPreDrawListener: contentView?.toBitmap()")
          val result = contentView?.toBitmap(window!!, sizeRect)
          result?.onSuccess { bitmap ->
            if (bitmap != lastSrcBitmap) {
              lastSrcBitmap?.recycle()
              lastSrcBitmap = bitmap

              println("onPreDrawListener: iBlurRequest.blur(bitmap)")
              iBlurRequest.blur(bitmap)
            }
          }
        }
      }
    }
    true
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    (context as Activity).window?.let { window ->
      this.window = window

      window.decorView.findViewById<View>(android.R.id.content).run {
        contentView = this
        viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
        sizeRect.set(getLocationRectInWindow(window))
        dstSizeRect.set(0, 0, sizeRect.width(), sizeRect.height())

        onWindowAttachListener?.onWindowAttached()
        iBlurRequest.initBlur(this@BlurringView, sizeRect.width(), sizeRect.height(), radius, resizeRatio)
        initialized = true

        invalidate()
      }
    }
  }

  override fun onDetachedFromWindow() {
    iBlurRequest.onDetachedFromWindow()
    onCleared()
    super.onDetachedFromWindow()
  }

  override fun onBlurred(bitmap: Bitmap) {
    mainScope.launchSafely {
      println("onBlurred()")
      blurredBitmap?.recycle()
      blurredBitmap = null
      blurredBitmap = bitmap
      if (blurredSizeRect.right == 0) blurredSizeRect.set(0, 0, sizeRect.width(), sizeRect.height())

      mutex.withLock { rendering = false }
      invalidate()
    }.onException { _, throwable ->
      throwable.printStackTrace()
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    blurredBitmap?.run {
      canvas.drawBitmap(this, null, dstSizeRect, null)
    }
  }

  override fun draw(canvas: Canvas) {
    super.draw(canvas)
  }

  override fun onCleared() {
    onWindowDetachListener?.onWindowDetached()
    contentView?.viewTreeObserver?.removeOnPreDrawListener(onPreDrawListener)
    contentView = null
    window = null

    blurredBitmap?.recycle()
    blurredBitmap = null
    lastSrcBitmap?.recycle()
    lastSrcBitmap = null

    mainScope.cancel()
  }
}
