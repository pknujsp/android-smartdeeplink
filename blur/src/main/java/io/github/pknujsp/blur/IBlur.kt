package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.view.Window
import androidx.annotation.Dimension
import androidx.annotation.FloatRange
import androidx.annotation.WorkerThread

interface IBlur {
  @WorkerThread
  fun nativeBlur(
    window: Window, @Dimension(unit = Dimension.DP) radius: Int, @FloatRange(from = 1.0, to = 8.0) resizeRatio: Double = 1.0,
  ): Result<Bitmap>

  suspend fun kotlinBlur(
    window: Window, @Dimension(unit = Dimension.DP) radius: Int, @FloatRange(from = 1.0, to = 8.0) resizeRatio: Double = 1.0,
  ): Result<Bitmap>

}
