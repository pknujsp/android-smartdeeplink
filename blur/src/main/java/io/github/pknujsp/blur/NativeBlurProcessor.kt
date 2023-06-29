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

  fun nativeBlur(
    window: Window,
    radius: Int,
    resizeRatio: Double,
  ): Result<Bitmap> = nativeImageProcessor.blur(
    window,
    radius,
    resizeRatio,
    BitmapUtils.statusBarHeight,
    BitmapUtils.navigationBarHeight,
  )


  override fun cancel() {
    cancelWorks()
  }

}
