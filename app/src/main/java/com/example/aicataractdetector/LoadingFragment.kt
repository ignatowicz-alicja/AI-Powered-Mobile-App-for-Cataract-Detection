package com.example.aicataractdetector

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.example.aicataractdetector.classification.EncoderClassifier
import com.example.aicataractdetector.util.EllipseCropper
import com.example.aicataractdetector.util.EllipsePointsHolder
import com.example.aicataractdetector.util.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class LoadingFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var lottieAnimationView: LottieAnimationView

    private lateinit var encoderClassifier: EncoderClassifier
    private var nuclearInterpreter: Interpreter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        encoderClassifier = EncoderClassifier(requireContext())
        nuclearInterpreter = Interpreter(loadModelFile("vgg11.tflite"))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_progress_overlay, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.progressBar)
        lottieAnimationView = view.findViewById(R.id.lottieAnimationView)
        lottieAnimationView.playAnimation()
        startImageClassification()
    }

    private fun startImageClassification() {
        val imageUriString = arguments?.getString("imageUri")
        if (imageUriString.isNullOrEmpty()) {
            Log.e("LoadingFragment", "imageUri is null")
            findNavController().navigateUp()
            return
        }

        lifecycleScope.launch {
            try {
                val (isCataract, filledBitmap, classificationTime) = withContext(Dispatchers.IO) {
                    val uri = Uri.parse(imageUriString)
                    val original = BitmapUtils.getBitmapFixed(requireContext(), uri)

                    val points = EllipsePointsHolder.points
                    if (points.isEmpty()) throw IllegalStateException("Ellipse points are empty")
                    val pupil = EllipseCropper().crop(original!!, points)

                    val start = System.currentTimeMillis()
                    val result = encoderClassifier.classify(pupil)
                    val duration = System.currentTimeMillis() - start

                    val isCat = result == "CATARACT"
                    Triple(isCat, pupil, duration)
                }

                if (isCataract) {
                    val filledForTFLite = removeTransparentBackground(filledBitmap)
                    val stages = classifyNuclearStage(filledForTFLite)
                    val labels = stages.map { it.first }.toTypedArray()
                    val scores = stages.map { it.second }.toFloatArray()
                    view?.postDelayed({
                        findNavController().navigate(
                            LoadingFragmentDirections
                                .actionLoadingFragmentToClassificationResultNuclearFragment(
                                    imageUri = imageUriString,
                                    labels = arrayOf("Cataract"),
                                    scores = floatArrayOf(1.0f),
                                    stageLabels = labels,
                                    stageScores = scores,
                                    stageTitle = "LOCS III Classification: NC",
                                    classificationTimeMillis = classificationTime
                                )
                        )
                    }, 500)
                } else {
                    view?.postDelayed({
                        findNavController().navigate(
                            LoadingFragmentDirections
                                .actionLoadingFragmentToClassificationResultFragment(
                                    imageUri = imageUriString,
                                    labels = arrayOf("Non-cataract"),
                                    scores = floatArrayOf(1.0f),
                                    classificationTimeMillis = classificationTime
                                )
                        )
                    }, 500)
                }

            } catch (e: Exception) {
                Log.e("LoadingFragment", "Error during classification", e)
            }
        }
    }

    private fun classifyNuclearStage(bitmap: Bitmap): List<Pair<String, Float>> {
        val input = preprocess(bitmap)
        val out = Array(1) { FloatArray(6) }
        nuclearInterpreter?.run(input, out)
        val probs = softmax(out[0])
        val labels = listOf("1", "2", "3", "4", "5", "6")
        return labels.mapIndexed { i, l -> l to probs[i] }
    }

    private fun preprocess(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = Array(1) { Array(3) { Array(224) { FloatArray(224) } } }
        for (y in 0 until 224) for (x in 0 until 224) {
            val p = resized.getPixel(x, y)
            val r = ((p shr 16) and 0xFF) / 255f
            val g = ((p shr 8) and 0xFF) / 255f
            val b = (p and 0xFF) / 255f
            input[0][0][y][x] = (r - 0.485f) / 0.229f
            input[0][1][y][x] = (g - 0.456f) / 0.224f
            input[0][2][y][x] = (b - 0.406f) / 0.225f
        }
        return input
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val max = logits.maxOrNull() ?: 0f
        val exps = logits.map { Math.exp((it - max).toDouble()) }
        val sum = exps.sum()
        return exps.map { (it / sum).toFloat() }.toFloatArray()
    }

    private fun removeTransparentBackground(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply { color = Color.BLACK }
        canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return result
    }

    private fun loadModelFile(assetName: String): MappedByteBuffer {
        val file = File(requireContext().filesDir, assetName)
        if (!file.exists()) {
            requireContext().assets.open(assetName).use { it.copyTo(FileOutputStream(file)) }
        }
        FileInputStream(file).use { fis ->
            return fis.channel.map(FileChannel.MapMode.READ_ONLY, 0, fis.channel.size())
        }
    }
}
