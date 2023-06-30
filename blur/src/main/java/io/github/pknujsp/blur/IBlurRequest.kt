package io.github.pknujsp.blur

import android.graphics.Bitmap

interface IBlurRequest {
  fun blur(srcBitmap: Bitmap)
  fun initBlur(blurManager: BlurManager, width: Int, height: Int, radius: Int, resizeRatio: Double)

  fun onDetachedFromWindow()
}
