package com.diverger.mig_android_sdk.ui.dashboard

import CustomCalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.PlayerModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.UnsafeAsyncImage
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.diverger.mig_android_sdk.ui.UnifiedReservationScreen
import com.diverger.mig_android_sdk.ui.competitions.formatDateFromShort
import com.diverger.mig_android_sdk.ui.reservations.IndividualReservationsCellOptions
import com.diverger.mig_android_sdk.ui.reservations.ReservationItem
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.Eye
import compose.icons.feathericons.Monitor
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Building
import compose.icons.fontawesomeicons.solid.UserCircle

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
        val isLoading by viewModel.isLoading.collectAsState()
        val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.getData()
    }

        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
        ) {

            Column(modifier = Modifier.padding(16.dp)) {
                // 📅 **Calendario de Entrenamientos**
                Spacer(Modifier.height(64.dp))
                CalendarComponent(
                    isCalendarVisible = isCalendarVisible,
                    onToggle = {
                        isCalendarVisible = !isCalendarVisible
                        calendarArrowRotation += 180f
                    },
                    reservations = trainings.map { it.startDate } + teamReservations.map { it.date },
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
                    UnifiedReservationScreen(
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

    // 📅 **Componente de Calendario**
    @Composable
    fun CalendarComponent(
        isCalendarVisible: Boolean,
        onToggle: () -> Unit,
        reservations: List<String> = emptyList(),
        markedDates: List<String> = emptyList(),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.trainings_calendar).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)

                )
                Icon(
                    imageVector = if (isCalendarVisible) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = "Toggle Calendar",
                    tint = Color.Cyan
                )
            }

            if (isCalendarVisible) {
                CustomCalendarView(
                    canUserInteract = false,
                    onDateSelected = {},
                    blockedDates = markedDates,
                    reservations = reservations
                )
            }
        }
    }

    // 📌 **Banner de Próximos Entrenamientos**
    @Composable
    fun NextTrainingsBanner(isTrainingsVisible: Boolean, onToggle: () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clickable(onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.future_trainings).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
            )
            Icon(
                imageVector = if (isTrainingsVisible) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = "Toggle Trainings",
                tint = Color.Cyan
            )
        }
    }

    @Composable
    fun UnifiedScheduleList(
        trainings: List<EventModel>,
        reservations: List<Reservation>,
        onTrainingAction: (EventModel, TrainingCellOption) -> Unit,
        onReservationAction: (Reservation, IndividualReservationsCellOptions) -> Unit
    ) {
        if (trainings.isEmpty() && reservations.isEmpty()) {
            Text(stringResource(R.string.no_scheduled_trainings), color = Color.Gray)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (reservations.isNotEmpty()) {
                    item {
                        Text(
                            text = "Reservas Individuales".uppercase(),
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
                            text = "Reservas de Equipo".uppercase(),
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(trainings) { training ->
                        TrainingItem(training) { action ->
                            onTrainingAction(training, action)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TrainingItem(training: EventModel, onAction: (TrainingCellOption) -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 🔹 Icono, Fecha y Botón de Borrar
                IconDateAndRemoveBannerComponent(training, onAction)

                Spacer(Modifier.height(10.dp))

                // 🔹 Ubicación de la reserva
                ShowReservationLocation(training)

                Spacer(Modifier.height(10.dp))

                // Lista de jugadores solo para entrenamiento tipo equipo
                if (!training.players.isNullOrEmpty()) {
                    PlayersCarouselComponent(training)
                    Spacer(Modifier.height(16.dp))
                }

                // 🔹 Notas (si existen)
                if (!training.notes.isNullOrEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    DescriptionComponent(training.notes)
                }

                // 🔹 Botón de ver detalles
                ButtonsComponent(onAction)
            }
        }
    }

    // 🔹 **Icono, Fecha y Botón de Borrar**
    @Composable
    private fun IconDateAndRemoveBannerComponent(training: EventModel, onAction: (TrainingCellOption) -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = when (training.type.lowercase()) {
                    "virtual" -> FeatherIcons.Monitor
                    "centre" -> FontAwesomeIcons.Solid.Building
                    else -> FeatherIcons.Monitor
                },
                contentDescription = stringResource(R.string.booking_type),
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${formatDateFromShort(training.startDate)} - ${training.time.slice(0..4)}",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.weight(1f))

            /*IconButton(onClick = { onAction(TrainingCellOption.RemoveCell) }) {
                Icon(Icons.Default.RemoveCircle, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(20.dp))
            }*/
        }
    }

    // 🔹 **Ubicación de la reserva**
    @Composable
    private fun ShowReservationLocation(training: EventModel) {
        Text(
            text = "${UserManager.selectedTeam.value?.name ?: stringResource(R.string.no_team)} - ${training.type}",
            color = Color.White.copy(0.8f),
            style = MaterialTheme.typography.bodySmall
        )
    }

    // 🔹 **Lista de jugadores**
    @Composable
    private fun PlayersCarouselComponent(training: EventModel) {
        Column {
            Text("Players", color = Color.White, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp, start = 2.dp))

            LazyRow(
                modifier = Modifier.padding(7.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(training.players ?: emptyList()) { player ->
                    player.userId?.let {
                        PlayerAvatar(it)
                    }
                }
            }
        }
    }

    @Composable
    private fun PlayerAvatar(player: PlayerModel) {
        Box(
            modifier = Modifier.size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            val imageUrl = player.avatar.takeIf { it?.isNotEmpty() ?: false  }
                ?.let { "${EnvironmentManager.getAssetsBaseUrl()}$it" }

            if (imageUrl != null) {
                UnsafeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(35.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                PlaceholderIcon()
            }
        }
    }

    // 🔹 **Fallback a Icono si no hay imagen**
    @Composable
    private fun PlaceholderIcon() {
        Icon(
            imageVector = FontAwesomeIcons.Solid.UserCircle,
            contentDescription = stringResource(R.string.no_avatar_user),
            tint = Color.Gray,
            modifier = Modifier.size(35.dp)
        )
    }

    // 🔹 **Notas**
    @Composable
    private fun DescriptionComponent(notes: String) {
        Column(modifier = Modifier.padding(7.dp)) {
            Text("Notas", color = Color.White, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
            Text(notes, color = Color.White.copy(0.8f), style = MaterialTheme.typography.labelSmall, maxLines = 3)
        }
    }

    // 🔹 **Botón de ver detalles**
    @Composable
    private fun ButtonsComponent(onAction: (TrainingCellOption) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = FeatherIcons.Eye,
                contentDescription = "Ver Reserva",
                tint = Color.Cyan,
                modifier = Modifier.size(18.dp))
            TextButton(onClick = { onAction(TrainingCellOption.SeeDetails) }) {
                Text(stringResource(R.string.see_booking).uppercase(), color = Color.Cyan, style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    enum class TrainingCellOption {
        RemoveCell, SeeDetails
    }


