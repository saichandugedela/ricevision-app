
package com.example.rice
import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.MappedByteBuffer
class TensorFlowLiteModel(context: Context) {

    private var interpreterModel1: Interpreter? = null
    private var interpreterModel2: Interpreter? = null
    private var interpreterMetaModel: Interpreter? = null



    private val assetManager = context.assets

    private var leafDetectionInterpreter: Interpreter? = null

    fun isLeaf(bitmap: Bitmap): Boolean {
        val inputSize = 224
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val buffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        buffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        for (pixel in pixels) {
            buffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
            buffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
            buffer.putFloat((pixel and 0xFF) / 255f)
        }

        // Load the interpreter only once
        if (leafDetectionInterpreter == null) {
            leafDetectionInterpreter = Interpreter(loadModelFile(assetManager, "converted_model.tflite"))
        }

        val output = Array(1) { FloatArray(2) }  // Output shape is (1, 2) => [not leaf, leaf]
        leafDetectionInterpreter?.run(buffer, output)
     return output[0][0] > output[0][1]
    // If "leaf" score > "not leaf"

    }


    // Lazy loading for models
    private fun loadInterpreterModel(modelPath: String): Interpreter {
        val modelFile = loadModelFile(assetManager, modelPath)
        return Interpreter(modelFile)
    }
    private fun resizeBitmap(bitmap: Bitmap, size: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, size, size, true)
    }

    private fun loadModelFile(assets: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    // Convert Bitmap to ByteBuffer (input for the model)
    private fun bitmapToByteBuffer(bitmap: Bitmap, inputSize: Int): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = intValues[pixel++]
                byteBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f) // R
                byteBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f) // G
                byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f) // B
            }
        }
        return byteBuffer
    }
    fun extractFeatures(bitmap: Bitmap): FloatArray {
        val inputSize = 224
        val resizedBitmap = resizeBitmap(bitmap, inputSize)
        val imgByteBuffer = bitmapToByteBuffer(resizedBitmap, inputSize)

        if (interpreterModel1 == null) interpreterModel1 = loadInterpreterModel("densenet169_model.tflite")
        if (interpreterModel2 == null) interpreterModel2 = loadInterpreterModel("inceptionv3_model.tflite")
        if (interpreterModel1 == null || interpreterModel2 == null ) {
            throw RuntimeException("Model loading failed!")
        }
        val output0 = Array(1) { FloatArray(2) }
        val output1 = Array(1) { FloatArray(10) }
        val output2 = Array(1) { FloatArray(10) }

        try {

            interpreterModel1?.run(imgByteBuffer, output1)
            interpreterModel2?.run(imgByteBuffer, output2)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Feature extraction failed: ${e.message}")
        }

        val combinedFeatures = FloatArray(output1[0].size + output2[0].size)
        System.arraycopy(output1[0], 0, combinedFeatures, 0, output1[0].size)
        System.arraycopy(output2[0], 0, combinedFeatures, output1[0].size, output2[0].size)
        return combinedFeatures
    }

    // Make prediction using the meta-model (meta_learner)
    fun predictWithMetaModel(features: FloatArray): Int {
        // Load meta-model lazily when needed
        if (interpreterMetaModel == null) interpreterMetaModel = loadInterpreterModel("meta_model.tflite")

        val input = Array(1) { features }
        val output = Array(1) { FloatArray(10) } // Assuming 10 output classes

        interpreterMetaModel?.run(input, output)

        // Get the predicted class (the class with the highest probability)
        val predictedClass = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        return predictedClass
    }

    // Clean up the interpreters to avoid memory leaks
    fun close() {
        interpreterModel1?.close()
        interpreterModel2?.close()
        interpreterMetaModel?.close()
    }
}