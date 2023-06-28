package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.view.Window


class NativeBlurProcessor : Workers(), IWorkers {

  private companion object {
    private var _nativeImageProcessor: NativeImageProcessor? = null
    private val nativeImageProcessor: NativeImageProcessor
      get() = _nativeImageProcessor!!


    init {
      BitmapUtils.init()
    }
  }

  init {
    _nativeImageProcessor = NativeImageProcessor()
  }

  fun nativeBlurOrDim(
    window: Window,
    radius: Int,
    resizeRatio: Double,
    dimFactor: Int,
  ): Result<Bitmap> = nativeImageProcessor.blurAndDim(
    window.decorView, radius, resizeRatio, BitmapUtils.statusBarHeight, BitmapUtils.navigationBarHeight,
    dimFactor,
  )

  override fun cancel() {
    cancelWorks()
  }

}
