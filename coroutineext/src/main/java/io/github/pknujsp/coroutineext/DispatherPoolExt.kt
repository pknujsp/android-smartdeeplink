package io.github.pknujsp.coroutineext

import androidx.annotation.IntRange
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlin.coroutines.CoroutineContext

/**
 * 코루틴 쓰레드 풀을 생성합니다.
 *
 * @param threadCount 설정할 쓰레드의 개수 - 기본값은 현재 CPU 코어의 개수
 * @return Dispatchers
 */
@OptIn(DelicateCoroutinesApi::class)
fun Dispatchers.pool(
  @IntRange(from = 1, to = 50) threadCount: Int = Runtime.getRuntime().availableProcessors(),
  poolName: String,
): ExecutorCoroutineDispatcher {
  return newFixedThreadPoolContext(threadCount, poolName)
}

fun ExecutorCoroutineDispatcher.enqueue(context: CoroutineContext, block: () -> Unit) {
  this.dispatch(context) {
    block()
  }
}

fun ExecutorCoroutineDispatcher.enqueue(context: CoroutineContext, block: Collection<() -> Unit>) {
  block.forEach {
    this.dispatch(context) {
      it()
    }
  }
}
