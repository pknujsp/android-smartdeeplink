package io.github.pknujsp.blur

import android.graphics.Bitmap

class IBlurRequestImpl(private val nativeImageProcessor: NativeImageProcessor) : IBlurRequest {
  override fun blur(srcBitmap: Bitmap) {
    nativeImageProcessor.blur(srcBitmap)
  }

  override fun initBlur(blurManager: BlurManager, width: Int, height: Int, radius: Int, resizeRatio: Double) {
    nativeImageProcessor.initBlur(blurManager, width, height, radius, resizeRatio)
  }
}
