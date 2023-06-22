package io.github.pknujsp.testbed.core.ui

import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.Dimension
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import java.lang.ref.WeakReference

object DialogInstances : SimpleDialogController {
  private var dialog: AlertDialog? = null

  fun add(dialog: AlertDialog) {
    this.dialog = dialog
  }

  override fun dismiss() {
    dialog?.dismiss()
    dialog = null
  }
}

private const val defaultBlurIndensity: Int = 15
private const val defaultDimIndensity: Int = 35
private const val defaultMarginBottom: Int = 20
private const val defaultMarginHorizontal: Int = 24
private const val defaultCornerRadius: Int = 16
private const val defaultElevation: Int = 12
private const val defaultWidth: Int = WRAP_CONTENT
private const val defaultHeight: Int = WRAP_CONTENT

class SimpleDialogBuilder private constructor(
  private val view: View, private val dialogType: DialogType,
) {
  private var blur: Boolean = true

  @IntRange(from = 0, to = 100) private var blurIndensity: Int = defaultBlurIndensity

  private var dim: Boolean = true

  @IntRange(from = 0, to = 100) private var dimIndensity: Int = defaultDimIndensity

  private var cancelable: Boolean = true

  @Dimension(unit = Dimension.DP) private var bottomMargin: Int = defaultMarginBottom

  @Dimension(unit = Dimension.DP) private var horizontalMargin: Int = defaultMarginHorizontal

  @Dimension(unit = Dimension.DP) private var cornerRadius: Int = defaultCornerRadius

  @IdRes private var backgroundResourceId: Int? = null

  @Dimension(unit = Dimension.DP) private var elevation: Int = defaultElevation

  @SizeMode private var layoutWidth: Int = defaultWidth

  @SizeMode private var layoutHeight: Int = defaultHeight

  companion object {
    fun builder(view: View, dialogType: DialogType): SimpleDialogBuilder = WeakReference(SimpleDialogBuilder(view, dialogType)).get()!!
  }

  fun setBlur(blur: Boolean, @IntRange(from = 0, to = 100) blurIndensity: Int = defaultBlurIndensity): SimpleDialogBuilder {
    this.blur = blur
    return this
  }

  fun setDim(dim: Boolean, @IntRange(from = 0, to = 100) dimIndensity: Int = defaultDimIndensity): SimpleDialogBuilder {
    this.dim = dim
    return this
  }

  fun setCancelable(cancelable: Boolean): SimpleDialogBuilder {
    this.cancelable = cancelable
    return this
  }

  /**
   * only applied for DialogType.BottomSheet
   */
  fun setMarginBottom(@Dimension(unit = Dimension.DP) marginBottom: Int): SimpleDialogBuilder {
    this.bottomMargin = marginBottom
    return this
  }

  /**
   * only applied for DialogType.BottomSheet
   */
  fun setMarginHorizontal(@Dimension(unit = Dimension.DP) marginHorizontal: Int): SimpleDialogBuilder {
    this.horizontalMargin = marginHorizontal
    return this
  }

  fun setCornerRadius(@Dimension(unit = Dimension.DP) cornerRadius: Int): SimpleDialogBuilder {
    this.cornerRadius = cornerRadius
    return this
  }

  fun setBackgroundResourceId(@IdRes backgroundResourceId: Int): SimpleDialogBuilder {
    this.backgroundResourceId = backgroundResourceId
    return this
  }

  fun setElevation(@Dimension(unit = Dimension.DP) elevation: Int): SimpleDialogBuilder {
    this.elevation = elevation
    return this
  }

  fun setLayoutSize(@SizeMode layoutWidth: Int, @SizeMode layoutHeight: Int): SimpleDialogBuilder {
    this.layoutWidth = layoutWidth
    this.layoutHeight = layoutHeight
    return this
  }

  /**
   * 다이얼로그를 빌드하고 보여줍니다.
   */
  fun buildAndShow() {
    DialogInstances.dismiss()
    DialogInstances.add(
      AlertDialog.Builder(view.context, theme(dialogType)).setView(view).create().apply {
        setOnDismissListener {
          DialogInstances.dismiss()
        }
        theme(
          SimpleDialogAttributes(
            blur, blurIndensity, dim, dimIndensity, cancelable, view, dialogType,
            bottomMargin, horizontalMargin, cornerRadius, backgroundResourceId, elevation,
            layoutWidth, layoutHeight,
          ),
        )
        show()
      },
    )
  }

  @StyleRes
  private fun theme(dialogType: DialogType): Int = when (dialogType) {
    DialogType.Normal, DialogType.Fullscreen -> R.style.Dialog
    DialogType.BottomSheet -> R.style.BottomSheet
  }
}
