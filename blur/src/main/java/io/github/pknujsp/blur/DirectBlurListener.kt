package io.github.pknujsp.blur

import android.graphics.Bitmap

interface DirectBlurListener {
  fun onBlurred(bitmap: Bitmap?)

}
