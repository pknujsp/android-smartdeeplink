package io.github.pknujsp.blur.processor

import io.github.pknujsp.coroutineext.launchSafely
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlin.coroutines.CoroutineContext

internal object BlurWorkerImpl : BlurWorker {

  private var scope: CoroutineScope = MainScope()
  private var job: Job? = null


  override fun launch(context: CoroutineContext, start: CoroutineStart, block: suspend CoroutineScope.() -> Unit) =
    scope.launchSafely(context, start = start, block = block).also {
      job = it
    }


  override fun onClear() {
    try {
      job?.run {
        if (isActive) cancel(CancellationException("onClear"))
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
