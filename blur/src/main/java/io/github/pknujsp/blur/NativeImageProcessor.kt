package io.github.pknujsp.blur

import android.graphics.Bitmap

object NativeImageProcessor : IBlurRequest {

  init {
    System.loadLibrary("image-processor")
  }


  fun blur(
    bitmap: Bitmap, radius: Int, resizeRatio: Double,
  ): Result<Bitmap> = with(
    applyBlur(
      bitmap, bitmap.width, bitmap.height, radius, resizeRatio,
    ),
  ) {
    return when (this) {
      null -> Result.failure(NullPointerException())
      is Throwable -> Result.failure(this)
      else -> Result.success(this as Bitmap)
    }
  }

  private external fun applyBlur(
    srcBitmap: Bitmap, width: Int, height: Int, radius: Int, resizeRatio: Double,
  ): Any?

  external override fun initBlur(blurManager: BlurManager, width: Int, height: Int, radius: Int, resizeRatio: Double)

  external override fun blur(srcBitmap: Bitmap)

  external override fun onDetachedFromWindow()
}
