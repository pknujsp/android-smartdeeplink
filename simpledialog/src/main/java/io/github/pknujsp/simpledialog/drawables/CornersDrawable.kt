package io.github.pknujsp.simpledialog.drawables

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable

internal class CornersDrawable(
  private val color: Int,
  private val topStartRadius: Float,
  private val topEndRadius: Float,
  private val bottomStartRadius: Float,
  private val bottomEndRadius: Float,
) : Drawable() {
  private val paint = Paint().apply {
    isAntiAlias = true
  }

  private val path = Path()

  override fun onBoundsChange(bounds: Rect) {
    super.onBoundsChange(bounds)
    path.reset()

    path.addRoundRect(
      RectF(bounds),
      floatArrayOf(
        topStartRadius, topStartRadius,
        topEndRadius, topEndRadius,
        bottomEndRadius, bottomEndRadius,
        bottomStartRadius, bottomStartRadius,
      ),
      Path.Direction.CW,
    )
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
