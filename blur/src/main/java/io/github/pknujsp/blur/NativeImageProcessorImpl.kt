package io.github.pknujsp.blur

import android.graphics.Bitmap

object NativeImageProcessorImpl : DirectBlurProcessor, NativeBlurProcessor {

  init {
    System.loadLibrary("image-processor")
  }

  external override fun blur(srcBitmap: Bitmap, width: Int, height: Int, radius: Int, resizeRatio: Double): Bitmap?

  external override fun initBlur(blurListener: DirectBlurListener, width: Int, height: Int, radius: Int, resizeRatio: Double)

  external override fun blur(srcBitmap: Bitmap)

  external override fun onClear()
}
