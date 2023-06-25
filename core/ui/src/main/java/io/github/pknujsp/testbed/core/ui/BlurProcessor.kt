package io.github.pknujsp.testbed.core.ui

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.annotation.Dimension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume


class BlurProcessor {

  private val scope = MainScope() + Job()

  private val displayDensity = Resources.getSystem().displayMetrics.density

  private val dispatcher = Dispatchers.Default

  private companion object {

    private val mulTable = intArrayOf(
      512, 512, 456, 512, 328, 456, 335, 512, 405, 328, 271, 456, 388, 335, 292,
      512, 454, 405, 364, 328, 298, 271, 496, 456, 420, 388, 360, 335, 312, 292,
      273, 512, 482, 454, 428, 405, 383, 364, 345, 328, 312, 298, 284, 271, 259,
      496, 475, 456, 437, 420, 404, 388, 374, 360, 347, 335, 323, 312, 302, 292,
      282, 273, 265, 512, 497, 482, 468, 454, 441, 428, 417, 405, 394, 383, 373,
      364, 354, 345, 337, 328, 320, 312, 305, 298, 291, 284, 278, 271, 265, 259,
      507, 496, 485, 475, 465, 456, 446, 437, 428, 420, 412, 404, 396, 388, 381,
      374, 367, 360, 354, 347, 341, 335, 329, 323, 318, 312, 307, 302, 297, 292,
      287, 282, 278, 273, 269, 265, 261, 512, 505, 497, 489, 482, 475, 468, 461,
      454, 447, 441, 435, 428, 422, 417, 411, 405, 399, 394, 389, 383, 378, 373,
      368, 364, 359, 354, 350, 345, 341, 337, 332, 328, 324, 320, 316, 312, 309,
      305, 301, 298, 294, 291, 287, 284, 281, 278, 274, 271, 268, 265, 262, 259,
      257, 507, 501, 496, 491, 485, 480, 475, 470, 465, 460, 456, 451, 446, 442,
      437, 433, 428, 424, 420, 416, 412, 408, 404, 400, 396, 392, 388, 385, 381,
      377, 374, 370, 367, 363, 360, 357, 354, 350, 347, 344, 341, 338, 335, 332,
      329, 326, 323, 320, 318, 315, 312, 310, 307, 304, 302, 299, 297, 294, 292,
      289, 287, 285, 282, 280, 278, 275, 273, 271, 269, 267, 265, 263, 261, 259,
    )

    private val shgTable = intArrayOf(
      9, 11, 12, 13, 13, 14, 14, 15, 15, 15, 15, 16, 16, 16, 16, 17,
      17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 19,
      19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 20, 20, 20,
      20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21,
      21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21,
      21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 22,
      22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
      22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 23,
      23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
      23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
      23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
      23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
      24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
      24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
      24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
      24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24,
    )

  }

  private data class Index(var index: Int, private val max: Int) {
    fun inc() {
      if (++index >= max) index = 0
    }


    fun set(value: Int) {
      index = value
    }

    fun get() = index
  }

  private data class BlurValues(
    var alpha: Int = 0,
    var red: Int = 0,
    var green: Int = 0,
    var blue: Int = 0,
  )

  /**
   * StackBlur
   */
  fun blur(view: View, window: Window, @Dimension(unit = Dimension.DP) radius: Int) {
    scope.launch(dispatcher) {
      val width = view.width
      val height = view.height
      val radiusPx = (radius * displayDensity).toInt()

      val blurredBitmap: Bitmap = WeakReference(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)).get()!!

      val copySuccess: Boolean = suspendCancellableCoroutine {
        val locationOfViewInWindow = IntArray(2)
        view.getLocationInWindow(locationOfViewInWindow)

        val locationRect = WeakReference(
          Rect(
            locationOfViewInWindow[0],
            locationOfViewInWindow[1],
            locationOfViewInWindow[0] + width,
            locationOfViewInWindow[1] + height,
          ),
        ).get()!!

        PixelCopy.request(
          window, locationRect, blurredBitmap,
          { result ->
            it.resume(result == PixelCopy.SUCCESS)
          },
          Handler(Looper.getMainLooper()),
        )
      }

      if (copySuccess) blur(blurredBitmap, radius)

    }
  }

  private fun blur(
    blurredBitmap: Bitmap, radius: Int,
  ) {
    val width = blurredBitmap.width
    val height = blurredBitmap.height
    val pixels = IntArray(width * height)
    blurredBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val newWidth = width - 1
    val newHeight = height - 1
    val newRadius = radius + 1
    val sumFactor = newRadius * (newRadius + 1) / 2
    val div = 2 * radius + 1

    val valueStack = Array(div - 1) {
      BlurValues()
    }
    val endValue = valueStack[newRadius]

    val mulSum = mulTable[radius]
    val shgSum = shgTable[radius]

    var yw = 0
    var yi = 0
    val currentValueIndexIn = Index(0, div - 1)
    val currentValueIndexOut = Index(0, div - 1)

    for (y in 0 until height) {

      val pa = pixels[yi]
      val pr = pixels[yi + 1]
      val pg = pixels[yi + 2]
      val pb = pixels[yi + 3]

      valueStack.forEachIndexed { i, value ->
        if (i == newRadius) {
          currentValueIndexIn.set(i)
          return@forEachIndexed
        }
        value.alpha = pa
        value.red = pr
        value.green = pg
        value.blue = pb
      }

      var aSumIn = 0
      var rSumIn = 0
      var gSumIn = 0
      var bSumIn = 0

      var aSumOut = newRadius * pa
      var rSumOut = newRadius * pr
      var gSumOut = newRadius * pg
      var bSumOut = newRadius * pb

      var aSum = sumFactor * pa
      var rSum = sumFactor * pr
      var gSum = sumFactor * pg
      var bSum = sumFactor * pb

      for (i in 1 until newRadius) {
        val p = yi + ((if (newWidth < i) newWidth else i) shl 2)

        val a = pixels[p]
        val r = pixels[p + 1]
        val g = pixels[p + 2]
        val b = pixels[p + 3]

        val rbs = newRadius - i

        valueStack[currentValueIndexIn.get()].alpha = a
        valueStack[currentValueIndexIn.get()].red = r
        valueStack[currentValueIndexIn.get()].green = g
        valueStack[currentValueIndexIn.get()].blue = b

        currentValueIndexIn.inc()

        aSumIn += a * rbs
        rSumIn += r * rbs
        gSumIn += g * rbs
        bSumIn += b * rbs

        aSumOut += a
        rSumOut += r
        gSumOut += g
        bSumOut += b
      }

      currentValueIndexIn.set(0)

      for (x in 0 until width) {
        val paInit = (aSum * mulSum) shr shgSum
        pixels[yi] = paInit

        if (paInit != 0) {
          val a = 255 / paInit
          pixels[yi + 1] = ((rSum * mulSum) shr shgSum) * a
          pixels[yi + 2] = ((gSum * mulSum) shr shgSum) * a
          pixels[yi + 3] = ((bSum * mulSum) shr shgSum) * a
        } else {
          pixels[yi + 1] = 0
          pixels[yi + 2] = 0
          pixels[yi + 3] = 0
        }

        aSum += aSumOut
        rSum -= rSumOut
        gSum -= gSumOut
        bSum -= bSumOut

        aSumOut -= valueStack[currentValueIndexIn.get()].alpha
        rSumOut -= valueStack[currentValueIndexIn.get()].red
        gSumOut -= valueStack[currentValueIndexIn.get()].green
        bSumOut -= valueStack[currentValueIndexIn.get()].blue

        var p = x + radius + 1
        p = if (yw + p < newWidth) p else newWidth
        p = p shl 2

        valueStack[currentValueIndexIn.get()].alpha = pixels[p]
        valueStack[currentValueIndexIn.get()].red = pixels[p + 1]
        valueStack[currentValueIndexIn.get()].green = pixels[p + 2]
        valueStack[currentValueIndexIn.get()].blue = pixels[p + 3]

        aSumIn += valueStack[currentValueIndexIn.get()].alpha
        rSumIn += valueStack[currentValueIndexIn.get()].red
        gSumIn += valueStack[currentValueIndexIn.get()].green
        bSumIn += valueStack[currentValueIndexIn.get()].blue

        aSum += aSumIn
        rSum += rSumIn
        gSum += gSumIn
        bSum += bSumIn

        currentValueIndexIn.inc()

        aSumOut += valueStack[currentValueIndexOut.get()].alpha
        rSumOut += valueStack[currentValueIndexOut.get()].red
        gSumOut += valueStack[currentValueIndexOut.get()].green
        bSumOut += valueStack[currentValueIndexOut.get()].blue

        aSumIn -= valueStack[currentValueIndexOut.get()].alpha
        rSumIn -= valueStack[currentValueIndexOut.get()].red
        gSumIn -= valueStack[currentValueIndexOut.get()].green
        bSumIn -= valueStack[currentValueIndexOut.get()].blue

        currentValueIndexOut.inc()

        yi += 4
      }
      yw += width
    }

    // width

    for (x in 0 until width) {
      var yi = x shl 2

      var pr = pixels[yi]
      var pg = pixels[yi + 1]
      var pb = pixels[yi + 2]
      var pa = pixels[yi + 3]
      var rOutSum = radiusPlus1 * pr
      var gOutSum = radiusPlus1 * pg
      var bOutSum = radiusPlus1 * pb
      var aOutSum = radiusPlus1 * pa
      var rSum = sumFactor * pr
      var gSum = sumFactor * pg
      var bSum = sumFactor * pb
      var aSum = sumFactor * pa

      var stack: BlurStack? = stackStart

      for (i in 0 until radiusPlus1) {
        stack?.r = pr
        stack?.g = pg
        stack?.b = pb
        stack?.a = pa
        stack = stack?.next
      }

      var yp = width

      var gInSum = 0
      var bInSum = 0
      var aInSum = 0
      var rInSum = 0
      for (i in 1..radius) {
        yi = (yp + x) shl 2

        val rbs = radiusPlus1 - i
        rSum += (stack?.r ?: 0).also { stack?.r = pixels[yi] } * rbs
        gSum += (stack?.g ?: 0).also { stack?.g = pixels[yi + 1] } * rbs
        bSum += (stack?.b ?: 0).also { stack?.b = pixels[yi + 2] } * rbs
        aSum += (stack?.a ?: 0).also { stack?.a = pixels[yi + 3] } * rbs

        rInSum += pr
        gInSum += pg
        bInSum += pb
        aInSum += pa

        stack = stack?.next

        if (i < heightMinus1) {
          yp += width
        }
      }

      yi = x
      var stackIn = stackStart
      var stackOut = stackEnd
      for (y in 0 until height) {
        var p = yi shl 2
        pixels[p + 3] = pa.also {
          aSum = (aSum * mulSum) shr shgSum
        }
        if (pa > 0) {
          pa = 255 / pa
          pixels[p] = ((rSum * mulSum) shr shgSum) * pa
          pixels[p + 1] = ((gSum * mulSum) shr shgSum) * pa
          pixels[p + 2] = ((bSum * mulSum) shr shgSum) * pa
        } else {
          pixels[p] = pixels[p + 1].also { pixels[p + 2] = it }
        }

        rSum -= rOutSum
        gSum -= gOutSum
        bSum -= bOutSum
        aSum -= aOutSum

        rOutSum -= stackIn?.r ?: 0
        gOutSum -= stackIn?.g ?: 0
        bOutSum -= stackIn?.b ?: 0
        aOutSum -= stackIn?.a ?: 0

        p = x + (minOf(y + radiusPlus1, heightMinus1) * width) shl 2

        rSum += rInSum.also { rInSum += (stackIn?.r ?: 0).also { stackIn?.r = pixels[p] } }
        gSum += gInSum.also { gInSum += (stackIn?.g ?: 0).also { stackIn?.g = pixels[p + 1] } }
        bSum += bInSum.also { bInSum += (stackIn?.b ?: 0).also { stackIn?.b = pixels[p + 2] } }
        aSum += aInSum.also { aInSum += (stackIn?.a ?: 0).also { stackIn?.a = pixels[p + 3] } }

        stackIn = stackIn?.next

        rOutSum += stackOut?.r ?: 0
        gOutSum += stackOut?.g ?: 0
        bOutSum += stackOut?.b ?: 0
        aOutSum += stackOut?.a ?: 0

        rInSum -= pr
        gInSum -= pg
        bInSum -= pb
        aInSum -= pa

        stackOut = stackOut?.next

        yi += width
      }
    }
  }

  private fun correctIndex(index: Int, length: Int): Int = if (index >= length) 0 else index


  private fun clamp(x: Int, a: Int, b: Int): Int = if (x < a) a else if (x > b) b else x
}
