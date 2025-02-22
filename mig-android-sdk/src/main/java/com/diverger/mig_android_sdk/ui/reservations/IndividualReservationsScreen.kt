package com.diverger.mig_android_sdk.ui.reservations

import ReservationFlowDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diverger.mig_android_sdk.data.Reservation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualReservationsScreen(userId: String) {
    val viewModel: ReservationViewModel = viewModel()
    val reservations by viewModel.reservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showCancelPopup by remember { mutableStateOf(false) }
    var selectedReservation by remember { mutableStateOf<Reservation?>(null) }
    val showReservationFlow = remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.fetchReservations(userId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reservas Individuales") }) },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator()
                    reservations.isEmpty() -> Text("No hay reservas", color = Color.White)
                    else -> LazyColumn(modifier = Modifier.padding(16.dp)) {
                        items(reservations) { reservation ->
                            ReservationItem(
                                reservation,
                                onReservationPressed = { option, res ->
                                    when (option) {
                                        IndividualReservationsCellOptions.SEE_RESERVATION -> {
                                            // TODO: Implementar lógica para ver reserva
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

                Button(
                    onClick = { showReservationFlow.value = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text("Reservar", color = Color.White)
                }

                // Mostrar el flujo de reserva
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
        }
    )

    CancelReservationPopup(
        reservation = selectedReservation,
        isVisible = showCancelPopup,
        onDismiss = { showCancelPopup = false },
        onConfirmCancel = { reservationId ->
            viewModel.deleteReservation(reservationId)
        }
    )
}

/*@Composable
fun ReservationItem(reservation: Reservation, onCancel: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Reserva - ${reservation.slot.space}", color = Color.White)
            Text("Fecha: ${reservation.date}", color = Color.White.copy(alpha = 0.8f))
            Button(onClick = { reservation.id?.let { onCancel(it) } }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("Cancelar", color = Color.White)
            }
        }
    }
}*/
