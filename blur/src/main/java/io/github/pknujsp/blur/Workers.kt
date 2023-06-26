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
    val scope = MainScope()
    var job: Job? = null
  }

  protected fun cancelWorks() {
    try {
      job?.cancel(cause = CancellationException("Cancelled by user or system."))
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun launch(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
  ) {
    job = scope.launch(context, start, block)
  }
}
