package io.github.pknujsp.blur

import android.content.Context
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

@Suppress("deprecation")
object BlurScript {
  private var _renderScript: RenderScript? = null
  private val renderScript: RenderScript get() = _renderScript!!

  private var _blurScript: ScriptIntrinsicBlur? = null
  private val blurScript: ScriptIntrinsicBlur get() = _blurScript!!

  fun init(context: Context) {
    takeIf { _renderScript == null }?.run {
      _renderScript = RenderScript.create(context)
      _blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    }
  }


}
