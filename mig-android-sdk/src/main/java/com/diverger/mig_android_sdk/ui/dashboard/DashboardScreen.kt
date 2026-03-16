package com.diverger.mig_android_sdk.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.ui.components.CalendarComponent
import com.diverger.mig_android_sdk.ui.components.NextTrainingsBanner
import com.diverger.mig_android_sdk.ui.components.TrainingCellOption
import com.diverger.mig_android_sdk.ui.components.TrainingCell
import com.diverger.mig_android_sdk.ui.reservations.IndividualReservationsCellOptions
import com.diverger.mig_android_sdk.ui.reservations.ReservationDetailScreen
import com.diverger.mig_android_sdk.ui.reservations.ReservationItem

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    var isCalendarVisible by remember { mutableStateOf(true) }
    var isTrainingsVisible by remember { mutableStateOf(true) }
    var calendarArrowRotation by remember { mutableStateOf(0f) }
    var trainingArrowRotation by remember { mutableStateOf(0f) }

    var selectedTraining by remember { mutableStateOf<EventModel?>(null) }
    var selectedReservation by remember { mutableStateOf<Reservation?>(null) }

    val showReservationDetail = remember { mutableStateOf(false) }

    val trainings by viewModel.trainings.collectAsState()
    val teamReservations by viewModel.teamReservations.collectAsState()
    val markedDates by viewModel.markedDates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.getData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(Modifier.height(64.dp))
            CalendarComponent(
                isCalendarVisible = isCalendarVisible,
                onToggle = {
                    isCalendarVisible = !isCalendarVisible
                    calendarArrowRotation += 180f
                },
                markedDates = markedDates,
                trainings = trainings.map { it.startDate },
                reservations = teamReservations.map { it.date }
            )

            Spacer(modifier = Modifier.height(16.dp))

            NextTrainingsBanner(
                isTrainingsVisible = isTrainingsVisible,
                onToggle = {
                    isTrainingsVisible = !isTrainingsVisible
                    trainingArrowRotation += 180f
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                errorMessage != null -> Text(errorMessage!!, color = Color.Red)
                else -> if (isTrainingsVisible) {
                    UnifiedScheduleList(
                        trainings = trainings,
                        reservations = teamReservations,
                        onTrainingAction = { training, action ->
                            when (action) {
                                TrainingCellOption.RemoveCell -> {}
                                TrainingCellOption.SeeDetails -> {
                                    selectedTraining = training
                                    showReservationDetail.value = true
                                }
                            }
                        },
                        onReservationAction = { reservation, option ->
                            when (option) {
                                IndividualReservationsCellOptions.SEE_RESERVATION -> {
                                    selectedReservation = reservation
                                    showReservationDetail.value = true
                                }

                                IndividualReservationsCellOptions.CANCEL_RESERVATION -> {}
                            }
                        }
                    )
                }
            }

            if (showReservationDetail.value && (selectedTraining != null || selectedReservation != null)) {
                ReservationDetailScreen(
                    isTeamReservation = selectedTraining != null,
                    training = selectedTraining,
                    reservation = selectedReservation,
                    onDismiss = {
                        showReservationDetail.value = false
                        selectedTraining = null
                        selectedReservation = null
                    }
                )
            }
        }
    }
}

@Composable
fun UnifiedScheduleList(
    trainings: List<EventModel>,
    reservations: List<Reservation>,
    onTrainingAction: (EventModel, TrainingCellOption) -> Unit,
    onReservationAction: (Reservation, IndividualReservationsCellOptions) -> Unit,
) {
    if (trainings.isEmpty() && reservations.isEmpty()) {
        Text(stringResource(R.string.no_scheduled_trainings), color = Color.Gray, style = MaterialTheme.typography.labelMedium)
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (reservations.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.individual_bookings).uppercase(),
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(reservations) { reservation ->
                    ReservationItem(
                        reservation,
                        compact = true,
                        onReservationPressed = { option, res ->
                            onReservationAction(res, option)
                        }
                    )
                }
            }
            if (trainings.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.team_reservations).uppercase(),
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(trainings) { training ->
                    TrainingCell(training) { action ->
                        onTrainingAction(training, action)
                    }
                }
            }
        }
    }
}