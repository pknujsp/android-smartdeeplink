package io.github.pknujsp.blur.natives

import android.graphics.Bitmap
import android.opengl.GLSurfaceView

class NativeGLBlurringImpl {
  companion object {
    init {
      System.loadLibrary("gl-blurring")
    }

    external fun onSurfaceCreated(blurringView: GLSurfaceView)
    external fun onSurfaceChanged(width: Int, height: Int, collectingViewRect: IntArray, windowRect: IntArray)
    external fun onDrawFrame(bitmap: Bitmap?)

    external fun blurAndDrawFrame(srcBitmap: Bitmap)

    external fun prepareBlur(width: Int, height: Int, radius: Int, resizeRatio: Double)


    external fun onPause()
  }
}
