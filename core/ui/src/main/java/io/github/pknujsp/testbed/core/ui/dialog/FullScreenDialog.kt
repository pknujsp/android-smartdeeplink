package io.github.pknujsp.testbed.core.ui.dialog

import android.app.Dialog

class FullScreenDialog(
  dialog: Dialog, attributes: SimpleDialogGeneralAttributes,
  styleAttributes: SimpleDialogStyleAttributes,
) : SimpleDialog(dialog, attributes, styleAttributes) {


  override fun initDrag() {}
}
