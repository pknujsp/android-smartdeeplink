package io.github.pknujsp.blur.renderscript

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

@Suppress("deprecation")
class BlurScript(context: Context) {
  private val renderScript: RenderScript = RenderScript.create(context)
  private val blurScript: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
  private var srcAllocation: Allocation? = null
  private var outAllocation: Allocation? = null

  fun prepare(radius: Int) {
    blurScript.setRadius(radius.toFloat().coerceAtLeast(1f).coerceAtMost(24f))
  }

  fun instrinsicBlur(srcBitmap: Bitmap): Bitmap? = try {
    srcAllocation = Allocation.createFromBitmap(renderScript, srcBitmap)
    outAllocation = Allocation.createTyped(renderScript, srcAllocation?.type)

    blurScript.setInput(srcAllocation)
    blurScript.forEach(outAllocation)

    srcAllocation?.destroy()
    outAllocation?.copyTo(srcBitmap)

    srcBitmap
  } catch (e: Exception) {
    null
  }


  fun onClear() {
    blurScript.destroy()
    renderScript.destroy()
    outAllocation?.destroy()
  }
}
