package com.adafruit.pyleap.ui.projects

/**
 * Created by Antonio García (antonio@openroad.es)
 */

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adafruit.pyleap.model.Project
import com.adafruit.pyleap.model.ProjectsFeed
import com.adafruit.pyleap.model.ProjectsRepository
import com.adafruit.pyleap.utils.LogUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProjectsViewModel(
    private val projectsRepository: ProjectsRepository
) : ViewModel() {
    // UI State
    sealed class UiState {
        object Loading : UiState()
        data class Error(val message: String) : UiState()
        data class Projects(val projects: List<Project>, val selectedProject: Project?) : UiState()
    }

    // Internal state
    private data class ViewModelState(
        val projects: List<Project> = emptyList(),
        val selectedProjectId: String? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val searchInput: String = ""
    ) {
        fun toUiState(): UiState =
            if (isLoading) {
                UiState.Loading
            } else if (errorMessage != null) {
                UiState.Error(errorMessage)
            } else {
                UiState.Projects(
                    projects = projects,
                    selectedProject = if (selectedProjectId != null) projects.firstOrNull { it.id == selectedProjectId } else null
                )
            }
    }

    // Data - Private
    //private val log by LogUtils()
    private val viewModelState = MutableStateFlow(ViewModelState(isLoading = true))

    // Data
    val uiState = viewModelState
        .map { it.toUiState() }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value.toUiState()
        )

    init {
        refreshProjects()
    }

    fun refreshProjects() {
        // Ui state is refreshing
        viewModelState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = projectsRepository.getAllProjects()

            viewModelState.update { viewModelState ->
                result.fold(
                    onSuccess = { projectsFeed: ProjectsFeed ->
                        viewModelState.copy(
                            projects = projectsFeed.allProjects,
                            isLoading = false
                        )
                    },
                    onFailure = {
                        viewModelState.copy(
                            errorMessage = "Can't get projects from server. Check your internet connection",
                            isLoading = false
                        )
                    }
                )
            }
        }
    }

    fun selectProjectId(id: String) {
        viewModelState.update {
            it.copy( selectedProjectId = id)
        }
    }

    fun unselectAll() {
        viewModelState.update {
            it.copy(selectedProjectId = null)
        }
    }

    /**
     * Factory that takes ProjectsRepository as a dependency
     */
    companion object {
        fun provideFactory(
            projectsRepository: ProjectsRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProjectsViewModel(projectsRepository) as T
            }
        }
    }
}