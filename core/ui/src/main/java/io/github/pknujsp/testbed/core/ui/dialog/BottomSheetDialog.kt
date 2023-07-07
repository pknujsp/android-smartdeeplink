package io.github.pknujsp.testbed.core.ui.dialog

import IGLSurfaceView
import android.app.Dialog

class BottomSheetDialog(
  dialog: Dialog, attributes: SimpleDialogGeneralAttributes,
  styleAttributes: SimpleDialogStyleAttributes,
  blurringViewLifeCycleListener: IGLSurfaceView? = null,
) : SimpleDialog(dialog, attributes, styleAttributes, blurringViewLifeCycleListener) {


}
