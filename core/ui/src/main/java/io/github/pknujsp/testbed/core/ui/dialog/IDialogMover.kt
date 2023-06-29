package io.github.pknujsp.testbed.core.ui.dialog

interface IDialogMover {
  fun move(targetX: Float, targetY: Float, animationDuration: Long = 0L, startDelayDuration: Long = 0L)
  fun moveX(x: Float, animationDuration: Long = 0L, startDelayDuration: Long)
  fun moveY(y: Float, animationDuration: Long = 0L, startDelayDuration: Long = 0L)
  fun moveToFirstPosition(animationDuration: Long = 0L, startDelayDuration: Long = 0L)
}
