package io.github.pknujsp.testbed.core.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog


class SimpleDialogBuilder private constructor(
  context: Context,
  var contentView: View?,
  var dialogType: DialogType,
) {

  private val dialogStyler: SimpleDialogStyler =
    SimpleDialogStyler(SimpleDialogAttributes(contentView = contentView, dialogType = dialogType), context)

  private val alertDialogBuilder = AlertDialog.Builder(context, theme(dialogType)).apply {
    if (contentView != null) setView(contentView)
  }

  companion object {
    /**
     * Create a new instance of [SimpleDialogBuilder] with [contentView] and [dialogType].(새로운 [SimpleDialogBuilder] 인스턴스를 [contentView]와 [dialogType]으로 생성합니다.)
     */
    fun builder(@ActivityContext context: Context, contentView: View, dialogType: DialogType): SimpleDialogBuilder = SimpleDialogBuilder(
      context, contentView,
      dialogType,
    )

    /**
     * Create a new instance of [SimpleDialogBuilder] with [dialogType].(새로운 [SimpleDialogBuilder] 인스턴스를 [dialogType]으로 생성합니다.)
     */
    fun builder(@ActivityContext context: Context, dialogType: DialogType): SimpleDialogBuilder = SimpleDialogBuilder(
      context, null,
      dialogType,
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
  fun setBlur(blur: Boolean, @IntRange(from = 0, to = 100) blurIndensity: Int = SimpleDialogAttributes.currentBlurIndensity): SimpleDialogBuilder {
    dialogStyler.simpleDialogAttributes.blur = blur
    dialogStyler.simpleDialogAttributes.blurIndensity = blurIndensity
    return this
  }

  /**
   * Set dim and dim intensity.(Dim 활성과 Dim 강도를 설정합니다.)
   *
   * If you set [dim] to false, [dimIndensity] will be ignored.(만약 [dim]을 false로 설정한다면 [dimIndensity]는 무시됩니다.)
   *
   * If DialogType is DialogType.FullScreen, [dim] will be ignored.(만약 DialogType이 DialogType.FullScreen이라면 [dim]은 무시됩니다.)
   */
  fun setDim(dim: Boolean, @IntRange(from = 0, to = 100) dimIndensity: Int = SimpleDialogAttributes.currentDimIndensity): SimpleDialogBuilder {
    dialogStyler.simpleDialogAttributes.dim = dim
    dialogStyler.simpleDialogAttributes.dimIndensity = dimIndensity
    return this
  }

  /**
   * Set cancelable.(취소 가능 여부를 설정합니다.)
   */
  fun setCancelable(cancelable: Boolean): SimpleDialogBuilder {
    dialogStyler.simpleDialogAttributes.cancelable = cancelable
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
    dialogStyler.simpleDialogAttributes.bottomMargin = bottomMargin
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
    dialogStyler.simpleDialogAttributes.horizontalMargin = horizontalMargin
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
    dialogStyler.simpleDialogAttributes.cornerRadius = cornerRadius
    return this
  }

  /**
   * Set background resource id.(배경 리소스 아이디를 설정합니다.)
   */
  fun setBackgroundResourceId(@IdRes backgroundResourceId: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogAttributes.backgroundResourceId = backgroundResourceId
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
    dialogStyler.simpleDialogAttributes.elevation = elevation
    return this
  }

  /**
   * Set layout size.(레이아웃 크기를 설정합니다.)
   *
   * If you set [layoutWidth] or [layoutHeight] to [WRAP_CONTENT], the size will be automatically adjusted.(만약 [layoutWidth]나 [layoutHeight]를 [WRAP_CONTENT]로 설정한다면 크기가 자동으로 조절됩니다.)
   */
  fun setLayoutSize(@SizeMode layoutWidth: Int, @SizeMode layoutHeight: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogAttributes.layoutWidth = layoutWidth
    dialogStyler.simpleDialogAttributes.layoutHeight = layoutHeight
    return this
  }

  /**
   * Set background color.(배경 색상을 설정합니다.)
   *
   * If you set [setBackgroundResourceId], this method will be ignored.(만약 [setBackgroundResourceId]를 설정했다면 이 메서드는 무시됩니다.)
   *
   */
  fun setBackgroundColor(@ColorInt color: Int): SimpleDialogBuilder {
    dialogStyler.simpleDialogAttributes.backgroundColor = color
    return this
  }

  /**
   * Build and show dialog.(다이얼로그를 생성하고 보여줍니다.)
   */
  fun buildAndShow(): AlertDialog = alertDialogBuilder.create().apply {
    dialogStyler.applyStyle(this)
    show()
  }


  @StyleRes
  private fun theme(dialogType: DialogType): Int = when (dialogType) {
    DialogType.Normal, DialogType.Fullscreen -> R.style.Dialog
    DialogType.BottomSheet -> R.style.BottomSheet
  }
}
