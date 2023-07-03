package io.github.pknujsp.blur

import android.graphics.Bitmap

interface DirectBlurProcessor {
  fun initBlur(blurListener: DirectBlurListener, width: Int, height: Int, radius: Int, resizeRatio: Double)

  fun blur(srcBitmap: Bitmap)

  fun onClear()
}
