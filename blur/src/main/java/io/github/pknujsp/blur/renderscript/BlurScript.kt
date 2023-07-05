package io.github.pknujsp.blur.renderscript

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

@Suppress("deprecation")
class BlurScript(context: Context) {
  private var renderScript: RenderScript? = null
  private var blurScript: ScriptIntrinsicBlur? = null
  private var srcAllocation: Allocation? = null
  private var outAllocation: Allocation? = null
  private var listener: DirectBlurListener? = null

  init {
    renderScript = RenderScript.create(context)
    blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
  }

  fun initBlur(blurListener: DirectBlurListener, width: Int, height: Int, radius: Int, resizeRatio: Double) {
    listener = blurListener
    blurScript?.setRadius(radius.toFloat().coerceAtLeast(1f).coerceAtMost(24f))
  }

  fun blur(srcBitmap: Bitmap): Bitmap? =
    blurScript?.run {
      val start = System.currentTimeMillis()
      srcAllocation = Allocation.createFromBitmap(renderScript, srcBitmap)
      outAllocation = Allocation.createTyped(renderScript, srcAllocation?.type)

      setInput(srcAllocation)
      forEach(outAllocation)

      outAllocation?.copyTo(srcBitmap)
      srcAllocation?.destroy()

      println("블러 연산 소요 시간 : ${System.currentTimeMillis() - start}ms")
      srcBitmap
    }


  fun onClear() {
    renderScript?.finish()
    blurScript?.destroy()
    outAllocation?.destroy()
  }
}
