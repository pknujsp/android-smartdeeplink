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

    @OptIn(DelicateCoroutinesApi::class) private val dispatchers = newFixedThreadPoolContext(3, "BlurThreads")

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

    fun getAndInc(): Int {
      val value = index
      inc()
      return value
    }
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

      val blurredBitmap: Bitmap = WeakReference(Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)).get()!!

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
        blur(blurredBitmap, radiusPx)
      }
    }
  }

  private fun blur(
    blurredBitmap: Bitmap, radius: Int,
  ) {
    val width = blurredBitmap.width
    val height = blurredBitmap.height
    val pixels = SoftReference(IntArray(width * height * 3)).get()!!
    blurredBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    val newWidth = width - 1
    val newHeight = height - 1
    val newRadius = radius + 1
    val sumFactor = newRadius * (newRadius + 1) / 2
    val div = 2 * radius + 1

    val valueStack = SoftReference(
      Array(div) {
        BlurValues()
      },
    ).get()!!

    val mulSum = mulTable[radius]
    val shgSum = shgTable[radius]

    var yw = 0
    var pixelSetStartIdx = 0

    val currentValueIndexIn = Index(0, valueStack.size)
    val currentValueIndexOut = Index(newRadius, valueStack.size)

    var alphaValue = 0
    var redValue = 0
    var greenValue = 0
    var blueValue = 0

    var aSum = 0
    var rSum = 0
    var gSum = 0
    var bSum = 0

    var aSumIn = 0
    var rSumIn = 0
    var gSumIn = 0
    var bSumIn = 0

    var aSumOut = 0
    var rSumOut = 0
    var gSumOut = 0
    var bSumOut = 0

    // height ------------------------------------------------------------------------------

    for (row in 0 until height) {
      println("pixels : ${pixels.size} firstElementIdx : $pixelSetStartIdx, y : $row / $height")

      alphaValue = pixels[pixelSetStartIdx]
      redValue = pixels[pixelSetStartIdx + 1]
      greenValue = pixels[pixelSetStartIdx + 2]
      blueValue = pixels[pixelSetStartIdx + 3]

      currentValueIndexIn.set(0)

      repeat(newRadius) {
        valueStack[currentValueIndexIn.getAndInc()].apply {
          alpha = alphaValue
          red = redValue
          green = greenValue
          blue = blueValue
        }
      }

      aSumIn = 0
      rSumIn = 0
      gSumIn = 0
      bSumIn = 0

      aSumOut = newRadius * alphaValue
      rSumOut = newRadius * redValue
      gSumOut = newRadius * greenValue
      bSumOut = newRadius * blueValue

      aSum = sumFactor * alphaValue
      rSum = sumFactor * redValue
      gSum = sumFactor * greenValue
      bSum = sumFactor * blueValue

      for (i in 1 until newRadius) {
        val p = pixelSetStartIdx + (minOf(newWidth, i) shl 2)
        val rbs = newRadius - i

        valueStack[currentValueIndexIn.getAndInc()].run {
          pixels[p].let {
            alpha = it
            aSum += it * rbs
            aSumIn += it
          }
          pixels[p + 1].let {
            red = it
            rSum += it * rbs
            rSumIn += it
          }
          pixels[p + 2].let {
            green = it
            gSum += it * rbs
            gSumIn += it
          }
          pixels[p + 3].let {
            blue = it
            bSum += it * rbs
            bSumIn += it
          }
        }
      }

      currentValueIndexIn.set(0)
      currentValueIndexOut.set(newRadius)

      // width -----------------------------------------------------

      for (col in 0 until width) {
        val paInit = (aSum * mulSum) shr shgSum
        pixels[pixelSetStartIdx] = paInit

        if (paInit != 0) {
          val a = 255 / paInit
          pixels[pixelSetStartIdx + 1] = ((rSum * mulSum) shr shgSum) * a
          pixels[pixelSetStartIdx + 2] = ((gSum * mulSum) shr shgSum) * a
          pixels[pixelSetStartIdx + 3] = ((bSum * mulSum) shr shgSum) * a
        } else {
          pixels[pixelSetStartIdx + 1] = 0
          pixels[pixelSetStartIdx + 2] = 0
          pixels[pixelSetStartIdx + 3] = 0
        }

        aSum -= aSumOut
        rSum -= rSumOut
        gSum -= gSumOut
        bSum -= bSumOut

        val p = (yw + minOf(col + radius + 1, newWidth)) shl 2

        valueStack[currentValueIndexIn.getAndInc()].run {
          aSumOut -= alpha
          rSumOut -= red
          gSumOut -= green
          bSumOut -= blue

          pixels[p].let {
            alpha = it
            aSumIn += it
            aSum += aSumIn
          }
          pixels[p + 1].let {
            red = it
            rSumIn += it
            rSum += rSumIn
          }
          pixels[p + 2].let {
            green = it
            gSumIn += it
            gSum += gSumIn
          }
          pixels[p + 3].let {
            blue = it
            bSumIn += it
            bSum += bSumIn
          }
        }

        valueStack[currentValueIndexOut.getAndInc()].run {
          aSumOut += alpha
          rSumOut += red
          gSumOut += green
          bSumOut += blue

          aSumIn -= alpha
          rSumIn -= red
          gSumIn -= green
          bSumIn -= blue
        }

        pixelSetStartIdx += 4
      }
      yw += width
    }

    // width --------------------------------------------------------------

    for (x in 0 until width) {
      pixelSetStartIdx = x shl 2

      alphaValue = pixels[pixelSetStartIdx]
      redValue = pixels[pixelSetStartIdx + 1]
      greenValue = pixels[pixelSetStartIdx + 2]
      blueValue = pixels[pixelSetStartIdx + 3]

      aSumOut = newRadius * alphaValue
      rSumOut = newRadius * redValue
      gSumOut = newRadius * greenValue
      bSumOut = newRadius * blueValue

      aSum = sumFactor * alphaValue
      rSum = sumFactor * redValue
      gSum = sumFactor * greenValue
      bSum = sumFactor * blueValue

      currentValueIndexIn.set(0)

      repeat(newRadius) {
        valueStack[currentValueIndexIn.getAndInc()].run {
          alpha = alphaValue
          red = redValue
          green = greenValue
          blue = blueValue
        }
      }

      var yp = width

      aSumIn = 0
      rSumIn = 0
      gSumIn = 0
      bSumIn = 0

      for (i in 1..radius) {
        pixelSetStartIdx = (yp + x) shl 2
        val rbs = newRadius - i
        valueStack[currentValueIndexIn.getAndInc()].run {
          pixels[pixelSetStartIdx].let {
            alpha = it
            alphaValue = it
            aSum += it * rbs
            aSumIn += it
          }
          pixels[pixelSetStartIdx + 1].let {
            red = it
            redValue = it
            rSum += it * rbs
            rSumIn += it
          }
          pixels[pixelSetStartIdx + 2].let {
            green = it
            greenValue = it
            gSum += it * rbs
            gSumIn += it
          }
          pixels[pixelSetStartIdx + 3].let {
            blue = it
            blueValue = it
            bSum += it * rbs
            bSumIn += it
          }
        }
        if (i < newHeight) yp += width
      }

      pixelSetStartIdx = x

      currentValueIndexIn.set(0)
      currentValueIndexOut.set(newRadius)

      // height -----------------------------------------------------------------------------------

      for (y in 0 until height) {
        var p = pixelSetStartIdx shl 2
        ((aSum * mulSum) shr shgSum).run {
          pixels[p] = this
          alphaValue = this
        }

        if (alphaValue > 0) {
          alphaValue = 255 / alphaValue
          pixels[p + 1] = ((rSum * mulSum) shr shgSum) * alphaValue
          pixels[p + 2] = ((gSum * mulSum) shr shgSum) * alphaValue
          pixels[p + 3] = ((bSum * mulSum) shr shgSum) * alphaValue
        } else {
          pixels[p + 1] = 0
          pixels[p + 2] = 0
          pixels[p + 3] = 0
        }

        aSum -= aSumOut
        rSum -= rSumOut
        gSum -= gSumOut
        bSum -= bSumOut

        valueStack[currentValueIndexIn.getAndInc()].run {
          aSumIn -= alpha
          rSumIn -= red
          gSumIn -= green
          bSumIn -= blue

          p = (x + (minOf(y + newRadius, newHeight) * width)) shl 2

          pixels[p].let {
            alpha = it
            aSumIn += it
            aSum += aSumIn
          }
          pixels[p + 1].let {
            red = it
            rSumIn += it
            rSum += rSumIn
          }
          pixels[p + 2].let {
            green = it
            gSumIn += it
            gSum += gSumIn
          }
          pixels[p + 3].let {
            blue = it
            bSumIn += it
            bSum += bSumIn
          }
        }

        valueStack[currentValueIndexOut.getAndInc()].run {
          alphaValue = alpha
          redValue = red
          greenValue = green
          blueValue = blue

          aSumOut += alphaValue
          rSumOut += redValue
          gSumOut += greenValue
          bSumOut += blueValue

          rSumIn -= redValue
          gSumIn -= greenValue
          bSumIn -= blueValue
          aSumIn -= alphaValue
        }
        pixelSetStartIdx += width
      }
    }

    blurredBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
  }
}
