package com.example.mygameslibapk.laravelclient

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed interface ChatUiState {
    object Loading : ChatUiState
    data class Success(val contact: UserDto, val messages: List<MessageDto>) : ChatUiState
    data class Error(val message: String) : ChatUiState
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf<ChatUiState>(ChatUiState.Loading)
        private set

    fun loadChat(userId: Int) {
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.getChatMessages(userId)

                if (response.isSuccessful && response.body()?.messages != null) {
                    val body = response.body()!!
                    uiState = ChatUiState.Success(body.contact, body.messages)
                } else {
                    uiState = ChatUiState.Error("Failed to load conversation (${response.code()})")
                }
            } catch (e: Exception) {
                uiState = ChatUiState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun sendMessage(userId: Int, text: String, onSent: () -> Unit) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.sendMessage(userId, SendMessageRequest(text.trim()))

                if (response.isSuccessful) {
                    onSent()
                    loadChat(userId)
                }
            } catch (_: Exception) {}
        }
    }
}