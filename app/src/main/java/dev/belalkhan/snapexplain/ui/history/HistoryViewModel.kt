package dev.belalkhan.snapexplain.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.belalkhan.snapexplain.core.base.Resource
import dev.belalkhan.snapexplain.data.model.Explanation
import dev.belalkhan.snapexplain.data.repository.ExplanationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    explanationRepository: ExplanationRepository
) : ViewModel() {
    
    val history: StateFlow<Resource<List<Explanation>>> =
        explanationRepository.getAllExplanations()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Resource.Loading
            )
}
