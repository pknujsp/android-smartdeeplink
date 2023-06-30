package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.view.Window
import io.github.pknujsp.blur.BitmapUtils.toBitmap


class NativeBlurProcessor : Workers(), IWorkers {
  private companion object {
    private var _nativeImageProcessor: NativeImageProcessor? = null
    private val nativeImageProcessor: NativeImageProcessor
      get() = _nativeImageProcessor!!


    init {
      _nativeImageProcessor = NativeImageProcessor()
      BitmapUtils.init()
    }
  }

  fun nativeBlur(
    window: Window,
    radius: Int,
    resizeRatio: Double,
  ): Result<Bitmap> = window.run {
    toBitmap().fold(
      onSuccess = { bitmap ->
        nativeImageProcessor.blur(bitmap, radius, resizeRatio)
      },
      onFailure = { throwable ->
        Result.failure(throwable)
      },
    )
  }


  override fun cancel() {
    cancelWorks()
  }

}
