package io.github.pknujsp.blur.view

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.opengl.GLES30
import android.opengl.GLES30.glGetError
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
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

    val quadVertices = floatArrayOf(
      -1.0f, -1.0f,
      1.0f, -1.0f,
      -1.0f, 1.0f,
      1.0f, 1.0f,
    )

    val textureCoords = floatArrayOf(
      0.0f, 1.0f,
      1.0f, 1.0f,
      0.0f, 0.0f,
      1.0f, 0.0f,
    )

    val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(quadVertices.size * 4).run {
      order(ByteOrder.nativeOrder())
      asFloatBuffer().apply {
        put(quadVertices)
        position(0)
      }
    }

    val textureBuffer: FloatBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4).run {
      order(ByteOrder.nativeOrder())
      asFloatBuffer().apply {
        put(textureCoords)
        position(0)
      }
    }

    val mMVPMatrix = FloatArray(16)
    val mProjMatrix = FloatArray(16)
    val mMMatrix = FloatArray(16)
    val mVMatrix = FloatArray(16)

    const val vertexShaderCode = """
    uniform mat4 uMVPMatrix;
    attribute vec4 vPosition;
    attribute vec2 inputTextureCoordinate;
    varying vec2 textureCoordinate;
    void main() {
      gl_Position = uMVPMatrix * vPosition;
      textureCoordinate = inputTextureCoordinate;
    }
    """

    const val fragmentShaderCode = """
    precision mediump float;
    varying vec2 textureCoordinate;
    uniform sampler2D sTexture;
    void main() {
      gl_FragColor = texture2D(sTexture, textureCoordinate);
    }
    """


    var glProgram: Int = 0

    var textures = IntArray(1)

    var positionHandle = 0
    var textureHandle = 0
    var textureCoordinateHandle = 0
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
    alpha = 0.5f

    setEGLContextClientVersion(2)
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
    glProgram = GLES30.glCreateProgram().also {
      val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode.trimIndent())
      val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode.trimIndent())

      GLES30.glAttachShader(it, vertexShader)
      GLES30.glAttachShader(it, fragmentShader)
      GLES30.glLinkProgram(it)

      val linkStatus = IntArray(1)
      GLES30.glGetProgramiv(it, GLES30.GL_LINK_STATUS, linkStatus, 0)
    }

    positionHandle = GLES30.glGetAttribLocation(glProgram, "vPosition")
    textureHandle = GLES30.glGetUniformLocation(glProgram, "sTexture")
    textureCoordinateHandle = GLES30.glGetAttribLocation(glProgram, "inputTextureCoordinate")

    GLES30.glClearColor(255f, 255f, 255f, 1.0f)

    GLES30.glGenTextures(1, textures, 0)
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)

    Matrix.setLookAtM(mVMatrix, 0, 0f, 0f, -5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

    println("onSurfaceCreated : ${glGetError()}")
  }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    GLES30.glViewport(0, 0, dstCoordinatesRect.width(), dstCoordinatesRect.height())
  }

  override fun onDrawFrame(gl: GL10?) {
    blurredBitmap?.run {
      GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
      GLES30.glUseProgram(glProgram)

      GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
      GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
      GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, this, 0)

      GLES30.glVertexAttribPointer(positionHandle, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer)
      GLES30.glEnableVertexAttribArray(positionHandle)
      GLES30.glVertexAttribPointer(textureCoordinateHandle, 2, GLES30.GL_FLOAT, false, 0, textureBuffer)
      GLES30.glEnableVertexAttribArray(textureCoordinateHandle)
      GLES30.glUniform1i(textureHandle, 0)
      GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)

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
