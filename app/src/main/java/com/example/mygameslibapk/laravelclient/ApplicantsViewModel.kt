package com.example.mygameslibapk.laravelclient

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed interface ApplicantsUiState {
    object Loading : ApplicantsUiState
    data class Success(val tickets: List<PositionTicketDto>) : ApplicantsUiState
    data class Error(val message: String) : ApplicantsUiState
}

class ApplicantsViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf<ApplicantsUiState>(ApplicantsUiState.Loading)
        private set

    var actionMessage by mutableStateOf<String?>(null)
        private set

    fun loadApplicants(projectId: Int) {
        uiState = ApplicantsUiState.Loading
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.getApplicants(projectId)

                if (response.isSuccessful && response.body()?.tickets != null) {
                    uiState = ApplicantsUiState.Success(response.body()!!.tickets!!)
                } else {
                    uiState = ApplicantsUiState.Error("Failed to load applicants (${response.code()})")
                }
            } catch (e: Exception) {
                uiState = ApplicantsUiState.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun reviewTicket(
        ticketId: Int,
        projectId: Int,
        action: String,
        reason: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.reviewTicket(ticketId, ReviewTicketRequest(action, reason))

                if (response.isSuccessful) {
                    actionMessage = "Application $action status saved."
                    loadApplicants(projectId)
                    onComplete()
                } else {
                    actionMessage = "Failed to submit review."
                }
            } catch (e: Exception) {
                actionMessage = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun clearActionMessage() {
        actionMessage = null
    }
}