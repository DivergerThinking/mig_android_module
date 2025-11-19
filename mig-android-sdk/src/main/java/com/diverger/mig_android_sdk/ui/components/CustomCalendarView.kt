import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.DateFormatSymbols
import java.util.Locale
import java.util.Calendar
import java.text.SimpleDateFormat

@Composable
fun CustomCalendarView(
    canUserInteract: Boolean,
    reservations: List<String>,  // 📌 Fechas con reservas individuales (azul)
    blockedDates: List<String>,  // 📌 Fechas bloqueadas (rojo)
    onDateSelected: (String) -> Unit
) {
    val todayCalendar = Calendar.getInstance()
    // Reset time to start of day for accurate comparison
    todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
    todayCalendar.set(Calendar.MINUTE, 0)
    todayCalendar.set(Calendar.SECOND, 0)
    todayCalendar.set(Calendar.MILLISECOND, 0)
    val today = todayCalendar.timeInMillis

    var selectedDate by remember { mutableStateOf(today) }
    var currentMonthCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        // Selector de mes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newCalendar = currentMonthCalendar.clone() as Calendar
                newCalendar.add(Calendar.MONTH, -1)
                currentMonthCalendar = newCalendar
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Mes Anterior", tint = Color.White)
            }

            val now = Calendar.getInstance()
            val pattern = if (currentMonthCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                "MMMM"
            } else {
                "MMMM yyyy"
            }

            Text(
                text = SimpleDateFormat(pattern, Locale.getDefault()).format(currentMonthCalendar.time)
                    .uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                val newCalendar = currentMonthCalendar.clone() as Calendar
                newCalendar.add(Calendar.MONTH, 1)
                currentMonthCalendar = newCalendar
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Mes Siguiente", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.size(16.dp))

        // Días de la semana (Cabecera)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            getLocalizedDayInitials().forEach { day ->
                Text(
                    text = day,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Días del mes
        val days = generateDaysInMonth(currentMonthCalendar)
        val reservationCount = reservations.groupingBy { it }.eachCount() // 🔹 Contar reservas por fecha

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days) { day ->
                val dayCalendar = Calendar.getInstance()
                dayCalendar.timeInMillis = day

                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(dayCalendar.time)
                val isToday = day == today // Assuming normalized midnights
                val isBlocked = blockedDates.contains(formattedDate)
                val reservationCountForDay = reservationCount[formattedDate] ?: 0
                val isMarked = reservations.contains(formattedDate)
                val isMaxReservations = reservationCountForDay >= 3 // ❌ Si hay 3 reservas, no se puede seleccionar
                val isSelected = day == selectedDate
                val isPastDay = day < today

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .clickable(enabled = canUserInteract && !isBlocked && !isPastDay && !isMaxReservations) {
                            selectedDate = day
                            onDateSelected(formattedDate)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // 📌 **Círculos para distintos estados**
                    val circles = mutableListOf<@Composable () -> Unit>()

                    // 🔴 **Bloqueado (Rojo)**
                    if (isBlocked) {
                        circles.add {
                            CircleBorder(color = Color.Red, strokeWidth = 3.dp, size = 44.dp)
                        }
                    }

                    // 🔵 **Reservas individuales (Azul)**
                    if (isMarked) {
                        circles.add {
                            CircleBorder(color = Color.Blue, strokeWidth = 3.dp, size = if (isToday) 38.dp else 44.dp)
                        }
                    }

                    // ❌ **Demasiadas reservas (Gris oscuro)**
                    if (isMaxReservations) {
                        circles.add {
                            CircleBorder(color = Color.Gray, strokeWidth = 3.dp, size = 38.dp)
                        }
                    }

                    // ⚪ **Hoy (Blanco)**
                    if (isToday) {
                        circles.add {
                            CircleBorder(color = Color.White, strokeWidth = 2.dp, size = 48.dp)
                        }
                    }

                    // 🔹 **Seleccionado (Cyan)**
                    if (isSelected && canUserInteract) {
                        circles.add {
                            CircleBorder(color = Color.Cyan, strokeWidth = 2.dp, size = 48.dp)
                        }
                    }

                    // 📌 **Dibujar todos los círculos**
                    circles.forEach { it() }

                    // 📅 **Número del día**
                    Text(
                        text = dayCalendar.get(Calendar.DAY_OF_MONTH).toString(),
                        color = if (isPastDay || isMaxReservations) Color.Gray else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }


}

@Composable
fun CircleBorder(color: Color, strokeWidth: Dp, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(strokeWidth, color, CircleShape)
    )
}

// ✅ **Genera los días del mes correctamente**
fun generateDaysInMonth(currentMonth: Calendar): List<Long> {
    val days = mutableListOf<Long>()

    val calendar = currentMonth.clone() as Calendar
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val firstDayOfWeek = calendar.firstDayOfWeek
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    val dayOffset = (dayOfWeek - firstDayOfWeek + 7) % 7
    calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)

    // 6 rows × 7 columns = 42 days
    repeat(42) {
        days.add(calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    return days
}

fun getLocalizedDayInitials(): List<String> {
    val locale = Locale.getDefault()
    val shortWeekdays = DateFormatSymbols(locale).shortWeekdays
    return shortWeekdays
        .filter { it.isNotEmpty() }
        .map { it.first().toString() }
}
