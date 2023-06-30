package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.view.Window

class BlurProcessor : IBlur, IWorkers {

  private companion object {
    private val nativeBlurProcessor: NativeBlurProcessor by lazy {
      NativeBlurProcessor()
    }

    private val kotlinImageProcessor by lazy {
      KotlinBlurProcessor()
    }

    const val MIN_RADIUS = 7

    init {
      BitmapUtils.init()
    }

  }

  override fun nativeBlur(window: Window, radius: Int, resizeRatio: Double): Result<Bitmap> = nativeBlurProcessor.nativeBlur(
    window, radius.coerceAtLeast(MIN_RADIUS), resizeRatio,
  )

  override suspend fun kotlinBlur(window: Window, radius: Int, resizeRatio: Double): Result<Bitmap> = kotlinImageProcessor.kotlinBlur(
    window, radius.coerceAtLeast(MIN_RADIUS), resizeRatio,
  )

  override fun cancel() {
    kotlinImageProcessor.cancel()
    nativeBlurProcessor.cancel()
  }
}
