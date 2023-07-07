package io.github.pknujsp.blur.renderscript

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import io.github.pknujsp.blur.toolkit.Toolkit

@Suppress("deprecation")
class BlurScript(context: Context) {
  private var renderScript: RenderScript? = RenderScript.create(context)
  private var blurScript: ScriptIntrinsicBlur? = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
  private var srcAllocation: Allocation? = null
  private var outAllocation: Allocation? = null
  private var radius = 0

  fun prepare(radius: Int) {
    this.radius = radius.coerceAtLeast(1).coerceAtMost(25)
    //blurScript?.setRadius(radius.toFloat().coerceAtLeast(1f).coerceAtMost(25f))
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

  fun blur(srcBitmap: Bitmap): Bitmap = Toolkit.blur(srcBitmap, radius)


  fun onClear() {
    try {
      blurScript?.destroy()
      renderScript?.destroy()
      outAllocation?.destroy()

      blurScript = null
      renderScript = null
      outAllocation = null
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
