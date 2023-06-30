package io.github.pknujsp.blur

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import io.github.pknujsp.blur.BitmapUtils.getLocationRectInWindow
import io.github.pknujsp.blur.BitmapUtils.toBitmap
import io.github.pknujsp.coroutineext.launchSafely
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class BlurringView private constructor(context: Context) : View(context), BlurManager {
  override var onWindowAttachListener: ViewTreeObserver.OnWindowAttachListener? = null
  override var onWindowDetachListener: ViewTreeObserver.OnWindowAttachListener? = null

  private var iBlurRequest: IBlurRequest by Delegates.notNull()
  private var resizeRatio by Delegates.notNull<Double>()
  private var radius by Delegates.notNull<Int>()

  private var initialized = false
  private var rendering = false
  private val mutex = Mutex()
  private var contentView: View? = null
  private var blurredBitmap: Bitmap? = null
  private var window: Window? = null

  private val sizeRect: Rect = Rect(0, 0, 0, 0)

  constructor(context: Context, iBlurRequest: IBlurRequest, resizeRatio: Double, radius: Int) : this(context) {
    this.iBlurRequest = iBlurRequest
    this.resizeRatio = resizeRatio
    this.radius = radius
  }

  private companion object {
    private val mainScope = MainScope()
  }

  init {
    layoutParams = ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT,
    )
  }

  private val onPreDrawListener: ViewTreeObserver.OnPreDrawListener = ViewTreeObserver.OnPreDrawListener {
    if (initialized && !rendering) {
      mainScope.launchSafely(Dispatchers.Default) {
        if (window != null) {
          mutex.withLock {
            rendering = true
          }

          println("onPreDrawListener: contentView?.toBitmap(window!!, sizeRect)")
          contentView?.toBitmap(window!!, sizeRect)?.onSuccess { bitmap ->
            withContext(Dispatchers.Main) {
              println("onPreDrawListener: iBlurRequest.blur(bitmap)")
              iBlurRequest.blur(bitmap)
            }
          }
        }
      }.onException { _, throwable ->
        throwable.printStackTrace()
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
      mutex.withLock { rendering = false }

      println("onBlurred()")
      recycleBitmap()
      blurredBitmap = bitmap
      invalidate()
    }.onException { _, throwable ->
      throwable.printStackTrace()
    }
  }

  override fun onDraw(canvas: Canvas) {
    blurredBitmap?.run {
      canvas.setBitmap(this)
    }
    super.onDraw(canvas)
  }

  override fun draw(canvas: Canvas) {
    super.draw(canvas)
  }

  override fun onCleared() {
    onWindowDetachListener?.onWindowDetached()
    contentView?.viewTreeObserver?.removeOnPreDrawListener(onPreDrawListener)
    contentView = null

    window = null

    recycleBitmap()
    mainScope.cancel()
  }


  private fun recycleBitmap() {
    //blurredBitmap?.recycle()
    //blurredBitmap = null
  }
}
