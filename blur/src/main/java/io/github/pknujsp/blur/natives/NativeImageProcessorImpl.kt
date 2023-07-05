package io.github.pknujsp.blur.natives

import android.graphics.Bitmap
import io.github.pknujsp.blur.processor.DirectBlurProcessor

object NativeImageProcessorImpl : DirectBlurProcessor, NativeBlurProcessor {

  init {
    System.loadLibrary("image-processor")
  }

  external override fun blur(srcBitmap: Bitmap, width: Int, height: Int, radius: Int, resizeRatio: Double): Bitmap?

  external override fun initBlur(width: Int, height: Int, radius: Int, resizeRatio: Double)

  external override fun blur(srcBitmap: Bitmap): Bitmap?

  external override fun onClear()
}
