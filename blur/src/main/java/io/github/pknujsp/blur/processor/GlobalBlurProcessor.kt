package io.github.pknujsp.blur.processor

import android.graphics.Bitmap
import android.view.Window
import androidx.annotation.Dimension
import androidx.annotation.FloatRange
import androidx.annotation.IntRange

interface GlobalBlurProcessor {

  suspend fun nativeBlur(
    window: Window, @Dimension(unit = Dimension.DP) @IntRange(from = 3, to = 24) radius: Int,
    @FloatRange(from = 1.0, to = 6.0) resizeRatio: Double = 1.0,
  ): Bitmap?

  suspend fun nativeBlur(
    srcBitmap: Bitmap, @Dimension(unit = Dimension.DP) @IntRange(from = 3, to = 24) radius: Int,
    @FloatRange(from = 1.0, to = 6.0) resizeRatio: Double = 1.0,
  ): Bitmap?

  suspend fun kotlinBlur(
    window: Window, @Dimension(unit = Dimension.DP) @IntRange(from = 3, to = 24) radius: Int,
    @FloatRange(from = 1.0, to = 6.0) resizeRatio: Double = 1.0,
  ): Bitmap?

  suspend fun kotlinBlur(
    srcBitmap: Bitmap, @Dimension(unit = Dimension.DP) @IntRange(from = 3, to = 24) radius: Int,
    @FloatRange(from = 1.0, to = 6.0) resizeRatio: Double = 1.0,
  ): Bitmap?

  fun onClear()
}
