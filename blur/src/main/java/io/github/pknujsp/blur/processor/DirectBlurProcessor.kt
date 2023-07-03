package io.github.pknujsp.blur.processor

import android.graphics.Bitmap
import io.github.pknujsp.blur.DirectBlurListener

interface DirectBlurProcessor {
  fun initBlur(blurListener: DirectBlurListener, width: Int, height: Int, radius: Int, resizeRatio: Double)

  fun blur(srcBitmap: Bitmap)

  fun onClear()
}
