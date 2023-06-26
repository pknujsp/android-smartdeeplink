package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.view.View
import android.view.Window
import io.github.pknujsp.blur.ViewBitmapUtils.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class NativeBlurProcessor : Workers(), IBlur {

  private companion object {
    private val nativeImageProcessor by lazy { NativeImageProcessor() }
  }

  override suspend fun blur(view: View, window: Window, radius: Int, resizeRatio: Double): Result<Bitmap> =
    suspendCancellableCoroutine { continuation ->
      launch(Dispatchers.Default) {
        view.toBitmap(window, resizeRatio).fold(
          onSuccess = { bitmap ->
            val result = nativeImageProcessor.blur(bitmap, radius, bitmap.width, bitmap.height)
            if (result) Result.success(bitmap)
            else Result.failure(RuntimeException("Native blur failed"))
          },
          onFailure = { Result.failure(it) },
        ).run {
          continuation.resume(this)
        }
      }
    }

  override fun cancel() {
    cancelWorks()
  }
}
