package io.github.pknujsp.testbed.core.ui.dialog

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import io.github.pknujsp.testbed.core.ui.R


class SimpleDialogBuilder private constructor(
  context: Context,
  dialogType: DialogType,
) {

  private val dialogStyler: SimpleDialogStyler = SimpleDialogStyler(SimpleDialogStyleAttributes(dialogType = dialogType), context)

  private val generalAttributes: SimpleDialogGeneralAttributes = SimpleDialogGeneralAttributes()

  private val alertDialogBuilder = AlertDialog.Builder(context, theme(dialogType))

  companion object {

    /**
     * Create Builder instance.(Builder 인스턴스를 생성합니다.)
     */
    fun builder(@ActivityContext context: Context, dialogType: DialogType): SimpleDialogBuilder = SimpleDialogBuilder(
      context, dialogType,
    )
  }


  /**
   * Set blur and blur intensity.(Blue 활성과 Blur 강도를 설정합니다.)
   *
   * If you set [blur] to false, [blurIndensity] will be ignored.(만약 [blur]을 false로 설정한다면 [blurIndensity]는 무시됩니다.)
   *
   * If DialogType is DialogType.FullScreen, [blur] will be ignored.(만약 DialogType이 DialogType.FullScreen이라면 [blur]은 무시됩니다.)
   *
   * Warning! Performance may be degraded if you set [blurIndensity] to a high value.(경고! [blurIndensity]를 높은 값으로 설정한다면 성능이 저하될 수 있습니다.)
   */
  fun setBlur(
    blur: Boolean,
    @IntRange(from = 0, to = 100) blurIndensity: Int = SimpleDialogStyleAttributes.currentBlurIndensity,
  ): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.blur = blur
    dialogStyler.simpleDialogStyleAttributes.blurIndensity = blurIndensity
    return this
  }

  /**
   * Set dim and dim intensity.(Dim 활성과 Dim 강도를 설정합니다.)
   *
   * If you set [dim] to false, [dimIndensity] will be ignored.(만약 [dim]을 false로 설정한다면 [dimIndensity]는 무시됩니다.)
   *
   * If DialogType is DialogType.FullScreen, [dim] will be ignored.(만약 DialogType이 DialogType.FullScreen이라면 [dim]은 무시됩니다.)
   */
  fun setDim(dim: Boolean, @IntRange(from = 0, to = 100) dimIndensity: Int = SimpleDialogStyleAttributes.currentDimIndensity): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.dim = dim
    dialogStyler.simpleDialogStyleAttributes.dimIndensity = dimIndensity
    return this
  }

  /**
   * Set cancelable.(취소 가능 여부를 설정합니다.)
   */
  fun setCancelable(cancelable: Boolean): SimpleDialogBuilder {
    generalAttributes.isCancelable = cancelable
    return this
  }

  /**
   * Set canceled on touch outside.(바깥쪽 터치로 취소 가능 여부를 설정합니다.)
   */
  fun setCanceledOnTouchOutside(canceledOnTouchOutside: Boolean): SimpleDialogBuilder {
    generalAttributes.isCanceledOnTouchOutside = canceledOnTouchOutside
    return this
  }

  /**
   * Set draggble.(드래그 가능 여부를 설정합니다.)
   *
   * If you set [draggable] to false, [setDragDirection] will be ignored.(만약 [draggable]을 false로 설정한다면 [setDragDirection]은 무시됩니다.)
   */
  fun setDraggable(draggable: Boolean): SimpleDialogBuilder {
    generalAttributes.isDraggable = draggable
    return this
  }

  /**
   * Set drag direction.(드래그 방향을 설정합니다.)
   *
   * If you set [setDraggable] to false, [DragDirection] will be ignored.(만약 [setDraggable]을 false로 설정한다면 [DragDirection]은 무시됩니다.)
   */
  fun setDragDirection(dragDirection: DragDirection): SimpleDialogBuilder {
    generalAttributes.dragDirection = dragDirection
    return this
  }

  /**
   * Set restrict views from off window.(화면 밖으로 나가는 뷰를 제한합니다.)
   *
   * If you set [restrictViewsFromOffWindow] to true, Dialog will not be able to move off the screen.(만약 [restrictViewsFromOffWindow]을 true로 설정한다면 Dialog는 화면 밖으로 나갈 수 없습니다.)
   *
   * else, Dialog will be able to move off the screen.(만약 [restrictViewsFromOffWindow]을 false로 설정한다면 Dialog는 화면 밖으로 나갈 수 있습니다.)
   */
  fun setRestrictViewsFromOffWindow(restrictViewsFromOffWindow: Boolean): SimpleDialogBuilder {
    generalAttributes.isRestrictViewsFromOffWindow = restrictViewsFromOffWindow
    return this
  }

  /**
   * Set draggble only on modal point.(모달 포인트에서만 드래그 가능 여부를 설정합니다.)
   *
   * If you set [isOnlyDraggleOnModalPoint] to true, Dialog will be able to move only on modal point.(만약 [isOnlyDraggleOnModalPoint]을 true로 설정한다면 Dialog는 모달 포인트에서만 움직일 수 있습니다.)
   */
  fun setIsOnlyDraggleOnModalPoint(isOnlyDraggleOnModalPoint: Boolean): SimpleDialogBuilder {
    generalAttributes.isOnlyDraggleOnModalPoint = isOnlyDraggleOnModalPoint
    return this
  }

  /**
   * Set show modal point.(모달 포인트를 보여줄지 설정합니다.)
   *
   * Only If you set [setDraggable] to true, [isShowModalPoint] will be applied.([setDraggable]을 true로 설정하는 경우에만 [isShowModalPoint]가 적용됩니다.)
   */
  fun setIsShowModalPoint(isShowModalPoint: Boolean): SimpleDialogBuilder {
    generalAttributes.isShowModalPoint = isShowModalPoint
    return this
  }

  /**
   * only applied for DialogType.BottomSheet.(DialogType.BottomSheet에만 적용됩니다.)
   *
   * Set bottom margin.(하단 여백을 설정합니다.)
   *
   * If you set [bottomMargin] to 0, the dialog will be full screen.(만약 [bottomMargin]을 0으로 설정한다면 전체 화면이 됩니다.)
   */
  fun setBottomMargin(@Dimension(unit = Dimension.DP) bottomMargin: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.bottomMargin = bottomMargin
    return this
  }

  /**
   * only applied for DialogType.BottomSheet.(DialogType.BottomSheet에만 적용됩니다.)
   *
   * Set horizontal margin.(좌우 여백을 설정합니다.)
   *
   * If you set [horizontalMargin] to 0, the dialog will be full screen.(만약 [horizontalMargin]을 0으로 설정한다면 전체 화면이 됩니다.)
   */
  fun setHorizontalMargin(@Dimension(unit = Dimension.DP) horizontalMargin: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.horizontalMargin = horizontalMargin
    return this
  }

  /**
   * Set corner radius.(모서리 반지름을 설정합니다.)
   *
   * If you set [cornerRadius] to 0, the corner will not be rounded.(만약 [cornerRadius]를 0으로 설정한다면 모서리가 둥글어지지 않습니다.)
   *
   * If DialogType is DialogType.FullScreen, this method will be ignored.(만약 DialogType이 DialogType.FullScreen이라면 이 메소드는 무시됩니다.)
   */
  fun setCornerRadius(@Dimension(unit = Dimension.DP) cornerRadius: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.cornerRadius = cornerRadius
    return this
  }

  /**
   * Set background resource id.(배경 리소스 아이디를 설정합니다.)
   */
  fun setBackgroundResourceId(@IdRes backgroundResourceId: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.backgroundResourceId = backgroundResourceId
    return this
  }

  /**
   * Set elevation.(그림자 수준을 설정합니다.)
   *
   * If you set [elevation] to 0, the shadow will not be drawn.(만약 [elevation]을 0으로 설정한다면 그림자가 그려지지 않습니다.)
   *
   * If DialogType is DialogType.FullScreen, this method will be ignored.(만약 DialogType이 DialogType.FullScreen이라면 이 메소드는 무시됩니다.)
   */
  fun setElevation(@Dimension(unit = Dimension.DP) elevation: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.elevation = elevation
    return this
  }

  /**
   * Set layout size.(레이아웃 크기를 설정합니다.)
   *
   * If you set [layoutWidth] or [layoutHeight] to [WRAP_CONTENT], the size will be automatically adjusted.(만약 [layoutWidth]나 [layoutHeight]를 [WRAP_CONTENT]로 설정한다면 크기가 자동으로 조절됩니다.)
   */
  fun setLayoutSize(@SizeMode layoutWidth: Int, @SizeMode layoutHeight: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.layoutWidth = layoutWidth
    dialogStyler.simpleDialogStyleAttributes.layoutHeight = layoutHeight
    return this
  }

  /**
   * Set background color.(배경 색상을 설정합니다.)
   *
   * If you set [setBackgroundResourceId], this method will be ignored.(만약 [setBackgroundResourceId]를 설정했다면 이 메서드는 무시됩니다.)
   *
   */
  fun setBackgroundColor(@ColorInt color: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.backgroundColor = color
    return this
  }

  fun setContentView(view: View): SimpleDialogBuilder {
    alertDialogBuilder.setView(view)
    return this
  }

  /**
   * Build and show dialog.(다이얼로그를 생성하고 보여줍니다.)
   */
  fun buildAndShow(): SimpleDialog = alertDialogBuilder.create().run {
    dialogStyler.applyStyle(this)
    show()

    when (dialogStyler.simpleDialogStyleAttributes.dialogType) {
      DialogType.BottomSheet -> BottomSheetDialog(this, generalAttributes, dialogStyler.simpleDialogStyleAttributes)
      DialogType.Normal -> NormalDialog(this, generalAttributes, dialogStyler.simpleDialogStyleAttributes)
      DialogType.Fullscreen -> FullScreenDialog(this, generalAttributes, dialogStyler.simpleDialogStyleAttributes)
    }
  }

  @StyleRes
  private fun theme(dialogType: DialogType): Int = when (dialogType) {
    DialogType.Normal, DialogType.Fullscreen -> R.style.Dialog
    DialogType.BottomSheet -> R.style.BottomSheet
  }
}
