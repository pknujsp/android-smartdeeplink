package io.github.pknujsp.blur.view

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import io.github.pknujsp.blur.processor.GlobalBlurProcessor

interface BlurringViewProcessor : GlobalBlurProcessor {

  fun initBlur(context: Context, directBlurListener: DirectBlurListener, size: Size, radius: Int, resizeRatio: Double)

  fun blur(srcBitmap: Bitmap): Bitmap?
}
