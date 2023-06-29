package io.github.pknujsp.testbed.core.ui.dialog

/**
 * Defines the basic properties of the Dialog.(Dialog의 기본 속성을 정의합니다.)
 *
 * @property isDraggable Whether the dialog is draggable.(Dialog가 드래그 가능한지 여부)
 * @property dragDirection The direction in which the dialog can be dragged.(Dialog가 드래그 가능한 방향)
 * @property isCancelable Whether the dialog is cancelable.(Dialog가 취소 가능한지 여부)
 * @property isCanceledOnTouchOutside Whether the dialog is canceled when touched outside.(Dialog가 바깥쪽을 터치하면 취소되는지 여부)
 * @property isRestrictViewsFromOffWindow Whether the dialog is restricted from going off the screen.(Dialog가 화면 밖으로 나가는 것을 제한하는지 여부)
 * @property isOnlyDraggleOnModalPoint Whether the dialog can only be dragged to the modal point.(Dialog를 모달 포인트로만 드래그 가능한지 여부)
 * @property isShowModalPoint Whether to show the modal point.(모달 포인트를 보여줄지 여부)
 */
data class SimpleDialogGeneralAttributes(
  var isDraggable: Boolean = false,
  var dragDirection: Collection<DragDirection> = emptyList(),
  var isCancelable: Boolean = false,
  var isCanceledOnTouchOutside: Boolean = false,
  var isRestrictViewsFromOffWindow: Boolean = true,
  var isOnlyDraggleOnModalPoint: Boolean = true,
  var isShowModalPoint: Boolean = false,
)

sealed class DragDirection {
  object Horizontal : DragDirection()
  object Vertical : DragDirection()
  object Both : DragDirection()
}
