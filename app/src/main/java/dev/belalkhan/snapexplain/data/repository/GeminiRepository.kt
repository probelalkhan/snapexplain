package dev.belalkhan.snapexplain.data.repository

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import dev.belalkhan.snapexplain.core.base.Resource
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class GeminiRepository @Inject constructor() {

    private val generativeModel = Firebase.ai.generativeModel("gemini-2.5-flash")

    // Maximum file size in bytes (200KB)
    private val MAX_IMAGE_SIZE_BYTES = 200 * 1024

    suspend fun explainCode(bitmap: Bitmap): Resource<String> {
        TODO()
    }


    suspend fun explainCodeFromText(codeSnippet: String): Resource<String> {
        TODO()
    }

    suspend fun extractCodeLanguage(explanation: String): String {
        return "unknown"
    }

    /**
     * Optimizes bitmap to ensure it's under MAX_IMAGE_SIZE_BYTES (200KB)
     * Reduces quality and dimensions as needed
     */
    private fun optimizeBitmap(bitmap: Bitmap): Bitmap {
        // First check if bitmap needs optimization
        val initialSize = getBitmapSizeInBytes(bitmap)
        if (initialSize <= MAX_IMAGE_SIZE_BYTES) {
            return bitmap
        }

        // Calculate scale factor to get close to target size
        val scaleFactor = sqrt(MAX_IMAGE_SIZE_BYTES.toFloat() / initialSize)
        val newWidth = (bitmap.width * scaleFactor).toInt().coerceAtLeast(100)
        val newHeight = (bitmap.height * scaleFactor).toInt().coerceAtLeast(100)

        var currentBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        // Now compress with varying quality until under limit
        var quality = 90
        var outputStream = ByteArrayOutputStream()
        currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

        while (outputStream.size() > MAX_IMAGE_SIZE_BYTES && quality > 10) {
            quality -= 10
            outputStream = ByteArrayOutputStream()
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        // If still too large, scale down more aggressively
        if (outputStream.size() > MAX_IMAGE_SIZE_BYTES) {
            val additionalScale = 0.8f
            val finalWidth = (currentBitmap.width * additionalScale).toInt()
            val finalHeight = (currentBitmap.height * additionalScale).toInt()
            currentBitmap = Bitmap.createScaledBitmap(currentBitmap, finalWidth, finalHeight, true)

            quality = 80
            outputStream = ByteArrayOutputStream()
            currentBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }

        return currentBitmap
    }

    /**
     * Calculate bitmap size in bytes
     */
    private fun getBitmapSizeInBytes(bitmap: Bitmap): Int {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        return outputStream.size()
    }
}
