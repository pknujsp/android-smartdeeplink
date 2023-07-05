package io.github.pknujsp.blur.view

import android.app.Activity
import android.content.Context
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
import io.github.pknujsp.coroutineext.launchSafely
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.newFixedThreadPoolContext
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.properties.Delegates


class BlurringView private constructor(context: Context) : GLSurfaceView(context), GLSurfaceView.Renderer {
  private var resizeRatio by Delegates.notNull<Double>()
  private var radius by Delegates.notNull<Int>()

  private var initialized = false

  private var collectingView: View? = null
  private var window: Window? = null

  private val collectingViewCoordinatesRect: Rect = Rect(0, 0, 0, 0)
  private val windowRect: Rect = Rect(0, 0, 0, 0)


  private companion object {

    val mainScope = MainScope()
    @OptIn(DelicateCoroutinesApi::class) val dispatcher = newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors(), "BlurringThreadPool")
  }

  private val onPreDrawListener = ViewTreeObserver.OnPreDrawListener {
    if (initialized) {
      mainScope.launchSafely(Dispatchers.Default) {
        collectingView?.drawToBitmap()?.let { bitmap ->
          NativeGLBlurringImpl.blurAndDrawFrame(bitmap)
          requestRender()
        }
      }.onException { _, t ->
        t.printStackTrace()
      }
    }
    true
  }

  constructor(context: Context, resizeRatio: Double, radius: Int) : this(context) {
    this.resizeRatio = resizeRatio
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

        NativeGLBlurringImpl.prepareBlur(
          collectingViewCoordinatesRect.width(),
          collectingViewCoordinatesRect.height(),
          radius,
          resizeRatio,
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
    NativeGLBlurringImpl.onDrawFrame()
  }


  override fun onPause() {
    super.onPause()
    NativeGLBlurringImpl.onPause()
    collectingView?.viewTreeObserver?.removeOnPreDrawListener(onPreDrawListener)
  }
}
