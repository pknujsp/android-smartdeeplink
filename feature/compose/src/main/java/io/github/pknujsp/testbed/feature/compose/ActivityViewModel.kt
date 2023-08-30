package io.github.pknujsp.testbed.feature.compose

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor() : ViewModel() {

  override fun onCleared() {
    super.onCleared()
  }

}
