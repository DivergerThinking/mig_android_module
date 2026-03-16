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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Custom calendar matching iOS CustomCalendarView.
 *
 * @param canUserInteract  Whether the user can tap to select dates.
 * @param reservations     Dates with individual reservations (blue circle) — "yyyy-MM-dd".
 * @param trainings        Dates with team trainings (dark blue circle) — "yyyy-MM-dd".
 * @param blockedDates     Blocked dates (red circle) — "yyyy-MM-dd".
 * @param initialSelectedDate  Optional pre-selected date in millis. Calendar navigates to its month.
 * @param onDateSelected   Callback with the selected date as "yyyy-MM-dd".
 */
@Composable
fun CustomCalendarView(
    canUserInteract: Boolean,
    reservations: List<String> = emptyList(),
    trainings: List<String> = emptyList(),
    blockedDates: List<String> = emptyList(),
    initialSelectedDate: Long? = null,
    onDateSelected: (String) -> Unit
) {
    // ── Today (normalized to midnight) ───────────────────────
    val todayCalendar = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val today = todayCalendar.timeInMillis

    // ── Selected date state ──────────────────────────────────
    // Like iOS: if initialSelectedDate is provided, pre-select it
    var selectedDate by remember {
        mutableLongStateOf(initialSelectedDate?.let { normalizeToMidnight(it) } ?: -1L)
    }

    // ── Current month — navigate to initialSelectedDate's month if provided
    var currentMonthCalendar by remember {
        mutableStateOf(
            if (initialSelectedDate != null) {
                Calendar.getInstance().apply { timeInMillis = initialSelectedDate }
            } else {
                Calendar.getInstance()
            }
        )
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        // ── Month selector ───────────────────────────────────
        MonthSelector(
            currentMonthCalendar = currentMonthCalendar,
            onPreviousMonth = {
                currentMonthCalendar = (currentMonthCalendar.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                }
            },
            onNextMonth = {
                currentMonthCalendar = (currentMonthCalendar.clone() as Calendar).apply {
                    add(Calendar.MONTH, 1)
                }
            }
        )

        Spacer(modifier = Modifier.size(16.dp))

        // ── Day-of-week headers ──────────────────────────────
        DayOfWeekHeaders()

        // ── Days grid ────────────────────────────────────────
        val days = generateDaysInMonth(currentMonthCalendar)
        val reservationCount = reservations.groupingBy { it }.eachCount()

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days) { dayMillis ->
                val normalizedDay = normalizeToMidnight(dayMillis)
                val dayCalendar = Calendar.getInstance().apply { timeInMillis = normalizedDay }
                val formattedDate = dateFormat.format(dayCalendar.time)

                val isToday = normalizedDay == today
                val isBlocked = blockedDates.contains(formattedDate)
                val isMarkedReservation = reservations.contains(formattedDate)
                val isMarkedTraining = trainings.contains(formattedDate)
                val reservationCountForDay = reservationCount[formattedDate] ?: 0
                val isMaxReservations = reservationCountForDay >= 3
                val isSelected = normalizedDay == selectedDate
                val isPastDay = normalizedDay < today

                // ── Day in current month check (like iOS isInCurrentMonth) ──
                val isInCurrentMonth = dayCalendar.get(Calendar.MONTH) == currentMonthCalendar.get(Calendar.MONTH)
                        && dayCalendar.get(Calendar.YEAR) == currentMonthCalendar.get(Calendar.YEAR)

                DayCell(
                    dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH),
                    isToday = isToday,
                    isSelected = isSelected && canUserInteract,
                    isBlocked = isBlocked,
                    isMarkedReservation = isMarkedReservation,
                    isMarkedTraining = isMarkedTraining,
                    isMaxReservations = isMaxReservations,
                    isPastDay = isPastDay,
                    isInCurrentMonth = isInCurrentMonth,
                    canInteract = canUserInteract && !isBlocked && !isPastDay && !isMaxReservations,
                    onClick = {
                        selectedDate = normalizedDay
                        onDateSelected(formattedDate)
                    }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Month selector (iOS bannerSelectMonth)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun MonthSelector(
    currentMonthCalendar: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val now = Calendar.getInstance()
    val pattern = if (currentMonthCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
        "MMMM"
    } else {
        "MMMM yyyy"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Mes Anterior", tint = Color.White)
        }
        Text(
            text = SimpleDateFormat(pattern, Locale.getDefault())
                .format(currentMonthCalendar.time)
                .uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Mes Siguiente", tint = Color.White)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Day-of-week headers (iOS calendarTitleDays)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DayOfWeekHeaders() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        getLocalizedDayInitials().forEach { day ->
            Text(
                text = day,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Individual day cell (iOS createTodayCircle + createMarkedDates
//   + createEnabledDayCircle + createDisabledDayText)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun DayCell(
    dayOfMonth: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isBlocked: Boolean,
    isMarkedReservation: Boolean,
    isMarkedTraining: Boolean,
    isMaxReservations: Boolean,
    isPastDay: Boolean,
    isInCurrentMonth: Boolean,
    canInteract: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(enabled = canInteract) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // ── Marker circles (outermost first, like iOS createMarkedDates) ──
        // iOS draws them from largest to smallest: blocked → reservation → training
        var circleSize = 44.dp

        if (isToday) {
            // iOS: Circle().stroke(Color.white, lineWidth: 3)
            CircleBorder(color = Color.White, strokeWidth = 3.dp, size = 47.dp)
        }

        if (isBlocked) {
            CircleBorder(color = Color.Red, strokeWidth = 3.dp, size = circleSize)
            circleSize -= 6.dp
        }

        if (isMarkedReservation) {
            CircleBorder(color = Color.Blue, strokeWidth = 3.dp, size = circleSize)
            circleSize -= 6.dp
        }

        if (isMarkedTraining) {
            CircleBorder(color = Color(0xFF2C5A90), strokeWidth = 3.dp, size = circleSize)
            circleSize -= 6.dp
        }

        if (isMaxReservations) {
            CircleBorder(color = Color.Gray, strokeWidth = 3.dp, size = 38.dp)
        }

        // ── Selected state: cyan filled circle (iOS createEnabledDayCircle) ──
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Cyan)
            )
        }

        // ── Day number ──
        val textColor = when {
            !isInCurrentMonth -> Color.DarkGray
            isPastDay || isMaxReservations -> Color.Gray
            isSelected -> Color.Black   // Black text on cyan background (like iOS)
            else -> Color.White
        }

        Text(
            text = dayOfMonth.toString(),
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Circle border helper
// ═══════════════════════════════════════════════════════════════

@Composable
fun CircleBorder(color: Color, strokeWidth: Dp, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(strokeWidth, color, CircleShape)
    )
}

// ═══════════════════════════════════════════════════════════════
// Generate days in month (matches iOS generateDaysInMonth)
// ═══════════════════════════════════════════════════════════════

fun generateDaysInMonth(currentMonth: Calendar): List<Long> {
    val days = mutableListOf<Long>()
    val locale = Locale.getDefault()
    val firstDayOfWeek = Calendar.getInstance(locale).firstDayOfWeek
    val calendar = (currentMonth.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val dayOffset = (dayOfWeek - firstDayOfWeek + 7) % 7
    calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)

    // 6 rows × 7 columns = 42 days (like iOS)
    repeat(42) {
        days.add(calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    return days
}

// ═══════════════════════════════════════════════════════════════
// Localized day initials
// ═══════════════════════════════════════════════════════════════

fun getLocalizedDayInitials(): List<String> {
    val locale = Locale.getDefault()
    val symbols = DateFormatSymbols(locale).shortWeekdays // index 1=SUNDAY
    val firstDayOfWeek = Calendar.getInstance(locale).firstDayOfWeek
    val days = (0..6).map { ((firstDayOfWeek + it - 1) % 7) + 1 }
    return days.map { symbols[it].take(1) }
}

// ═══════════════════════════════════════════════════════════════
// Normalize millis to midnight
// ═══════════════════════════════════════════════════════════════

private fun normalizeToMidnight(millis: Long): Long {
    return Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}