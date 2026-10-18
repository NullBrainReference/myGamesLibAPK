package com.example.mygameslibapk.laravelclient

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val user: UserDto) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    var uiState by mutableStateOf<ProfileUiState>(ProfileUiState.Loading)
        private set

    var userProfile by mutableStateOf<UserDto?>(null)
        private set

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        uiState = ProfileUiState.Loading
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.getProfile()

                if (response.isSuccessful && response.body()?.user != null) {
                    val user = response.body()!!.user!!
                    userProfile = user
                    uiState = ProfileUiState.Success(user)
                } else {
                    uiState = ProfileUiState.Error("Failed to fetch profile (${response.code()})")
                }
            } catch (e: Exception) {
                uiState = ProfileUiState.Error("Network error: ${e.localizedMessage ?: "Could not connect"}")
            }
        }
    }

    fun performLogout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // Invalidate Sanctum token on server
                ApiClient.getService(getApplication()).logout()
            } catch (_: Exception) {
                // Ignore network failure on logout; local cleanup takes precedence
            }

            // Remove saved token locally
            tokenManager.clearToken()
            onLogoutComplete()
        }
    }
}