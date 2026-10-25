package com.example.mygameslibapk.laravelclient

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed interface MessagesUiState {
    object Loading : MessagesUiState
    data class Success(val conversations: List<ConversationContactDto>) : MessagesUiState
    data class Error(val message: String) : MessagesUiState
}

class MessagesViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf<MessagesUiState>(MessagesUiState.Loading)
        private set

    fun loadConversations() {
        uiState = MessagesUiState.Loading
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.getConversations()

                if (response.isSuccessful && response.body()?.conversations != null) {
                    uiState = MessagesUiState.Success(response.body()!!.conversations)
                } else {
                    uiState = MessagesUiState.Error("Failed to load conversations")
                }
            } catch (e: Exception) {
                uiState = MessagesUiState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}