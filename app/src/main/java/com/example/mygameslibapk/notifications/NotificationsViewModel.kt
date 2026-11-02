package com.example.mygameslibapk.notifications

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mygameslibapk.laravelclient.ApiClient
import kotlinx.coroutines.launch

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    var notifications by mutableStateOf<List<NotificationDto>>(emptyList())
        private set

    var unreadCount by mutableStateOf(0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.getNotifications()

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    notifications = body.notifications.data
                    unreadCount = body.unreadCount
                } else {
                    errorMessage = "Failed to load notifications"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun markAsRead(id: Int) {
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.markNotificationAsRead(id)

                if (response.isSuccessful) {
                    notifications = notifications.map {
                        if (it.id == id) it.copy(isRead = true) else it
                    }
                    unreadCount = (unreadCount - 1).coerceAtLeast(0)
                }
            } catch (_: Exception) {}
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.markAllNotificationsAsRead()

                if (response.isSuccessful) {
                    notifications = notifications.map { it.copy(isRead = true) }
                    unreadCount = 0
                }
            } catch (_: Exception) {}
        }
    }
}