package com.diverger.mig_android_sdk.ui.reservations

import ReservationBottomSheet
import ReservationFlowDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diverger.mig_android_sdk.R
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
        val message by viewModel.toastMessage.collectAsState()

        var showDniDialog by remember { mutableStateOf(false) }
        var enteredDni by remember { mutableStateOf("") }

        LaunchedEffect(userId) {
            viewModel.fetchReservations(userId)
        }

        LaunchedEffect(message) {
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.clearToastMessage()
            }
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
                onClick = {
                    if (viewModel.userIsValidated() && !viewModel.userCanBook()) {
                        Toast.makeText(context, context.getString(R.string.booking_limit_reached), Toast.LENGTH_LONG).show()
                    } else if (!viewModel.userIsValidated() && !viewModel.userCanBook()) {
                        Toast.makeText(context, context.getString(R.string.booking_limit_reached_and_dni_required), Toast.LENGTH_LONG).show()
                    } else {
                        if (viewModel.userHasDNI()) {
                            showReservationFlow.value = true
                        } else {
                            showDniDialog = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.userCanBook()) Color.Cyan else Color.Gray),
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

            if (showDniDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isLoading) showDniDialog = false },
                    title = { Text(stringResource(R.string.insert_id)) },
                    text = {
                        OutlinedTextField(
                            value = enteredDni,
                            onValueChange = { enteredDni = it },
                            label = { Text(stringResource(R.string.id_label)) },
                            singleLine = true,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (enteredDni.isNotBlank()) {
                                    showDniDialog = false
                                    coroutineScope.launch {
                                        viewModel.updateUserDNI(userId, dni = enteredDni) { result ->
                                            if (result) {
                                                enteredDni = ""
                                                showReservationFlow.value = true
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = enteredDni.isNotBlank() && !isLoading
                        ) {
                            Text(stringResource(R.string.accept_label))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDniDialog = false
                                showReservationFlow.value = false
                            },
                            enabled = !isLoading
                        ) {
                            Text(stringResource(R.string.cancel_label))
                        }
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

        if (viewModel.toastMessage.value != null) {
            Toast.makeText(context, viewModel.toastMessage.value, Toast.LENGTH_LONG)
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