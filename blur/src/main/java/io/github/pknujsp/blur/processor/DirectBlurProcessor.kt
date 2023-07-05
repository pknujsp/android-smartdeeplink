package io.github.pknujsp.blur.processor

import android.graphics.Bitmap

interface DirectBlurProcessor {
  fun initBlur(width: Int, height: Int, radius: Int, resizeRatio: Double)

  fun blur(srcBitmap: Bitmap)

  fun onClear()
}
