package com.pknujsp.feature.holographic

import android.graphics.Outline
import android.graphics.Path
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.RotateAnimation
import com.pknujsp.feature.holographic.databinding.FragmentImageBinding


class ImageFragment : Fragment() {

    private var _binding: FragmentImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageview.also { imageView ->
            ShadowManager(requireContext().applicationContext, lifecycle) { x, y, rotX, rotY ->
                imageView.rotationX = rotX
                imageView.rotationY = rotY

                imageView.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline?) {
                        outline?.apply {
                            setRoundRect(0, 0, view.width, view.height, 8f)
                            offset(y.toInt() * 2, x.toInt() * 2)
                        }
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