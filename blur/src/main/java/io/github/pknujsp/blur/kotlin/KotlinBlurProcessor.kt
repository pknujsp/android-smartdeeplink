package io.github.pknujsp.blur.kotlin

import android.graphics.Bitmap
import android.util.Size
import io.github.pknujsp.blur.SharedBlurAttrs
import io.github.pknujsp.blur.processor.BlurWorkerImpl.launch
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.ref.WeakReference
import kotlin.coroutines.resume


internal object KotlinBlurProcessor : SharedBlurAttrs() {

  private val mulTable =
    intArrayOf(
      512, 512, 456, 512, 328, 456, 335, 512, 405, 328, 271, 456, 388, 335, 292, 512,
      454, 405, 364, 328, 298, 271, 496, 456, 420, 388, 360, 335, 312, 292, 273, 512,
      482, 454, 428, 405, 383, 364, 345, 328, 312, 298, 284, 271, 259, 496, 475, 456,
      437, 420, 404, 388, 374, 360, 347, 335, 323, 312, 302, 292, 282, 273, 265, 512,
      497, 482, 468, 454, 441, 428, 417, 405, 394, 383, 373, 364, 354, 345, 337, 328,
      320, 312, 305, 298, 291, 284, 278, 271, 265, 259, 507, 496, 485, 475, 465, 456,
      446, 437, 428, 420, 412, 404, 396, 388, 381, 374, 367, 360, 354, 347, 341, 335,
      329, 323, 318, 312, 307, 302, 297, 292, 287, 282, 278, 273, 269, 265, 261, 512,
      505, 497, 489, 482, 475, 468, 461, 454, 447, 441, 435, 428, 422, 417, 411, 405,
      399, 394, 389, 383, 378, 373, 368, 364, 359, 354, 350, 345, 341, 337, 332, 328,
      324, 320, 316, 312, 309, 305, 301, 298, 294, 291, 287, 284, 281, 278, 274, 271,
      268, 265, 262, 259, 257, 507, 501, 496, 491, 485, 480, 475, 470, 465, 460, 456,
      451, 446, 442, 437, 433, 428, 424, 420, 416, 412, 408, 404, 400, 396, 392, 388,
      385, 381, 377, 374, 370, 367, 363, 360, 357, 354, 350, 347, 344, 341, 338, 335,
      332, 329, 326, 323, 320, 318, 315, 312, 310, 307, 304, 302, 299, 297, 294, 292,
      289, 287, 285, 282, 280, 278, 275, 273, 271, 269, 267, 265, 263, 261, 259,
    )


  private val shrTable =
    intArrayOf(
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


  private val availableThreads = Runtime.getRuntime().availableProcessors()

  @OptIn(DelicateCoroutinesApi::class) private val dispatchers = newFixedThreadPoolContext(availableThreads, "StackBlurThreads")


  private data class SharedValues(
    val widthMax: Int,
    val heightMax: Int,
    val divisor: Int,
    val multiplySum: Int,
    val shiftSum: Int,
  )


  suspend fun blur(
    srcBitmap: Bitmap, radius: Int,
  ): Bitmap? = suspendCancellableCoroutine { continuation ->
    launch(dispatchers) {
      try {
        val width = srcBitmap.width.run { if (this % 2 == 0) this else this - 1 }
        val height = srcBitmap.height.run { if (this % 2 == 0) this else this - 1 }

        val pixels = WeakReference(IntArray(width * height)).get()!!
        srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val r = srcBitmap.height / availableThreads
        val c = srcBitmap.width / availableThreads

        val newRadius = radius.coerceAtLeast(MIN_RADIUS)

        val sharedValues = WeakReference(
          SharedValues(
            widthMax = width - 1,
            heightMax = height - 1,
            divisor = newRadius * 2 + 1,
            multiplySum = mulTable[newRadius],
            shiftSum = shrTable[newRadius],
          ),
        ).get()!!

        val reducedSize = Size(width, height)

        val rowWorks = Array(availableThreads) { thread ->
          ProcessingRow(sharedValues, pixels, reducedSize, thread * r, (thread + 1) * r - 1, newRadius)
        }
        val columnWorks = Array(availableThreads) { thread ->
          ProcessingColumn(sharedValues, pixels, reducedSize, thread * c, (thread + 1) * c - 1, newRadius)
        }

        rowWorks.map { rowWork ->
          async {
            rowWork()
          }
        }.awaitAll()

        columnWorks.map { columnWork ->
          async {
            columnWork()
          }
        }.awaitAll()

        srcBitmap.recycle()
        srcBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        continuation.resume(srcBitmap)
      } catch (e: Exception) {
        continuation.resume(null)
      }
    }
  }


  private class ProcessingRow(
    private val sharedValues: SharedValues,
    private val imagePixels: IntArray,
    private val bitmapSize: Size,
    private val startRow: Int,
    private val endRow: Int,
    private val blurRadius: Int,
  ) {
    operator fun invoke() {
      var sumRed: Long
      var sumGreen: Long
      var sumBlue: Long

      var sumInputRed: Long
      var sumInputGreen: Long
      var sumInputBlue: Long

      var sumOutputRed: Long
      var sumOutputGreen: Long
      var sumOutputBlue: Long

      var startPixelIndex: Int
      var inPixelIndex: Int
      var outputPixelIndex: Int
      var stackStart: Int
      var stackPointer: Int
      var stackIndex: Int
      var colOffset: Int

      var red: Int
      var green: Int
      var blue: Int

      var multiplier: Int

      sharedValues.run {
        val blurStack = WeakReference(IntArray(divisor)).get()!!

        for (row in startRow..endRow) {
          sumRed = 0
          sumGreen = 0
          sumBlue = 0

          sumInputRed = 0
          sumInputGreen = 0
          sumInputBlue = 0

          sumOutputRed = 0
          sumOutputGreen = 0
          sumOutputBlue = 0

          startPixelIndex = bitmapSize.width * row
          inPixelIndex = startPixelIndex
          stackIndex = blurRadius

          for (rad in 0..blurRadius) {
            stackIndex = rad
            var pixel = imagePixels[startPixelIndex]
            blurStack[stackIndex] = pixel

            red = ((pixel ushr 16) and 0xff)
            green = ((pixel ushr 8) and 0xff)
            blue = (pixel and 0xff)

            multiplier = rad + 1

            sumRed += red * multiplier
            sumGreen += green * multiplier
            sumBlue += blue * multiplier

            sumOutputRed += red
            sumOutputGreen += green
            sumOutputBlue += blue

            if (rad >= 1) {
              if (rad <= widthMax) inPixelIndex++
              stackIndex = rad + blurRadius
              pixel = imagePixels[inPixelIndex]
              blurStack[stackIndex] = pixel

              multiplier = blurRadius + 1 - rad

              red = ((pixel ushr 16) and 0xff)
              green = ((pixel ushr 8) and 0xff)
              blue = (pixel and 0xff)

              sumRed += red * multiplier
              sumGreen += green * multiplier
              sumBlue += blue * multiplier

              sumInputRed += red
              sumInputGreen += green
              sumInputBlue += blue
            }
          }

          stackStart = blurRadius
          stackPointer = blurRadius
          colOffset = blurRadius.coerceAtMost(widthMax)
          inPixelIndex = colOffset + row * bitmapSize.width
          outputPixelIndex = startPixelIndex

          for (col in 0 until bitmapSize.width) {
            imagePixels[outputPixelIndex] =
              ((imagePixels[outputPixelIndex] and 0xff000000.toInt()) or ((((sumRed.toInt() * multiplySum) ushr shiftSum) and 0xff) shl 16) or ((((sumGreen.toInt() * multiplySum) ushr shiftSum) and 0xff) shl 8) or (((sumBlue.toInt() * multiplySum) ushr shiftSum) and 0xff))

            outputPixelIndex++
            sumRed -= sumOutputRed
            sumGreen -= sumOutputGreen
            sumBlue -= sumOutputBlue

            stackIndex = (stackPointer + divisor - blurRadius).let {
              stackStart = if (it >= divisor) it - divisor else it
              stackStart
            }

            sumOutputRed -= ((blurStack[stackIndex] ushr 16) and 0xff)
            sumOutputGreen -= ((blurStack[stackIndex] ushr 8) and 0xff)
            sumOutputBlue -= (blurStack[stackIndex] and 0xff)

            if (colOffset < widthMax) {
              inPixelIndex++
              colOffset++
            }

            val pixel = imagePixels[inPixelIndex]
            blurStack[stackIndex] = pixel

            red = ((pixel ushr 16) and 0xff)
            green = ((pixel ushr 8) and 0xff)
            blue = (pixel and 0xff)

            sumInputRed += red
            sumInputGreen += green
            sumInputBlue += blue

            sumRed += sumInputRed
            sumGreen += sumInputGreen
            sumBlue += sumInputBlue

            if (++stackPointer >= divisor) stackPointer = 0
            stackIndex = stackPointer

            blurStack[stackIndex].let { stackPixel ->
              red = ((stackPixel ushr 16) and 0xff)
              green = ((stackPixel ushr 8) and 0xff)
              blue = (stackPixel and 0xff)

              sumOutputRed += red
              sumOutputGreen += green
              sumOutputBlue += blue

              sumInputRed -= red
              sumInputGreen -= green
              sumInputBlue -= blue
            }

          }
        }
      }
    }
  }

  private class ProcessingColumn(
    private val sharedValues: SharedValues,
    private val imagePixels: IntArray,
    private val bitmapSize: Size,
    private val startColumn: Int,
    private val endColumn: Int,
    private val blurRadius: Int,
  ) {
    operator fun invoke() {
      var xOffset: Int
      var yOffset: Int
      var blurStackIndex: Int
      var stackStart: Int
      var stackIndex: Int
      var stackPointer: Int

      var sourceIndex: Int
      var destinationIndex: Int

      var sumRed: Long
      var sumGreen: Long
      var sumBlue: Long
      var sumInputRed: Long
      var sumInputGreen: Long
      var sumInputBlue: Long
      var sumOutputRed: Long
      var sumOutputGreen: Long
      var sumOutputBlue: Long

      var red: Int
      var green: Int
      var blue: Int

      sharedValues.run {
        val blurStack = IntArray(divisor)

        for (col in startColumn..endColumn) {
          sumOutputBlue = 0
          sumOutputGreen = 0
          sumOutputRed = 0

          sumInputBlue = 0
          sumInputGreen = 0
          sumInputRed = 0

          sumBlue = 0
          sumGreen = 0
          sumRed = 0

          sourceIndex = col

          for (rad in 0..blurRadius) {
            stackIndex = rad
            var pixel = imagePixels[sourceIndex]
            blurStack[stackIndex] = pixel

            red = ((pixel ushr 16) and 0xff)
            green = ((pixel ushr 8) and 0xff)
            blue = (pixel and 0xff)

            var multiplier = rad + 1

            sumRed += red * multiplier
            sumGreen += green * multiplier
            sumBlue += blue * multiplier

            sumOutputRed += red
            sumOutputGreen += green
            sumOutputBlue += blue

            if (rad >= 1) {
              if (rad <= heightMax) sourceIndex += bitmapSize.width

              stackIndex = rad + blurRadius
              pixel = imagePixels[sourceIndex]
              blurStack[stackIndex] = pixel

              multiplier = blurRadius + 1 - rad

              red = ((pixel ushr 16) and 0xff)
              green = ((pixel ushr 8) and 0xff)
              blue = (pixel and 0xff)

              sumRed += red * multiplier
              sumGreen += green * multiplier
              sumBlue += blue * multiplier

              sumInputRed += red
              sumInputGreen += green
              sumInputBlue += blue
            }
          }

          stackPointer = blurRadius
          yOffset = minOf(blurRadius, heightMax)
          sourceIndex = col + yOffset * bitmapSize.width
          destinationIndex = col

          for (y in 0 until bitmapSize.height) {
            imagePixels[destinationIndex] =
              ((imagePixels[destinationIndex] and 0xff000000.toInt()) or ((((sumRed.toInt() * multiplySum) ushr shiftSum) and 0xff) shl 16) or ((((sumGreen.toInt() * multiplySum) ushr shiftSum.toInt()) and 0xff) shl 8) or (((sumBlue.toInt() * multiplySum) ushr shiftSum.toInt()) and 0xff))

            destinationIndex += bitmapSize.width
            sumRed -= sumOutputRed
            sumGreen -= sumOutputGreen
            sumBlue -= sumOutputBlue

            stackIndex = (stackPointer + divisor - blurRadius).let {
              stackStart = if (it >= divisor) it - divisor else it
              stackStart
            }

            sumOutputRed -= ((blurStack[stackIndex] ushr 16) and 0xff)
            sumOutputGreen -= ((blurStack[stackIndex] ushr 8) and 0xff)
            sumOutputBlue -= (blurStack[stackIndex] and 0xff)

            if (yOffset < heightMax) {
              sourceIndex += bitmapSize.width
              yOffset++
            }

            blurStack[stackIndex] = imagePixels[sourceIndex]

            sumInputRed += ((imagePixels[sourceIndex] ushr 16) and 0xff)
            sumInputGreen += ((imagePixels[sourceIndex] ushr 8) and 0xff)
            sumInputBlue += (imagePixels[sourceIndex] and 0xff)

            sumRed += sumInputRed
            sumGreen += sumInputGreen
            sumBlue += sumInputBlue

            if (++stackPointer >= divisor) stackPointer = 0
            stackIndex = stackPointer

            blurStack[stackIndex].let { stackPixel ->
              red = ((stackPixel ushr 16) and 0xff)
              green = ((stackPixel ushr 8) and 0xff)
              blue = (stackPixel and 0xff)

              sumOutputRed += red
              sumOutputGreen += green
              sumOutputBlue += blue

              sumInputRed -= red
              sumInputGreen -= green
              sumInputBlue -= blue
            }
          }
        }
      }
    }
  }

}
