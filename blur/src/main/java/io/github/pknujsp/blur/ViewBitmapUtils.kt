package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Size
import android.view.PixelCopy
import android.view.View
import android.view.Window
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

object ViewBitmapUtils {

  fun View.toBitmap(window: Window): Result<Bitmap> {
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

      return if (copySuccess) Result.success(originalBitmap)
      else Result.failure(Exception("PixelCopy failed"))
    } catch (e: Exception) {
      return Result.failure(e)
    }
  }
}
