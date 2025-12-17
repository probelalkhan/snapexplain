package dev.belalkhan.snapexplain.data.repository

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import dev.belalkhan.snapexplain.core.base.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class GeminiRepository @Inject constructor() {

    private val generativeModel = Firebase.ai.generativeModel("gemini-2.5-flash")
    
    // Maximum file size in bytes (200KB)
    private val MAX_IMAGE_SIZE_BYTES = 200 * 1024

    suspend fun explainCode(bitmap: Bitmap): Resource<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("GeminiAPI", "🚀 Starting API request...")
            
            // Optimize bitmap before sending to API
            val optimizedBitmap = optimizeBitmap(bitmap)
            android.util.Log.d("GeminiAPI", "📦 Image optimized to ${getBitmapSizeInBytes(optimizedBitmap) / 1024}KB")
            
            // Add timeout to prevent infinite loading
            kotlinx.coroutines.withTimeout(60000L) { // 60 second timeout
            val prompt = """
                Analyze this image carefully. 
                
                If the image contains CODE (programming code, algorithm, snippet, etc.):
                1. Extract and identify the programming language
                2. Provide a clear, beginner-friendly explanation of what the code does
                3. Break down key concepts and logic
                4. Explain any important syntax or patterns used
                5. Format your response in a structured way with sections
                
                If the image DOES NOT contain code:
                You must roast the user in a funny, sarcastic way for uploading the wrong image! 
                Be creative and humorous, but keep it friendly and educational. 
                Suggest they should upload an image with actual code instead.
                
                Keep your response concise but comprehensive.
            """.trimIndent()

            val content = content {
                image(optimizedBitmap)
                text(prompt)
            }

            android.util.Log.d("GeminiAPI", "📤 Sending request to Gemini...")
            val response = generativeModel.generateContent(content)
            android.util.Log.d("GeminiAPI", "📥 Received response from Gemini")
            
            val text = response.text ?: "Unable to generate explanation"
            android.util.Log.d("GeminiAPI", "✅ API request completed successfully")

            Resource.Success(text)
            } // end withTimeout
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.e("GeminiAPI", "⏱️ Request timed out after 60 seconds")
            Resource.Error("Request timed out. Please try again with a smaller image.", e)
        } catch (e: Exception) {
            android.util.Log.e("GeminiAPI", "❌ API request failed: ${e.message}")
            e.printStackTrace()
            Resource.Error("Failed to analyze image: ${e.message}", e)
        }
    }
    
    // New function for text-based code explanation
    suspend fun explainCodeFromText(codeSnippet: String): Resource<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("GeminiAPI", "🚀 Starting API request with text input...")
            
            // Add timeout to prevent infinite loading
            kotlinx.coroutines.withTimeout(60000L) { // 60 second timeout
                val prompt = """
                    Analyze the following code snippet:
                    
                    ```
                    $codeSnippet
                    ```
                    
                    Please provide:
                    1. Identify the programming language
                    2. Explain what this code does in simple terms
                    3. Break down key concepts and logic
                    4. Point out important syntax or patterns
                    5. Format your response using markdown with proper headings and code blocks
                    
                    Make your explanation clear, beginner-friendly, and well-structured.
                """.trimIndent()

                android.util.Log.d("GeminiAPI", "📤 Sending request to Gemini...")
                val response = generativeModel.generateContent(prompt)
                android.util.Log.d("GeminiAPI", "📥 Received response from Gemini")
                
                val text = response.text ?: "Unable to generate explanation"
                android.util.Log.d("GeminiAPI", "✅ API request completed successfully")

                Resource.Success(text)
            } // end withTimeout
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.e("GeminiAPI", "⏱️ Request timed out after 60 seconds")
            Resource.Error("Request timed out. Please try again.", e)
        } catch (e: Exception) {
            android.util.Log.e("GeminiAPI", "❌ API request failed: ${e.message}")
            e.printStackTrace()
            
            // Handle quota exceeded error with helpful message
            val errorMessage = when {
                e.message?.contains("quota", ignoreCase = true) == true -> {
                    "Daily quota exceeded. Please wait 24 hours or upgrade to a paid plan."
                }
                e.message?.contains("rate limit", ignoreCase = true) == true -> {
                    "Too many requests. Please wait a minute and try again."
                }
                else -> "Failed to analyze code: ${e.message}"
            }
            
            Resource.Error(errorMessage, e)
        }
    }

    suspend fun extractCodeLanguage(explanation: String): String {
        // Simple language detection from explanation
        val languageKeywords = mapOf(
            "python" to listOf("python", "def ", "import ", "print("),
            "java" to listOf("java", "public class", "public static void main"),
            "kotlin" to listOf("kotlin", "fun ", "val ", "var "),
            "javascript" to listOf("javascript", "function", "const ", "let ", "=>"),
            "c++" to listOf("c++", "cpp", "#include", "cout", "cin"),
            "c" to listOf(" c ", "#include", "printf", "scanf"),
            "swift" to listOf("swift", "func ", "var ", "let "),
            "go" to listOf("golang", " go ", "package main", "func main")
        )

        val lowerExplanation = explanation.lowercase()
        for ((language, keywords) in languageKeywords) {
            if (keywords.any { lowerExplanation.contains(it) }) {
                return language
            }
        }

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
