import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.GamingSpaceTime
import com.diverger.mig_android_sdk.data.Space
import com.diverger.mig_android_sdk.support.dateFromString
import com.diverger.mig_android_sdk.support.isToday
import com.diverger.mig_android_sdk.support.toUIDateString
import com.diverger.mig_android_sdk.ui.reservations.ReservationFlowViewModel
import compose.icons.FeatherIcons
import compose.icons.feathericons.XCircle
import kotlinx.coroutines.launch
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationFlowDialog(
    onDismiss: () -> Unit,
    userId: String,
    onReservationSuccess: () -> Unit
) {
    val viewModel = ReservationFlowViewModel()
    val currentStep by viewModel.currentStep.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState() // Nuevo estado para saber si está procesando
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showDniDialog by remember { mutableStateOf(false) }
    var enteredDni by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .background(
                    // Brush.verticalGradient(
                    //colors = listOf(Color.Black, Color.Black, Color.White.copy(alpha = 0.15f))),
                    Color.Black,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = { if (!isProcessing) onDismiss() },
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.Transparent, shape = CircleShape)
                    ) {
                        Icon(FeatherIcons.XCircle, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Indicador de progreso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center) {
                    repeat(3) { index ->
                        CircleIndicator(isActive = index == currentStep)
                        if (index < 2) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (currentStep) {
                    0 -> SelectDateView(viewModel, onNext = { viewModel.goToNextStep() })
                    1 -> SelectSlotView(
                        viewModel,
                        onNext = { viewModel.goToNextStep() },
                        onPrevious = {
                            viewModel.cleanSlots()
                            viewModel.goToPreviousStep()
                        }
                    )
                    2 -> SelectSpaceView(viewModel, onConfirm = {
                        coroutineScope.launch {
                            viewModel.createReservation(userId).let { result ->
                                if (result) {
                                    Toast.makeText(context,
                                        context.getString(R.string.booking_creation_success), Toast.LENGTH_SHORT).show()
                                    onReservationSuccess() // Actualizar lista
                                } else {
                                    Toast.makeText(context,
                                        context.getString(R.string.booking_creation_failure), Toast.LENGTH_SHORT).show()
                                }
                                onDismiss()
                            }
                        }
                    })
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isProcessing) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                // Botón de cierre
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.close_label), color = Color.White)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SelectDateView(viewModel: ReservationFlowViewModel, onNext: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(stringResource(R.string.select_date), color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(20.dp))

        val availableDates by viewModel.availableDates.collectAsState()
        val blockedDates by viewModel.blockedDates.collectAsState()
        val selectedDate by viewModel.selectedDate.collectAsState()
        val reservations = viewModel.markedDates.collectAsState().value.filter { it.isReservation }.map { it.date } // 🔹 Filtramos solo reservas individuales

        if (availableDates.isEmpty() && blockedDates.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            CustomCalendarView(
                canUserInteract = true,
                blockedDates = viewModel.blockedDates.collectAsState().value,
                onDateSelected = {
                    viewModel.setSelectedDate(it)
                },
                reservations = reservations
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, disabledContainerColor = Color.White.copy(0.35f)),
            enabled = selectedDate != null,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.next), color = Color.Black)
        }
    }
}

@Composable
fun SelectSlotView(viewModel: ReservationFlowViewModel, onNext: () -> Unit, onPrevious: () -> Unit) {
    val availableSlots by viewModel.availableSlots.collectAsState()
    val selectedSlots by viewModel.selectedSlots.collectAsState()
    val enabledSlots by viewModel.enabledSlots.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(stringResource(R.string.select_schedule), color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(20.dp))

        selectedDate?.let { date ->
            dateFromString(date)?.toUIDateString()?.let { formattedDate ->
                Text(formattedDate, color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
        Spacer(Modifier.height(20.dp))
        if (availableSlots.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            // Mostrar los slots en un Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),  // 3 columnas por fila
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableSlots) { slot ->
                    SlotItem(
                        slot = slot,
                        date = selectedDate,
                        isSelected = selectedSlots.contains(slot),
                        isEnabled = enabledSlots.contains(slot),
                        onClick = { viewModel.toggleSlotSelection(slot) }
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPrevious,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Cyan,
                    disabledContainerColor = Color.White.copy(0.35f)
                ),
                enabled = true,
                modifier = Modifier.align(Alignment.CenterVertically)

            ) {
                Text(stringResource(R.string.previous), color = Color.Black)
            }
            Spacer(Modifier.width(20.dp))
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Cyan,
                    disabledContainerColor = Color.White.copy(0.35f)
                ),
                enabled = selectedSlots.isNotEmpty(),
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(stringResource(R.string.next), color = Color.Black)
            }
        }
    }
}

@Composable
fun SlotItem(slot: GamingSpaceTime, date: String?, isSelected: Boolean, isEnabled: Boolean, onClick: () -> Unit) {

    val calendar = Calendar.getInstance()
    val currentHourPlus2 = calendar.get(Calendar.HOUR_OF_DAY) + 1
    val isSlotDateToday = isToday(date)
    val isSlotTimeValid = slot.value > currentHourPlus2

    val computedEnabled = if (isSlotDateToday) (isSlotTimeValid && isEnabled) else isEnabled

    Button(
        onClick = { if (isEnabled) onClick() },
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isSelected -> Color.White  // Seleccionado -> Blanco
                computedEnabled -> Color.Transparent    // Disponible -> Gris
                else -> Color.DarkGray     // Deshabilitado -> Oscuro
            },
        ),
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(50.dp)
            .border(
                2.dp,
                if (computedEnabled && !isSelected) Color.White else Color.Transparent,
                RoundedCornerShape(40.dp)
            ),
    enabled = computedEnabled
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
        Text(stringResource(R.string.select_space), color = Color.White, fontSize = 18.sp)

        Spacer(Modifier.height(20.dp))

        val availableSpaces by viewModel.availableSpaces.collectAsState()
        val selectedSpace by viewModel.selectedSpace.collectAsState()

        if (availableSpaces.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),  // Grid con 3 columnas
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

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, disabledContainerColor = Color.White.copy(0.35f)),
            enabled = selectedSpace != null,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.book_training), color = Color.Black)
        }
    }
}

@Composable
fun SpaceItem(space: Space, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color.White else Color.Transparent),
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(50.dp)
            .border(
                2.dp,
                if (!isSelected) Color.White else Color.Transparent,
                RoundedCornerShape(40.dp)
            )
        ) {
        Text(space.device, color = if (isSelected) Color.Black else Color.White)
    }
}
