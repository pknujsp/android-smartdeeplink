package io.github.pknujsp.testbed.core.ui.dialog

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

abstract class SimpleDialog(val alertDialog: AlertDialog) : DialogInterface by alertDialog {

  override fun cancel() {
    alertDialog.cancel()
  }

  override fun dismiss() {
    alertDialog.dismiss()
  }
}
