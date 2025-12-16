package dev.belalkhan.snapexplain.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.belalkhan.snapexplain.core.base.Resource
import dev.belalkhan.snapexplain.data.model.Explanation
import dev.belalkhan.snapexplain.data.repository.AuthRepository
import dev.belalkhan.snapexplain.data.repository.ExplanationRepository
import dev.belalkhan.snapexplain.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiRepository: GeminiRepository,
    private val explanationRepository: ExplanationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _currentExplanation = MutableStateFlow<Explanation?>(null)
    val currentExplanation: StateFlow<Explanation?> = _currentExplanation.asStateFlow()
    
    private val _analysisState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val analysisState: StateFlow<Resource<Unit>> = _analysisState.asStateFlow()
    
    fun analyzeImage(imageUri: Uri) {
        viewModelScope.launch {
            _analysisState.value = Resource.Loading
            
            try {
                // Convert URI to Bitmap
                val bitmap = uriToBitmap(imageUri)
                
                // Analyze with Gemini
                when (val result = geminiRepository.explainCode(bitmap)) {
                    is Resource.Success -> {
                        val explanation = result.data
                        val language = geminiRepository.extractCodeLanguage(explanation)
                        
                        // Save to Firestore
                        when (val saveResult = explanationRepository.saveExplanation(
                            imageUri = imageUri,
                            codeSnippet = "", // Could extract from explanation if needed
                            explanation = explanation,
                            language = language
                        )) {
                            is Resource.Success -> {
                                _currentExplanation.value = saveResult.data
                                _analysisState.value = Resource.Success(Unit)
                                
                                // Update user stats
                                updateUserStats()
                            }
                            is Resource.Error -> {
                                _analysisState.value = Resource.Error(saveResult.message)
                            }
                            else -> {}
                        }
                    }
                    is Resource.Error -> {
                        _analysisState.value = Resource.Error(result.message)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _analysisState.value = Resource.Error("Failed to process image: ${e.message}")
            }
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            _currentExplanation.value?.let { explanation ->
                val newFavoriteState = !explanation.isFavorite
                explanationRepository.toggleFavorite(explanation.id, newFavoriteState)
                _currentExplanation.value = explanation.copy(isFavorite = newFavoriteState)
            }
        }
    }
    
    private suspend fun updateUserStats() {
        // Simple stats update - could be made more sophisticated
        val profile = authRepository.getUserProfile()
        authRepository.updateUserStats(
            studentScore = 75 // Placeholder logic
        )
    }
    
    private fun uriToBitmap(uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }
}
