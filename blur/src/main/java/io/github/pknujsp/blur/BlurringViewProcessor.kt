package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.util.Size

interface BlurringViewProcessor : GlobalBlurProcessor {
  fun initBlur(directBlurListener: DirectBlurListener, size: Size, radius: Int, resizeRatio: Double)

  fun blur(srcBitmap: Bitmap)
}
