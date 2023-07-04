package io.github.pknujsp.blur.renderscript

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import io.github.pknujsp.blur.DirectBlurListener

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
    blurScript?.setRadius(radius.toFloat())
  }

  fun blur(srcBitmap: Bitmap) = blurScript?.run {
    srcAllocation = Allocation.createFromBitmap(renderScript, srcBitmap)

    outAllocation = Allocation.createTyped(renderScript, srcAllocation?.type)
    setInput(srcAllocation)
    forEach(outAllocation)
    outAllocation?.copyTo(srcBitmap)

    srcAllocation?.destroy()
    srcBitmap
  }


  fun onClear() {
    blurScript?.destroy()
    renderScript?.destroy()
    outAllocation?.destroy()
  }
}
