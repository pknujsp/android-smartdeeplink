package io.github.pknujsp.testbed.feature.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.pknujsp.testbed.core.ui.dialog.DragDirection
import io.github.pknujsp.testbed.core.ui.dialog.SimpleDialogBuilder
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainDialogViewModel : ViewModel() {
  private val _dialogBuilder = MutableSharedFlow<SimpleDialogBuilder?>(
    replay = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
  )
  val dialogBuilder get() = _dialogBuilder.asSharedFlow()

  fun init(builder: SimpleDialogBuilder) {
    viewModelScope.launch {
      _dialogBuilder.emit(builder)
    }
  }

  fun blur(blur: Int) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setBehindBlur(blur = blur > 0, blurIndensity = blur)
        _dialogBuilder.emit(it)
      }
    }
  }

  fun dim(dim: Int) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setDim(dim = dim > 0, dimIndensity = dim)
        _dialogBuilder.emit(it)

      }
    }
  }

  fun bottomMargin(margin: Int) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setBottomMargin(margin)
        _dialogBuilder.emit(it)

      }
    }
  }

  fun horizontalMargin(margin: Int) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setStartMargin(margin)
        _dialogBuilder.emit(it)

      }
    }
  }

  fun cornerRadius(radius: Int) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setCornerRadius(radius)
        _dialogBuilder.emit(it)

      }
    }
  }


  fun size(width: Int, height: Int) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setLayoutSize(width, height)
        _dialogBuilder.emit(it)

      }
    }
  }

  fun elevation(elevation: Int) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setElevation(elevation)
        _dialogBuilder.emit(it)
      }
    }
  }

  fun draggable(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setDraggable(checked)
        _dialogBuilder.emit(it)
      }
    }
  }

  fun cancelable(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setCancelable(checked)
        _dialogBuilder.emit(it)
      }
    }
  }

  fun canceledOnTouchOutside(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setCanceledOnTouchOutside(checked)
        _dialogBuilder.emit(it)
      }
    }
  }

  fun restrictViewsFromOffWindow(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setRestrictViewsFromOffWindow(checked)
        _dialogBuilder.emit(it)
      }
    }
  }

  fun showModalPoint(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setIsShowModalPoint(checked)
        _dialogBuilder.emit(it)
      }
    }
  }

  fun onlyDraggleOnModalPoint(checked: Boolean) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setIsOnlyDraggleOnModalPoint(checked)
        _dialogBuilder.emit(it)
      }
    }
  }

  fun draggleDirections(direction: DragDirection) {
    viewModelScope.launch {
      _dialogBuilder.replayCache.last().also {
        it?.setDragDirection(direction)
        _dialogBuilder.emit(it)
      }
    }
  }

}
