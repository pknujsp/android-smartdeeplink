package io.github.pknujsp.simpledialog.interfaces

interface IDialogMover {
  fun move(targetX: Float, targetY: Float, animationDuration: Long = 0L, startDelayDuration: Long = 0L)
  fun moveX(x: Float, animationDuration: Long = 0L, startDelayDuration: Long)
  fun moveY(y: Float, animationDuration: Long = 0L, startDelayDuration: Long = 0L)
  fun moveToFirstPosition(animationDuration: Long = 0L, startDelayDuration: Long = 0L)
}
