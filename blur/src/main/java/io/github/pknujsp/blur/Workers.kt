package io.github.pknujsp.blur

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

open class Workers {
  protected companion object {
    val scope by lazy { MainScope() }
    var _job: Job? = null
    val job: Job?
      get() = _job
  }

  protected fun cancelWorks() {
    try {
      with(job?.isActive) {
        if (this == true) job?.cancel(cause = CancellationException("Cancelled by user or system."))
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
  ) {
    //cancelWorks()
    _job = scope.launch(start = start, block = block)
  }

  fun Int.toOdd(): Int = if (this % 2 == 0) this + 1 else this
}
