package io.github.pknujsp.blur

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.core.graphics.applyCanvas
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

internal object BitmapUtils {

  private var _navigationBarHeight: Int = 0
  val navigationBarHeight: Int
    get() = _navigationBarHeight
  private var _statusBarHeight: Int = 0
  val statusBarHeight: Int
    get() = _statusBarHeight

  @SuppressLint("InternalInsetResource", "DiscouragedApi")
  fun init() {
    if (_navigationBarHeight == 0 || _statusBarHeight == 0) {
      Resources.getSystem().run {
        _navigationBarHeight = getDimensionPixelSize(getIdentifier("navigation_bar_height", "dimen", "android"))
        _statusBarHeight = getDimensionPixelSize(getIdentifier("status_bar_height", "dimen", "android"))
      }
    }
  }


  fun View.toBitmap(window: Window, resizeRatio: Double = 1.0): Result<Bitmap> {
    try {
      val originalSize = Size(width, height)
      val originalBitmap: Bitmap = Bitmap.createBitmap(originalSize.width, originalSize.height, Bitmap.Config.ARGB_8888)

      val copySuccess: Boolean = runBlocking {
        suspendCancellableCoroutine {
          val locationOfViewInWindow = IntArray(2)
          getLocationInWindow(locationOfViewInWindow)
          val locationRect = WeakReference(
            Rect(
              locationOfViewInWindow[0],
              locationOfViewInWindow[1],
              locationOfViewInWindow[0] + originalSize.width,
              locationOfViewInWindow[1] + originalSize.height,
            ),
          ).get()!!

          PixelCopy.request(
            window, locationRect, originalBitmap,
            { result ->
              it.resume(result == PixelCopy.SUCCESS)
            },
            Handler(Looper.getMainLooper()),
          )
        }
      }

      if (copySuccess) {
        if (resizeRatio > 1.0) {
          val reducedSize = Size(
            (originalSize.width / resizeRatio).toInt().let { if (it % 2 == 0) it else it - 1 },
            (originalSize.height / resizeRatio).toInt().let { if (it % 2 == 0) it else it - 1 },
          )
          val pixels = WeakReference(IntArray(reducedSize.width * reducedSize.height)).get()!!
          val reducedBitmap = WeakReference(Bitmap.createScaledBitmap(originalBitmap, reducedSize.width, reducedSize.height, false)).get()!!
          reducedBitmap.getPixels(pixels, 0, reducedSize.width, 0, 0, reducedSize.width, reducedSize.height)
          originalBitmap.recycle()

          return Result.success(reducedBitmap)
        }
        return Result.success(originalBitmap)
      }
      return Result.failure(Exception("Failed to copy view to bitmap"))
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }

  fun Window.toBitmap(): Result<Bitmap> {
    try {
      val contentView = decorView.findViewById<View>(android.R.id.content)
      val size = Size(contentView.width, contentView.height)

      val bitmap: Bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.RGB_565)

      val copySuccess: Boolean = runBlocking {
        suspendCancellableCoroutine {
          val locationOfViewInWindow = IntArray(2)
          contentView.getLocationInWindow(locationOfViewInWindow)
          val locationRect = WeakReference(
            Rect(
              locationOfViewInWindow[0],
              locationOfViewInWindow[1],
              locationOfViewInWindow[0] + size.width,
              locationOfViewInWindow[1] + size.height,
            ),
          ).get()!!

          PixelCopy.request(
            this@toBitmap, locationRect, bitmap,
            { result ->
              it.resume(result == PixelCopy.SUCCESS)
            },
            Handler(Looper.getMainLooper()),
          )
        }
      }

      if (copySuccess) return Result.success(bitmap)
      return Result.failure(Exception("Failed to copy view to bitmap"))
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }


  fun Window.toBitmap(resizeRatio: Double = 1.0): Result<Bitmap> {
    try {
      val width = decorView.width
      val height = decorView.height

      val locationRect = Rect(
        0,
        _statusBarHeight,
        width,
        height - _navigationBarHeight,
      )

      val originalBitmap = Bitmap.createBitmap(locationRect.width(), locationRect.height(), Bitmap.Config.RGB_565).applyCanvas {
        translate(0f, -_statusBarHeight.toFloat())
        decorView.draw(this)
      }


      //val originalBitmap: Bitmap = Bitmap.createBitmap(locationRect.width(), locationRect.height(), Bitmap.Config.RGB_565)

      /**
      val copySuccess: Boolean = runBlocking {
      suspendCancellableCoroutine {
      val start = System.currentTimeMillis()
      PixelCopy.request(
      this@toBitmap, locationRect, originalBitmap,
      { result ->
      it.resume(result == PixelCopy.SUCCESS)
      println("PixelCopy.request() took ${System.currentTimeMillis() - start}ms")
      },
      Handler(Looper.getMainLooper()),
      )
      }
      }
       */


      if (resizeRatio > 1.0) {
        val reducedSize = Size(
          (width / resizeRatio).toInt().let { if (it % 2 == 0) it else it - 1 },
          (height / resizeRatio).toInt().let { if (it % 2 == 0) it else it - 1 },
        )
        val pixels = WeakReference(IntArray(reducedSize.width * reducedSize.height)).get()!!
        val reducedBitmap = WeakReference(Bitmap.createScaledBitmap(originalBitmap, reducedSize.width, reducedSize.height, true)).get()!!
        reducedBitmap.getPixels(pixels, 0, reducedSize.width, 0, 0, reducedSize.width, reducedSize.height)
        originalBitmap.recycle()

        return Result.success(reducedBitmap)
      }
      return Result.success(originalBitmap)
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }


}
