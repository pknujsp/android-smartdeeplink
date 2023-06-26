package io.github.pknujsp.blur

import android.graphics.Bitmap

class NativeImageProcessor {

  private companion object {

    init {
      System.loadLibrary("image-processor")
    }
  }

  external fun blur(
    srcBitmap: Bitmap, radius: Int, targetWidth: Int, targetHeight: Int,
  ): IntArray
}
