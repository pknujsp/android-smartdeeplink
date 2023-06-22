package io.github.pknujsp.testbed.feature.holographic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.pknujsp.testbed.feature.holographic.databinding.FragmentImageBinding
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class ImageFragment : Fragment() {

    private var _binding: FragmentImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        binding.root.doOnLayout {
                            ColorAnalyzer().analyzeColor(statusTextview, imageview,
                                resources.getColor(com.pknujsp.core.ui.R.color.card_shadow_color, null))
                        }
                    }
                }
            }

             */

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    val outlineProvider = OutlineProvider(0, 0)
                    imageview.outlineProvider = outlineProvider
                    imageview.clipToOutline = true
                    imageview.invalidate()

                    ShadowManager(requireContext().applicationContext,
                        viewLifecycleOwner.lifecycle,
                        SupervisorJob()).gravityChannel.collectLatest { gravity ->

                        val status = "$gravity  ->  Image : ${imageview.width} x ${imageview.height}  -> " +
                                " Outline : ${outlineProvider.rect.width()} x " + "${outlineProvider.rect.height()}"
                        statusTextview.text = status
                        imageview.invalidateOutline()
                    }
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
