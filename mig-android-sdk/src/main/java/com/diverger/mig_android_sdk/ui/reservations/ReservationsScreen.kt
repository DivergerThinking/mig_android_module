package com.diverger.mig_android_sdk.ui.reservations

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.ui.components.TrainingCellOption
import com.diverger.mig_android_sdk.ui.components.TrainingItem
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(userId: String, personalReservation: Boolean) {
    MIGAndroidSDKTheme {
        val viewModel: ReservationViewModel = viewModel()
        val reservations by viewModel.reservations.collectAsState()
        val teamReservations by viewModel.teamReservations.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val message by viewModel.toastMessage.collectAsState()
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // Individual state
        var showCancelPopup by remember { mutableStateOf(false) }
        var selectedReservation by remember { mutableStateOf<Reservation?>(null) }
        val showReservationFlow = remember { mutableStateOf(false) }
        val showDetailBottomSheet = remember { mutableStateOf(false) }

        // Team state
        var showTeamCancelPopup by remember { mutableStateOf(false) }
        var selectedTeamReservation by remember { mutableStateOf<EventModel?>(null) }
        val showTeamDetailBottomSheet = remember { mutableStateOf(false) }

        // DNI
        var showDniDialog by remember { mutableStateOf(false) }
        var enteredDni by remember { mutableStateOf("") }

        // ── Fetch según modo ─────────────────────────────────────
        LaunchedEffect(userId, personalReservation) {
            viewModel.refreshData(personalReservation, userId)
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
            // ── Título + Refresh ─────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        if (personalReservation) R.string.individual_bookings
                        else R.string.team_bookings
                    ),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    viewModel.refreshData(personalReservation, userId)
                }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Recargar", tint = Color.White)
                }
            }

            // ── Lista ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.Cyan)
                        }
                    }

                    // Empty states
                    personalReservation && reservations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.no_bookings),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    !personalReservation && teamReservations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.no_bookings),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    // ── Lista individual ──────────────────────────
                    personalReservation -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = rememberLazyListState()
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

                    // ── Lista equipo (reutiliza TrainingItem) ────
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = rememberLazyListState()
                        ) {
                            items(teamReservations) { training ->
                                TrainingItem(training = training) { action ->
                                    when (action) {
                                        TrainingCellOption.SeeDetails -> {
                                            selectedTeamReservation = training
                                            showTeamDetailBottomSheet.value = true
                                        }

                                        TrainingCellOption.RemoveCell -> {
                                            selectedTeamReservation = training
                                            showTeamCancelPopup = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Botón "Reservar" ─────────────────────────────────
            // Individual: siempre visible
            // Equipo: solo Manager (igual que iOS)
            val showBookButton = if (personalReservation) {
                true
            } else {
                viewModel.getUserRole().lowercase() == "manager"
            }

            if (showBookButton) {
                Button(
                    onClick = {
                        if (personalReservation) {
                            if (viewModel.userIsValidated() && !viewModel.userCanBook()) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.booking_limit_reached),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (!viewModel.userIsValidated() && !viewModel.userCanBook()) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.booking_limit_reached_and_dni_required),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                if (viewModel.userHasDNI()) {
                                    showReservationFlow.value = true
                                } else {
                                    showDniDialog = true
                                }
                            }
                        } else {
                            showReservationFlow.value = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (personalReservation && !viewModel.userCanBook()) Color.Gray else Color.Cyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 16.dp)
                        .padding(horizontal = 50.dp)
                ) {
                    Text(
                        stringResource(R.string.book_label),
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // ── Flujo de reserva ─────────────────────────────────
            if (showReservationFlow.value) {
                ReservationFlowDialog(
                    onDismiss = { showReservationFlow.value = false },
                    userId = userId,
                    onReservationSuccess = {
                        viewModel.refreshData(personalReservation, userId)
                        showReservationFlow.value = false
                    }
                )
            }

            // ── Diálogo DNI ──────────────────────────────────────
            if (showDniDialog) {
                val dniRegex = remember { Regex("^[0-9]{8}[A-Za-z]$") }

                AlertDialog(
                    onDismissRequest = { if (!isLoading) showDniDialog = false },
                    title = {
                        Column {
                            Text(
                                stringResource(R.string.insert_id),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                stringResource(R.string.insert_confirm_dni),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            DniTextField(
                                enteredDni,
                                onDniChange = { enteredDni = it },
                                isLoading = isLoading
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                stringResource(R.string.confirm_dni_warning),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (enteredDni.isNotBlank()) {
                                    showDniDialog = false
                                    coroutineScope.launch {
                                        viewModel.updateUserDNI(
                                            userId,
                                            dni = enteredDni
                                        ) { result ->
                                            if (result) {
                                                enteredDni = ""
                                                showReservationFlow.value = true
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = dniRegex.matches(enteredDni) && !isLoading,
                        ) {
                            Text(stringResource(R.string.accept_label), color = Color.Black)
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showDniDialog = false
                                showReservationFlow.value = false
                            },
                            enabled = !isLoading
                        ) {
                            Text(stringResource(R.string.cancel_label), color = Color.Black)
                        }
                    },
                    containerColor = Color.DarkGray,
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }
        }

        // ── Bottom Sheet individual ──────────────────────────────
        if (showDetailBottomSheet.value && selectedReservation != null) {
            ReservationBottomSheet(
                reservation = selectedReservation!!,
                onDismiss = { showDetailBottomSheet.value = false }
            )
        }

        // ── Bottom Sheet equipo (reutiliza ReservationDetailScreen) ─
        if (showTeamDetailBottomSheet.value && selectedTeamReservation != null) {
            ReservationDetailScreen(
                isTeamReservation = true,
                training = selectedTeamReservation,
                onDismiss = { showTeamDetailBottomSheet.value = false }
            )
        }

        // ── Popup cancelación individual ─────────────────────────
        CancelReservationPopup(
            reservation = selectedReservation,
            isVisible = showCancelPopup,
            onDismiss = { showCancelPopup = false },
            onConfirmCancel = { reservationId ->
                showCancelPopup = false
                viewModel.deleteReservation(reservationId, context)
            }
        )

        // ── Popup cancelación equipo ─────────────────────────────
        if (showTeamCancelPopup && selectedTeamReservation != null) {
            CancelTeamReservationPopup(
                training = selectedTeamReservation!!,
                onDismiss = { showTeamCancelPopup = false },
                onConfirmCancel = {
                    showTeamCancelPopup = false
                    viewModel.deleteTraining(selectedTeamReservation!!, context)
                    selectedTeamReservation = null
                }
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// Popup cancelación equipo
// ═════════════════════════════════════════════════════════════════

@Composable
fun CancelTeamReservationPopup(
    training: EventModel,
    onDismiss: () -> Unit,
    onConfirmCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "CANCELAR RESERVA",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                "¿Quieres cancelar la reserva del equipo ${training.teamName ?: ""}?",
                color = Color.White
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(stringResource(R.string.accept_label), color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_label), color = Color.White)
            }
        },
        containerColor = Color.DarkGray
    )
}

// ═════════════════════════════════════════════════════════════════
// DNI TextField
// ═════════════════════════════════════════════════════════════════

@Composable
fun DniTextField(
    enteredDni: String,
    onDniChange: (String) -> Unit,
    isLoading: Boolean
) {
    val dniRegex = remember { Regex("^[0-9]{8}[A-Za-z]\$") }
    val isError = enteredDni.isNotEmpty() && !dniRegex.matches(enteredDni)

    Column {
        OutlinedTextField(
            value = enteredDni,
            onValueChange = { onDniChange(it) },
            label = { Text(stringResource(R.string.id_label)) },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = stringResource(R.string.invalid_dni),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            textStyle = TextStyle(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }
}