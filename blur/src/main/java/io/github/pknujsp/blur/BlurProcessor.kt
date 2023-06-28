package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.view.Window

class BlurProcessor : IBlur, IWorkers {

  private companion object {
    private val nativeImageProcessor by lazy {
      NativeBlurProcessor()
    }

    private val kotlinImageProcessor by lazy {
      KotlinBlurProcessor()
    }

    init {
      BitmapUtils.init()
    }

  }

  override fun nativeBlurOrDim(window: Window, radius: Int, resizeRatio: Double, dimFactor: Int): Result<Bitmap> =
    nativeImageProcessor.nativeBlurOrDim(
      window, radius, resizeRatio, dimFactor,
    )

  override suspend fun kotlinBlurOrDim(window: Window, radius: Int, resizeRatio: Double, dimFactor: Int): Result<Bitmap> =
    kotlinImageProcessor.kotlinBlurOrDim(
      window, radius, resizeRatio, dimFactor,
    )

  override fun cancel() {
    kotlinImageProcessor.cancel()
    nativeImageProcessor.cancel()
  }
}
