package com.diverger.mig_android_sdk.ui.reservations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.support.dateFromString
import com.diverger.mig_android_sdk.support.toUIDateString

@Composable
fun CancelReservationPopup(
    reservation: Reservation?,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirmCancel: (Int) -> Unit
) {
    if (isVisible && reservation != null) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {
                TextButton(
                    onClick = {
                        reservation.id?.let { onConfirmCancel(it) }
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.yes_label), fontSize = 16.sp, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text(stringResource(R.string.no_label), fontSize = 16.sp, color = Color.Gray)
                }
            },
            title = { Text(stringResource(R.string.cancel_booking).uppercase(), fontSize = 20.sp, color = Color.DarkGray) },
            text = {
                val date = dateFromString(reservation.date)?.toUIDateString()
                if (date != null) {
                    Text(
                        stringResource(R.string.confirm_booking_cancellation_date, date, reservation.times.first().time),
                        fontSize = 16.sp,
                        color = Color.DarkGray.copy(alpha = 0.8f)
                    )
                } else {
                    Text(
                        stringResource(R.string.confirm_booking_cancellation),
                        fontSize = 16.sp,
                        color = Color.DarkGray.copy(alpha = 0.8f)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.DarkGray, RoundedCornerShape(12.dp))
        )
    }
}
