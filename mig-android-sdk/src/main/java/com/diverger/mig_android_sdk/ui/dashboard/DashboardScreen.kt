package com.diverger.mig_android_sdk.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Dashboard Screen")
        Button(onClick = { viewModel.loadData() }) {
            Text(text = "Cargar Datos")
        }
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
        Text(text = "Datos: ${uiState.data}")
    }
}
