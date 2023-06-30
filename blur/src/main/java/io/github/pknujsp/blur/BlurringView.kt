package io.github.pknujsp.blur

import android.annotation.SuppressLint
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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class BlurringView(context: Context) : View(context), BlurManager {

  private lateinit var iBlurRequest: IBlurRequest

  private var initialized = false

  constructor(context: Context, iBlurRequest: IBlurRequest) : this(context) {
    this.iBlurRequest = iBlurRequest
  }

  override var onWindowAttachListener: ViewTreeObserver.OnWindowAttachListener? = null
  override var onWindowDetachListener: ViewTreeObserver.OnWindowAttachListener? = null

  private var _sizeRect: Rect? = null
  private val sizeRect: Rect get() = _sizeRect!!

  private companion object {
    @SuppressLint("StaticFieldLeak") private var _contentView: View? = null
    private val contentView: View get() = _contentView!!
    private val mainScope = MainScope()
    private var _blurredBitmap: Bitmap? = null
    private var _window: Window? = null
    private val window: Window get() = _window!!
    private val blurredBitmap: Bitmap get() = _blurredBitmap!!
  }

  init {
    _contentView = null
    recycleBitmap()

    layoutParams = ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT,
    )
  }

  private val onPreDrawListener: ViewTreeObserver.OnPreDrawListener = ViewTreeObserver.OnPreDrawListener {
    if (initialized) contentView.toBitmap(window, sizeRect).onSuccess { bitmap ->
      iBlurRequest.blur(bitmap)
    }
    true
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    (context as Activity).window?.let { window ->
      window.decorView.let { decorView ->
        _contentView = decorView.findViewById(android.R.id.content)
        contentView.viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
        _sizeRect = contentView.getLocationRectInWindow(window)

        onWindowAttachListener?.onWindowAttached()
        initialized = true
        contentView.invalidate()
      }
    }
  }

  override fun onDetachedFromWindow() {
    onCleared()
    iBlurRequest.onDetachedFromWindow()
    super.onDetachedFromWindow()
  }

  override fun onBlurred(bitmap: Bitmap) {
    mainScope.launchSafely {
      recycleBitmap()
      _blurredBitmap = bitmap
      invalidate()
    }.onException { _, throwable ->
      throwable.printStackTrace()
    }
  }

  override fun onDraw(canvas: Canvas) {
    canvas.setBitmap(blurredBitmap)
    super.onDraw(canvas)
  }

  override fun draw(canvas: Canvas) {
    super.draw(canvas)
  }

  override fun onCleared() {
    onWindowDetachListener?.onWindowDetached()
    _contentView?.viewTreeObserver?.removeOnPreDrawListener(onPreDrawListener)
    _contentView = null
    _sizeRect = null
    initialized = false

    recycleBitmap()
    mainScope.cancel()
  }


  private fun recycleBitmap() {
    _blurredBitmap?.recycle()
    _blurredBitmap = null
  }
}
