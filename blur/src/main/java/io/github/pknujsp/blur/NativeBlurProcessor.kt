package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.view.View
import android.view.Window
import io.github.pknujsp.blur.ViewBitmapUtils.toBitmap


class NativeBlurProcessor : IBlur {
  private val nativeImageProcessor = NativeImageProcessor()

  override suspend fun blur(view: View, window: Window, radius: Int, resizeRatio: Double): Result<Bitmap> = view.toBitmap(window, resizeRatio).fold(
    onSuccess = { bitmap ->
      val result = nativeImageProcessor.blur(bitmap, radius, bitmap.width, bitmap.height)
      if (result) Result.success(bitmap)
      else Result.failure(RuntimeException("Native blur failed"))
    },
    onFailure = { Result.failure(it) },
  )
}
