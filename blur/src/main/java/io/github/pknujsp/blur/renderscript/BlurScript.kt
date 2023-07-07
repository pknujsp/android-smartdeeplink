package io.github.pknujsp.blur.renderscript

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import io.github.pknujsp.blur.toolkit.Toolkit
import kotlin.properties.Delegates

@Suppress("deprecation")
object BlurScript {
  private var initialized = false
  private var renderScript: RenderScript by Delegates.notNull()
  private var blurScript: ScriptIntrinsicBlur? = null
  private var srcAllocation: Allocation? = null
  private var outAllocation: Allocation? = null
  private var radius = 0

  fun init(context: Context) {
    if (!initialized) {
      renderScript = RenderScript.create(context)
      initialized = true
    }
    blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
  }

  fun prepare(radius: Int) {
    this.radius = radius.coerceAtLeast(1).coerceAtMost(25)
    blurScript?.setRadius(radius.toFloat().coerceAtLeast(1f).coerceAtMost(25f))
  }

  fun instrinsicBlur(srcBitmap: Bitmap): Bitmap? = try {
    srcAllocation = Allocation.createFromBitmap(renderScript, srcBitmap)
    outAllocation = Allocation.createTyped(renderScript, srcAllocation?.type)

    blurScript?.setInput(srcAllocation)
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
      renderScript.destroy()
      blurScript = null
      outAllocation?.destroy()
      outAllocation = null
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
