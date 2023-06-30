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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class BlurringView private constructor(context: Context) : View(context), BlurManager {

  private lateinit var iBlurRequest: IBlurRequest
  private var resizeRatio by Delegates.notNull<Double>()
  private var radius by Delegates.notNull<Int>()
  private var initialized = false


  override var onWindowAttachListener: ViewTreeObserver.OnWindowAttachListener? = null
  override var onWindowDetachListener: ViewTreeObserver.OnWindowAttachListener? = null

  private var _sizeRect: Rect? = null
  private val sizeRect: Rect get() = _sizeRect!!

  private val scope = MainScope()

  private val onPreDrawListener: ViewTreeObserver.OnPreDrawListener = ViewTreeObserver.OnPreDrawListener {
    if (initialized) {
      scope.launchSafely(Dispatchers.Default) {
        contentView.toBitmap(window, sizeRect).onSuccess { bitmap ->
          withContext(Dispatchers.Main) {
            iBlurRequest.blur(bitmap)
          }
        }
      }.onException { _, throwable ->
        throwable.printStackTrace()
      }
    }
    true
  }

  constructor(context: Context, iBlurRequest: IBlurRequest, resizeRatio: Double, radius: Int) : this(context) {
    this.iBlurRequest = iBlurRequest
    this.resizeRatio = resizeRatio
    this.radius = radius
  }

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


  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    (context as Activity).window?.let { window ->
      _window = window
      window.decorView.let { decorView ->
        _contentView = decorView.findViewById(android.R.id.content)
        contentView.viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
        _sizeRect = contentView.getLocationRectInWindow(window)

        onWindowAttachListener?.onWindowAttached()
        initialized = true

        iBlurRequest.initBlur(this, sizeRect.width(), sizeRect.height(), radius, resizeRatio)

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
    if (_blurredBitmap != null) canvas.setBitmap(blurredBitmap)
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
    _window = null
    initialized = false

    recycleBitmap()
    mainScope.cancel()
  }


  private fun recycleBitmap() {
    _blurredBitmap?.recycle()
    _blurredBitmap = null
  }
}
