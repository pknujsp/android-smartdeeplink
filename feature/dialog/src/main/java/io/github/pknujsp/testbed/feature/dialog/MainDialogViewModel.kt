package io.github.pknujsp.testbed.feature.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.pknujsp.simpledialog.SimpleDialogBuilder
import io.github.pknujsp.simpledialog.attrs.DragDirection
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

  fun behindBlur(blur: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.dialogStyleAttributes?.run {
          behindBlurIndensity = blur
          behindBlur = blur > 0
        }
        it
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
        it?.setBottomMargin(margin)
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

  fun draggable(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setDraggable(checked)
      }
    }
  }

  fun cancelable(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setCancelable(checked)
      }
    }
  }

  fun canceledOnTouchOutside(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setCanceledOnTouchOutside(checked)
      }
    }
  }

  fun restrictViewsFromOffWindow(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setRestrictViewsFromOffWindow(checked)
      }
    }
  }

  fun showModalPoint(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setIsShowModalPoint(checked)
      }
    }
  }


  fun draggleDirections(direction: DragDirection) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setDragDirection(direction)
      }
    }
  }

  fun topMargin(toInt: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setTopMargin(toInt)
      }
    }
  }

  fun startMargin(toInt: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setStartMargin(toInt)
      }
    }
  }

  fun endMargin(toInt: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setEndMargin(toInt)
      }
    }
  }

  fun applyForceBlur(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.dialogStyleAttributes?.behindBlurForce = checked
        it
      }
    }
  }

  fun topStartCornerRadius(toInt: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.dialogStyleAttributes?.topStartCornerRadius = toInt
        it
      }
    }
  }

  fun topEndCornerRadius(toInt: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.dialogStyleAttributes?.topEndCornerRadius = toInt

        it
      }
    }
  }

  fun bottomStartCornerRadius(toInt: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.dialogStyleAttributes?.bottomStartCornerRadius = toInt
        it
      }
    }
  }

  fun bottomEndCornerRadius(toInt: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.dialogStyleAttributes?.bottomEndCornerRadius = toInt
        it
      }
    }
  }

  fun backgroundBlur(toInt: Int) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setBackgroundBlur(toInt > 0, false, toInt)
      }
    }
  }

  fun foldable(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.update {
        it?.setFoldable(checked)
      }
    }
  }

}
