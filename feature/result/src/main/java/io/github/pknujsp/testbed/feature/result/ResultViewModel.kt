package io.github.pknujsp.testbed.feature.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.pknujsp.coroutineext.launchSafely

class ResultViewModel : ViewModel() {

  fun test() {
    val safetyJob = viewModelScope.launchSafely {
      val test = 1 as String
    }.onException { context, throwable ->
      throwable.printStackTrace()
    }
  }
}
