package io.github.pknujsp.blur.natives

import android.graphics.Bitmap

interface NativeBlurProcessor {
  fun prepareBlur(width: Int, height: Int, radius: Int, resizeRatio: Double)

  fun blur(srcBitmap: Bitmap): Bitmap?

  fun onClear()
}
