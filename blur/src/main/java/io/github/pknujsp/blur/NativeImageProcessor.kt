package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.view.View

internal class NativeImageProcessor {

  private companion object {

    init {
      System.loadLibrary("image-processor")
    }
  }

  fun blurAndDim(
    decorView: View, radius: Int, resizeRatio: Double, statusBarHeight: Int, navigationBarHeight: Int, dimFactor: Int,
  ): Result<Bitmap> = with(
    applyBlur(
      decorView, radius, resizeRatio, statusBarHeight, navigationBarHeight, dimFactor,
    ),
  ) {
    if (this != null) Result.success(this)
    else Result.failure(RuntimeException("Blurring failed"))
  }

  private external fun applyBlur(
    decorView: View, radius: Int, resizeRatio: Double, statusBarHeight: Int, navigationBarHeight: Int, dimFactor: Int,
  ): Bitmap?
}
