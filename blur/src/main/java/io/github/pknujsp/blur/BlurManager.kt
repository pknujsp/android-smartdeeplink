package io.github.pknujsp.blur

import android.graphics.Bitmap
import android.view.ViewTreeObserver

interface BlurManager {
  var onWindowAttachListener: ViewTreeObserver.OnWindowAttachListener?
  var onWindowDetachListener: ViewTreeObserver.OnWindowAttachListener?
  fun onBlurred(bitmap: Bitmap)
  fun onCleared()
}
