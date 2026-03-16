package com.diverger.mig_android_sdk.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.EventModel

@Composable
fun TrainingsList(
    trainings: List<EventModel>,
    onAction: (EventModel, TrainingCellOption) -> Unit
) {
    if (trainings.isEmpty()) {
        Text(
            stringResource(R.string.no_scheduled_trainings),
            color = Color.Gray,
            style = MaterialTheme.typography.labelMedium
        )
    } else {
        LazyColumn {
            item {
                Text(
                    text = stringResource(R.string.team_reservations).uppercase(),
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(trainings) { training ->
                TrainingCell(training) { action ->
                    onAction(training, action)
                }
            }
        }
    }
}