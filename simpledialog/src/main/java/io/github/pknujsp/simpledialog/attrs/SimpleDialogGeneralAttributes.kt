package io.github.pknujsp.simpledialog.attrs

import android.view.ViewGroup.LayoutParams
import kotlin.reflect.KFunction1

/**
 * Defines the basic properties of the Dialog.(Dialog의 기본 속성을 정의합니다.)
 *
 * @property isDraggable Whether the dialog is draggable.(Dialog가 드래그 가능한지 여부)
 * @property dragDirection The direction in which the dialog can be dragged.(Dialog가 드래그 가능한 방향)
 * @property isCancelable Whether the dialog is cancelable.(Dialog가 취소 가능한지 여부)
 * @property isCanceledOnTouchOutside Whether the dialog is canceled when touched outside.(Dialog가 바깥쪽을 터치하면 취소되는지 여부)
 * @property isRestrictViewsFromOffWindow Whether the dialog is restricted from going off the screen.(Dialog가 화면 밖으로 나가는 것을 제한하는지 여부)
 * @property isFoldable Whether the dialog is foldable.(Dialog가 접힐 수 있는지 여부)
 */
data class SimpleDialogGeneralAttributes(
  var isDraggable: Boolean = false,
  var dragDirection: DragDirection = DragDirection.Both,
  var isCancelable: Boolean = true,
  var isCanceledOnTouchOutside: Boolean = true,
  var isRestrictViewsFromOffWindow: Boolean = true,
  var isFoldable: Boolean = false,
  var foldDirection: FoldDirection = FoldDirection.Vertical,
  var viewMethodsInteractWith: InteractController? = null,
)

sealed class DragDirection {
  object Horizontal : DragDirection()
  object Vertical : DragDirection()
  object Both : DragDirection()
}

sealed class FoldDirection {
  object Horizontal : FoldDirection()
  object Vertical : FoldDirection()
}

data class InteractController(
  val setBottom: (Int) -> Unit,
  val updateLayoutParams: KFunction1<LayoutParams.() -> Unit, Unit>,
  val setTranslationY: (Float) -> Unit,
  val scrollBy: (Int, Int) -> Unit,
  val originalWidth: Int,
  val originalHeight: Int,
  val originalX: Float,
  val originalY: Float,
)
