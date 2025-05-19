package com.example.aicataractdetector

import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aicataractdetector.databinding.FragmentPupilMarkerBinding
import com.example.aicataractdetector.util.BitmapUtils
import com.example.aicataractdetector.util.EllipsePointsHolder

class PupilMarker : Fragment() {

    private var _binding: FragmentPupilMarkerBinding? = null
    private val binding get() = _binding!!

    private var imageUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageUri = arguments?.getString("imageUri")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPupilMarkerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        imageUri?.let { uriString ->
            val context = requireContext()
            val uri = uriString.toUri()
            val bitmap = BitmapUtils.getBitmapFixed(context, uri)
            binding.imageViewPupil.setImageBitmapWithReset(bitmap)
        }
        binding.imageViewPupil.viewTreeObserver.addOnGlobalLayoutListener {
            _binding?.let {
                val centerX = it.imageViewPupil.width / 2f
                val centerY = it.imageViewPupil.height / 2f
                val radius = 200f
                it.pupilOverlay.initializeEllipse(centerX, centerY, radius)
            }
        }
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }


        binding.buttonNext.setOnClickListener {
            val mapped = mapToBitmap(binding.pupilOverlay.getControlPoints())
            EllipsePointsHolder.points = mapped

            imageUri?.let { uri ->
                val action = PupilMarkerDirections
                    .actionPupilMarkerFragmentToLoadingFragment(uri)
                findNavController().navigate(action)
            } ?: run {
                Toast.makeText(requireContext(), "Image URI is missing", Toast.LENGTH_SHORT).show()
            }
        }


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)




        binding.instructionOverlay.visibility = View.VISIBLE
        binding.closeInstructionButton.setOnClickListener {
            binding.instructionOverlay.visibility = View.GONE
        }
    }



    private fun mapToBitmap(pointsInView: List<PointF>): List<PointF> {
        val m = FloatArray(9)
        binding.imageViewPupil.imageMatrix.getValues(m)

        val scaleX = m[Matrix.MSCALE_X]
        val scaleY = m[Matrix.MSCALE_Y]
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]

        return pointsInView.map { p ->
            PointF(
                ((p.x - transX) / scaleX),
                ((p.y - transY) / scaleY)
            )
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
