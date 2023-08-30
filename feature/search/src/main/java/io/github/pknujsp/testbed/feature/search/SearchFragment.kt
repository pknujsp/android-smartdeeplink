package io.github.pknujsp.testbed.feature.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
      /*

      findNavController().deepNavigate(
        "app://result",
        PersonInfoArgs(
          name = binding.nameEdit.text.toString(),
          age = binding.ageEdit.text.toString().toInt(),
          height = binding.heightEdit.text.toString().toFloat(),
          isMale = binding.maleCheck.isChecked,
        ),
      )
       */
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
