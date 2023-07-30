package io.github.pknujsp.testbed.feature.compose.ui.state

import io.github.pknujsp.core.annotation.KBindFunc

@KBindFunc
sealed interface UiState<out T> {
  data class Success<out T>(val data: T) : UiState<T>
  data class Error(val exception: Throwable, val size: Int) : UiState<Nothing>
  object Loading : UiState<Nothing>
}


sealed class DataState(val value: String) {
  object Success : DataState("Success")
  object Error : DataState("Error")
  object Loading : DataState("Loading")
}


sealed class ValueState<T : Int>(val value: T) {
  data class Success<T : Int>(val data: T) : ValueState<T>(data)

  data class Error(val exception: Throwable) : ValueState<Int>(3)
}
