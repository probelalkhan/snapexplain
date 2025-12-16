package dev.belalkhan.snapexplain.data.repository

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import dev.belalkhan.snapexplain.core.base.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor() {

    private val generativeModel = Firebase.ai.generativeModel("gemini-1.5-flash")

    suspend fun explainCode(bitmap: Bitmap): Resource<String> = withContext(Dispatchers.IO) {
        try {
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
                image(bitmap)
                text(prompt)
            }

            val response = generativeModel.generateContent(content)
            val text = response.text ?: "Unable to generate explanation"

            Resource.Success(text)
        } catch (e: Exception) {
            Resource.Error("Failed to analyze image: ${e.message}", e)
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
}
