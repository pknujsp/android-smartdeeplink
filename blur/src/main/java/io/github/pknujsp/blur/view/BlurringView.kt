package io.github.pknujsp.blur.view

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES30
import android.opengl.GLES30.glGetError
import android.opengl.GLSurfaceView
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.FrameLayout
import io.github.pknujsp.blur.BlurUtils.getCoordinatesInWindow
import io.github.pknujsp.blur.DirectBlurListener
import io.github.pknujsp.blur.R
import io.github.pknujsp.blur.processor.GlobalBlurProcessorImpl
import io.github.pknujsp.coroutineext.launchSafely
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.newSingleThreadContext
import java.nio.Buffer
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

  private var contentView: View? = null
  private var blurredBitmap: Bitmap? = null
  private var window: Window? = null

  private var renderNum = 0L

  private val originalCoordinatesRect: Rect = Rect(0, 0, 0, 0)
  private val dstCoordinatesRect: Rect = Rect(0, 0, 0, 0)

  private var lastStartTime = System.currentTimeMillis()

  private companion object {
    val mainScope = MainScope()
    @OptIn(DelicateCoroutinesApi::class) private val dispatcher = newSingleThreadContext("BlurringView")

    val blurProcessor: BlurringViewProcessor = GlobalBlurProcessorImpl

    val vertexShader = "" + "attribute vec4 vPosition;" + "void main() {" + "  gl_Position = vPosition;" + "}"

    val fragmentShader = "" + "precision mediump float;" + "void main() {" + "  gl_FragColor = vec4(0.0, 1.0, 0.0, 1.0);" + "}"

    val triangleVertices: Buffer = floatArrayOf(
      0.0f, 0.5f,
      -0.5f, -0.5f,
      0.5f, -0.5f,
    ).createBuffer()

    var vPositionHandle: Int = 0
    var program: Int = 0
    var grey = 0f
    var flag = false
  }

  private val onDrawListener = ViewTreeObserver.OnPreDrawListener {
    if (initialized) {
      mainScope.launchSafely(dispatcher) {
        contentView?.drawingCache?.run {
          renderNum++
          blurProcessor.blur(this)
        }
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
    alpha = 0.4f

    setEGLContextClientVersion(3)
    setRenderer(this)
    renderMode = RENDERMODE_WHEN_DIRTY
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    (context as Activity).window.let { window ->
      this.window = window

      window.decorView.findViewById<View>(android.R.id.content).let { contentView ->
        this.contentView = contentView
        contentView.isDrawingCacheEnabled = true
        contentView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH

        contentView.viewTreeObserver.addOnPreDrawListener(onDrawListener)
        originalCoordinatesRect.set(contentView.getCoordinatesInWindow(window))
        dstCoordinatesRect.set(0, 0, originalCoordinatesRect.width(), originalCoordinatesRect.height())

        blurProcessor.initBlur(
          context,
          this@BlurringView,
          Size(originalCoordinatesRect.width(), originalCoordinatesRect.height()),
          radius,
          resizeRatio,
        )

        initialized = true
      }
    }
  }


  override fun onBlurred(bitmap: Bitmap?) {
    println("onBlurred : $renderNum, ${System.currentTimeMillis() - lastStartTime}MS")
    lastStartTime = System.currentTimeMillis()
    blurredBitmap = bitmap
    requestRender()
  }


  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    program = GLES30.glCreateProgram().also {
      val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShader.trimIndent())
      val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShader.trimIndent())

      GLES30.glAttachShader(it, vertexShader)
      GLES30.glAttachShader(it, fragmentShader)
      GLES30.glLinkProgram(it)
      val linkStatus = IntArray(1)
      GLES30.glGetProgramiv(it, GLES30.GL_LINK_STATUS, linkStatus, 0)
    }

    vPositionHandle = glGetAttribLocation(program, "vPosition")

    println("onSurfaceCreated : ${glGetError()}")
  }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    GLES30.glViewport(0, 0, dstCoordinatesRect.width(), dstCoordinatesRect.height())
  }

  override fun onDrawFrame(gl: GL10?) {
    blurredBitmap?.run {
      if (grey > 1.0f || grey < 0.0f) {
        flag = !flag
      }
      if (flag) {
        grey += 0.01f
      } else {
        grey -= 0.01f
      }

      GLES30.glClearColor(grey, grey, grey, 1.0f)

      GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)

      GLES30.glUseProgram(program)

      GLES30.glVertexAttribPointer(vPositionHandle, 2, GLES30.GL_FLOAT, false, 0, triangleVertices)

      GLES30.glEnableVertexAttribArray(vPositionHandle)

      GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
    }
    println("onDrawFrame : ${glGetError()}")
  }

  private fun loadShader(type: Int, shaderCode: String): Int = GLES30.glCreateShader(type).also { shader ->
    GLES30.glShaderSource(shader, shaderCode)
    GLES30.glCompileShader(shader)
  }


  override fun onPause() {
    contentView?.destroyDrawingCache()
    contentView?.isDrawingCacheEnabled = false
    blurProcessor.onClear()
    super.onPause()
  }


}

private fun FloatArray.createBuffer(): FloatBuffer = ByteBuffer.allocateDirect(size * 4).run {
  order(ByteOrder.nativeOrder())
  asFloatBuffer().apply {
    put(this@createBuffer)
    position(0)
  }
}
