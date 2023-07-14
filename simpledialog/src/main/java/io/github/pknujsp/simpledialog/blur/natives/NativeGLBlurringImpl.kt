package io.github.pknujsp.simpledialog.blur.natives

import android.graphics.Bitmap
import android.opengl.GLSurfaceView

class NativeGLBlurringImpl {
  companion object {
    init {
      System.loadLibrary("gl-surface")
    }

    external fun onSurfaceCreated(blurringView: GLSurfaceView)
    external fun onSurfaceChanged(width: Int, height: Int, collectingViewRect: IntArray, windowRect: IntArray)
    external fun onDrawFrame(bitmap: Bitmap?)
    external fun onPause()
  }
}
