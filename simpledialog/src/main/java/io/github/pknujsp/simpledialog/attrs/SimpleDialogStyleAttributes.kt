package io.github.pknujsp.simpledialog.attrs

import android.content.res.Resources
import android.graphics.Color
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.IntRange
import io.github.pknujsp.simpledialog.SizeMode
import io.github.pknujsp.simpledialog.constants.DialogType

private const val defaultBlurIndensity: Int = 15
private const val defaultDimIndensity: Int = 35
private const val defaultMarginBottom: Int = 0
private const val defaultMarginTop: Int = 0
private const val defaultMarginEnd: Int = 0
private const val defaultMarginStart: Int = 0
private const val defaultCornerRadius: Int = 12
private const val defaultElevation: Int = 8
private const val defaultWidth: Int = WRAP_CONTENT
private const val defaultHeight: Int = WRAP_CONTENT
private const val defaultBackgroundColor: Int = Color.WHITE

data class SimpleDialogStyleAttributes(
  var backgroundBlurForce: Boolean = false,
  var behindBlurForce: Boolean = false,
  var behindBlur: Boolean = false,
  @IntRange(from = 0, to = 100) var behindBlurIndensity: Int = currentBlurIndensity,
  var backgroundBlur: Boolean = false,
  @IntRange(from = 0, to = 100) var backgroundBlurIndensity: Int = currentBlurIndensity,
  var dim: Boolean = true,
  @IntRange(from = 0, to = 100) var dimIndensity: Int = currentDimIndensity,
  var dialogType: DialogType,
  @Dimension(unit = Dimension.DP) var bottomMargin: Int = currentMarginBottom,
  @Dimension(unit = Dimension.DP) var topMargin: Int = currentMarginTop,
  @Dimension(unit = Dimension.DP) var startMargin: Int = currentMarginStart,
  @Dimension(unit = Dimension.DP) var endMargin: Int = currentMarginEnd,
  @Dimension(unit = Dimension.DP) var topStartCornerRadius: Int = currentCornerRadius,
  @Dimension(unit = Dimension.DP) var topEndCornerRadius: Int = currentCornerRadius,
  @Dimension(unit = Dimension.DP) var bottomStartCornerRadius: Int = currentCornerRadius,
  @Dimension(unit = Dimension.DP) var bottomEndCornerRadius: Int = currentCornerRadius,
  @DrawableRes var backgroundResourceId: Int? = null,
  @ColorInt var backgroundColor: Int = currentBackgroundColor,
  @Dimension(unit = Dimension.DP) var elevation: Int = currentElevation,
  @SizeMode var layoutWidth: Int = currentWidth,
  @SizeMode var layoutHeight: Int = currentHeight,
  @DrawableRes var customModalViewId: Int? = null,
  var isShowModalPoint: Boolean = false,
) {


  companion object {
    /**
     * Current attributes.(현재 속성 값들입니다.)
     */

    val modalIconHeight: Int = (Resources.getSystem().displayMetrics.density * 14).toInt()

    /**
     * The currently set blur intensity.(현재 설정된  Blur 강도입니다.)
     *
     * Default value is 15.(기본 값은 15입니다.)
     */
    @IntRange(from = 0, to = 100) var currentBlurIndensity: Int = defaultBlurIndensity

    /**
     * The currently set dim intensity.(현재 설정된 Dim 강도입니다.)
     *
     * Default value is 30.(기본 값은 30입니다.)
     */
    @IntRange(from = 0, to = 100) var currentDimIndensity: Int = defaultDimIndensity

    /**
     * The currently set  margin bottom.(현재 설정된 하단 여백입니다.)
     *
     * Default value is 0.(기본 값은 0입니다.)
     */
    @Dimension(unit = Dimension.DP) var currentMarginBottom: Int = defaultMarginBottom

    /**
     * The currently set  margin top.(현재 설정된 상단 여백입니다.)
     *
     * Default value is 0.(기본 값은 0입니다.)
     */
    @Dimension(unit = Dimension.DP) var currentMarginTop: Int = defaultMarginTop

    /**
     * The currently set layout horizontal start area margin.(현재 설정된 레이아웃 시작 영역 여백입니다.)
     *
     * Default value is 0.(기본 값은 0입니다.)
     */
    @Dimension(unit = Dimension.DP) var currentMarginStart: Int = defaultMarginStart

    /**
     * The currently set layout horizontal end area margin.(현재 설정된 레이아웃 끝 영역 여백입니다.)
     *
     * Default value is 0.(기본 값은 0입니다.)
     */
    @Dimension(unit = Dimension.DP) var currentMarginEnd: Int = defaultMarginEnd

    /**
     * The currently set corner radius.(현재 설정된 모서리 반지름입니다.)
     *
     * Default value is 12.(기본 값은 12입니다.)
     */
    @Dimension(unit = Dimension.DP) var currentCornerRadius: Int = defaultCornerRadius

    /**
     * The currently set  elevation(Z-position).(현재 설정된 그림자를 위한 Z좌표입니다.)
     *
     * Default value is 8.(기본 값은 8입니다.)
     */
    @Dimension(unit = Dimension.DP) var currentElevation: Int = defaultElevation

    /**
     * The currently set  width.(현재 설정된  너비입니다.)
     *
     * Default value is [WRAP_CONTENT].(기본 값은 [WRAP_CONTENT]입니다.)
     */
    @SizeMode var currentWidth: Int = defaultWidth

    /**
     * The currently set  height.(현재 설정된  높이입니다.)
     *
     * Default value is [WRAP_CONTENT].(기본 값은 [WRAP_CONTENT]입니다.)
     */
    @SizeMode var currentHeight: Int = defaultHeight

    /**
     * The currently set background color.(현재 설정된 배경 색상입니다.)
     *
     * Default value is [Color.WHITE].(기본 값은 [Color.WHITE]입니다.)
     */
    @ColorInt var currentBackgroundColor: Int = defaultBackgroundColor

    /**
     * Recover all attributes with defaults.(속성 값을 모두 기본값으로 재설정합니다.)
     */
    fun recoverAttrs() {
      currentBlurIndensity = defaultBlurIndensity
      currentDimIndensity = defaultDimIndensity
      currentMarginBottom = defaultMarginBottom
      currentMarginStart = defaultMarginStart
      currentMarginEnd = defaultMarginEnd
      currentMarginTop = defaultMarginTop
      currentCornerRadius = defaultCornerRadius
      currentElevation = defaultElevation
      currentWidth = defaultWidth
      currentHeight = defaultHeight
      currentBackgroundColor = defaultBackgroundColor
    }

  }
}
