package com.example.aicataractdetector.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aicataractdetector.R

class ClassificationResult : Fragment() {

    private val args: ClassificationResultArgs by navArgs()

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

        val uri = Uri.parse(args.imageUri)
        imageView.setImageURI(uri)

        val labels = args.labels
        val classificationTime = args.classificationTimeMillis
        Log.d("ClassificationResult", "Labels: ${labels.joinToString()}")
        Log.d("ClassificationResult", "Classification time: ${classificationTime} ms")


        resultTextView.text = labels.joinToString("\n")

        if (classificationTime > 0) {
            timeTextView.text = "Classification time: ${classificationTime} ms"
        } else {
            timeTextView.text = ""
        }

        val returnButton = view.findViewById<Button>(R.id.buttonReturnToPicker)
        returnButton.setOnClickListener {
            findNavController().navigate(R.id.action_classificationResultFragment_to_imagePickerFragment)
        }
    }
}
