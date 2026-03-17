import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.support.dateFromString
import com.diverger.mig_android_sdk.support.isToday
import com.diverger.mig_android_sdk.support.toUIDateString
import com.diverger.mig_android_sdk.ui.reservations.ReservationFlowViewModel
import com.diverger.mig_android_sdk.ui.reservations.loadBitmapFromResource
import compose.icons.FeatherIcons
import compose.icons.feathericons.XCircle
import kotlinx.coroutines.launch
import java.util.Calendar

// ═══════════════════════════════════════════════════════════════
// Colors matching iOS
// ═══════════════════════════════════════════════════════════════
private val SurfaceColor = Color.White.copy(alpha = 0.06f)
private val BorderColor = Color.White.copy(alpha = 0.15f)
private val SubtleText = Color.White.copy(alpha = 0.4f)
private val DisabledText = Color.White.copy(alpha = 0.25f)

// ═══════════════════════════════════════════════════════════════
// Main Dialog
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReservationFlowDialog(
    onDismiss: () -> Unit,
    userId: String,
    personalReservation: Boolean = true,
    teamReservationInfo: EventModel? = null,
    individualReservationInfo: Reservation? = null,
    onReservationSuccess: () -> Unit,
) {
    val viewModel = remember {
        ReservationFlowViewModel(
            personalReservations = personalReservation,
            teamReservationInfo = teamReservationInfo,
            individualReservationInfo = individualReservationInfo
        )
    }

    val currentStep by viewModel.currentStep.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val logoBitmap = UserManager.qr_logo?.let { loadBitmapFromResource(it) }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A1A),
                            Color.Black,
                            Color.Black,
                            Color(0xFF0D0D0D)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(0.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Header: X button + dots ──────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Invisible placeholder for centering
                    Box(modifier = Modifier.size(44.dp))

                    Spacer(Modifier.weight(1f))

                    // Step dots
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(viewModel.totalSteps) { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (index <= currentStep) Color.White
                                        else Color.White.copy(alpha = 0.25f),
                                        CircleShape
                                    )
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    IconButton(
                        onClick = { if (!isProcessing) onDismiss() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            FeatherIcons.XCircle,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // ── Summary ──────────────────────────────────
                SummaryBanner(viewModel)

                // ── Step content ─────────────────────────────
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    if (isProcessing) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.Cyan)
                        }
                    } else {
                        AnimatedContent(
                            targetState = currentStep,
                            transitionSpec = { fadeIn() with fadeOut() }
                        ) { step ->
                            when {
                                // Team: step 0 = Place & Notes
                                !personalReservation && step == 0 -> SelectPlaceAndNotesStep(viewModel)
                                // Team: step 1 = Players
                                !personalReservation && step == 1 -> SelectPlayerStep(viewModel)
                                // Date step
                                (personalReservation && step == 0) || (!personalReservation && step == 2) ->
                                    SelectDateStep(viewModel)
                                // Virtual: Time step
                                viewModel.isVirtualTeam && (
                                        (personalReservation && step == 1) || (!personalReservation && step == 3)
                                        ) -> SelectTimeStep(viewModel)
                                // Centre: Space step
                                (personalReservation && step == 1) || (!personalReservation && step == 3) ->
                                    SelectSpaceStep(viewModel)
                                // Centre: Slot step
                                (personalReservation && step == 2) || (!personalReservation && step == 4) ->
                                    SelectSlotStep(viewModel)
                            }
                        }
                    }
                }

                // ── Bottom buttons ───────────────────────────
                if (!isProcessing) {
                    BottomButtons(
                        viewModel = viewModel,
                        personalReservation = personalReservation,
                        userId = userId,
                        logoBitmap = logoBitmap,
                        coroutineScope = coroutineScope,
                        context = context,
                        onSuccess = onReservationSuccess,
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Summary banner (iOS ReservationSummaryView)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SummaryBanner(viewModel: ReservationFlowViewModel) {
    val tokens = viewModel.getSummaryTokens()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.book_space),
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        if (tokens.isNotEmpty()) {
            Text(
                tokens.joinToString(" · "),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Step 0 (Team): Select Place & Notes
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectPlaceAndNotesStep(viewModel: ReservationFlowViewModel) {
    val selectedType by viewModel.selectedSpaceType.collectAsState()
    val notes by viewModel.reservationNotes.collectAsState()
    val spaceTypes = listOf("Virtual", "E-Sports Center")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepTitle(stringResource(R.string.select_space))

        // Dropdown
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .background(SurfaceColor, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        selectedType ?: "Selecciona tipo",
                        color = if (selectedType == null) SubtleText else Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = SubtleText, modifier = Modifier.size(14.dp))
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                spaceTypes.forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Row {
                                Text(type)
                                if (selectedType == type) {
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        onClick = {
                            viewModel.setSelectedSpaceType(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Notes
        OutlinedTextField(
            value = notes,
            onValueChange = { viewModel.setReservationNotes(it) },
            placeholder = { Text("Notas (Opcional)", color = Color.White.copy(alpha = 0.3f)) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = BorderColor,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor,
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.weight(1f))

        PrimaryFlowButton(stringResource(R.string.next), enabled = selectedType != null) {
            viewModel.goToNextStep()
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Step 1 (Team): Select Players
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SelectPlayerStep(viewModel: ReservationFlowViewModel) {
    val players by viewModel.selectedPlayers.collectAsState()
    val teamPlayers by viewModel.teamPlayers.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepTitle(stringResource(R.string.select_player))

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .background(SurfaceColor, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.select_a_player), color = SubtleText, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = SubtleText, modifier = Modifier.size(14.dp))
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                teamPlayers.forEach { player ->
                    // iOS: player.usersId?.username
                    val username = player.userId.username
                    DropdownMenuItem(
                        text = {
                            Row {
                                Text(username)
                                if (players.contains(username)) {
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        onClick = {
                            viewModel.addPlayer(username)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Selected player chips
        if (players.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                players.forEach { player ->
                    PlayerChip(player) { viewModel.removePlayer(player) }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        SecondaryFlowButton(stringResource(R.string.back)) { viewModel.goToPreviousStep() }
        PrimaryFlowButton(stringResource(R.string.next), enabled = players.isNotEmpty()) {
            viewModel.goToNextStep()
        }
    }
}

@Composable
private fun PlayerChip(name: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Icon(
            Icons.Default.Close,
            contentDescription = "Remove",
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp).clickable { onRemove() }
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Select Date Step
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SelectDateStep(viewModel: ReservationFlowViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val blockedDates by viewModel.blockedDates.collectAsState()
    val availableDates by viewModel.availableDates.collectAsState()
    val markedDates by viewModel.markedDates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val initialMillis by viewModel.initialSelectedDateMillis.collectAsState()

    val reservations = markedDates.filter { it.isReservation }.map { it.date }
    val trainings = markedDates.filter { it.isTraining }.map { it.date }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepTitle(stringResource(R.string.select_date), modifier = Modifier.weight(1f))
            TextButton(onClick = { viewModel.toggleLegendPopup() }) {
                Text("Leyenda", color = Color.Cyan, fontSize = 13.sp)
            }
        }

        if (isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Cyan)
            }
        } else {
            Box(modifier = Modifier.weight(1f)) {
                CustomCalendarView(
                    canUserInteract = true,
                    blockedDates = blockedDates,
                    reservations = reservations,
                    trainings = trainings,
                    initialSelectedDate = initialMillis,
                    onDateSelected = { viewModel.setSelectedDate(it) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (viewModel.currentStep.collectAsState().value > 0) {
            SecondaryFlowButton(stringResource(R.string.back)) { viewModel.goToPreviousStep() }
        }
        PrimaryFlowButton(stringResource(R.string.next), enabled = selectedDate != null) {
            viewModel.goToNextStep()
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Select Time Step (Virtual team)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SelectTimeStep(viewModel: ReservationFlowViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTime by viewModel.selectedTime.collectAsState()
    var hour by remember { mutableStateOf(12) }
    var minute by remember { mutableStateOf(0) }

    // Set initial time when entering this step
    LaunchedEffect(Unit) {
        if (selectedTime.isEmpty()) {
            viewModel.setSelectedTime(String.format("%02d:%02d", hour, minute))
        }
    }

    LaunchedEffect(selectedTime) {
        if (selectedTime.contains(":")) {
            val parts = selectedTime.split(":")
            hour = parts[0].toIntOrNull() ?: 12
            minute = parts[1].toIntOrNull() ?: 0
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepTitle(stringResource(R.string.select_hour))

        // Selected date card
        selectedDate?.let { date ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.date_selected), color = SubtleText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                dateFromString(date)?.toUIDateString()?.let {
                    Text(it, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Simple time input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor, RoundedCornerShape(12.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour picker
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { if (hour < 23) { hour++; viewModel.setSelectedTime(String.format("%02d:%02d", hour, minute)) } }) {
                    Text("▲", color = Color.White, fontSize = 20.sp)
                }
                Text(String.format("%02d", hour), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { if (hour > 0) { hour--; viewModel.setSelectedTime(String.format("%02d:%02d", hour, minute)) } }) {
                    Text("▼", color = Color.White, fontSize = 20.sp)
                }
            }

            Text(":", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))

            // Minute picker
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { minute = (minute + 5) % 60; viewModel.setSelectedTime(String.format("%02d:%02d", hour, minute)) }) {
                    Text("▲", color = Color.White, fontSize = 20.sp)
                }
                Text(String.format("%02d", minute), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { minute = if (minute >= 5) minute - 5 else 55; viewModel.setSelectedTime(String.format("%02d:%02d", hour, minute)) }) {
                    Text("▼", color = Color.White, fontSize = 20.sp)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        SecondaryFlowButton(stringResource(R.string.back)) { viewModel.goToPreviousStep() }
    }
}

// ═══════════════════════════════════════════════════════════════
// Select Space Step
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SelectSpaceStep(viewModel: ReservationFlowViewModel) {
    val spaces by viewModel.availableSpaces.collectAsState()
    val selectedSpace by viewModel.selectedSpace.collectAsState()

    LaunchedEffect(Unit) { viewModel.fetchAvailableSpaces() }

    Column(modifier = Modifier.fillMaxSize()) {
        StepTitle(stringResource(R.string.select_space))
        Spacer(Modifier.height(16.dp))

        if (spaces.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Cyan)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(spaces) { space ->
                    PillButton(
                        title = space.device,
                        isSelected = selectedSpace?.id == space.id,
                        isEnabled = true,
                        onClick = { viewModel.selectSpace(space) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SecondaryFlowButton(stringResource(R.string.back)) {
            viewModel.cleanSpace()
            viewModel.goToPreviousStep()
        }
        PrimaryFlowButton(stringResource(R.string.next), enabled = selectedSpace != null) {
            viewModel.goToNextStep()
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Select Slot Step
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SelectSlotStep(viewModel: ReservationFlowViewModel) {
    val slots by viewModel.availableSlots.collectAsState()
    val selectedSlots by viewModel.selectedSlots.collectAsState()
    val enabledSlots by viewModel.enabledSlots.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedSpace by viewModel.selectedSpace.collectAsState()

    LaunchedEffect(Unit) {
        if (slots.isEmpty()) {
            selectedDate?.let { viewModel.setSelectedDate(it) }
        }
    }

    val isSimulador = selectedSpace?.device?.lowercase()?.contains("simulador") == true
    val maxLabel = if (isSimulador) "Máximo 1 slot" else if (viewModel.personalReservations) "Máximo 3 slots consecutivos" else "Máximo 2 slots consecutivos"

    Column(modifier = Modifier.fillMaxSize()) {
        StepTitle(stringResource(R.string.select_time_slot))
        Spacer(Modifier.height(16.dp))

        if (slots.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Cyan)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(slots) { slot ->
                    val isEnabled = enabledSlots.contains(slot)
                    val isSelected = selectedSlots.contains(slot)
                    val calendar = Calendar.getInstance()
                    val currentHourPlus1 = calendar.get(Calendar.HOUR_OF_DAY) + 1
                    val isSlotDateToday = isToday(selectedDate)
                    val isSlotTimeValid = slot.value > currentHourPlus1
                    val computedEnabled = if (isSlotDateToday) (isSlotTimeValid && isEnabled) else isEnabled

                    PillButton(
                        title = slot.time,
                        isSelected = isSelected,
                        isEnabled = computedEnabled,
                        onClick = { if (computedEnabled) viewModel.toggleSlotSelection(slot) }
                    )
                }
            }
        }

        Text(maxLabel, color = Color.White.copy(alpha = 0.35f), fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        Spacer(Modifier.height(12.dp))

        SecondaryFlowButton(stringResource(R.string.back)) {
            viewModel.cleanSlots()
            viewModel.goToPreviousStep()
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Bottom action buttons
// ═══════════════════════════════════════════════════════════════

@Composable
private fun BottomButtons(
    viewModel: ReservationFlowViewModel,
    personalReservation: Boolean,
    userId: String,
    logoBitmap: android.graphics.Bitmap?,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val selectedSlots by viewModel.selectedSlots.collectAsState()
    val selectedSpace by viewModel.selectedSpace.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTime by viewModel.selectedTime.collectAsState()

    val isLastStep = currentStep == viewModel.totalSteps - 1
    val isVirtualLastStep = viewModel.isVirtualTeam && (
            (personalReservation && currentStep == 1) || (!personalReservation && currentStep == 3)
            )

    val canConfirm = when {
        isVirtualLastStep -> selectedTime.isNotEmpty()
        isLastStep -> selectedSpace != null && selectedSlots.isNotEmpty() && selectedDate != null
        else -> false
    }

    if (isLastStep || isVirtualLastStep) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            val buttonLabel = if (viewModel.isEditing) stringResource(R.string.save) else stringResource(R.string.book)

            PrimaryFlowButton(buttonLabel, enabled = canConfirm) {
                coroutineScope.launch {
                    val success = when {
                        // Virtual team
                        isVirtualLastStep && !personalReservation -> {
                            if (viewModel.isEditing) viewModel.updateVirtualTeamReservation()
                            else viewModel.createVirtualTeamReservation()
                        }
                        // Centre team
                        !personalReservation -> {
                            if (viewModel.isEditing) viewModel.updateCenterTeamReservation()
                            else viewModel.createTeamReservation(logoBitmap)
                        }
                        // Individual edit
                        viewModel.isEditing -> viewModel.updateIndividualReservation()
                        // Individual create
                        else -> viewModel.createReservation(userId, logoBitmap)
                    }

                    val msg = if (success) context.getString(R.string.booking_creation_success)
                    else context.getString(R.string.booking_creation_failure)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) onSuccess()
                    onDismiss()
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Reusable buttons (iOS style)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PrimaryFlowButton(title: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Cyan,
            disabledContainerColor = Color.Transparent
        ),
        border = if (!enabled) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null
    ) {
        Text(
            title,
            color = if (enabled) Color.Black else Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.SemiBold, fontSize = 15.sp
        )
    }

    Spacer(Modifier.height(16.dp))
}

@Composable
private fun SecondaryFlowButton(title: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Text(title, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
private fun PillButton(title: String, isSelected: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier.height(44.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.Cyan else Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        border = when {
            isSelected -> null
            isEnabled -> androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
            else -> androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        }
    ) {
        Text(
            title, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1,
            color = when {
                isSelected -> Color.Black
                isEnabled -> Color.White
                else -> DisabledText
            }
        )
    }
}

@Composable
private fun StepTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = modifier)
}