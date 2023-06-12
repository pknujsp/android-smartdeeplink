package com.pknujsp.feature.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pknujsp.core.model.PersonInfoArgs
import com.pknujsp.deeplink.navArguments
import com.pknujsp.feature.result.databinding.FragmentResultBinding

class ResultFragment : Fragment() {
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private val args by navArguments<PersonInfoArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val result = args.toString()
            argumentsTextview.text = result
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}