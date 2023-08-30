package io.github.pknujsp.blur.natives

import android.graphics.Bitmap

object NativeImageProcessorImpl : NativeBlurProcessor {

  init {
    System.loadLibrary("stack-blur")
  }


  external override fun prepareBlur(width: Int, height: Int, radius: Int, resizeRatio: Double)

  external override fun blur(srcBitmap: Bitmap): Bitmap?

  external override fun onClear()
}
