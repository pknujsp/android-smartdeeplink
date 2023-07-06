package io.github.pknujsp.blur.view

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
import io.github.pknujsp.coroutineext.launchSafely
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.properties.Delegates


class BlurringView private constructor(context: Context) : GLSurfaceView(context), GLSurfaceView.Renderer, GLSurfaceLifeCycleListener,
  IGLSurfaceViewLayout {
  private var radius by Delegates.notNull<Int>()

  private var initialized = false

  private var collectingView: View? = null
  private var window: Window? = null

  private val collectingViewCoordinatesRect: Rect = Rect(0, 0, 0, 0)
  private val windowRect: Rect = Rect(0, 0, 0, 0)

  private val blurScript = BlurScript(context)

  private val mutex = Mutex()

  private val queue = ArrayDeque<Bitmap>(60)

  private val mainScope = MainScope()

  private companion object {
    @OptIn(DelicateCoroutinesApi::class) val dispatcher = newFixedThreadPoolContext(3, "BlurringThreadPool")
  }

  private val onPreDrawListener = ViewTreeObserver.OnPreDrawListener {
    if (initialized) {
      mainScope.launchSafely(dispatcher) {
        mutex.withLock {
          val bitmap = collectingView?.drawToBitmap()?.let { blurScript.instrinsicBlur(it) }
          if (bitmap != null) {
            queue.add(bitmap)
            requestRender()
          }
        }
      }.onException { _, t ->
        t.printStackTrace()
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
        collectingView = window.decorView
        collectingView!!.viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
        collectingViewCoordinatesRect.set(actionBarRoot.getCoordinatesInWindow(window))

        windowRect.right = window.decorView.width
        windowRect.bottom = window.decorView.height

        blurScript.prepare(
          radius,
        )

        initialized = true
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
    if (queue.isNotEmpty()) NativeGLBlurringImpl.onDrawFrame(queue.removeFirst())
  }

  override fun onPause() {
    collectingView?.viewTreeObserver?.removeOnPreDrawListener(onPreDrawListener)
    blurScript.onClear()
    super.onPause()
    queue.clear()
    NativeGLBlurringImpl.onPause()
    collectingView = null
    if (mainScope.isActive) mainScope.cancel()
  }

  override fun setBackgroundColor(color: Int) {
    super.setBackgroundColor(color)
  }
}

interface GLSurfaceLifeCycleListener {
  fun onResume()
  fun onPause()
}

interface IGLSurfaceViewLayout {
  fun setBackgroundColor(color: Int)
}
