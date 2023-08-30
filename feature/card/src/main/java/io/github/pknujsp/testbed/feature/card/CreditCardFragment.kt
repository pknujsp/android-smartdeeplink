package io.github.pknujsp.testbed.feature.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.pknujsp.testbed.feature.card.databinding.FragmentCreditCardBinding

class CreditCardFragment : Fragment() {

  private var _binding: FragmentCreditCardBinding? = null
  private val binding get() = _binding!!

  private lateinit var viewModel: CreditCardViewModel

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    _binding = FragmentCreditCardBinding.inflate(inflater, container, false)
    return binding.root
  }


}
