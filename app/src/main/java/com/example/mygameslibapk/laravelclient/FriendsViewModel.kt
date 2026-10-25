package com.example.mygameslibapk.laravelclient

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed interface FriendsUiState {
    object Loading : FriendsUiState
    data class Success(
        val friends: List<UserDto>,
        val incoming: List<UserDto>,
        val sent: List<UserDto>
    ) : FriendsUiState
    data class Error(val message: String) : FriendsUiState
}

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf<FriendsUiState>(FriendsUiState.Loading)
        private set

    fun loadFriends() {
        uiState = FriendsUiState.Loading
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.getFriends()

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    uiState = FriendsUiState.Success(body.friends, body.incomingRequests, body.sentRequests)
                } else {
                    uiState = FriendsUiState.Error("Failed to load friends")
                }
            } catch (e: Exception) {
                uiState = FriendsUiState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun acceptRequest(userId: Int) {
        viewModelScope.launch {
            try {
                ApiClient.getService(getApplication()).acceptFriendRequest(userId)
                loadFriends()
            } catch (_: Exception) {}
        }
    }

    fun removeFriend(userId: Int) {
        viewModelScope.launch {
            try {
                ApiClient.getService(getApplication()).removeFriend(userId)
                loadFriends()
            } catch (_: Exception) {}
        }
    }
}