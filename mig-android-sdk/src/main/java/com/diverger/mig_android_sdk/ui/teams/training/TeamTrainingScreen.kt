package com.diverger.mig_android_sdk.ui.teams.training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.ui.components.CalendarComponent
import com.diverger.mig_android_sdk.ui.components.NextTrainingsBanner
import com.diverger.mig_android_sdk.ui.components.TrainingCellOption
import com.diverger.mig_android_sdk.ui.components.TrainingsList
import com.diverger.mig_android_sdk.ui.reservations.ReservationDetailScreen

@Composable
fun TeamTrainingScreen(viewModel: TeamTrainingViewModel = viewModel()) {
    var isCalendarVisible by remember { mutableStateOf(true) }
    var isTrainingsVisible by remember { mutableStateOf(true) }
    var calendarArrowRotation by remember { mutableStateOf(0f) }
    var trainingArrowRotation by remember { mutableStateOf(0f) }

    var selectedTraining by remember { mutableStateOf<EventModel?>(null) }
    val showReservationDetail = remember { mutableStateOf(false) }

    val trainings by viewModel.trainings.collectAsState()
    val teamReservations by viewModel.teamReservations.collectAsState()
    val markedDates by viewModel.markedDates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val teamLogo by viewModel.teamLogo.collectAsState()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .padding(bottom = 64.dp)) {

        Column(modifier = Modifier.padding(16.dp)) {
            // 📅 **Calendario de Entrenamientos**

            CalendarComponent(
                isCalendarVisible = isCalendarVisible,
                onToggle = {
                    isCalendarVisible = !isCalendarVisible
                    calendarArrowRotation += 180f
                },
                trainings = trainings.map { it.startDate },
                markedDates = markedDates,
                teamLogo = teamLogo
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 📌 **Próximos Entrenamientos**
            NextTrainingsBanner(
                isTrainingsVisible = isTrainingsVisible,
                onToggle = {
                    isTrainingsVisible = !isTrainingsVisible
                    trainingArrowRotation += 180f
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 📌 **Lista de Entrenamientos**
            when {
                isLoading -> CircularProgressIndicator(color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
                errorMessage != null -> Text(errorMessage!!, color = Color.Red)
                else -> if (isTrainingsVisible) TrainingsList(trainings = trainings, onAction = { training, action ->
                    when(action) {
                        TrainingCellOption.RemoveCell -> {
                        }
                        TrainingCellOption.SeeDetails -> {
                            selectedTraining = training
                            showReservationDetail.value = true
                        }
                    }
                })
            }

            if (showReservationDetail.value && selectedTraining != null) {
//                TeamReservationBottomSheet(
//                    training = selectedTraining!!,
//                    onDismiss =
//                )

                ReservationDetailScreen(
                    isTeamReservation = true,
                    training = selectedTraining!!,
                    onDismiss = { showReservationDetail.value = false }
                )
            }
        }
    }
}

