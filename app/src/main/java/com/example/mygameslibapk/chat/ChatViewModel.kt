package com.example.mygameslibapk.chat

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygameslibapk.laravelclient.ApiClient
import com.example.mygameslibapk.laravelclient.UserDto
import kotlinx.coroutines.launch

sealed interface ChatUiState {
    object Loading : ChatUiState
    data class Success(val contact: UserDto, val messages: List<MessageDto>) : ChatUiState
    data class Error(val message: String) : ChatUiState
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf<ChatUiState>(ChatUiState.Loading)
        private set

    fun loadChat(userId: Int, isSilentReload: Boolean = false) {
        viewModelScope.launch {
            if (!isSilentReload) {
                uiState = ChatUiState.Loading
            }
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.getChatMessages(userId)

                if (response.isSuccessful && response.body()?.messages != null) {
                    val body = response.body()!!
                    uiState = ChatUiState.Success(body.contact, body.messages)
                } else {
                    if (!isSilentReload) {
                        uiState = ChatUiState.Error("Failed to load conversation (${response.code()})")
                    }
                }
            } catch (e: Exception) {
                if (!isSilentReload) {
                    uiState = ChatUiState.Error("Network error: ${e.localizedMessage}")
                }
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
                    // Silent reload preserves current screen state while updating message list
                    loadChat(userId, isSilentReload = true)
                }
            } catch (_: Exception) {}
        }
    }
}