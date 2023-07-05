package io.github.pknujsp.blur.view

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.opengl.GLES32.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.FrameLayout
import androidx.core.view.drawToBitmap
import io.github.pknujsp.blur.BlurUtils.getCoordinatesInWindow
import io.github.pknujsp.blur.DirectBlurListener
import io.github.pknujsp.blur.R
import io.github.pknujsp.blur.natives.NativeGLBlurringImpl
import io.github.pknujsp.blur.processor.GlobalBlurProcessorImpl
import io.github.pknujsp.coroutineext.launchSafely
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.properties.Delegates


class BlurringView private constructor(context: Context) : GLSurfaceView(context), DirectBlurListener, GLSurfaceView.Renderer {
  private var resizeRatio by Delegates.notNull<Double>()
  private var radius by Delegates.notNull<Int>()

  private var initialized = false

  private var collectingView: View? = null
  private var blurredBitmap: Bitmap? = null
  private var window: Window? = null

  private val collectingViewCoordinatesRect: Rect = Rect(0, 0, 0, 0)
  private val windowRect: Rect = Rect(0, 0, 0, 0)

  private val mutex = Mutex()

  private val nativeGLBlurringImpl = NativeGLBlurringImpl()

  private companion object {

    val mainScope = MainScope()
    @OptIn(DelicateCoroutinesApi::class) private val dispatcher =
      newFixedThreadPoolContext(Runtime.getRuntime().availableProcessors(), "BlurringThreadPool")

    val blurProcessor: BlurringViewProcessor = GlobalBlurProcessorImpl

    const val vertexShader = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        attribute vec2 a_texCoord;
        varying vec2 v_texCoord;
        void main() {
          gl_Position = uMVPMatrix * vPosition;
          v_texCoord = a_texCoord;
        }
        """

    const val fragmentShader = """
        precision mediump float;
        varying vec2 v_texCoord;
        uniform sampler2D s_texture;
        void main() {
          gl_FragColor = texture2D(s_texture, v_texCoord);
        }
        """

    val vertices = floatArrayOf(
      -1.0f, -1.0f, 0f,  // bottom left
      1.0f, -1.0f, 0f,  // bottom right
      1.0f, 1.0f, 0f,  // top right
      -1.0f, 1.0f, 0f,  // top left
    )

    val indices = byteArrayOf(
      0, 1, 2,
      2, 3, 0,
    )

    val vertexBuffer = vertices.createBuffer(4)
    val indexBuffer: ByteBuffer = ByteBuffer.allocateDirect(indices.size).apply {
      put(indices)
      position(0)
    }

    var positionHandle: Int = 0
    var uvHandle: Int = 0
    var program: Int = 0
    val textures = IntArray(1)
    var mvpMatrixHandle: Int = 0

    val vpMatrix: FloatArray = FloatArray(16)
    val modelMatrix = FloatArray(16)
    val mvpMatrix = FloatArray(16)
  }

  private val onPreDrawListener = ViewTreeObserver.OnPreDrawListener {
    if (initialized) {
      mainScope.launchSafely(dispatcher) {
        mutex.withLock {
          collectingView?.drawToBitmap()?.let { bitmap ->
            blurredBitmap = blurProcessor.blur(bitmap)
            if (blurredBitmap != null) requestRender()
          }
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

    Matrix.setIdentityM(vpMatrix, 0)
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

        blurProcessor.initBlur(
          context,
          this@BlurringView,
          Size(collectingViewCoordinatesRect.width(), collectingViewCoordinatesRect.height()),
          radius,
          resizeRatio,
        )

        initialized = true
      }
    }
  }


  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    NativeGLBlurringImpl.onSurfaceCreated()
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
    NativeGLBlurringImpl.onDrawFrame(blurredBitmap)
  }

  private fun loadShader(type: Int, shaderCode: String): Int = glCreateShader(type).also { shader ->
    glShaderSource(shader, shaderCode)
    glCompileShader(shader)
  }

  override fun onPause() {
    super.onPause()
    collectingView?.viewTreeObserver?.removeOnPreDrawListener(onPreDrawListener)
    blurProcessor.onClear()
  }

  override fun onBlurred(bitmap: Bitmap?) {
    TODO("Not yet implemented")
  }
}

private fun FloatArray.createBuffer(capacity: Int): FloatBuffer = ByteBuffer.allocateDirect(size * capacity).run {
  order(ByteOrder.nativeOrder())
  asFloatBuffer().apply {
    put(this@createBuffer)
    position(0)
  }
}
