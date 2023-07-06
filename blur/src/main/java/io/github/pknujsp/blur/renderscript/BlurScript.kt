package io.github.pknujsp.blur.renderscript

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

@Suppress("deprecation")
class BlurScript(context: Context) {
  private var renderScript: RenderScript? = RenderScript.create(context)
  private var blurScript: ScriptIntrinsicBlur? = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
  private var srcAllocation: Allocation? = null
  private var outAllocation: Allocation? = null

  fun prepare(radius: Int) {
    blurScript?.setRadius(radius.toFloat().coerceAtLeast(1f).coerceAtMost(24f))
  }

  fun instrinsicBlur(srcBitmap: Bitmap): Bitmap? = if (blurScript == null) null else try {
    srcAllocation = Allocation.createFromBitmap(renderScript, srcBitmap)
    outAllocation = Allocation.createTyped(renderScript, srcAllocation?.type)

    blurScript!!.setInput(srcAllocation)
    blurScript!!.forEach(outAllocation)

    srcAllocation?.destroy()
    outAllocation?.copyTo(srcBitmap)

    srcBitmap
  } catch (e: Exception) {
    null
  }


  fun onClear() {
    blurScript?.destroy()
    renderScript?.destroy()
    blurScript = null
    renderScript = null
    outAllocation?.destroy()
  }
}
