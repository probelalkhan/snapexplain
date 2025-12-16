package dev.belalkhan.snapexplain.ui.auth

import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.belalkhan.snapexplain.core.base.Resource
import dev.belalkhan.snapexplain.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _signInState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val signInState: StateFlow<Resource<Unit>> = _signInState.asStateFlow()
    
    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn()
    
    // Google Sign-In
    fun beginSignIn(onIntentSender: (IntentSender) -> Unit) {
        viewModelScope.launch {
            _signInState.value = Resource.Loading
            when (val result = authRepository.beginSignIn()) {
                is Resource.Success -> onIntentSender(result.data)
                is Resource.Error -> _signInState.value = Resource.Error(result.message)
                else -> {}
            }
        }
    }
    
    fun signInWithGoogle(intent: Intent) {
        viewModelScope.launch {
            _signInState.value = Resource.Loading
            when (val result = authRepository.signInWithGoogle(intent)) {
                is Resource.Success -> _signInState.value = Resource.Success(Unit)
                is Resource.Error -> _signInState.value = Resource.Error(result.message)
                else -> {}
            }
        }
    }
    
    // Email/Password Sign-In
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _signInState.value = Resource.Loading
            when (val result = authRepository.signInWithEmail(email, password)) {
                is Resource.Success -> _signInState.value = Resource.Success(Unit)
                is Resource.Error -> _signInState.value = Resource.Error(result.message)
                else -> {}
            }
        }
    }
    
    // Email/Password Sign-Up
    fun signUpWithEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _signInState.value = Resource.Loading
            when (val result = authRepository.signUpWithEmail(email, password, displayName)) {
                is Resource.Success -> _signInState.value = Resource.Success(Unit)
                is Resource.Error -> _signInState.value = Resource.Error(result.message)
                else -> {}
            }
        }
    }
    
    fun signOut() {
        authRepository.signOut()
        _signInState.value = Resource.Loading
    }
}

