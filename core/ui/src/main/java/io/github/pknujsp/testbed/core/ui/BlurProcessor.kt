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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.plus
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import kotlin.coroutines.resume


class BlurProcessor {

  private val scope = MainScope() + Job()

  private val displayDensity = Resources.getSystem().displayMetrics.density

  private companion object {

    @OptIn(DelicateCoroutinesApi::class)
    private val dispatchers = newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors(), "BlurThreads")

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
    scope.launch(dispatchers.limitedParallelism(1)) {
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

      if (copySuccess) {

      }
    }
  }

  private fun blur(
    blurredBitmap: Bitmap, radius: Int,
  ) {
    val width = blurredBitmap.width
    val height = blurredBitmap.height
    val pixels = SoftReference(IntArray(width * height)).get()!!
    blurredBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val newWidth = width - 1
    val newHeight = height - 1
    val newRadius = radius + 1
    val sumFactor = newRadius * (newRadius + 1) / 2
    val div = 2 * radius + 1

    val valueStack = Array(div - 1) {
      BlurValues()
    }

    val mulSum = mulTable[radius]
    val shgSum = shgTable[radius]

    var yw = 0
    var yi = 0

    val currentValueIndexIn = Index(0, valueStack.size)
    val currentValueIndexOut = Index(newRadius, valueStack.size)

    // height ------------------------------------------------------------------------------

    for (y in 0 until height) {
      val pa = pixels[yi]
      val pr = pixels[yi + 1]
      val pg = pixels[yi + 2]
      val pb = pixels[yi + 3]

      for (i in 0 until newRadius) {
        valueStack[i].alpha = pa
        valueStack[i].red = pr
        valueStack[i].green = pg
        valueStack[i].blue = pb
      }

      currentValueIndexIn.set(newRadius)

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
        val p = yi + (minOf(newWidth, i) shl 2)

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
      }

      currentValueIndexIn.set(0)

      // width -----------------------------------------------------

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

    // width --------------------------------------------------------------

    for (x in 0 until width) {
      yi = x shl 2
      var pa = pixels[yi]
      var pr = pixels[yi + 1]
      var pg = pixels[yi + 2]
      var pb = pixels[yi + 3]

      var aOutSum = newRadius * pa
      var rOutSum = newRadius * pr
      var gOutSum = newRadius * pg
      var bOutSum = newRadius * pb

      var aSum = sumFactor * pa
      var rSum = sumFactor * pr
      var gSum = sumFactor * pg
      var bSum = sumFactor * pb

      currentValueIndexIn.set(0)

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

      var yp = width

      var aInSum = 0
      var rInSum = 0
      var gInSum = 0
      var bInSum = 0

      for (i in 1..radius) {
        yi = (yp + x) shl 2

        val rbs = newRadius - i

        aSum += valueStack[currentValueIndexIn.get()].alpha.also { valueStack[currentValueIndexIn.get()].alpha = pixels[yi] } * rbs
        rSum += valueStack[currentValueIndexIn.get()].red.also { valueStack[currentValueIndexIn.get()].red = pixels[yi + 1] } * rbs
        gSum += valueStack[currentValueIndexIn.get()].green.also { valueStack[currentValueIndexIn.get()].green = pixels[yi + 2] } * rbs
        bSum += valueStack[currentValueIndexIn.get()].blue.also { valueStack[currentValueIndexIn.get()].blue = pixels[yi + 3] } * rbs

        rInSum += pr
        gInSum += pg
        bInSum += pb
        aInSum += pa

        currentValueIndexIn.inc()

        if (i < newHeight) yp += width
      }

      yi = x

      currentValueIndexIn.set(0)
      currentValueIndexOut.set(newRadius)

      // height -----------------------------------------------------------------------------------

      for (y in 0 until height) {
        var p = yi shl 2
        pixels[p] = pa.also {
          pa = (aSum * mulSum) shr shgSum
        }

        if (pa > 0) {
          pa = 255 / pa
          pixels[p + 1] = ((rSum * mulSum) shr shgSum) * pa
          pixels[p + 2] = ((gSum * mulSum) shr shgSum) * pa
          pixels[p + 3] = ((bSum * mulSum) shr shgSum) * pa
        } else {
          pixels[p + 1] = pixels[p + 2].also {
            pixels[p + 2] = 0
            pixels[p + 3] = 0
          }
        }

        aSum -= aOutSum
        rSum -= rOutSum
        gSum -= gOutSum
        bSum -= bOutSum

        aOutSum -= valueStack[currentValueIndexIn.get()].alpha
        rOutSum -= valueStack[currentValueIndexIn.get()].red
        gOutSum -= valueStack[currentValueIndexIn.get()].green
        bOutSum -= valueStack[currentValueIndexIn.get()].blue

        p = x + (minOf(y + newRadius, newHeight) * width) shl 2

        aSum += aInSum.also {
          aInSum += valueStack[currentValueIndexIn.get()].alpha.also {
            valueStack[currentValueIndexIn.get()].alpha = pixels[p + 0]
          }
        }
        rSum += rInSum.also {
          rInSum += valueStack[currentValueIndexIn.get()].red.also {
            valueStack[currentValueIndexIn.get()].red = pixels[p + 1]
          }
        }
        gSum += gInSum.also {
          gInSum += valueStack[currentValueIndexIn.get()].green.also {
            valueStack[currentValueIndexIn.get()].green = pixels[p + 2]
          }
        }
        bSum += bInSum.also {
          bInSum += valueStack[currentValueIndexIn.get()].blue.also {
            valueStack[currentValueIndexIn.get()].blue = pixels[p + 3]
          }
        }

        currentValueIndexIn.inc()

        aOutSum += valueStack[currentValueIndexOut.get()].alpha.apply {
          pa = this
        }
        rOutSum += valueStack[currentValueIndexOut.get()].red.apply {
          pr = this
        }
        gOutSum += valueStack[currentValueIndexOut.get()].green.apply {
          pg = this
        }
        bOutSum += valueStack[currentValueIndexOut.get()].blue.apply {
          pb = this
        }

        rInSum -= pr
        gInSum -= pg
        bInSum -= pb
        aInSum -= pa

        currentValueIndexOut.inc()
        yi += width
      }
    }

    blurredBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
  }
}
