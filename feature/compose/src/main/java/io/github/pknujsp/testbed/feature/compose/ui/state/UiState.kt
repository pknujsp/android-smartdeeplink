package io.github.pknujsp.testbed.feature.compose.ui.state


sealed interface UiState<out T> {
  data class Success<out T>(val data: T) : UiState<T>
  data class Error(val exception: Throwable) : UiState<Nothing>
  object Loading : UiState<Nothing>
}

inline fun <T> UiState<T>.onSuccess(block: (T) -> Unit): UiState<T> {
  if (this is UiState.Success) block(data)
  return this
}

inline fun <T> UiState<T>.onError(block: (Throwable) -> Unit): UiState<T> {
  if (this is UiState.Error) block(exception)
  return this
}

inline fun <T> UiState<T>.onLoading(block: () -> Unit): UiState<T> {
  if (this is UiState.Loading) block()
  return this
}
