package com.example.aicataractdetector.classification

import android.content.Context
import android.graphics.*
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.pow

class EncoderClassifier(private val context: Context) {

    private val modelFileName = "ae_model.pt"
    private val thresholdFileName = "threshold.txt"

    private val model: Module
    private val threshold: Float

    private val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    private val std = floatArrayOf(0.229f, 0.224f, 0.225f)
    private val targetSize = 224


    var lastProcessedBitmap: Bitmap? = null
        private set

    init {
        val modelPath = assetFilePath(context, modelFileName)
        model = Module.load(modelPath)

        threshold = try {
            context.assets.open(thresholdFileName).bufferedReader().readLine().toFloat()
        } catch (e: IOException) {
            Log.e("AEClassifier", "Error loading the Autoencoder model", e)
            throw RuntimeException("Cannot load Autoencoder model from assets", e)
        }
        Log.i("AEClassifier", "AE model and threshold loaded. Threshold = $threshold")
    }

    fun classify(rawBitmap: Bitmap): String {
        val processed = preprocessBeforeEncoding(rawBitmap)

        lastProcessedBitmap = processed

        val inputTensor = bitmapToNormalizedTensor(processed)
        val outputTensor: Tensor = model.forward(IValue.from(inputTensor)).toTensor()
        val mse = calculateMSE(inputTensor, outputTensor)

        Log.d("AEClassifier", "Computed MSE = $mse, Threshold = $threshold")
        return if (mse <= threshold) "CATARACT" else "NON-CATARACT"
    }

    private fun preprocessBeforeEncoding(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var left = width
        var right = 0
        var top = height
        var bottom = 0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val alpha = Color.alpha(pixels[y * width + x])
                if (alpha > 0) {
                    if (x < left) left = x
                    if (x > right) right = x
                    if (y < top) top = y
                    if (y > bottom) bottom = y
                }
            }
        }

        val cropped = Bitmap.createBitmap(bitmap, left, top, right - left + 1, bottom - top + 1)
        return Bitmap.createScaledBitmap(cropped, targetSize, targetSize, true)
    }

    private fun bitmapToNormalizedTensor(bitmap: Bitmap): Tensor {
        val wh = targetSize * targetSize
        val data = FloatArray(3 * wh)

        var idxR = 0
        var idxG = wh
        var idxB = 2 * wh

        val pixels = IntArray(wh)
        bitmap.getPixels(pixels, 0, targetSize, 0, 0, targetSize, targetSize)

        for (i in pixels.indices) {
            val p = pixels[i]
            val r = ((p ushr 16) and 0xFF) / 255f
            val g = ((p ushr 8) and 0xFF) / 255f
            val b = (p and 0xFF) / 255f

            data[idxR++] = (r - mean[0]) / std[0]
            data[idxG++] = (g - mean[1]) / std[1]
            data[idxB++] = (b - mean[2]) / std[2]
        }

        return Tensor.fromBlob(data, longArrayOf(1, 3, targetSize.toLong(), targetSize.toLong()))
    }

    private fun calculateMSE(input: Tensor, output: Tensor): Float {
        val inputData = input.dataAsFloatArray
        val outputData = output.dataAsFloatArray
        val numElements = inputData.size.toFloat()
        var sumSquaredError = 0.0f

        for (i in inputData.indices) {
            val error = inputData[i] - outputData[i]
            sumSquaredError += error.pow(2)
        }

        return sumSquaredError / numElements
    }

    @Throws(IOException::class)
    private fun assetFilePath(ctx: Context, name: String): String {
        val file = File(ctx.filesDir, name)
        if (file.exists() && file.length() > 0) return file.absolutePath

        ctx.assets.open(name).use { input ->
            FileOutputStream(file).use { output ->
                val buffer = ByteArray(4096)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
            }
        }
        return file.absolutePath
    }
}
