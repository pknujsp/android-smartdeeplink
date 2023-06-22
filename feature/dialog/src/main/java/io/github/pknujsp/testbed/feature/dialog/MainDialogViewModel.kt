package io.github.pknujsp.testbed.feature.dialog

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.pknujsp.testbed.core.ui.DialogType
import io.github.pknujsp.testbed.core.ui.SimpleDialogBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainDialogViewModel : ViewModel() {
  private val _dialogBuilder = MutableStateFlow<SimpleDialogBuilder?>(null)
  val dialogBuilder get() = _dialogBuilder.asStateFlow()

  fun init(builder: SimpleDialogBuilder) {
    viewModelScope.launch {
      _dialogBuilder.value = builder
    }
  }

  fun blur(blur: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setBlur(blur = blur > 0, blurIndensity = blur)
      }
    }
  }

  fun dim(dim: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setDim(dim = dim > 0, dimIndensity = dim)
      }
    }
  }

  fun bottomMargin(margin: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setMarginBottom(margin)
      }
    }
  }

  fun horizontalMargin(margin: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setMarginHorizontal(margin)
      }
    }
  }

  fun cornerRadius(radius: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setCornerRadius(radius)
      }
    }
  }

  fun dialogType(dialogType: DialogType, view: View) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.dialogType = dialogType
        it
      }
    }
  }

  fun size(width: Int, height: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setLayoutSize(width, height)
      }
    }
  }

  fun elevation(elevation: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setElevation(elevation)
      }
    }
  }
}
