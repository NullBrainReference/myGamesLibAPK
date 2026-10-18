package com.example.mygameslibapk.laravelclient

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    fun performLogin(onSuccess: () -> Unit) {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail.isBlank() || cleanPassword.isBlank()) {
            uiState = LoginUiState.Error("Please fill in all fields")
            return
        }

        uiState = LoginUiState.Loading

        viewModelScope.launch {
            try {
                // Pass Application context to getService to attach headers/tokens
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.login(LoginRequest(cleanEmail, cleanPassword))

                if (response.isSuccessful && response.body()?.token != null) {
                    val token = response.body()!!.token!!
                    tokenManager.saveToken(token)
                    uiState = LoginUiState.Success
                    onSuccess()
                } else {
                    // Extract detailed error body to pinpoint exact Laravel message
                    val rawError = response.errorBody()?.string()
                    val errorMsg = if (!rawError.isNullOrBlank()) {
                        "HTTP ${response.code()}: $rawError"
                    } else if (response.code() == 403) {
                        "Account is banned."
                    } else {
                        "Invalid credentials"
                    }
                    uiState = LoginUiState.Error(errorMsg)
                }
            } catch (e: Exception) {
                uiState = LoginUiState.Error("Network error: ${e.localizedMessage ?: "Could not connect"}")
            }
        }
    }
}