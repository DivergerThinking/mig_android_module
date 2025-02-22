package com.diverger.mig_android_sdk.ui.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.ui.components.CustomCalendarView

@Composable
fun TeamTrainingScreen(viewModel: TeamTrainingViewModel = viewModel()) {
    var isCalendarVisible by remember { mutableStateOf(false) }
    var isTrainingsVisible by remember { mutableStateOf(true) }
    var calendarArrowRotation by remember { mutableStateOf(0f) }
    var trainingArrowRotation by remember { mutableStateOf(0f) }

    val teamReservations by viewModel.teamReservations.collectAsState()
    val markedDates by viewModel.markedDates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 📅 **Calendario de Entrenamientos**
            CalendarComponent(
                isCalendarVisible = isCalendarVisible,
                onToggle = {
                    isCalendarVisible = !isCalendarVisible
                    calendarArrowRotation += 180f
                },
                markedDates = markedDates
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
                else -> if (isTrainingsVisible) TrainingsList(trainings = teamReservations)
            }
        }
    }
}

// 📅 **Componente de Calendario**
@Composable
fun CalendarComponent(
    isCalendarVisible: Boolean,
    onToggle: () -> Unit,
    markedDates: List<String>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calendario de Entrenamientos",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Toggle Calendar",
                    tint = Color.Cyan
                )
            }
        }

        if (isCalendarVisible) {
            CustomCalendarView(
                canUserInteract = false,
                markedDates = markedDates,
                onDateSelected = {},
                blockedDates = emptyList()
            )
        }
    }
}

// 📌 **Banner de Próximos Entrenamientos**
@Composable
fun NextTrainingsBanner(isTrainingsVisible: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Próximos Entrenamientos",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Toggle Trainings",
                tint = Color.Cyan
            )
        }
    }
}

// 🏋️ **Lista de Entrenamientos**
@Composable
fun TrainingsList(trainings: List<Reservation>) {
    if (trainings.isEmpty()) {
        Text("No hay entrenamientos programados.", color = Color.Gray)
    } else {
        LazyColumn {
            items(trainings) { training ->
                TrainingItem(training)
            }
        }
    }
}

// 📌 **Elemento de Entrenamiento**
@Composable
fun TrainingItem(training: Reservation) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Training Date",
                    tint = Color.Yellow
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fecha: ${training.date}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Slot: ${training.slot.position}",
                color = Color.Cyan,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Horas: ${training.times.joinToString { it.time }}",
                color = Color.White
            )
        }
    }
}
