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

  fun View.toBitmap(window: Window, sampleRatio: Double = 1.0): Result<Bitmap> {
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
        if (sampleRatio > 1.0) {
          val reducedSize = Size(
            (originalSize.width / sampleRatio).toInt().let { if (it % 2 == 0) it else it - 1 },
            (originalSize.height / sampleRatio).toInt().let { if (it % 2 == 0) it else it - 1 },
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
}
