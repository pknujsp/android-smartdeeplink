package io.github.pknujsp.simpledialog.dialogs

import android.content.Context
import io.github.pknujsp.simpledialog.attrs.SimpleDialogGeneralAttributes
import io.github.pknujsp.simpledialog.attrs.SimpleDialogStyleAttributes
import io.github.pknujsp.simpledialog.blur.view.IGLSurfaceView

class NormalDialog(
  context: Context, attributes: SimpleDialogGeneralAttributes,
  styleAttributes: SimpleDialogStyleAttributes,
  blurringViewLifeCycleListener: IGLSurfaceView? = null,
) : SimpleDialog(context, attributes, styleAttributes, blurringViewLifeCycleListener)
