package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.view.Window

internal class NativeImageProcessor {

  private companion object {

    init {
      System.loadLibrary("image-processor")
    }
  }

  fun blur(
    window: Window, radius: Int, resizeRatio: Double, statusBarHeight: Int, navigationBarHeight: Int,
  ): Result<Bitmap> = with(
    applyBlur(
      window, radius, resizeRatio, statusBarHeight, navigationBarHeight,
    ),
  ) {
    return when (this) {
      null -> Result.failure(NullPointerException())
      is Throwable -> Result.failure(this)
      else -> Result.success(this as Bitmap)
    }
  }

  private external fun applyBlur(
    window: Bitmap, width: Int, height: Int, radius: Int, resizeRatio: Double, statusBarHeight: Int, navigationBarHeight: Int,
  ): Any?

}
