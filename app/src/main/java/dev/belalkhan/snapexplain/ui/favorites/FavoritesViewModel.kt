package dev.belalkhan.snapexplain.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.belalkhan.snapexplain.core.base.Resource
import dev.belalkhan.snapexplain.data.model.Explanation
import dev.belalkhan.snapexplain.data.repository.ExplanationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val explanationRepository: ExplanationRepository
) : ViewModel() {
    
    val favorites: StateFlow<Resource<List<Explanation>>> =
        explanationRepository.getFavorites()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Resource.Loading
            )
    
    fun toggleFavorite(explanationId: String) {
        viewModelScope.launch {
            explanationRepository.toggleFavorite(explanationId, false)
        }
    }
}
