package io.github.pknujsp.testbed.core.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.github.pknujsp.testbed.core.ui.dialog.DialogType

private typealias Inflate<Binding> = (LayoutInflater, ViewGroup?, Boolean) -> Binding

abstract class BaseDialogFragment<Binding : ViewDataBinding>(
  private val inflate: Inflate<Binding>,
  private val dialogType: DialogType,
) : DialogFragment() {


    private var _binding: Binding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
      theme(dialogType)
        _binding = inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Shows the dialog fragment.
     *
     * @param fragmentManager The fragment manager.
     */
    fun show(fragmentManager: FragmentManager) {
        show(fragmentManager, this::class.qualifiedName)
    }
}


private fun DialogFragment.theme(dialogType: DialogType) {
  /*
  <item name="android:windowFullscreen">false</item>
  <item name="android:windowIsFloating">true</item>
  <item name="android:windowNoTitle">true</item>
  <item name="android:windowBackground">@android:color/transparent</item>
   */
  dialog?.window?.apply {
    requestFeature(Window.FEATURE_NO_TITLE)
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
    }
}
