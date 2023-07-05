package io.github.pknujsp.blur.natives

import android.graphics.Bitmap

class NativeGLBlurringImpl {
  companion object {
    init {
      System.loadLibrary("gl-blurring")
    }

    external fun onSurfaceCreated()
    external fun onSurfaceChanged(width: Int, height: Int, collectingViewRect: IntArray, windowRect: IntArray)
    external fun onDrawFrame(bitmap: Bitmap?)
  }
}
