package io.github.pknujsp.testbed.feature.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import io.github.pknujsp.simpledialog.SimpleDialogBuilder
import io.github.pknujsp.simpledialog.attrs.DragDirection
import io.github.pknujsp.simpledialog.constants.DialogType
import io.github.pknujsp.simpledialog.dialogs.SimpleDialog
import io.github.pknujsp.testbed.core.ui.databinding.BottomsheetTestBinding
import io.github.pknujsp.testbed.core.ui.databinding.ViewFullLoadingBinding
import io.github.pknujsp.testbed.core.ui.databinding.ViewLoadingBinding
import io.github.pknujsp.testbed.feature.dialog.databinding.FragmentMainDialogBinding
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")

class MainDialogFragment : Fragment() {
  private var _binding: FragmentMainDialogBinding? = null
  private val binding get() = _binding!!

  private val viewModel by viewModels<MainDialogViewModel>()

  private var dialog: SimpleDialog? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentMainDialogBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.apply {
      showDialogBtn.setOnClickListener {
        viewModel.dialogBuilder.value?.also { builder ->
          dialog?.dismiss()
          builder.setContentView(dialogView(dialogType()))
          dialog = builder.buildAndShow()
        }
      }

      behindBlurSlider.addOnChangeListener { _, value, _ ->
        behindBlurTextview.text = "Behind Blur -> ${value.toInt()}"
        viewModel.behindBlur(value.toInt())
      }
      backgroundBlurSlider.addOnChangeListener { _, value, _ ->
        backgroundBlurTextview.text = "Background Blur -> ${value.toInt()}"
        viewModel.backgroundBlur(value.toInt())
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
      endMarginSlider.addOnChangeListener { _, value, _ ->
        endMarginTextview.text = "End Margin -> ${value.toInt()}"
        viewModel.endMargin(value.toInt())
      }
      startMarginSlider.addOnChangeListener { _, value, _ ->
        startMarginTextview.text = "Start Margin -> ${value.toInt()}"
        viewModel.startMargin(value.toInt())
      }
      topMarginSlider.addOnChangeListener { _, value, _ ->
        topMarginTextview.text = "Top Margin -> ${value.toInt()}"
        viewModel.topMargin(value.toInt())
      }
      topStartCornerRadiusSlider.addOnChangeListener { _, value, _ ->
        topStartCornerRadiusTextview.text = "Top Start Corner Radius -> ${value.toInt()}"
        viewModel.topStartCornerRadius(value.toInt())
      }
      topEndCornerRadiusSlider.addOnChangeListener { _, value, _ ->
        topEndCornerRadiusTextview.text = "Top End Corner Radius -> ${value.toInt()}"
        viewModel.topEndCornerRadius(value.toInt())
      }
      bottomStartCornerRadiusSlider.addOnChangeListener { _, value, _ ->
        bottomStartCornerRadiusTextview.text = "Bottom Start Corner Radius -> ${value.toInt()}"
        viewModel.bottomStartCornerRadius(value.toInt())
      }
      bottomEndCornerRadiusSlider.addOnChangeListener { _, value, _ ->
        bottomEndCornerRadiusTextview.text = "Bottom End Corner Radius -> ${value.toInt()}"
        viewModel.bottomEndCornerRadius(value.toInt())
      }
      applyForceBlur.setOnCheckedChangeListener { _, isChecked ->
        viewModel.applyForceBlur(isChecked)
      }
      foldable.setOnCheckedChangeListener { _, isChecked ->
        viewModel.foldable(isChecked)
      }
      dialogTypesRadioGroup.setOnCheckedChangeListener { _, checkedId ->
        initAttrs()
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
      draggable.setOnCheckedChangeListener { _, isChecked ->
        viewModel.draggable(isChecked)
      }
      cancelable.setOnCheckedChangeListener { _, isChecked ->
        viewModel.cancelable(isChecked)
      }
      canceledOnTouchOutside.setOnCheckedChangeListener { _, isChecked ->
        viewModel.canceledOnTouchOutside(isChecked)
      }
      restrictViewsFromOffWindow.setOnCheckedChangeListener { _, isChecked ->
        viewModel.restrictViewsFromOffWindow(isChecked)
      }
      showModalPoint.setOnCheckedChangeListener { _, isChecked ->
        viewModel.showModalPoint(isChecked)
      }
      draggleDirectionsRadioGroup.setOnCheckedChangeListener { _, checkedId ->
        viewModel.draggleDirections(
          when (checkedId) {
            R.id.horizontal_radio -> DragDirection.Horizontal
            R.id.vertical_radio -> DragDirection.Vertical
            else -> DragDirection.Both
          },
        )
      }

    }
  }

  override fun onStart() {
    super.onStart()
    viewLifecycleOwner.lifecycleScope.launch {
      if (dialog?.isShowing() != true) {
        initAttrs()
      }
    }
  }

  private fun initAttrs() {
    binding.apply {
      val dialogType = dialogType()
      viewModel.init(
        SimpleDialogBuilder.builder(requireContext(), dialogType).setContentView(dialogView(dialogType)).setElevation(
          elevationSlider.value.toInt(),
        ).setLayoutSize(
          if (width.isChecked) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
          if (height.isChecked) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT,
        ).setCornerRadius(
          topStartCornerRadiusSlider.value.toInt(),
          topEndCornerRadiusSlider.value.toInt(),
          bottomStartCornerRadiusSlider.value.toInt(),
          bottomEndCornerRadiusSlider.value.toInt(),
        ).setDim(dimSlider.value.toInt() > 0, dimSlider.value.toInt())
          .setBehindBlur(behindBlurSlider.value.toInt() > 0, applyForceBlur.isChecked, behindBlurSlider.value.toInt())
          .setCancelable(cancelable.isChecked).setBackgroundBlur(backgroundBlurSlider.value.toInt() > 0, false, backgroundBlurSlider.value.toInt())
          .setStartMargin(startMarginSlider.value.toInt()).setBottomMargin(bottomMarginSlider.value.toInt())
          .setTopMargin(topMarginSlider.value.toInt()).setEndMargin(endMarginSlider.value.toInt()).setDragDirection(
            when (draggleDirectionsRadioGroup.checkedRadioButtonId) {
              R.id.horizontal_radio -> DragDirection.Horizontal
              R.id.vertical_radio -> DragDirection.Vertical
              else -> DragDirection.Both
            },
          ).setDraggable(draggable.isChecked).setCanceledOnTouchOutside(canceledOnTouchOutside.isChecked)
          .setRestrictViewsFromOffWindow(restrictViewsFromOffWindow.isChecked)
          .setIsShowModalPoint(showModalPoint.isChecked),
      )
    }
  }


  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
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
