package io.github.pknujsp.testbed.feature.compose.ui.state

import io.github.pknujsp.core.annotation.BindFunc

@BindFunc
sealed interface UiState<out T> {
  data class Success<out T>(val data: T) : UiState<T>
  data class Error(val exception: Throwable, val size: Int) : UiState<Nothing>
  object Loading : UiState<Nothing>
}
