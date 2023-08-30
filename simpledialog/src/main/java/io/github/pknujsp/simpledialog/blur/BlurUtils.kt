package io.github.pknujsp.simpledialog.blur

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.Window
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal object BlurUtils {

  private val bitmapConfig = Bitmap.Config.ARGB_8888

  fun View.toBitmap(window: Window, coordinatesInWindow: Rect? = null, resizeRatio: Double = 1.0): Bitmap? = toBitmap(
    window,
    coordinatesInWindow ?: getCoordinatesInWindow(window),
  )?.run {
    if (resizeRatio != 1.0) resize(resizeRatio) else this
  }

  fun Window.toBitmap(resizeRatio: Double = 1.0): Bitmap? =
    decorView.findViewById<View>(android.R.id.content).toBitmap(this, resizeRatio = resizeRatio)

  fun View.getCoordinatesInWindow(window: Window): Rect {
    val locationOfViewInWindow = IntArray(2)
    getLocationInWindow(locationOfViewInWindow)

    return Rect(
      locationOfViewInWindow[0],
      locationOfViewInWindow[1],
      locationOfViewInWindow[0] + width,
      locationOfViewInWindow[1] + height,
    )
  }

  fun View.getContentView() = findViewById<View>(android.R.id.content)

  private fun View.toBitmap(window: Window, coordinatesInWindow: Rect): Bitmap? = try {
    Bitmap.createBitmap(coordinatesInWindow.width(), coordinatesInWindow.height(), bitmapConfig).let { bitmap ->
      when (val copySuccess = runBlocking {
        suspendCancellableCoroutine {
          PixelCopy.request(
            window,
            coordinatesInWindow,
            bitmap,
            { result ->
              it.resume(result == PixelCopy.SUCCESS)
            },
            Handler(Looper.getMainLooper()),
          )
        }
      }) {
        true -> bitmap
        false -> null
      }
    }
  } catch (e: Exception) {
    null
  }


  fun Bitmap.resize(resizeRatio: Double): Bitmap? = run {
    try {
      Bitmap.createScaledBitmap(this, (width / resizeRatio).toInt(), (height / resizeRatio).toInt(), true)
    } catch (e: Exception) {
      null
    }
  }
}
