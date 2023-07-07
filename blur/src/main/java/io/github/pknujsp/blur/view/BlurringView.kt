package io.github.pknujsp.blur.view

import IGLSurfaceView
import IGLSurfaceViewLayout
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.opengl.GLES32.*
import android.opengl.GLSurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.FrameLayout
import androidx.core.view.drawToBitmap
import io.github.pknujsp.blur.BlurUtils.getCoordinatesInWindow
import io.github.pknujsp.blur.R
import io.github.pknujsp.blur.natives.NativeGLBlurringImpl
import io.github.pknujsp.blur.renderscript.BlurScript
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.properties.Delegates


class BlurringView private constructor(context: Context) : GLSurfaceView(context), GLSurfaceView.Renderer, IGLSurfaceView, IGLSurfaceViewLayout {
  private var radius by Delegates.notNull<Int>()

  private var collectingView: View? = null
  private var window: Window? = null

  private val collectingViewCoordinatesRect: Rect = Rect(0, 0, 0, 0)
  private val windowRect: Rect = Rect(0, 0, 0, 0)

  private val blurScript = BlurScript(context)

  private val viewMutex = Mutex()

  private val srcBitmapChannel = Channel<Bitmap>(capacity = 30, onBufferOverflow = BufferOverflow.SUSPEND)
  private val blurredBitmapChannel = Channel<Bitmap>(capacity = 30, onBufferOverflow = BufferOverflow.SUSPEND)

  private val copyScope = CoroutineScope(copyDispatcher) + SupervisorJob()
  private val blurScope = CoroutineScope(blurDispatcher) + SupervisorJob()

  private companion object {
    @OptIn(DelicateCoroutinesApi::class) val copyDispatcher = newFixedThreadPoolContext(1, "CopyThread")
    @OptIn(DelicateCoroutinesApi::class) val blurDispatcher = newFixedThreadPoolContext(2, "BlurThread")
  }

  init {
    srcBitmapChannel.consumeAsFlow().map { bitmap ->
      blurScript.instrinsicBlur(bitmap)?.also {
        blurredBitmapChannel.send(it)
        this@BlurringView.queueEvent {
          requestRender()
        }
      }
    }.launchIn(blurScope)
  }

  private val onPreDrawListener = ViewTreeObserver.OnPreDrawListener {
    copyScope.launch {
      if (!viewMutex.isLocked) {
        viewMutex.withLock {
          collectingView?.drawToBitmap()?.run { srcBitmapChannel.send(this) }
        }
      }
    }
    true
  }

  constructor(context: Context, radius: Int) : this(context) {
    this.radius = radius

    id = R.id.blurring_view
    layoutParams = FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT,
    )

    setEGLContextClientVersion(3)
    setRenderer(this)
    renderMode = RENDERMODE_WHEN_DIRTY
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    (context as Activity).window.let { window ->
      this.window = window

      window.decorView.findViewById<View>(androidx.appcompat.R.id.action_bar_root).let { actionBarRoot ->
        collectingView = window.decorView.apply {
          collectingViewCoordinatesRect.set(actionBarRoot.getCoordinatesInWindow(window))
          windowRect.right = window.decorView.width
          windowRect.bottom = window.decorView.height

          blurScript.prepare(radius)
          viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
        }
      }
    }
  }


  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    NativeGLBlurringImpl.onSurfaceCreated(this)
  }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    NativeGLBlurringImpl.onSurfaceChanged(
      width, height,
      collectingViewCoordinatesRect.run {
        intArrayOf(left, top, right, bottom)
      },
      windowRect.run {
        intArrayOf(left, top, right, bottom)
      },
    )
  }

  override fun onDrawFrame(gl: GL10?) {
    blurredBitmapChannel.tryReceive().onSuccess {
      NativeGLBlurringImpl.onDrawFrame(it)
    }
  }

  override fun onPause() {
    if (copyScope.isActive) copyScope.cancel()
    collectingView?.viewTreeObserver?.removeOnPreDrawListener(onPreDrawListener)
    blurScript.onClear()
    super.onPause()
    NativeGLBlurringImpl.onPause()
    collectingView = null
    window = null
  }

  override fun setBackgroundColor(color: Int) {
    super.setBackgroundColor(color)
  }

  override fun setOnTouchListener(l: View.OnTouchListener) {
    super.setOnTouchListener(l)
  }

}
