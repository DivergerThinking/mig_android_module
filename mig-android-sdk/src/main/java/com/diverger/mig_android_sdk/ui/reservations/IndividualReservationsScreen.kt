package com.diverger.mig_android_sdk.ui.reservations

import ReservationBottomSheet
import ReservationFlowDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualReservationsScreen(userId: String) {
    MIGAndroidSDKTheme {
        val viewModel: ReservationViewModel = viewModel()
        val reservations by viewModel.reservations.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        var showCancelPopup by remember { mutableStateOf(false) }
        var selectedReservation by remember { mutableStateOf<Reservation?>(null) }
        val showReservationFlow = remember { mutableStateOf(false) }
        val isProcessingCancel = remember { mutableStateOf(false) }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val showDetailBottomSheet = remember { mutableStateOf(false) }

        LaunchedEffect(userId) {
            viewModel.fetchReservations(userId)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(top = 80.dp)
        ) {
            // 📌 Título "Reservas Individuales"
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reservas Individuales",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.fetchReservations(userId) }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Recargar", tint = Color.White)
                }
            }

            // 📌 Contenedor de la lista de reservas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f) // Para que la lista ocupe el espacio disponible sin afectar el botón
            ) {
                when {
                    isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.Cyan)
                        }
                    }

                    reservations.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay reservas", color = Color.White)
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = rememberLazyListState() // Permite el scroll
                        ) {
                            items(reservations) { reservation ->
                                ReservationItem(
                                    reservation,
                                    onReservationPressed = { option, res ->
                                        when (option) {
                                            IndividualReservationsCellOptions.SEE_RESERVATION -> {
                                                selectedReservation = res
                                                showDetailBottomSheet.value = true
                                            }
                                            IndividualReservationsCellOptions.CANCEL_RESERVATION -> {
                                                selectedReservation = res
                                                showCancelPopup = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 📌 Botón "Reservar" en la parte inferior
            Button(
                onClick = { showReservationFlow.value = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 16.dp)
                    .padding(horizontal = 50.dp)
            ) {
                Text("RESERVAR", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }

            // 📌 Mostrar el flujo de reserva
            if (showReservationFlow.value) {
                ReservationFlowDialog(
                    onDismiss = { showReservationFlow.value = false },
                    userId = userId,
                    onReservationSuccess = {
                        viewModel.fetchReservations(userId)
                        showReservationFlow.value = false
                    }
                )
            }
        }

        // 📌 Bottom Sheet para mostrar detalle de la reserva
        if (showDetailBottomSheet.value && selectedReservation != null) {
            ReservationBottomSheet(
                reservation = selectedReservation!!,
                onDismiss = { showDetailBottomSheet.value = false }
            )
        }

        // 📌 Popup de cancelación
        CancelReservationPopup(
            reservation = selectedReservation,
            isVisible = showCancelPopup,
            onDismiss = { showCancelPopup = false },
            onConfirmCancel = { reservationId ->
                showCancelPopup = false
                viewModel.deleteReservation(reservationId)
            }
        ) }
}