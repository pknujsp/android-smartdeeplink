package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.view.View
import android.view.Window
import androidx.annotation.Dimension
import androidx.annotation.FloatRange

interface IBlur {
  suspend fun blur(
    view: View, window: Window, @Dimension(unit = Dimension.DP) radius: Int,
    @FloatRange(from = 1.0, to = 10.0) resizeRatio: Double = 1.0,
  ): Result<Bitmap>

  fun cancel()
}
