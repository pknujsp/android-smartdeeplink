package com.pknujsp.feature.holographic

import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.core.view.drawToBitmap
import androidx.palette.graphics.Palette

class ColorAnalyzer {

    @RequiresApi(Build.VERSION_CODES.P)
    fun analyzeColor(statusTextView: TextView, imageView: ImageView, @ColorInt defaultColor: Int) {
        Palette.from(imageView.drawToBitmap(Bitmap.Config.ARGB_8888)).generate { palette ->
            val dominantColor = palette?.getDominantColor(defaultColor) ?: defaultColor
            imageView.outlineAmbientShadowColor = dominantColor
            imageView.outlineSpotShadowColor = dominantColor
            imageView.invalidate()
            statusTextView.background = ColorDrawable(dominantColor)
        }
    }
}