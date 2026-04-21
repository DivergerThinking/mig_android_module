package com.diverger.mig_android_sdk.ui.reservations

import ReservationFlowDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.support.LocalizationManager
import com.diverger.mig_android_sdk.support.localizedString
import com.diverger.mig_android_sdk.ui.components.TrainingCellOption
import com.diverger.mig_android_sdk.ui.components.TrainingCell
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
        val selectedTeam by UserManager.selectedTeam.collectAsState()
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
        var dniError by remember { mutableStateOf<String?>(null) }

        // ── Fetch según modo (se relanza al cambiar de equipo) ───
        LaunchedEffect(userId, personalReservation, selectedTeam?.id) {
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
                .then(if (personalReservation) Modifier.padding(top = 55.dp) else Modifier)
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
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
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
                                localizedString("dashboard.profile.reserves.noReserves", R.string.no_bookings),
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
                                localizedString("dashboard.profile.reserves.noReserves", R.string.no_bookings),
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

                                            IndividualReservationsCellOptions.EDIT_RESERVATION -> {
                                                selectedReservation = res
                                                selectedTeamReservation = null
                                                showReservationFlow.value = true
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
                                TrainingCell(
                                    training = training,
                                    showDeleteAndEdit = viewModel.isManagerOrTrainer()
                                ) { action ->
                                    when (action) {
                                        TrainingCellOption.SeeDetails -> {
                                            selectedTeamReservation = training
                                            showTeamDetailBottomSheet.value = true
                                        }

                                        TrainingCellOption.RemoveCell -> {
                                            selectedTeamReservation = training
                                            showTeamCancelPopup = true
                                        }

                                        TrainingCellOption.EditBooking -> {
                                            selectedTeamReservation = training
                                            showReservationFlow.value = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Botón "Reservar" ─────────────────────────────────
            // Individual: siempre visible, habilitado según reserves_allowed
            // Equipo: solo Manager, habilitado según teamReservationLimit
            val showBookButton = if (personalReservation) {
                true
            } else {
                viewModel.getUserRole().lowercase() == "manager"
            }

            if (showBookButton) {
                val canBook = if (personalReservation) viewModel.userCanBook() else viewModel.teamCanBook()

                Button(
                    onClick = {
                        if (personalReservation) {
                            if (viewModel.userIsValidated() && !viewModel.userCanBook()) {
                                Toast.makeText(
                                    context,
                                    LocalizationManager.string("dashboard.profile.reserves.maxReserves") ?: context.getString(R.string.booking_limit_reached),
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
                                    selectedTeamReservation = null
                                    selectedReservation = null
                                    showReservationFlow.value = true
                                } else {
                                    showDniDialog = true
                                }
                            }
                        } else {
                            // Team: check limit
                            if (!viewModel.teamCanBook()) {
                                Toast.makeText(
                                    context,
                                    LocalizationManager.string("dashboard.profile.reserves.maxReserves") ?: context.getString(R.string.booking_limit_reached),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                selectedTeamReservation = null
                                selectedReservation = null
                                showReservationFlow.value = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canBook) Color.Cyan else Color.Gray
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 16.dp)
                        .padding(horizontal = 50.dp)
                ) {
                    Text(
                        localizedString("dashboard.profile.reserves.reserveButton", R.string.book_label),
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
                    personalReservation = personalReservation,
                    teamReservationInfo = selectedTeamReservation,
                    individualReservationInfo = selectedReservation,
                    onReservationSuccess = {
                        viewModel.refreshData(personalReservation, userId)
                        showReservationFlow.value = false
                    }
                )
            }

            // ── Diálogo DNI ──────────────────────────────────────
            if (showDniDialog) {
                var selectedDocType by remember { mutableStateOf(DocumentType.DNI_ID) }

                AlertDialog(
                    onDismissRequest = { if (!isLoading) showDniDialog = false },
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(when (selectedDocType) {
                                    DocumentType.DNI_ID   -> R.string.confirm_title_dni_id
                                    DocumentType.NIE      -> R.string.confirm_title_nie
                                    DocumentType.PASSPORT -> R.string.confirm_title_passport
                                }),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                stringResource(when (selectedDocType) {
                                    DocumentType.DNI_ID   -> R.string.intro_title_dni_id
                                    DocumentType.NIE      -> R.string.intro_title_nie
                                    DocumentType.PASSPORT -> R.string.intro_title_passport
                                }),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Document type selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.Cyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                DocumentType.entries.forEach { type ->
                                    val selected = type == selectedDocType
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (selected) Color.Cyan else Color.White.copy(alpha = 0.08f))
                                            .clickable {
                                                selectedDocType = type
                                                enteredDni = ""
                                                dniError = null
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (type) {
                                                DocumentType.DNI_ID   -> "DNI / ID"
                                                DocumentType.NIE      -> "NIE"
                                                DocumentType.PASSPORT -> stringResource(R.string.passport_label)
                                            },
                                            color = if (selected) Color.Black else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            DniTextField(
                                enteredDni = enteredDni,
                                label = when (selectedDocType) {
                                    DocumentType.DNI_ID   -> "DNI / ID"
                                    DocumentType.NIE      -> "NIE"
                                    DocumentType.PASSPORT -> stringResource(R.string.passport_label)
                                },
                                isValid = enteredDni.isEmpty() || selectedDocType.validate(enteredDni),
                                validationError = when (selectedDocType) {
                                    DocumentType.DNI_ID   -> stringResource(R.string.invalid_document_dni_id)
                                    DocumentType.NIE      -> stringResource(R.string.invalid_document_nie)
                                    DocumentType.PASSPORT -> stringResource(R.string.invalid_document_passport)
                                },
                                onDniChange = {
                                    enteredDni = it
                                    dniError = null
                                },
                                isLoading = isLoading
                            )
                            if (dniError != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = dniError!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                localizedString("dashboard.teams.teamAreas.reserves.dniWarning", R.string.confirm_dni_warning),
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (enteredDni.isNotBlank()) {
                                    coroutineScope.launch {
                                        viewModel.updateUserDNI(
                                            userId,
                                            dni = enteredDni
                                        ) { success, isDuplicate ->
                                            if (success) {
                                                dniError = null
                                                enteredDni = ""
                                                showDniDialog = false
                                                showReservationFlow.value = true
                                            } else if (isDuplicate) {
                                                dniError = when (selectedDocType) {
                                                    DocumentType.DNI_ID   -> LocalizationManager.string("doc.duplicate.dni_id") ?: context.getString(R.string.duplicate_document_dni_id)
                                                    DocumentType.NIE      -> LocalizationManager.string("doc.duplicate.nie") ?: context.getString(R.string.duplicate_document_nie)
                                                    DocumentType.PASSPORT -> LocalizationManager.string("doc.duplicate.passport") ?: context.getString(R.string.duplicate_document_passport)
                                                }
                                            } else {
                                                dniError = context.getString(R.string.error_document_update)
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = selectedDocType.validate(enteredDni) && !isLoading,
                        ) {
                            Text(localizedString("dashboard.teams.teamAreas.reserves.accept", R.string.accept_label), color = Color.Black)
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                dniError = null
                                enteredDni = ""
                                showDniDialog = false
                                showReservationFlow.value = false
                            },
                            enabled = !isLoading
                        ) {
                            Text(localizedString("dashboard.profile.reserves.cancel", R.string.cancel_label), color = Color.Black)
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
                Text(localizedString("dashboard.teams.accept", R.string.accept_label), color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(localizedString("dashboard.profile.reserves.cancel", R.string.cancel_label), color = Color.White)
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
    label: String,
    isValid: Boolean,
    validationError: String,
    onDniChange: (String) -> Unit,
    isLoading: Boolean
) {
    val isError = enteredDni.isNotEmpty() && !isValid

    Column {
        OutlinedTextField(
            value = enteredDni,
            onValueChange = { onDniChange(it) },
            placeholder = { Text(label, color = Color.White.copy(alpha = 0.5f)) },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(
                        text = validationError,
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