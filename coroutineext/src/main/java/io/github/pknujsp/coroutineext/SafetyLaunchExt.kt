package io.github.pknujsp.coroutineext

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.properties.Delegates

private abstract class SafetyJobImpl : SafetyJob {
  var action: ((context: CoroutineContext, throwable: Throwable) -> Unit)? = null

  override fun onException(action: (context: CoroutineContext, throwable: Throwable) -> Unit): SafetyJob {
    this.action = action
    return this
  }
}

interface SafetyJob : Job {
  fun onException(action: (context: CoroutineContext, throwable: Throwable) -> Unit): SafetyJob
}

/**
 * 코루틴 예외를 처리하는 CoroutineExceptionHandler를 내부적으로 추가한 launch 함수
 *
 * @param context context of the coroutine. The default value is an empty coroutine context.
 * @param start coroutine start option. The default value is [CoroutineStart.DEFAULT].
 * @param block the coroutine code.
 *
 * @return [SafetyJob] interface
 */
fun CoroutineScope.launchSafely(
  context: CoroutineContext = EmptyCoroutineContext,
  start: CoroutineStart = CoroutineStart.DEFAULT,
  block: suspend CoroutineScope.() -> Unit,
): SafetyJob {
  var safetyJobImpl: SafetyJobImpl by Delegates.notNull()
  val handler = CoroutineExceptionHandler { c, t ->
    safetyJobImpl.action?.invoke(c, t)
  }

  val job = (this + handler).launch(context, start, block)
  safetyJobImpl = object : SafetyJobImpl(), Job by job {}
  return safetyJobImpl
}
