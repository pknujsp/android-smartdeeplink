package io.github.pknujsp.simpledialog.blur.view

import android.view.View

interface IGLSurfaceView {
  fun setOnTouchListener(listener: View.OnTouchListener)

  fun onResume()
  fun onPause()
}

interface IGLSurfaceViewLayout {
  fun setBackgroundColor(color: Int)
}
