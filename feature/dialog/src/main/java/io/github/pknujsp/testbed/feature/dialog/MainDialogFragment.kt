package io.github.pknujsp.testbed.feature.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.pknujsp.testbed.core.ui.DialogType
import io.github.pknujsp.testbed.core.ui.SimpleDialogBuilder
import io.github.pknujsp.testbed.core.ui.databinding.BottomsheetTestBinding
import io.github.pknujsp.testbed.core.ui.databinding.ViewFullLoadingBinding
import io.github.pknujsp.testbed.core.ui.databinding.ViewLoadingBinding
import io.github.pknujsp.testbed.feature.dialog.databinding.FragmentMainDialogBinding
import kotlinx.coroutines.launch

class MainDialogFragment : Fragment() {
  private var _binding: FragmentMainDialogBinding? = null
  private val binding get() = _binding!!

  private val viewModel by viewModels<MainDialogViewModel>()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentMainDialogBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.apply {
      showDialogBtn.setOnClickListener {
        viewModel.dialogBuilder.value?.also { builder ->
          builder.contentView = dialogView(builder.dialogType)
          builder.buildAndShow()
        }
      }

      blurSlider.addOnChangeListener { _, value, _ ->
        blurTextview.text = "Blur -> ${value.toInt()}"
        viewModel.blur(value.toInt())
      }
      dimSlider.addOnChangeListener { _, value, _ ->
        dimTextview.text = "Dim -> ${value.toInt()}"
        viewModel.dim(value.toInt())
      }
      elevationSlider.addOnChangeListener { _, value, _ ->
        elevationTextview.text = "Elevation -> ${value.toInt()}"
        viewModel.elevation(value.toInt())
      }
      bottomMarginSlider.addOnChangeListener { _, value, _ ->
        bottomMarginTextview.text = "Bottom Margin -> ${value.toInt()}"
        viewModel.bottomMargin(value.toInt())
      }
      horizontalMarginSlider.addOnChangeListener { _, value, _ ->
        horizontalMarginTextview.text = "Horizontal Margin -> ${value.toInt()}"
        viewModel.horizontalMargin(value.toInt())
      }
      cornerRadiusSlider.addOnChangeListener { _, value, _ ->
        cornerRadiusTextview.text = "Corner Radius -> ${value.toInt()}"
        viewModel.cornerRadius(value.toInt())
      }
      dialogTypesRadioGroup.setOnCheckedChangeListener { _, checkedId ->
        val dialogType = dialogType()
        viewModel.dialogType(dialogType, dialogView(dialogType))
      }
      width.setOnCheckedChangeListener { _, isChecked ->
        viewModel.size(
          if (width.isChecked) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
          if (height.isChecked) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
        )
      }
      height.setOnCheckedChangeListener { _, _ ->
        viewModel.size(
          if (width.isChecked) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
          if (height.isChecked) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
        )
      }
    }
  }

  override fun onViewStateRestored(savedInstanceState: Bundle?) {
    super.onViewStateRestored(savedInstanceState)
    binding.apply {
      viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
          val dialogType = dialogType()
          viewModel.init(
            SimpleDialogBuilder.builder(requireContext(), dialogView(dialogType), dialogType).setElevation(
              elevationSlider.value.toInt(),
            ).setLayoutSize(
              if (width.isChecked) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
              if (height.isChecked) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
            ).setCornerRadius(cornerRadiusSlider.value.toInt()).setDim(dimSlider.value.toInt() > 0, dimSlider.value.toInt())
              .setBlur(blurSlider.value.toInt() > 0, blurSlider.value.toInt()).setCancelable(true)
              .setHorizontalMargin(horizontalMarginSlider.value.toInt()).setBottomMargin(bottomMarginSlider.value.toInt()),
          )
        }
      }

    }
  }


  override fun onDestroyView() {
    _binding = null
    super.onDestroyView()
  }

  private fun dialogType(): DialogType = when (val type = binding.dialogTypesRadioGroup.checkedRadioButtonId) {
    R.id.fullscreen_radio -> DialogType.Fullscreen
    R.id.normal_radio -> DialogType.Normal
    else -> DialogType.BottomSheet
  }

  private fun dialogView(dialogType: DialogType): View = when (dialogType) {
    DialogType.Normal -> ViewLoadingBinding.inflate(layoutInflater).root
    DialogType.Fullscreen -> ViewFullLoadingBinding.inflate(layoutInflater).root
    DialogType.BottomSheet -> BottomsheetTestBinding.inflate(layoutInflater).root
  }
}

/*
data class SimpleDialogAttributes(
  val blur: Boolean,
  @IntRange(from = 0, to = 100) val blurIndensity: Int,
  val dim: Boolean,
  @IntRange(from = 0, to = 100) val dimIndensity: Int,
  val cancelable: Boolean = true,
  val view: View, val dialogType: DialogType,
  @Dimension(unit = Dimension.DP) val bottomMargin: Int,
  @Dimension(unit = Dimension.DP) val horizontalMargin: Int,
  @Dimension(unit = Dimension.DP) val cornerRadius: Int,
  @IdRes val backgroundResourceId: Int? = null,
  @Dimension(unit = Dimension.DP) val elevation: Int,
  @SizeMode val layoutWidth: Int,
  @SizeMode val layoutHeight: Int,
)

 */
