package com.diverger.mig_android_sdk.ui.components

import CustomCalendarView
import SelectTeamBottomSheet
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.UnsafeAsyncImage
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.support.EnvironmentManager
import compose.icons.FeatherIcons
import compose.icons.feathericons.XCircle

// 📅 **Componente de Calendario**
@Composable
fun CalendarComponent(
    isCalendarVisible: Boolean,
    onToggle: () -> Unit,
    reservations: List<String> = emptyList(),
    markedDates: List<String> = emptyList(),
    trainings: List<String> = emptyList(),
    teamLogo: String? = null
) {

    var showTeamSelection by remember { mutableStateOf(false) }
    val showLegend = remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clickable(onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically
        ) {
            teamLogo?.let { imageName ->
                UnsafeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("${EnvironmentManager.getAssetsBaseUrl()}${imageName}")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Team Image",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = { showTeamSelection = true })
                )
                Spacer(Modifier.width(10.dp))
            }

            Box(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.trainings_calendar),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .drawBehind {
                            val strokeWidth = 4.dp.toPx()
                            val y = size.height + 8 - strokeWidth / 2
                            drawLine(
                                color = Color.Cyan,
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                )
            }
            TextButton(onClick = { showLegend.value = true }, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    stringResource(R.string.legend),
                    color = Color.Cyan,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
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
                reservations = reservations,
                trainings = trainings
            )
        }

        if (showLegend.value) {
            Dialog(onDismissRequest = { showLegend.value = false }) {
                LegendDialog(onDismiss = { showLegend.value = false })
            }
        }

        if (showTeamSelection) {
            UserManager.getUser()?.let { user ->
                SelectTeamBottomSheet(
                    user = user ,
                    onTeamSelected = { team ->
                        UserManager.setSelectedTeam(team)
                        showTeamSelection = false
                    },
                    onDismiss = { showTeamSelection = false }
                )
            }
        }
    }
}

@Composable
fun LegendDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color(0xFF232323), shape = RoundedCornerShape(30.dp))
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.legend),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = FeatherIcons.XCircle,
                        contentDescription = "Close Legend",
                        tint = Color.LightGray
                    )
                }
            }
            LegendItem(
                color = Color.White,
                label = stringResource(R.string.current_day).uppercase()
            )
            LegendItem(
                color = Color.Blue,
                label = stringResource(R.string.individual_booking).uppercase()
            )
            LegendItem(
                color = Color(0xFF2C5A90),
                label = stringResource(R.string.team_booking).uppercase()
            )
            LegendItem(
                color = Color.Red,
                label = stringResource(R.string.unavailable_day).uppercase()
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        modifier = Modifier.padding(vertical = 2.dp).padding(start = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(color = color, shape = CircleShape)
        )
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 16.sp
        )
    }
}
