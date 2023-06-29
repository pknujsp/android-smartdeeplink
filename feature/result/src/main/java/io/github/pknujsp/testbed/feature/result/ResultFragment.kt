package io.github.pknujsp.testbed.feature.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.pknujsp.testbed.feature.result.databinding.FragmentResultBinding
import kotlinx.coroutines.launch

class ResultFragment : Fragment() {
  private var _binding: FragmentResultBinding? = null
  private val binding get() = _binding!!

  private val viewModel: ResultViewModel by viewModels()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentResultBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.apply {

    }

    viewLifecycleOwner.lifecycle.coroutineScope.launch {
      viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.test()
      }
    }
  }

  override fun onDestroyView() {
    _binding = null
    super.onDestroyView()
  }
}
