package io.github.pknujsp.blur.processor

import android.graphics.Bitmap
import android.view.Window
import io.github.pknujsp.blur.BlurUtils.toBitmap
import io.github.pknujsp.blur.kotlin.KotlinBlurProcessor
import io.github.pknujsp.blur.natives.NativeBlurProcessor
import io.github.pknujsp.blur.natives.NativeImageProcessorImpl
import io.github.pknujsp.blur.renderscript.BlurScript

internal object GlobalBlurProcessorImpl : GlobalBlurProcessor {
  private val blurWorker = BlurWorkerImpl

  private val nativeBlurProcessor: NativeBlurProcessor by lazy { NativeImageProcessorImpl }
  private val kotlinBlurProcessor by lazy { KotlinBlurProcessor }
  private var blurScriptProcessor: BlurScript? = null


  override suspend fun nativeBlur(window: Window, radius: Int, resizeRatio: Double): Bitmap? = null


  override suspend fun nativeBlur(srcBitmap: Bitmap, radius: Int, resizeRatio: Double): Bitmap? = null

  override suspend fun kotlinBlur(window: Window, radius: Int, resizeRatio: Double): Bitmap? = window.toBitmap()?.run {
    kotlinBlurProcessor.blur(this, radius)
  }

  override suspend fun kotlinBlur(srcBitmap: Bitmap, radius: Int, resizeRatio: Double): Bitmap? = kotlinBlurProcessor.blur(srcBitmap, radius)
  override fun onClear() {

  }

}
