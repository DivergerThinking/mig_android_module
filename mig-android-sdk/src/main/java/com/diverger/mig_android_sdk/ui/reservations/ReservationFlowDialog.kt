import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diverger.mig_android_sdk.data.GamingSpaceTime
import com.diverger.mig_android_sdk.data.Space
import com.diverger.mig_android_sdk.ui.components.CustomCalendarView
import com.diverger.mig_android_sdk.ui.reservations.ReservationFlowViewModel

@Composable
fun ReservationFlowDialog(
    onDismiss: () -> Unit,
    userId: String,
    onReservationSuccess: () -> Unit
) {
    val viewModel = ReservationFlowViewModel()
    val currentStep by viewModel.currentStep.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
        ) {
            Column {
                // Indicador de progreso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { index ->
                        CircleIndicator(isActive = index == currentStep)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (currentStep) {
                    0 -> SelectDateView(viewModel, onNext = { viewModel.goToNextStep() })
                    1 -> SelectSlotView(viewModel, onNext = { viewModel.goToNextStep() })
                    2 -> SelectSpaceView(viewModel, onConfirm = {
                        viewModel.createReservation(userId)
                        onReservationSuccess()
                    })
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón de cierre
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CircleIndicator(isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(if (isActive) Color.White else Color.Transparent, CircleShape)
            .border(1.dp, Color.White, CircleShape)
            .padding(2.dp)
    )
}

@Composable
fun SelectDateView(viewModel: ReservationFlowViewModel, onNext: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Selecciona una fecha", color = Color.White, fontSize = 18.sp)

        val availableDates by viewModel.availableDates.collectAsState()
        val blockedDates by viewModel.blockedDates.collectAsState()
        val selectedDate by viewModel.selectedDate.collectAsState()

        if (availableDates.isEmpty() && blockedDates.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            CustomCalendarView(
                canUserInteract = true,
                markedDates = viewModel.availableDates.collectAsState().value,
                blockedDates = viewModel.blockedDates.collectAsState().value,
                onDateSelected = { selectedDate ->
                    viewModel.setSelectedDate(selectedDate)
                }
            )
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
            enabled = selectedDate != null,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Siguiente", color = Color.White)
        }
    }
}

@Composable
fun SelectSlotView(viewModel: ReservationFlowViewModel, onNext: () -> Unit) {
    val availableSlots by viewModel.availableSlots.collectAsState()
    val selectedSlots by viewModel.selectedSlots.collectAsState()
    val enabledSlots by viewModel.enabledSlots.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Selecciona un horario", color = Color.White, fontSize = 18.sp)

        if (availableSlots.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            // Mostrar los slots en un Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),  // 3 columnas por fila
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableSlots) { slot ->
                    SlotItem(
                        slot = slot,
                        isSelected = selectedSlots.contains(slot),
                        isEnabled = enabledSlots.contains(slot),
                        onClick = { viewModel.toggleSlotSelection(slot) }
                    )
                }
            }
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
            enabled = selectedSlots.isNotEmpty(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Siguiente", color = Color.White)
        }
    }
}

@Composable
fun SlotItem(slot: GamingSpaceTime, isSelected: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = { if (isEnabled) onClick() },
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isSelected -> Color.White  // Seleccionado -> Blanco
                isEnabled -> Color.Gray    // Disponible -> Gris
                else -> Color.DarkGray     // Deshabilitado -> Oscuro
            }
        ),
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(50.dp),
        enabled = isEnabled
    ) {
        Text(
            text = slot.time,
            color = if (isSelected) Color.Black else Color.White
        )
    }
}

@Composable
fun SelectSpaceView(viewModel: ReservationFlowViewModel, onConfirm: () -> Unit) {
    LaunchedEffect(Unit) {
        viewModel.fetchAvailableSpaces()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Selecciona un espacio", color = Color.White, fontSize = 18.sp)

        val availableSpaces by viewModel.availableSpaces.collectAsState()
        val selectedSpace by viewModel.selectedSpace.collectAsState()

        if (availableSpaces.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),  // Grid con 3 columnas
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableSpaces) { space ->
                    SpaceItem(
                        space = space,
                        isSelected = selectedSpace == space,
                        onClick = { viewModel.selectSpace(space) }
                    )
                }
            }
        }

        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
            enabled = selectedSpace != null,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Reservar", color = Color.White)
        }
    }
}

@Composable
fun SpaceItem(space: Space, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color.White else Color.Gray),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(space.device, color = if (isSelected) Color.Black else Color.White)
    }
}
