package com.example.rice

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.nio.ByteBuffer
import java.nio.ByteOrder
import androidx.core.graphics.scale

object ImageUtils {
    fun uriToBitmap(context: Context, uri: Uri): Bitmap {
        return BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))!!
    }

    fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val resizedBitmap = bitmap.scale(224, 224) // Adjust size based on your model input
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(224 * 224)
        resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
        for (pixelValue in intValues) {
            val r = (pixelValue shr 16 and 0xFF) / 255.0f
            val g = (pixelValue shr 8 and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        return byteBuffer
    }
}
