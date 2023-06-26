package io.github.pknujsp.blur

internal class OpenGL {

  private companion object {

    init {
      System.loadLibrary("native-lib")
    }
  }

  external fun init()
  external fun resize(width: Int, height: Int)
  external fun draw()
  external fun destroy()
}
