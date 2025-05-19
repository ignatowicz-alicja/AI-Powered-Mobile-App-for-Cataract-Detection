package com.example.aicataractdetector.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aicataractdetector.R
import android.widget.Button


class ClassificationResultNC : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var timeTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_classification_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = view.findViewById(R.id.imageViewOriginal)
        resultTextView = view.findViewById(R.id.textViewResult)
        timeTextView = view.findViewById(R.id.textViewTime)


        val imageUri = arguments?.getString("imageUri")
        val labels = arguments?.getStringArray("labels")
        val scores = arguments?.getFloatArray("scores")
        val stageLabels = arguments?.getStringArray("stageLabels")
        val stageScores = arguments?.getFloatArray("stageScores")
        val stageTitle = arguments?.getString("stageTitle") ?: "LOCS III Classification: NC"
        val classificationTime = arguments?.getLong("classificationTimeMillis") ?: 0

        imageUri?.let {
            val uri = Uri.parse(it)
            imageView.setImageURI(uri)
        }

        val sb = StringBuilder()
        if (labels != null && scores != null && labels.size == scores.size) {
            val resultText = labels.zip(scores.toTypedArray())
                .joinToString("\n") { (label, score) ->
                    if (label.equals("Cataract", ignoreCase = true)) {
                        label
                    } else {
                        "$label: ${(score * 100).format(1)}%"
                    }
                }
            sb.append("Main Classification:\n")
            sb.append(resultText)
        }

        if (stageLabels != null && stageScores != null && stageLabels.size == stageScores.size) {
            sb.append("\n\n$stageTitle:\n")
            sb.append(
                stageLabels.zip(stageScores.toTypedArray())
                    .joinToString("\n") { (label, score) ->
                        "$label: ${(score * 100).format(1)}%"
                    }
            )
        }

        resultTextView.text = sb.toString()

        if (classificationTime > 0) {
            timeTextView.text = "Classification time: ${classificationTime} ms"
        } else {
            timeTextView.text = ""
        }

        val returnButton = view.findViewById<Button>(R.id.buttonReturnToPicker)
        returnButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_classificationResultNuclearFragment_to_imagePickerFragment
            )
        }



    }

    }

    private fun Float.format(decimals: Int): String =
        "%.${decimals}f".format(this)
