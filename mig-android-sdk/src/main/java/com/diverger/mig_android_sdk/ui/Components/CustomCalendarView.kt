package com.diverger.mig_android_sdk.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomCalendarView(
    canUserInteract: Boolean,
    markedDates: List<String>,
    blockedDates: List<String>,
    onDateSelected: (String) -> Unit
) {
    val viewModel = remember { CustomCalendarViewModel() }

    Column(modifier = Modifier.padding(16.dp)) {
        bannerSelectMonth(viewModel)
        calendarTitleDays(viewModel)
        calendarNumberDays(viewModel, canUserInteract, markedDates, blockedDates, onDateSelected)
    }
}

// ----------------------- BANNER MESES -----------------------
@Composable
private fun bannerSelectMonth(viewModel: CustomCalendarViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { viewModel.changeMonth(-1) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Mes Anterior",
                tint = Color.White
            )
        }
        Text(
            text = viewModel.monthAndYearString(viewModel.currentDate).uppercase(),
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        IconButton(onClick = { viewModel.changeMonth(1) }) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Mes Siguiente",
                tint = Color.White
            )
        }
    }
}

// ----------------------- NOMBRES DE DÍAS -----------------------
@Composable
private fun calendarTitleDays(viewModel: CustomCalendarViewModel) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        viewModel.daysOfWeek.forEach { day ->
            Text(
                text = day,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ----------------------- DÍAS DEL MES -----------------------
@Composable
private fun calendarNumberDays(
    viewModel: CustomCalendarViewModel,
    canUserInteract: Boolean,
    markedDates: List<String>,
    blockedDates: List<String>,
    onDateSelected: (String) -> Unit
) {
    val daysInMonth = viewModel.generateDaysInMonth(viewModel.currentDate)
    val rows = daysInMonth.chunked(7) // Agrupar en filas de 7 días

    Column {
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                week.forEach { date ->
                    val formattedDate = viewModel.dateFormatter.format(date)

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    viewModel.isToday(date) -> Color.White
                                    //markedDates.contains(formattedDate) -> Color.LightGray
                                    blockedDates.contains(formattedDate) -> Color.Red
                                    viewModel.selectedDate == date -> Color.Cyan
                                    else -> Color.Transparent
                                }
                            )
                            .clickable {
                                if (canUserInteract && !blockedDates.contains(formattedDate)) {
                                    viewModel.selectedDate = date
                                    onDateSelected(formattedDate)
                                    Log.d("CustomCalendarView", "Fecha seleccionada: $formattedDate")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.dateText(date),
                            color = if (blockedDates.contains(formattedDate)) Color.Red else Color.White
                        )
                    }
                }
            }
        }
    }
}

// ----------------------- VIEWMODEL -----------------------
class CustomCalendarViewModel {
    private val calendar = Calendar.getInstance()
    var currentDate by mutableStateOf(calendar.time)
    var selectedDate by mutableStateOf<Date?>(null)

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val daysOfWeek = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    fun monthAndYearString(date: Date): String {
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    fun changeMonth(offset: Int) {
        calendar.time = currentDate
        calendar.add(Calendar.MONTH, offset)
        currentDate = calendar.time
    }

    fun generateDaysInMonth(date: Date): List<Date> {
        val days = mutableListOf<Date>()
        val monthCalendar = Calendar.getInstance().apply { time = date }

        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 2
        if (firstDayOfMonth < 0) monthCalendar.add(Calendar.DATE, -7) // Ajustar para empezar en Lunes

        while (days.size < 42) { // Máximo 6 filas * 7 días
            days.add(monthCalendar.time)
            monthCalendar.add(Calendar.DATE, 1)
        }
        return days
    }

    fun dateText(date: Date): String = SimpleDateFormat("d", Locale.getDefault()).format(date)

    fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val dayCalendar = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == dayCalendar.get(Calendar.DAY_OF_YEAR)
    }
}
