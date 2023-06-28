package io.github.pknujsp.testbed.core.ui

import android.graphics.Color
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IdRes
import androidx.annotation.IntRange

private const val defaultBlurIndensity: Int = 20
private const val defaultDimIndensity: Int = 40
private const val defaultMarginBottom: Int = 12
private const val defaultMarginHorizontal: Int = 16
private const val defaultCornerRadius: Int = 12
private const val defaultElevation: Int = 8
private const val defaultWidth: Int = WRAP_CONTENT
private const val defaultHeight: Int = WRAP_CONTENT
private const val defaultBackgroundColor: Int = Color.WHITE

data class SimpleDialogAttributes(
  var blur: Boolean = true,
  @IntRange(from = 0, to = 100) var blurIndensity: Int = currentBlurIndensity,
  var dim: Boolean = true,
  @IntRange(from = 0, to = 100) var dimIndensity: Int = currentDimIndensity,
  var cancelable: Boolean = true,
  var dialogType: DialogType,
  @Dimension(unit = Dimension.DP) var bottomMargin: Int = currentMarginBottom,
  @Dimension(unit = Dimension.DP) var horizontalMargin: Int = currentMarginHorizontal,
  @Dimension(unit = Dimension.DP) var cornerRadius: Int = currentCornerRadius,
  @IdRes var backgroundResourceId: Int? = null,
  @ColorInt var backgroundColor: Int = currentBackgroundColor,
  @Dimension(unit = Dimension.DP) var elevation: Int = currentElevation,
  @SizeMode var layoutWidth: Int = currentWidth,
  @SizeMode var layoutHeight: Int = currentHeight,
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

  }
}
