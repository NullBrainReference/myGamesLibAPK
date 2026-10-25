package com.example.mygameslibapk.laravelclient

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed interface VacanciesUiState {
    object Loading : VacanciesUiState
    data class Success(val positions: List<HirePositionDto>) : VacanciesUiState
    data class Error(val message: String) : VacanciesUiState
}

class VacanciesViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf<VacanciesUiState>(VacanciesUiState.Loading)
        private set

    var actionMessage by mutableStateOf<String?>(null)
        private set

    fun loadVacancies(projectId: Int) {
        uiState = VacanciesUiState.Loading
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.getVacancies(projectId)

                if (response.isSuccessful && response.body()?.positions != null) {
                    uiState = VacanciesUiState.Success(response.body()!!.positions!!)
                } else {
                    uiState = VacanciesUiState.Error("Failed to load vacancies (${response.code()})")
                }
            } catch (e: Exception) {
                uiState = VacanciesUiState.Error("Network error: ${e.localizedMessage ?: "Could not connect"}")
            }
        }
    }

    fun applyForPosition(positionId: Int, projectId: Int, reason: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.applyForPosition(positionId, ApplyTicketRequest(reason))

                if (response.isSuccessful) {
                    actionMessage = "Application submitted successfully!"
                    loadVacancies(projectId)
                    onComplete()
                } else {
                    val rawError = response.errorBody()?.string()
                    actionMessage = "Error: ${rawError ?: "Could not apply"}"
                }
            } catch (e: Exception) {
                actionMessage = "Network error: ${e.localizedMessage}"
            }
        }
    }

    fun createPosition(
        projectId: Int,
        title: String,
        description: String,
        ticketsAmount: Int,
        skills: List<String>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.createVacancy(
                    projectId,
                    CreatePositionRequest(title, description, ticketsAmount, skills)
                )

                if (response.isSuccessful) {
                    actionMessage = "Position published successfully!"
                    loadVacancies(projectId)
                    onComplete()
                } else {
                    actionMessage = "Failed to create position."
                }
            } catch (e: Exception) {
                actionMessage = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun requestMoreTickets(
        projectId: Int,
        positionId: Int,
        quantity: Int,
        reason: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.requestMoreTickets(
                    projectId,
                    RequestMoreTicketsRequest(positionId, quantity, reason)
                )

                if (response.isSuccessful) {
                    actionMessage = "Ticket extension request sent to admins."
                    onComplete()
                } else {
                    actionMessage = "Failed to submit ticket extension request."
                }
            } catch (e: Exception) {
                actionMessage = "Error: ${e.localizedMessage}"
            }
        }
    }

    fun requestPositionSlots(
        projectId: Int,
        title: String,
        quantity: Int,
        reason: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.requestPositionSlots(
                    projectId,
                    RequestPositionSlotsRequest(title, quantity, reason)
                )

                if (response.isSuccessful) {
                    actionMessage = "Position slot request sent to admins!"
                    onComplete()
                } else {
                    actionMessage = "Failed to submit position slot request."
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