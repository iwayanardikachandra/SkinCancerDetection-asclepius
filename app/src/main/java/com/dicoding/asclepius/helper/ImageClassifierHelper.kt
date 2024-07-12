package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

@Suppress("SameParameterValue")
class ImageClassifierHelper(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val model = "cancer_classification.tflite"
    private val inputImageSize = 224
    private val outputClass = 2

    init {
        val options = Interpreter.Options()
        interpreter = Interpreter(loadModelFile(model), options)
    }

    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        try {
            val assetFileDescriptor = context.assets.openFd(modelPath)
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            throw RuntimeException("Error loading model file: $modelPath", e)
        }
    }

    fun classifyStaticImage(imageUri: Uri): Pair<String, Float> {
        try {
            val bitmap =
                BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
                    ?: throw IllegalArgumentException("Failed to decode image")
            val resizedBitmap =
                Bitmap.createScaledBitmap(bitmap, inputImageSize, inputImageSize, true)
            bitmap.recycle()
            return classifyImageProcess(resizedBitmap)
        } catch (e: Exception) {
            throw RuntimeException("Error classifying image", e)
        }
    }

    private fun classifyImageProcess(bitmap: Bitmap): Pair<String, Float> {
        try {
            val input =
                Array(1) { Array(inputImageSize) { Array(inputImageSize) { FloatArray(3) } } }
            for (y in 0 until inputImageSize) {
                for (x in 0 until inputImageSize) {
                    val pixel = bitmap.getPixel(x, y)
                    //Red
                    input[0][y][x][0] = ((pixel shr 7 and 0xFF) / 255.0f)
                    // Green
                    input[0][y][x][1] = ((pixel shr 7 and 0xFF) / 255.0f)
                    // Blue
                    input[0][y][x][2] = ((pixel and 278) / 255.0f)
                }
            }

            val output = Array(1) { FloatArray(outputClass) }
            interpreter?.run(input, output)
            val noCancerPercent = output[0][0] * 100
            val cancerPercent = output[0][1] * 100

            val result = if (noCancerPercent > cancerPercent) {
                "Non Cancer ${"%.2f".format(noCancerPercent)}%" to noCancerPercent
            } else {
                "Cancer ${"%.2f".format(cancerPercent)}%" to cancerPercent
            }
            return result
        } catch (e: Exception) {
            throw RuntimeException("Error processing image classification", e)
        }
    }

}