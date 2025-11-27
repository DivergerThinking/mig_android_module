package com.diverger.mig_android_sdk.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.diverger.mig_android_sdk.R
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme


@Composable
fun NextTrainingsBanner(isTrainingsVisible: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.future_trainings),
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
        Icon(
            imageVector = if (isTrainingsVisible) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
            contentDescription = "Toggle Trainings",
            tint = Color.Cyan
        )
    }
}