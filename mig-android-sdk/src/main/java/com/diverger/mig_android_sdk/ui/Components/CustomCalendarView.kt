import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomCalendarView(
    canUserInteract: Boolean,
    reservations: List<String>,  // 📌 Fechas con reservas individuales (azul)
    blockedDates: List<String>,  // 📌 Fechas bloqueadas (rojo)
    onDateSelected: (String) -> Unit
) {
    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        // Selector de mes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Mes Anterior", tint = Color.White)
            }
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Mes Siguiente", tint = Color.White)
            }
        }

        // Días de la semana (Cabecera)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM").forEach { day ->
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
        val days = generateDaysInMonth(currentMonth)
        val reservationCount = reservations.groupingBy { it }.eachCount() // 🔹 Contar reservas por fecha

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days) { day ->
                val formattedDate = day.format(DateTimeFormatter.ISO_DATE)
                val isToday = day == today
                val isBlocked = blockedDates.contains(formattedDate)
                val reservationCountForDay = reservationCount[formattedDate] ?: 0
                val isMarked = reservations.contains(formattedDate)
                val isMaxReservations = reservationCountForDay >= 3 // ❌ Si hay 3 reservas, no se puede seleccionar
                val isSelected = day == selectedDate
                val isPastDay = day.isBefore(today)

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
                            CircleBorder(color = Color.Blue, strokeWidth = 3.dp, size = 38.dp)
                        }
                    }

                    // ❌ **Demasiadas reservas (Gris oscuro)**
                    if (isMaxReservations) {
                        circles.add {
                            CircleBorder(color = Color.Gray, strokeWidth = 3.dp, size = 38.dp)
                        }
                    }

                    // 🔹 **Seleccionado (Cyan)**
                    if (isSelected) {
                        circles.add {
                            CircleBorder(color = Color.Cyan, strokeWidth = 3.dp, size = 32.dp)
                        }
                    }

                    // ⚪ **Hoy (Blanco)**
                    if (isToday) {
                        circles.add {
                            CircleBorder(color = Color.White, strokeWidth = 2.dp, size = 48.dp)
                        }
                    }

                    // 📌 **Dibujar todos los círculos**
                    circles.forEach { it() }

                    // 📅 **Número del día**
                    Text(
                        text = day.dayOfMonth.toString(),
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
@RequiresApi(Build.VERSION_CODES.O)
fun generateDaysInMonth(month: YearMonth): List<LocalDate> {
    val firstDayOfMonth = month.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Lunes es 0
    val days = mutableListOf<LocalDate>()

    // Ajustar para que la primera semana siempre empiece en lunes
    val startDay = firstDayOfMonth.minusDays(firstDayOfWeek.toLong())
    repeat(42) { // 6 filas x 7 días = 42 espacios
        days.add(startDay.plusDays(it.toLong()))
    }

    return days
}
