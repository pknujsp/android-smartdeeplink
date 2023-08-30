package io.github.pknujsp.testbed.feature.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.github.pknujsp.testbed.feature.compose.ComposeActivity
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
      btnHomeToDialog.setOnClickListener {
        navigate("app://main_dialog")
      }
      btnHomeToSearch.setOnClickListener {
        navigate("app://search")
      }
      btnHomeToCompose.setOnClickListener {
        startActivity(Intent(requireContext(), ComposeActivity::class.java))
      }
      btnHomeToCard.setOnClickListener {
        navigate("app://card")
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
