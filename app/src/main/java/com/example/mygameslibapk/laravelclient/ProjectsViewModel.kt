package com.example.mygameslibapk.laravelclient

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

sealed interface ProjectsUiState {
    object Loading : ProjectsUiState
    data class Success(val projects: List<ProjectDto>) : ProjectsUiState
    data class Error(val message: String) : ProjectsUiState
}

class ProjectsViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf<ProjectsUiState>(ProjectsUiState.Loading)
        private set

    init {
        loadProjects()
    }

    fun loadProjects() {
        uiState = ProjectsUiState.Loading

        viewModelScope.launch {
            try {
                val apiService = ApiClient.getService(getApplication())
                val response = apiService.getProjects()

                if (response.isSuccessful && response.body()?.data != null) {
                    uiState = ProjectsUiState.Success(response.body()!!.data)
                } else {
                    uiState = ProjectsUiState.Error("Failed to load projects (${response.code()})")
                }
            } catch (e: Exception) {
                uiState = ProjectsUiState.Error("Network error: ${e.localizedMessage ?: "Could not connect"}")
            }
        }
    }
}