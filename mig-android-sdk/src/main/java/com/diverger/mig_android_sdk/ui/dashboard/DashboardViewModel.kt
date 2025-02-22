package com.diverger.mig_android_sdk.ui.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {
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
