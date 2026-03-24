import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.window.DialogProperties
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.GamingSpaceTime
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.Space
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
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1A1A1A), Color.Black, Color.Black, Color(0xFF0D0D0D))),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(44.dp))
                    Spacer(Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(viewModel.totalSteps) { index ->
                            Box(modifier = Modifier.size(8.dp).background(
                                if (index <= currentStep) Color.White else Color.White.copy(alpha = 0.25f), CircleShape
                            ))
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { if (!isProcessing) onDismiss() }, modifier = Modifier.size(44.dp)) {
                        Icon(FeatherIcons.XCircle, stringResource(R.string.close_label), tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    }
                }

                SummaryBanner(viewModel)

                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)) {
                    if (isProcessing) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Cyan) }
                    } else {
                        AnimatedContent(targetState = currentStep, transitionSpec = { fadeIn() with fadeOut() }) { step ->
                            when {
                                viewModel.isEditing && !personalReservation && step == 0 -> SelectPlayerStep(viewModel)
                                !personalReservation && step == 0 -> SelectPlaceAndNotesStep(viewModel)
                                !personalReservation && step == 1 -> SelectPlayerStep(viewModel)
                                (personalReservation && step == 0) || (!personalReservation && step == 2) -> SelectDateStep(viewModel)
                                viewModel.isVirtualTeam && ((personalReservation && step == 1) || (!personalReservation && step == 3)) -> SelectTimeStep(viewModel)
                                (personalReservation && step == 1) || (!personalReservation && step == 3) -> SelectSpaceStep(viewModel)
                                (personalReservation && step == 2) || (!personalReservation && step == 4) -> SelectSlotStep(viewModel)
                                personalReservation && !viewModel.isSelectedSpaceSimulator && step == 3 -> AskSimulatorStep(viewModel)
                                personalReservation && !viewModel.isSelectedSpaceSimulator && step == 4 -> SelectSimulatorSlotStep(viewModel)
                                personalReservation && viewModel.isSelectedSpaceSimulator && step == 3 -> AskExtraSpaceStep(viewModel)
                                personalReservation && viewModel.isSelectedSpaceSimulator && step == 4 -> SelectExtraSpaceStep(viewModel)
                                personalReservation && viewModel.isSelectedSpaceSimulator && step == 5 -> SelectExtraSlotStep(viewModel)
                            }
                        }
                    }
                }

                if (!isProcessing) {
                    BottomButtons(viewModel, personalReservation, userId, logoBitmap, coroutineScope, context, onReservationSuccess, onDismiss)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Summary
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SummaryBanner(vm: ReservationFlowViewModel) {
    val tokens = vm.getSummaryTokens()
    val title = if (vm.isEditing && !vm.personalReservations) stringResource(R.string.edit_players_title) else stringResource(R.string.book_space)
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (tokens.isNotEmpty()) Text(tokens.joinToString(" · "), color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, textAlign = TextAlign.Center, maxLines = 2)
    }
}

// ═══════════════════════════════════════════════════════════════
// Place & Notes
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectPlaceAndNotesStep(vm: ReservationFlowViewModel) {
    val selectedType by vm.selectedSpaceType.collectAsState()
    val notes by vm.reservationNotes.collectAsState()
    val spaceTypes = listOf("Virtual", "E-Sports Center")
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepTitle(stringResource(R.string.select_space))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Box(modifier = Modifier.fillMaxWidth().menuAnchor().background(SurfaceColor, RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp)).clickable { expanded = true }.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(selectedType ?: stringResource(R.string.select_type_placeholder), color = if (selectedType == null) SubtleText else Color.White, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = SubtleText, modifier = Modifier.size(14.dp))
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                spaceTypes.forEach { type -> DropdownMenuItem(text = { Row { Text(type); if (selectedType == type) { Spacer(Modifier.weight(1f)); Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } }, onClick = { vm.setSelectedSpaceType(type); expanded = false }) }
            }
        }
        OutlinedTextField(value = notes, onValueChange = { vm.setReservationNotes(it) }, placeholder = { Text(stringResource(R.string.notes_optional), color = Color.White.copy(alpha = 0.3f)) }, modifier = Modifier.fillMaxWidth().height(120.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = BorderColor, unfocusedBorderColor = BorderColor, focusedContainerColor = SurfaceColor, unfocusedContainerColor = SurfaceColor), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.weight(1f))
        PrimaryFlowButton(stringResource(R.string.next), enabled = selectedType != null) { vm.goToNextStep() }
    }
}

// ═══════════════════════════════════════════════════════════════
// Players
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SelectPlayerStep(vm: ReservationFlowViewModel) {
    val players by vm.selectedPlayers.collectAsState()
    val teamPlayers by vm.teamPlayers.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepTitle(stringResource(R.string.select_a_player))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Box(modifier = Modifier.fillMaxWidth().menuAnchor().background(SurfaceColor, RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp)).clickable { expanded = true }.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.select_a_player), color = SubtleText, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = SubtleText, modifier = Modifier.size(14.dp))
                }
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                teamPlayers.forEach { player ->
                    val username = player.userId.username
                    DropdownMenuItem(text = { Row { Text(username); if (players.contains(username)) { Spacer(Modifier.weight(1f)); Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } }, onClick = { vm.addPlayer(username); expanded = false })
                }
            }
        }
        if (players.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                players.forEach { player -> PlayerChip(player) { vm.removePlayer(player) } }
            }
        }
        Spacer(Modifier.weight(1f))
        if (!(vm.isEditing && !vm.personalReservations)) {
            SecondaryFlowButton(stringResource(R.string.back)) { vm.goToPreviousStep() }
            PrimaryFlowButton(stringResource(R.string.next), enabled = players.isNotEmpty()) { vm.goToNextStep() }
        }
    }
}

@Composable
private fun PlayerChip(name: String, onRemove: () -> Unit) {
    Row(modifier = Modifier.background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Icon(Icons.Default.Close, stringResource(R.string.delete_label), tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(14.dp).clickable { onRemove() })
    }
}

// ═══════════════════════════════════════════════════════════════
// Date
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SelectDateStep(vm: ReservationFlowViewModel) {
    val selectedDate by vm.selectedDate.collectAsState()
    val blockedDates by vm.blockedDates.collectAsState()
    val markedDates by vm.markedDates.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val initialMillis by vm.initialSelectedDateMillis.collectAsState()
    val reservations = markedDates.filter { it.isReservation }.map { it.date }
    val trainings = markedDates.filter { it.isTraining }.map { it.date }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepTitle(stringResource(R.string.select_date), modifier = Modifier.weight(1f))
            TextButton(onClick = { vm.toggleLegendPopup() }) { Text(stringResource(R.string.legend), color = Color.Cyan, fontSize = 13.sp) }
        }
        if (isLoading) { Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Cyan) } }
        else {
            Box(modifier = Modifier.weight(1f)) {
                CustomCalendarView(canUserInteract = true, blockedDates = blockedDates, reservations = reservations, trainings = trainings, initialSelectedDate = initialMillis, onDateSelected = { vm.setSelectedDate(it) })
            }
        }
        Spacer(Modifier.height(12.dp))
        if (vm.currentStep.collectAsState().value > 0) SecondaryFlowButton(stringResource(R.string.back)) { vm.goToPreviousStep() }
        PrimaryFlowButton(stringResource(R.string.next), enabled = selectedDate != null) { vm.goToNextStep() }
    }
}

// ═══════════════════════════════════════════════════════════════
// Time (Virtual)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SelectTimeStep(vm: ReservationFlowViewModel) {
    val selectedDate by vm.selectedDate.collectAsState()
    val selectedTime by vm.selectedTime.collectAsState()
    var hour by remember { mutableStateOf(12) }; var minute by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) { if (selectedTime.isEmpty()) vm.setSelectedTime(String.format("%02d:%02d", hour, minute)) }
    LaunchedEffect(selectedTime) { if (selectedTime.contains(":")) { val p = selectedTime.split(":"); hour = p[0].toIntOrNull() ?: 12; minute = p[1].toIntOrNull() ?: 0 } }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepTitle(stringResource(R.string.select_hour))
        selectedDate?.let { date -> Column(modifier = Modifier.fillMaxWidth().background(SurfaceColor, RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(16.dp)) {
            Text(stringResource(R.string.date_selected), color = SubtleText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            dateFromString(date)?.toUIDateString()?.let { Text(it, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
        }}
        Row(modifier = Modifier.fillMaxWidth().background(SurfaceColor, RoundedCornerShape(12.dp)).border(1.dp, BorderColor, RoundedCornerShape(12.dp)).padding(24.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { if (hour < 23) { hour++; vm.setSelectedTime(String.format("%02d:%02d", hour, minute)) } }) { Text("▲", color = Color.White, fontSize = 20.sp) }
                Text(String.format("%02d", hour), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { if (hour > 0) { hour--; vm.setSelectedTime(String.format("%02d:%02d", hour, minute)) } }) { Text("▼", color = Color.White, fontSize = 20.sp) }
            }
            Text(":", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { minute = (minute + 5) % 60; vm.setSelectedTime(String.format("%02d:%02d", hour, minute)) }) { Text("▲", color = Color.White, fontSize = 20.sp) }
                Text(String.format("%02d", minute), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { minute = if (minute >= 5) minute - 5 else 55; vm.setSelectedTime(String.format("%02d:%02d", hour, minute)) }) { Text("▼", color = Color.White, fontSize = 20.sp) }
            }
        }
        Spacer(Modifier.weight(1f))
        SecondaryFlowButton(stringResource(R.string.back)) { vm.goToPreviousStep() }
    }
}

// ═══════════════════════════════════════════════════════════════
// Space
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SelectSpaceStep(vm: ReservationFlowViewModel) {
    val spaces by vm.availableSpaces.collectAsState(); val selectedSpace by vm.selectedSpace.collectAsState()
    LaunchedEffect(Unit) { vm.fetchAvailableSpaces() }
    Column(modifier = Modifier.fillMaxSize()) {
        StepTitle(stringResource(R.string.select_space)); Spacer(Modifier.height(16.dp))
        if (spaces.isEmpty()) { Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Cyan) } }
        else { LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(spaces) { space -> PillButton(space.device, selectedSpace?.id == space.id, true) { vm.selectSpace(space) } } } }
        Spacer(Modifier.height(16.dp))
        SecondaryFlowButton(stringResource(R.string.back)) { vm.cleanSpace(); vm.goToPreviousStep() }
        PrimaryFlowButton(stringResource(R.string.next), enabled = selectedSpace != null) { vm.goToNextStep() }
    }
}

// ═══════════════════════════════════════════════════════════════
// Slot
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SelectSlotStep(vm: ReservationFlowViewModel) {
    val slots by vm.availableSlots.collectAsState(); val selectedSlots by vm.selectedSlots.collectAsState()
    val enabledSlots by vm.enabledSlots.collectAsState(); val selectedDate by vm.selectedDate.collectAsState()
    val selectedSpace by vm.selectedSpace.collectAsState()

    LaunchedEffect(Unit) { if (slots.isEmpty()) selectedDate?.let { vm.setSelectedDate(it) } }

    val isSim = selectedSpace?.device?.lowercase()?.contains("simulador") == true
    val maxLabel = if (isSim) stringResource(R.string.max_1_slot) else if (vm.personalReservations) stringResource(R.string.maximum_3_consecutive_slots) else stringResource(R.string.max_2_consecutive_slots)

    Column(modifier = Modifier.fillMaxSize()) {
        StepTitle(stringResource(R.string.select_time_slot)); Spacer(Modifier.height(16.dp))
        if (slots.isEmpty()) { Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Cyan) } }
        else { LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(slots) { slot -> val isEnabled = enabledSlots.contains(slot); val isSelected = selectedSlots.contains(slot)
                val cal = Calendar.getInstance(); val hourPlus1 = cal.get(Calendar.HOUR_OF_DAY) + 1
                val isTodayDate = isToday(selectedDate); val isTimeValid = slot.value > hourPlus1
                val computed = if (isTodayDate) (isTimeValid && isEnabled) else isEnabled
                PillButton(slot.time, isSelected, computed) { if (computed) vm.toggleSlotSelection(slot) }
            }
        }}
        Text(maxLabel, color = Color.White.copy(alpha = 0.35f), fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        SecondaryFlowButton(stringResource(R.string.back)) { vm.cleanSlots(); vm.goToPreviousStep() }
        if (vm.shouldGoToAskAddon) {
            PrimaryFlowButton(stringResource(R.string.next), enabled = selectedSlots.isNotEmpty()) { vm.goToNextStep() }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Ask Simulator
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AskSimulatorStep(vm: ReservationFlowViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Text("🎮", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.ask_simulator_title), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.ask_simulator_description), color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 30.dp))
        Spacer(Modifier.weight(1f))
        SecondaryFlowButton(stringResource(R.string.back)) { vm.goToPreviousStep() }
        Spacer(Modifier.height(4.dp))
        Button(onClick = { vm.setWantsSimulator(true); vm.findSimulatorSpace(); vm.fetchSimulatorSlots(); vm.goToNextStep() },
            modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) { Text(stringResource(R.string.yes_add_simulator), color = Color.Cyan, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
    }
}

// ═══════════════════════════════════════════════════════════════
// Select Simulator Slot
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SelectSimulatorSlotStep(vm: ReservationFlowViewModel) {
    val slots by vm.availableSimulatorSlots.collectAsState()
    val selectedSlots by vm.simulatorSelectedSlots.collectAsState()
    val selectedDate by vm.selectedDate.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        StepTitle(stringResource(R.string.select_simulator_slot_title)); Spacer(Modifier.height(16.dp))
        if (slots.isEmpty()) { Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Cyan) } }
        else { LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(slots) { slot ->
                val isSelected = selectedSlots.any { it.id == slot.id }
                val cal = Calendar.getInstance(); val hourPlus1 = cal.get(Calendar.HOUR_OF_DAY) + 1
                val isTodayDate = isToday(selectedDate); val isTimeValid = slot.value > hourPlus1
                val isBlocked = vm.isSimulatorSlotBlockedByMain(slot)
                val computed = if (isTodayDate) (isTimeValid && !isBlocked) else !isBlocked
                PillButton(slot.time, isSelected, computed) { if (computed) vm.toggleSimulatorSlotSelection(slot) }
            }
        }}
        Text(stringResource(R.string.max_1_simulator_slot), color = Color.White.copy(alpha = 0.35f), fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        SecondaryFlowButton(stringResource(R.string.back)) { vm.goToPreviousStep() }
    }
}

// ═══════════════════════════════════════════════════════════════
// Ask Extra Space
// ═══════════════════════════════════════════════════════════════

@Composable
private fun AskExtraSpaceStep(vm: ReservationFlowViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Text("🖥️", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.ask_extra_space_title), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.ask_extra_space_description), color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 30.dp))
        Spacer(Modifier.weight(1f))
        SecondaryFlowButton(stringResource(R.string.back)) { vm.goToPreviousStep() }
        Spacer(Modifier.height(4.dp))
        Button(onClick = { vm.setWantsExtraSpace(true); vm.goToNextStep() },
            modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) { Text(stringResource(R.string.yes_add_space), color = Color.Cyan, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
    }
}

// ═══════════════════════════════════════════════════════════════
// Select Extra Space
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SelectExtraSpaceStep(vm: ReservationFlowViewModel) {
    val spaces = vm.nonSimulatorSpaces; val extraSpace by vm.extraSpace.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        StepTitle(stringResource(R.string.select_extra_space_title)); Spacer(Modifier.height(16.dp))
        if (spaces.isEmpty()) { Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Cyan) } }
        else { LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(spaces) { space -> PillButton(space.device, extraSpace?.id == space.id, true) { vm.selectExtraSpace(space) } }
        }}
        Spacer(Modifier.height(16.dp))
        SecondaryFlowButton(stringResource(R.string.back)) { vm.goToPreviousStep() }
        PrimaryFlowButton(stringResource(R.string.next), enabled = extraSpace != null) { vm.fetchExtraSpaceSlots(); vm.goToNextStep() }
    }
}

// ═══════════════════════════════════════════════════════════════
// Select Extra Slot
// ═══════════════════════════════════════════════════════════════

@Composable
private fun SelectExtraSlotStep(vm: ReservationFlowViewModel) {
    val slots by vm.availableExtraSpaceSlots.collectAsState()
    val selectedSlots by vm.extraSpaceSelectedSlots.collectAsState()
    val enabledSlots by vm.enabledExtraSpaceSlots.collectAsState()
    val selectedDate by vm.selectedDate.collectAsState()
    val extraSpace by vm.extraSpace.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        StepTitle(stringResource(R.string.select_time_slot))
        extraSpace?.let { Text(it.device, color = Color.Cyan, fontSize = 14.sp) }
        Spacer(Modifier.height(16.dp))
        if (slots.isEmpty()) { Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.Cyan) } }
        else { LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(slots) { slot ->
                val isEnabled = enabledSlots.contains(slot); val isSelected = selectedSlots.contains(slot)
                val cal = Calendar.getInstance(); val hourPlus1 = cal.get(Calendar.HOUR_OF_DAY) + 1
                val isTodayDate = isToday(selectedDate); val isTimeValid = slot.value > hourPlus1
                val isBlocked = vm.isExtraSpaceSlotBlockedBySimulator(slot)
                val computed = if (isTodayDate) (isTimeValid && isEnabled && !isBlocked) else (isEnabled && !isBlocked)
                PillButton(slot.time, isSelected, computed) { if (computed) vm.toggleExtraSpaceSlotSelection(slot) }
            }
        }}
        Text(stringResource(R.string.maximum_3_consecutive_slots), color = Color.White.copy(alpha = 0.35f), fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        SecondaryFlowButton(stringResource(R.string.back)) { vm.goToPreviousStep() }
    }
}

// ═══════════════════════════════════════════════════════════════
// Bottom Buttons
// ═══════════════════════════════════════════════════════════════

@Composable
private fun BottomButtons(
    vm: ReservationFlowViewModel, personalReservation: Boolean, userId: String,
    logoBitmap: android.graphics.Bitmap?, coroutineScope: kotlinx.coroutines.CoroutineScope,
    context: android.content.Context, onSuccess: () -> Unit, onDismiss: () -> Unit
) {
    val currentStep by vm.currentStep.collectAsState()
    val selectedSlots by vm.selectedSlots.collectAsState()
    val selectedSpace by vm.selectedSpace.collectAsState()
    val selectedDate by vm.selectedDate.collectAsState()
    val selectedTime by vm.selectedTime.collectAsState()
    val selectedPlayers by vm.selectedPlayers.collectAsState()
    val simSlots by vm.simulatorSelectedSlots.collectAsState()
    val extraSlots by vm.extraSpaceSelectedSlots.collectAsState()

    val isEditingTeam = vm.isEditing && !personalReservation
    val isVirtualLastStep = vm.isVirtualTeam && ((personalReservation && currentStep == 1) || (!personalReservation && currentStep == 3))
    val isAskSimulatorStep = personalReservation && !vm.isSelectedSpaceSimulator && currentStep == 3
    val isAskExtraSpaceStep = personalReservation && vm.isSelectedSpaceSimulator && currentStep == 3
    val isSimulatorSlotStep = personalReservation && !vm.isSelectedSpaceSimulator && currentStep == 4
    val isExtraSlotStep = personalReservation && vm.isSelectedSpaceSimulator && currentStep == 5
    val isSlotStepNoAddon = !vm.shouldGoToAskAddon && ((personalReservation && currentStep == 2) || (!personalReservation && currentStep == 4))

    val canConfirm = when {
        isEditingTeam -> selectedPlayers.isNotEmpty()
        isVirtualLastStep -> selectedTime.isNotEmpty()
        isAskSimulatorStep || isAskExtraSpaceStep -> true
        isSimulatorSlotStep -> simSlots.isNotEmpty()
        isExtraSlotStep -> extraSlots.isNotEmpty()
        isSlotStepNoAddon -> selectedSpace != null && selectedSlots.isNotEmpty() && selectedDate != null
        else -> false
    }

    val showButton = isEditingTeam || isVirtualLastStep || isAskSimulatorStep || isAskExtraSpaceStep || isSimulatorSlotStep || isExtraSlotStep || isSlotStepNoAddon

    if (showButton) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            val buttonLabel = when {
                isEditingTeam -> stringResource(R.string.save)
                isAskSimulatorStep -> stringResource(R.string.no_reserve_without_simulator)
                isAskExtraSpaceStep -> stringResource(R.string.no_reserve_only_simulator)
                vm.isEditing -> stringResource(R.string.save)
                else -> stringResource(R.string.book)
            }

            PrimaryFlowButton(buttonLabel, enabled = canConfirm) {
                coroutineScope.launch {
                    val success = when {
                        isEditingTeam -> { if (vm.isVirtualTeam) vm.updateVirtualTeamReservation() else vm.updateCenterTeamReservation() }
                        isAskSimulatorStep || isAskExtraSpaceStep -> { vm.resetSimulatorSelection(); vm.resetExtraSpaceSelection(); if (vm.isEditing) vm.updateIndividualReservation() else vm.createReservation(userId, logoBitmap) }
                        isSimulatorSlotStep || isExtraSlotStep -> { if (vm.isEditing) vm.updateIndividualReservation() else vm.createReservation(userId, logoBitmap) }
                        isVirtualLastStep && !personalReservation -> vm.createVirtualTeamReservation()
                        isSlotStepNoAddon && !personalReservation -> vm.createTeamReservation(logoBitmap)
                        isSlotStepNoAddon && vm.isEditing -> vm.updateIndividualReservation()
                        isSlotStepNoAddon -> vm.createReservation(userId, logoBitmap)
                        else -> vm.createReservation(userId, logoBitmap)
                    }
                    val msg = if (success) context.getString(R.string.booking_creation_success) else context.getString(R.string.booking_creation_failure)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) onSuccess()
                    onDismiss()
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Reusable buttons
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PrimaryFlowButton(title: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, disabledContainerColor = Color.Transparent),
        border = if (!enabled) BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null
    ) { Text(title, color = if (enabled) Color.Black else Color.White.copy(alpha = 0.4f), fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
}

@Composable
private fun SecondaryFlowButton(title: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().height(50.dp).padding(bottom = 4.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))) {
        Text(title, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
private fun PillButton(title: String, isSelected: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = isEnabled, modifier = Modifier.height(44.dp), shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color.Cyan else Color.Transparent, disabledContainerColor = Color.Transparent),
        border = when { isSelected -> null; isEnabled -> BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)); else -> BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)) }
    ) { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, color = when { isSelected -> Color.Black; isEnabled -> Color.White; else -> DisabledText }) }
}

@Composable
private fun StepTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = modifier)
}