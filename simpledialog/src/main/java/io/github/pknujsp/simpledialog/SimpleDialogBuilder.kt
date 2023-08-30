package io.github.pknujsp.simpledialog

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import androidx.annotation.StyleRes
import androidx.core.view.updateLayoutParams
import io.github.pknujsp.simpledialog.attrs.DragDirection
import io.github.pknujsp.simpledialog.attrs.InteractController
import io.github.pknujsp.simpledialog.attrs.SimpleDialogGeneralAttributes
import io.github.pknujsp.simpledialog.attrs.SimpleDialogStyleAttributes
import io.github.pknujsp.simpledialog.constants.DialogType
import io.github.pknujsp.simpledialog.dialogs.SimpleDialog
import java.lang.ref.WeakReference


class SimpleDialogBuilder private constructor(
  context: Context,
  dialogType: DialogType,
) {

  private val dialogStyler: SimpleDialogStyler =
    WeakReference(SimpleDialogStyler(SimpleDialogStyleAttributes(dialogType = dialogType), context)).get()!!

  private val generalAttributes: SimpleDialogGeneralAttributes = WeakReference(SimpleDialogGeneralAttributes()).get()!!

  private val dialog: SimpleDialog =
    SimpleDialog(context, theme(dialogType), generalAttributes, dialogStyler.simpleDialogStyleAttributes, dialogStyler.blurringViewLifeCycleListener)


  companion object {
    /**
     * Create Builder instance.(Builder 인스턴스를 생성합니다.)
     */
    fun builder(context: Context, dialogType: DialogType): SimpleDialogBuilder = SimpleDialogBuilder(
      context, dialogType,
    )
  }


  /**
   * Set blur and blur intensity on window behind this dialog.(Dialog가 표시되는 Window에 Blur 활성화 및 Blur 강도를 설정합니다.)
   *
   * Default value is false and 15.(기본 값은 false, 15입니다.)
   *
   * If you set [blur] to false, [blurIndensity] will be ignored.(만약 [blur]을 false로 설정한다면 [blurIndensity]는 무시됩니다.)
   *
   * If DialogType is DialogType.FullScreen, [blur] will be ignored.(만약 DialogType이 DialogType.FullScreen이라면 [blur]은 무시됩니다.)
   *
   * Warning! Performance may be degraded if you set [blurIndensity] to a high value.(경고! [blurIndensity]를 높은 값으로 설정한다면 성능이 저하될 수 있습니다.)
   *
   * @param blur Whether to activate blur.(Blur 활성화 여부)
   * @param forceApply Whether to force apply blur.(Blur 강제 적용 여부)
   * @param blurIndensity Blur intensity.(Blur 강도)
   *
   * - Blur는 Android 12이상에서 공식적으로 지원되는 기능입니다.
   * - 기기가 Android 12이상이더라도, 제조사의 기본 설정에 따라 Blur가 적용되지 않을 수 있습니다.
   * - Android 12미만에서 이 기능을 적용하려면, [forceApply]를 true로 설정해야 합니다.
   * - [forceApply]를 true로 설정하는 경우, 자체적으로 제작한 Blur 기능을 사용하며, 제조사의 설정과 관계없이 항상 Blur가 적용됩니다.
   */
  fun setBehindBlur(
    blur: Boolean,
    forceApply: Boolean = false,
    @IntRange(from = 0, to = 100) blurIndensity: Int = SimpleDialogStyleAttributes.currentBlurIndensity,
  ): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.behindBlur = blur
    dialogStyler.simpleDialogStyleAttributes.behindBlurIndensity = blurIndensity
    dialogStyler.simpleDialogStyleAttributes.behindBlurForce = forceApply
    return this
  }

  /**
   * Set blur and blur intensity for background on dialog.(Dialog의 background에 Blur 활성화 및 Blur 강도를 설정합니다.)
   *
   * Default value is false and 15.(기본 값은 false, 15입니다.)
   *
   * If you set [blur] to false, [blurIndensity] will be ignored.(만약 [blur]을 false로 설정한다면 [blurIndensity]는 무시됩니다.)
   *
   * If DialogType is DialogType.FullScreen, [blur] will be ignored.(만약 DialogType이 DialogType.FullScreen이라면 [blur]은 무시됩니다.)
   *
   * Warning! Performance may be degraded if you set [blurIndensity] to a high value.(경고! [blurIndensity]를 높은 값으로 설정한다면 성능이 저하될 수 있습니다.)
   *
   * @param blur Whether to activate blur.(Blur 활성화 여부)
   * @param forceApply Whether to force apply blur.(Blur 강제 적용 여부)
   * @param blurIndensity Blur intensity.(Blur 강도)
   *
   * - Blur는 Android 12이상에서 공식적으로 지원되는 기능입니다.
   * - 기기가 Android 12이상이더라도, 제조사의 기본 설정에 따라 Blur가 적용되지 않을 수 있습니다.
   * - Android 12미만에서 이 기능을 적용하려면, [forceApply]를 true로 설정해야 합니다.
   * - [forceApply]를 true로 설정하는 경우, 자체적으로 제작한 Blur 기능을 사용하며, 제조사의 설정과 관계없이 항상 Blur가 적용됩니다.
   */
  fun setBackgroundBlur(
    blur: Boolean,
    forceApply: Boolean = false,
    @IntRange(from = 0, to = 100) blurIndensity: Int = SimpleDialogStyleAttributes.currentBlurIndensity,
  ): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.backgroundBlur = blur
    dialogStyler.simpleDialogStyleAttributes.backgroundBlurIndensity = blurIndensity
    dialogStyler.simpleDialogStyleAttributes.backgroundBlurForce = forceApply
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
   * Set custom modal view id.(커스텀 모달 뷰 id를 설정합니다.)
   *
   */
  fun setCustomModalViewId(@DrawableRes customModalViewId: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.customModalViewId = customModalViewId
    return this
  }

  /**
   * Set show modal point.(모달 포인트를 보여줄지 설정합니다.)
   *
   * Only If you set [setDraggable] to true, [isShowModalPoint] will be applied.([setDraggable]을 true로 설정하는 경우에만 [isShowModalPoint]가 적용됩니다.)
   */
  fun setIsShowModalPoint(isShowModalPoint: Boolean): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.isShowModalPoint = isShowModalPoint
    return this
  }

  /**
   * only applied for DialogType.BottomSheet.(DialogType.BottomSheet에만 적용됩니다.)
   *
   * Set bottom margin.(하단 여백을 설정합니다.)
   */
  fun setBottomMargin(@Dimension(unit = Dimension.DP) bottomMargin: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.bottomMargin = bottomMargin
    return this
  }

  /**
   * only applied for DialogType.BottomSheet.(DialogType.BottomSheet에만 적용됩니다.)
   *
   * Set top margin.(상단 여백을 설정합니다.)
   */
  fun setTopMargin(@Dimension(unit = Dimension.DP) topMargin: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.topMargin = topMargin
    return this
  }

  /**
   * Set start margin.(좌우 여백을 설정합니다.)
   */
  fun setStartMargin(@Dimension(unit = Dimension.DP) startMargin: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.startMargin = startMargin
    return this
  }

  /**
   * Set end margin.(좌우 여백을 설정합니다.)
   */
  fun setEndMargin(@Dimension(unit = Dimension.DP) endMargin: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.endMargin = endMargin
    return this
  }

  /**
   * Set the corner rounding for the top left, top right, bottom left, and bottom right.(왼쪽 상단, 오른쪽 상단, 왼쪽 하단, 오른쪽 하단의 모서리를 둥글게 만듭니다.)
   */
  fun setCornerRadius(
    @Dimension(unit = Dimension.DP) topStart: Int,
    @Dimension(unit = Dimension.DP) topEnd: Int,
    @Dimension(unit = Dimension.DP) bottomStart: Int,
    @Dimension(unit = Dimension.DP) bottomEnd: Int,
  ): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.topStartCornerRadius = topStart
    dialogStyler.simpleDialogStyleAttributes.topEndCornerRadius = topEnd
    dialogStyler.simpleDialogStyleAttributes.bottomStartCornerRadius = bottomStart
    dialogStyler.simpleDialogStyleAttributes.bottomEndCornerRadius = bottomEnd
    return this
  }

  /**
   * Set the corner rounding for all corners.(모든 모서리를 둥글게 만듭니다.)
   */
  fun setCornerRadius(@Dimension(unit = Dimension.DP) cornerRadius: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.topStartCornerRadius = cornerRadius
    dialogStyler.simpleDialogStyleAttributes.topEndCornerRadius = cornerRadius
    dialogStyler.simpleDialogStyleAttributes.bottomStartCornerRadius = cornerRadius
    dialogStyler.simpleDialogStyleAttributes.bottomEndCornerRadius = cornerRadius
    return this
  }

  /**
   * Set background resource id.(배경 리소스 아이디를 설정합니다.)
   */
  fun setBackgroundResourceId(@DrawableRes backgroundResourceId: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogStyleAttributes.backgroundResourceId = backgroundResourceId
    return this
  }

  /**
   * Set elevation.(그림자 수준을 설정합니다.)
   *
   * If you set [elevation] to 0, the shadow will not be drawn.(만약 [elevation]을 0으로 설정한다면 그림자가 그려지지 않습니다.)
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
   * Set viewId interact with.(상호작용할 뷰 id를 설정합니다.)
   */
  private fun setViewInteractWith(view: View): SimpleDialogBuilder {
    generalAttributes.viewMethodsInteractWith = InteractController(
      setBottom = view::setBottom,
      updateLayoutParams = view::updateLayoutParams,
      setTranslationY = view::setTranslationY,
      scrollBy = view::scrollBy,
      originalWidth = view.width,
      originalHeight = view.height,
      originalX = view.x,
      originalY = view.y,
    )
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

  /**
   * Set content view.(컨텐츠 뷰를 설정합니다.)
   */
  fun setContentView(view: View): SimpleDialogBuilder {
    dialog.setContentView(
      FrameLayout(view.context).apply {
        id = R.id.dialog_base_content
        addView(view)
      },
    )
    return this
  }

  /**
  /**
   * Set foldable.(접을 수 있는지 설정합니다.)
  */
  fun setFoldable(isFoldable: Boolean): SimpleDialogBuilder {
  generalAttributes.isFoldable = isFoldable
  return this
  }
   */

  /**
   * Build and show dialog.(다이얼로그를 생성하고 보여줍니다.)
   */
  fun buildAndShow(): SimpleDialog = dialog.apply {
    create()
    dialogStyler.applyStyle(this)
    show()
  }

  @StyleRes
  private fun theme(dialogType: DialogType): Int = when (dialogType) {
    DialogType.Normal, DialogType.Fullscreen -> R.style.Dialog
    DialogType.BottomSheet -> R.style.BottomSheet
  }
}
