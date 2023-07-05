package io.github.pknujsp.blur.processor

import android.graphics.Bitmap

interface DirectBlurProcessor {
  fun prepareBlur(width: Int, height: Int, radius: Int, resizeRatio: Double)

  fun blur(srcBitmap: Bitmap): Bitmap?

  fun onClear()
}
