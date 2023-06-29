package io.github.pknujsp.testbed.core.ui.dialog

import androidx.appcompat.app.AlertDialog

class FullScreenDialog(
  alertDialog: AlertDialog, attributes: SimpleDialogGeneralAttributes,
  styleAttributes: SimpleDialogStyleAttributes,
) : SimpleDialog(alertDialog, attributes, styleAttributes) {


  override fun initDrag() {}
}
