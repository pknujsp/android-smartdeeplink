package com.pknujsp.core.ui

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.pknujsp.core.ui.LoadingDialogInstances.dialog
import com.pknujsp.core.ui.databinding.ViewLoadingBinding


object LoadingDialogInstances {
    var dialog: AlertDialog? = null
}

fun Context.showLoadingDialog(dialogType: DialogType = DialogType.NORMAL) {
    LoadingDialogInstances.dialog?.dismiss()

    LoadingDialogInstances.dialog = AlertDialog.Builder(this, com.pknujsp.core.ui.R.style.Dialog)
        .setCancelable(false)
        .setView(ViewLoadingBinding.inflate(LayoutInflater.from(this)).let {
            it.cancelButton.setOnClickListener {
                LoadingDialogInstances.dialog?.dismiss()
            }
            it.root
        })
        .create()

    LoadingDialogInstances.dialog?.show()
}

private fun AlertDialog.style(dialogType: DialogType): Dialog {
    dialog?.window?.apply {
        /*
        <item name="android:windowMinWidthMajor">@dimen/abc_dialog_min_width_major</item>
        <item name="android:windowMinWidthMinor">@dimen/abc_dialog_min_width_minor</item>
         */

        /*
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setBackgroundDrawableResource(android.R.color.transparent)

        setClipToOutline(false)


        attributes = attributes.apply {
            copyFrom(attributes)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags = WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                blurBehindRadius = 8
            } else {
                flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                dimAmount = 0.3f
            }
        }

         */
    }
    return this
}