package com.diverger.mig_android_sdk.ui.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadData() {
        _uiState.value = DashboardUiState(isLoading = true, data = "Cargando...")
        _uiState.value = DashboardUiState(isLoading = false, data = "Datos cargados correctamente")
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val data: String = ""
)
