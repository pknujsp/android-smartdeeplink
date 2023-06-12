package com.pknujsp.feature.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pknujsp.core.model.PersonInfoArgs
import com.pknujsp.deeplink.deepNavigate
import com.pknujsp.feature.search.databinding.FragmentSearchBinding

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
            findNavController().deepNavigate("app://result",
                PersonInfoArgs(
                    name = "홍길동",
                    age = 20,
                    height = 180.0f,
                    isMale = true
                ))
        }


    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}