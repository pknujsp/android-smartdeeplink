package core.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import io.github.pknujsp.simpledialog.SimpleDialogBuilder
import io.github.pknujsp.simpledialog.constants.DialogType

abstract class SimpleDialogFragment() : DialogFragment(), DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {
  var dialogType: DialogType = DialogType.Normal
  private var mShowsDialog = true
  private var mBackStackId = -1
  private var mCreatingDialog = false


  private val mHandler: Handler? = null
  private val mDismissRunnable = Runnable { mOnDismissListener.onDismiss(mDialog) }

  private val mOnCancelListener = DialogInterface.OnCancelListener {
    if (mDialog != null) {
      this@DialogFragment.onCancel(mDialog)
    }
  }

  private val mOnDismissListener = DialogInterface.OnDismissListener {
    if (mDialog != null) {
      this@DialogFragment.onDismiss(mDialog)
    }
  }

  private val mObserver: Observer<LifecycleOwner> = object : Observer<LifecycleOwner?> {
    @SuppressLint("SyntheticAccessor")
    override fun onChanged(lifecycleOwner: LifecycleOwner?) {
      if (lifecycleOwner != null && mShowsDialog) {
        val view = requireView()
        check(view.parent == null) { "DialogFragment can not be attached to a container view" }
        if (mDialog != null) {
          if (FragmentManager.isLoggingEnabled(Log.DEBUG)) {
            Log.d(
              FragmentManager.TAG,
              "DialogFragment " + this + " setting the content view on " + mDialog,
            )
          }
          mDialog.setContentView(view)
        }
      }
    }
  }

  private val mDialog: Dialog? = null
  private val mViewDestroyed = false
  private var mDismissed = false
  private var mShownByMe = false
  private val mDialogCreated = false


  override fun show(manager: FragmentManager, tag: String?) {
    mDismissed = false
    mShownByMe = true
    val ft = manager.beginTransaction()
    ft.add(this, tag)
    ft.commit()
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return SimpleDialogBuilder.builder(requireContext(), dialogType).run {
      setCancelable(isCancelable)
      buildAndShow().dialog
    }
  }

  override fun onGetLayoutInflater(savedInstanceState: Bundle?): LayoutInflater {
    var layoutInflater = super.onGetLayoutInflater(savedInstanceState)
    if (!mShows || mCreatingDialog) {
      if (FragmentManager.isLoggingEnabled(Log.VERBOSE)) {
        val message = "getting layout inflater for DialogFragment $this"
        if (!mShowsDialog) {
          Log.d(FragmentManager.TAG, "mShowsDialog = false: $message")
        } else {
          Log.d(FragmentManager.TAG, "mCreatingDialog = true: $message")
        }
      }
      return layoutInflater
    }

    prepareDialog(savedInstanceState)

    if (FragmentManager.isLoggingEnabled(Log.VERBOSE)) {
      Log.d(FragmentManager.TAG, "get layout inflater for DialogFragment $this from dialog context")
    }

    if (mDialog != null) {
      layoutInflater = layoutInflater.cloneInContext(mDialog!!.context)
    }
    return layoutInflater
  }

}
