package dev.belalkhan.snapexplain.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.belalkhan.snapexplain.core.base.Resource
import dev.belalkhan.snapexplain.data.model.UserProfile
import dev.belalkhan.snapexplain.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _profileState = MutableStateFlow<Resource<UserProfile>>(Resource.Loading)
    val profileState: StateFlow<Resource<UserProfile>> = _profileState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = Resource.Loading
            val profile = authRepository.getUserProfile()
            _profileState.value = if (profile != null) {
                Resource.Success(profile)
            } else {
                Resource.Error("Failed to load profile")
            }
        }
    }
    
    fun signOut() {
        authRepository.signOut()
    }
}
