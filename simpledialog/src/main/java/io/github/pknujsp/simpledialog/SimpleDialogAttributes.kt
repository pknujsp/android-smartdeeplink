package io.github.pknujsp.simpledialog

import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IdRes
import androidx.annotation.IntRange

data class SimpleDialogAttributes(
  val blur: Boolean,
  @IntRange(from = 0, to = 100) val blurIndensity: Int,
  val dim: Boolean,
  @IntRange(from = 0, to = 100) val dimIndensity: Int,
  val cancelable: Boolean = true,
  val view: View, val dialogType: DialogType,
  @Dimension(unit = Dimension.DP) val bottomMargin: Int,
  @Dimension(unit = Dimension.DP) val horizontalMargin: Int,
  @Dimension(unit = Dimension.DP) val cornerRadius: Int,
  @IdRes val backgroundResourceId: Int? = null,
  @ColorInt val backgroundColor: Int,
  @Dimension(unit = Dimension.DP) val elevation: Int,
  @SizeMode val layoutWidth: Int,
  @SizeMode val layoutHeight: Int,
)
