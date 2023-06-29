package io.github.pknujsp.testbed.feature.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.github.pknujsp.testbed.feature.search.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
  private var _binding: FragmentSearchBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentSearchBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.btnSearchToResult.setOnClickListener {
      findNavController().navigate("app://result".toUri())
    }
  }

  override fun onViewStateRestored(savedInstanceState: Bundle?) {
    super.onViewStateRestored(savedInstanceState)

  }

  override fun onDestroyView() {
    _binding = null
    super.onDestroyView()
  }
}
