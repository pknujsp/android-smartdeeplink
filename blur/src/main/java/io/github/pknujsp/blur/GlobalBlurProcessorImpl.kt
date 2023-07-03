package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.util.Size
import android.view.Window
import io.github.pknujsp.blur.BlurUtils.toBitmap

internal object GlobalBlurProcessorImpl : BlurringViewProcessor {

  private val blurWorker = BlurWorkerImpl

  private val nativeBlurProcessor: NativeBlurProcessor by lazy { NativeImageProcessorImpl }
  private val nativeDirectBlurProcessor: DirectBlurProcessor by lazy { NativeImageProcessorImpl }
  private val kotlinBlurProcessor by lazy { KotlinBlurProcessor }


  override fun initBlur(directBlurListener: DirectBlurListener, size: Size, radius: Int, resizeRatio: Double) {
    nativeDirectBlurProcessor.initBlur(directBlurListener, size.width, size.height, radius, resizeRatio)
  }

  override fun blur(srcBitmap: Bitmap) {
    nativeDirectBlurProcessor.blur(srcBitmap)
  }

  override suspend fun nativeBlur(window: Window, radius: Int, resizeRatio: Double): Bitmap? = window.toBitmap()?.run {
    nativeBlurProcessor.blur(this, width, height, radius, resizeRatio)
  }

  override suspend fun nativeBlur(srcBitmap: Bitmap, radius: Int, resizeRatio: Double): Bitmap? =
    nativeBlurProcessor.blur(srcBitmap, srcBitmap.width, srcBitmap.height, radius, resizeRatio)

  override suspend fun kotlinBlur(window: Window, radius: Int, resizeRatio: Double): Bitmap? = window.toBitmap()?.run {
    kotlinBlurProcessor.blur(this, radius)
  }

  override suspend fun kotlinBlur(srcBitmap: Bitmap, radius: Int, resizeRatio: Double): Bitmap? = kotlinBlurProcessor.blur(srcBitmap, radius)

}
