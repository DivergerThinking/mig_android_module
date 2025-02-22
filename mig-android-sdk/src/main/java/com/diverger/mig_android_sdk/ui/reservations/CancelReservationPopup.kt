package com.diverger.mig_android_sdk.ui.reservations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diverger.mig_android_sdk.data.Reservation

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
                    Text("Confirmar", fontSize = 16.sp, color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancelar", fontSize = 16.sp, color = Color.Gray)
                }
            },
            title = { Text("CANCELAR RESERVA", fontSize = 20.sp, color = Color.DarkGray) },
            text = {
                Text(
                    "¿Quieres cancelar la reserva del día ${reservation.date}?",
                    fontSize = 16.sp,
                    color = Color.DarkGray.copy(alpha = 0.8f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color.DarkGray, RoundedCornerShape(12.dp))
        )
    }
}
