package io.github.pknujsp.testbed.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.github.pknujsp.testbed.core.ui.DialogType
import io.github.pknujsp.testbed.core.ui.SimpleDialogBuilder
import io.github.pknujsp.testbed.core.ui.databinding.BottomsheetTestBinding
import io.github.pknujsp.testbed.core.ui.databinding.ViewLoadingBinding
import io.github.pknujsp.testbed.feature.home.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
  private var _binding: FragmentHomeBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.apply {
      btnHomeToCardlayout.setOnClickListener {
        navigate("app://holographic")
      }
      btnHomeToBottomsheetdialog.setOnClickListener {
        SimpleDialogBuilder.builder(BottomsheetTestBinding.inflate(layoutInflater).root, DialogType.BottomSheet).setCancelable(true).setDim(true)
          .setLayoutSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).setBlur(true).buildAndShow()
      }
      btnHomeToNormaldialog.setOnClickListener {
        SimpleDialogBuilder.builder(ViewLoadingBinding.inflate(layoutInflater).root, DialogType.Normal).setCancelable(true).setDim(true).setBlur(true)
          .setLayoutSize(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).buildAndShow()
      }
      btnHomeToSearch.setOnClickListener {
        navigate("app://search")
      }
    }
  }

  private fun navigate(uri: String) {
    findNavController().navigate(uri.toUri())
  }

  override fun onDestroyView() {
    _binding = null
    super.onDestroyView()
  }
}
