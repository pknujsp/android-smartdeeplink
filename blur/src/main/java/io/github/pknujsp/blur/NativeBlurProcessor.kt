package io.github.pknujsp.blur

import android.graphics.Bitmap

interface NativeBlurProcessor {
  fun blur(
    srcBitmap: Bitmap, width: Int, height: Int, radius: Int, resizeRatio: Double,
  ): Bitmap?

}
