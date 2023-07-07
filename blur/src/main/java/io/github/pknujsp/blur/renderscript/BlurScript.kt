package io.github.pknujsp.blur.renderscript

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import io.github.pknujsp.blur.toolkit.Toolkit
import kotlin.properties.Delegates

@Suppress("deprecation")
object BlurScript : Application() {
  private var initialized = false
  private var renderScript: RenderScript by Delegates.notNull()
  private var blurScript: ScriptIntrinsicBlur by Delegates.notNull()
  private var srcAllocation: Allocation? = null
  private var outAllocation: Allocation? = null
  private var radius = 0

  fun init(context: Context) {
    if (!initialized) {
      renderScript = RenderScript.create(context)
      blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
      initialized = true
    }
  }

  fun prepare(radius: Int) {
    blurScript.setRadius(radius.toFloat().coerceAtLeast(1f).coerceAtMost(25f))
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

  fun blur(srcBitmap: Bitmap): Bitmap = Toolkit.blur(srcBitmap, radius)


  fun onClear() {
    try {
      blurScript.destroy()
      renderScript.destroy()
      outAllocation?.destroy()
      outAllocation = null
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
