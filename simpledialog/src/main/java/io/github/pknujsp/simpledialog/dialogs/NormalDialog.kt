package io.github.pknujsp.simpledialog.dialogs

import android.app.Dialog
import io.github.pknujsp.simpledialog.attrs.SimpleDialogGeneralAttributes
import io.github.pknujsp.simpledialog.attrs.SimpleDialogStyleAttributes
import io.github.pknujsp.simpledialog.blur.view.IGLSurfaceView

class NormalDialog(
  dialog: Dialog, attributes: SimpleDialogGeneralAttributes,
  styleAttributes: SimpleDialogStyleAttributes,
  blurringViewLifeCycleListener: IGLSurfaceView? = null,
) : SimpleDialog(dialog, attributes, styleAttributes, blurringViewLifeCycleListener)
