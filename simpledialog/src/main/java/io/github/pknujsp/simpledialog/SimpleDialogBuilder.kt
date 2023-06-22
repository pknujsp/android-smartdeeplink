package io.github.pknujsp.simpledialog

import android.graphics.Color
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog

private const val defaultBlurIndensity: Int = 20
private const val defaultDimIndensity: Int = 40
private const val defaultMarginBottom: Int = 12
private const val defaultMarginHorizontal: Int = 16
private const val defaultCornerRadius: Int = 12
private const val defaultElevation: Int = 8
private const val defaultWidth: Int = WRAP_CONTENT
private const val defaultHeight: Int = WRAP_CONTENT
private const val defaultBackgroundColor: Int = Color.WHITE

class SimpleDialogBuilder private constructor(
  var view: View, var dialogType: DialogType,
) {
  companion object {
    /**
     * Current attributes.(현재 속성 값들입니다.)
     */

    /**
     * The currently set global blur intensity.(현재 설정된 전역 Blur 강도입니다.)
     *
     * Default value is 20.(기본 값은 20입니다.)
     */
    @IntRange(from = 0, to = 100) var currentBlurIndensity: Int = defaultBlurIndensity

    /**
     * The currently set global dim intensity.(현재 설정된 전역 Dim 강도입니다.)
     *
     * Default value is 40.(기본 값은 40입니다.)
     */
    @IntRange(from = 0, to = 100) var currentDimIndensity: Int = defaultDimIndensity

    /**
     * The currently set global margin bottom.(현재 설정된 전역 하단 여백입니다.)
     *
     * Default value is 12.(기본 값은 12입니다.)
     */
    @Dimension(unit = Dimension.DP) var currentMarginBottom: Int = defaultMarginBottom

    /**
     * The currently set global margin horizontal.(현재 설정된 전역 좌우 여백입니다.)
     *
     * Default value is 16.(기본 값은 16입니다.)
     */
    @Dimension(unit = Dimension.DP) var currentMarginHorizontal: Int = defaultMarginHorizontal

    /**
     * The currently set global corner radius.(현재 설정된 전역 모서리 반지름입니다.)
     *
     * Default value is 12.(기본 값은 12입니다.)
     */
    @Dimension(unit = Dimension.DP) var currentCornerRadius: Int = defaultCornerRadius

    /**
     * The currently set global elevation.(현재 설정된 전역 고도입니다.)
     *
     * Default value is 8.(기본 값은 8입니다.)
     */
    @Dimension(unit = Dimension.DP) var currentElevation: Int = defaultElevation

    /**
     * The currently set global width.(현재 설정된 전역 너비입니다.)
     *
     * Default value is [WRAP_CONTENT].(기본 값은 [WRAP_CONTENT]입니다.)
     */
    @SizeMode var currentWidth: Int = defaultWidth

    /**
     * The currently set global height.(현재 설정된 전역 높이입니다.)
     *
     * Default value is [WRAP_CONTENT].(기본 값은 [WRAP_CONTENT]입니다.)
     */
    @SizeMode var currentHeight: Int = defaultHeight

    /**
     * The currently set global background color.(현재 설정된 전역 배경 색상입니다.)
     *
     * Default value is [Color.WHITE].(기본 값은 [Color.WHITE]입니다.)
     */
    @ColorInt var currentBackgroundColor: Int = defaultBackgroundColor

    /**
     * Recover all attributes with defaults.(속성 값을 모두 기본값으로 재설정합니다.)
     */
    fun resetCurrentAttributes() {
      currentBlurIndensity = defaultBlurIndensity
      currentDimIndensity = defaultDimIndensity
      currentMarginBottom = defaultMarginBottom
      currentMarginHorizontal = defaultMarginHorizontal
      currentCornerRadius = defaultCornerRadius
      currentElevation = defaultElevation
      currentWidth = defaultWidth
      currentHeight = defaultHeight
      currentBackgroundColor = defaultBackgroundColor
    }

    /**
     * Create a new instance of [SimpleDialogBuilder] with [view] and [dialogType].(새로운 [SimpleDialogBuilder] 인스턴스를 [view]와 [dialogType]으로 생성합니다.)
     */
    fun builder(view: View, dialogType: DialogType): SimpleDialogBuilder = SimpleDialogBuilder(view, dialogType)
  }

  private var blur: Boolean = true

  @IntRange(from = 0, to = 100) private var blurIndensity: Int = currentBlurIndensity

  private var dim: Boolean = true

  @IntRange(from = 0, to = 100) private var dimIndensity: Int = currentDimIndensity

  private var cancelable: Boolean = true

  @Dimension(unit = Dimension.DP) private var bottomMargin: Int = currentMarginBottom

  @Dimension(unit = Dimension.DP) private var horizontalMargin: Int = currentMarginHorizontal

  @Dimension(unit = Dimension.DP) private var cornerRadius: Int = currentCornerRadius

  @IdRes private var backgroundResourceId: Int? = null

  @Dimension(unit = Dimension.DP) private var elevation: Int = currentElevation

  @SizeMode private var layoutWidth: Int = currentWidth

  @SizeMode private var layoutHeight: Int = currentHeight

  @ColorInt private var backgroundColor: Int = currentBackgroundColor

  /**
   * Set blur and blur intensity.(Blue 활성과 Blur 강도를 설정합니다.)
   *
   * If you set [blur] to false, [blurIndensity] will be ignored.(만약 [blur]을 false로 설정한다면 [blurIndensity]는 무시됩니다.)
   *
   * If DialogType is DialogType.FullScreen, [blur] will be ignored.(만약 DialogType이 DialogType.FullScreen이라면 [blur]은 무시됩니다.)
   *
   * Warning! Performance may be degraded if you set [blurIndensity] to a high value.(경고! [blurIndensity]를 높은 값으로 설정한다면 성능이 저하될 수 있습니다.)
   */
  fun setBlur(blur: Boolean, @IntRange(from = 0, to = 100) blurIndensity: Int = currentBlurIndensity): SimpleDialogBuilder {
    this.blur = blur
    this.blurIndensity = blurIndensity
    return this
  }

  /**
   * Set dim and dim intensity.(Dim 활성과 Dim 강도를 설정합니다.)
   *
   * If you set [dim] to false, [dimIndensity] will be ignored.(만약 [dim]을 false로 설정한다면 [dimIndensity]는 무시됩니다.)
   *
   * If DialogType is DialogType.FullScreen, [dim] will be ignored.(만약 DialogType이 DialogType.FullScreen이라면 [dim]은 무시됩니다.)
   */
  fun setDim(dim: Boolean, @IntRange(from = 0, to = 100) dimIndensity: Int = defaultDimIndensity): SimpleDialogBuilder {
    this.dim = dim
    this.dimIndensity = dimIndensity
    return this
  }

  /**
   * Set cancelable.(취소 가능 여부를 설정합니다.)
   */
  fun setCancelable(cancelable: Boolean): SimpleDialogBuilder {
    this.cancelable = cancelable
    return this
  }

  /**
   * only applied for DialogType.BottomSheet.(DialogType.BottomSheet에만 적용됩니다.)
   *
   * Set bottom margin.(하단 여백을 설정합니다.)
   *
   * If you set [bottomMargin] to 0, the dialog will be full screen.(만약 [bottomMargin]을 0으로 설정한다면 전체 화면이 됩니다.)
   */
  fun setMarginBottom(@Dimension(unit = Dimension.DP) marginBottom: Int): SimpleDialogBuilder {
    this.bottomMargin = marginBottom
    return this
  }

  /**
   * only applied for DialogType.BottomSheet.(DialogType.BottomSheet에만 적용됩니다.)
   *
   * Set horizontal margin.(좌우 여백을 설정합니다.)
   *
   * If you set [horizontalMargin] to 0, the dialog will be full screen.(만약 [horizontalMargin]을 0으로 설정한다면 전체 화면이 됩니다.)
   */
  fun setMarginHorizontal(@Dimension(unit = Dimension.DP) marginHorizontal: Int): SimpleDialogBuilder {
    this.horizontalMargin = marginHorizontal
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
    this.cornerRadius = cornerRadius
    return this
  }

  /**
   * Set background resource id.(배경 리소스 아이디를 설정합니다.)
   */
  fun setBackgroundResourceId(@IdRes backgroundResourceId: Int): SimpleDialogBuilder {
    this.backgroundResourceId = backgroundResourceId
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
    this.elevation = elevation
    return this
  }

  /**
   * Set layout size.(레이아웃 크기를 설정합니다.)
   *
   * If you set [layoutWidth] or [layoutHeight] to [WRAP_CONTENT], the size will be automatically adjusted.(만약 [layoutWidth]나 [layoutHeight]를 [WRAP_CONTENT]로 설정한다면 크기가 자동으로 조절됩니다.)
   */
  fun setLayoutSize(@SizeMode layoutWidth: Int, @SizeMode layoutHeight: Int): SimpleDialogBuilder {
    this.layoutWidth = layoutWidth
    this.layoutHeight = layoutHeight
    return this
  }

  /**
   * Set background color.(배경 색상을 설정합니다.)
   *
   * If you set [backgroundResourceId], this method will be ignored.(만약 [backgroundResourceId]를 설정했다면 이 메서드는 무시됩니다.)
   *
   */
  fun setBackgroundColor(@ColorInt color: Int): SimpleDialogBuilder {
    this.backgroundColor = color
    return this
  }

  /**
   * Build and show dialog.(다이얼로그를 생성하고 보여줍니다.)
   */
  fun buildAndShow(): AlertDialog =
    AlertDialog.Builder(view.context, theme(dialogType)).setView(view).create().apply {
      theme(
        SimpleDialogAttributes(
          blur, blurIndensity, dim, dimIndensity, cancelable, view, dialogType,
          bottomMargin, horizontalMargin, cornerRadius, backgroundResourceId, backgroundColor, elevation,
          layoutWidth, layoutHeight,
        ),
      )
      show()
    }


  @StyleRes
  private fun theme(dialogType: DialogType): Int = when (dialogType) {
    DialogType.Normal, DialogType.Fullscreen -> R.style.Dialog
    DialogType.BottomSheet -> R.style.BottomSheet
  }
}
