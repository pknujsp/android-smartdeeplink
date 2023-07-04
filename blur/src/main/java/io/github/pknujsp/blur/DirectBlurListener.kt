package io.github.pknujsp.blur

import android.graphics.Bitmap

interface DirectBlurListener {
  suspend fun onBlurred(bitmap: Bitmap?)

}
