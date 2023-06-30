package io.github.pknujsp.testbed.core.ui.dialog

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Dimension.Companion.PX

internal class CornersDrawable(
  @ColorInt private val color: Int,
  @Dimension(PX) private val topLeftRadius: Float,
  @Dimension(PX) private val topRightRadius: Float,
  @Dimension(PX) private val bottomRightRadius: Float,
  @Dimension(PX) private val bottomLeftRadius: Float,
) : Drawable() {
  private val paint = Paint().apply {
    isAntiAlias = true
  }

  private val path = Path()

  override fun onBoundsChange(bounds: Rect) {
    super.onBoundsChange(bounds)
    path.reset()

    val radius = floatArrayOf(
      topLeftRadius, topLeftRadius,
      topRightRadius, topRightRadius,
      bottomRightRadius, bottomRightRadius,
      bottomLeftRadius, bottomLeftRadius,
    )

    path.addRoundRect(RectF(bounds), radius, Path.Direction.CW)
  }

  override fun draw(canvas: Canvas) {
    paint.color = color
    canvas.drawPath(path, paint)
  }

  override fun setAlpha(alpha: Int) {
    paint.alpha = alpha
  }

  override fun setColorFilter(colorFilter: ColorFilter?) {
    paint.colorFilter = colorFilter
  }

  @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat"))
  override fun getOpacity(): Int = PixelFormat.OPAQUE
}
