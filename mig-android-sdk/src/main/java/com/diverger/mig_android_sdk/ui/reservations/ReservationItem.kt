package com.diverger.mig_android_sdk.ui.reservations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.ui.competitions.formatDate
import com.diverger.mig_android_sdk.ui.competitions.formatDateFromShort

enum class IndividualReservationsCellOptions {
    SEE_RESERVATION,
    CANCEL_RESERVATION
}

@Composable
fun ReservationItem(reservation: Reservation, onReservationPressed: (IndividualReservationsCellOptions, Reservation) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de la reserva
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Reserva",
                tint = Color.Cyan,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Información de la reserva
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reserva - ${reservation.slot.space}",
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = "Fecha: ${formatDateFromShort(reservation.date)}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "Horas: ${reservation.times.joinToString { it.time }}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            // Botones de acción
            Column(horizontalAlignment = Alignment.End) {
                /*TextButton(onClick = { onReservationPressed(IndividualReservationsCellOptions.SEE_RESERVATION, reservation) }) {
                    Text("Ver reserva", color = Color.Cyan, fontSize = 14.sp)
                }*/
                TextButton(onClick = { onReservationPressed(IndividualReservationsCellOptions.CANCEL_RESERVATION, reservation) }) {
                    Text("Cancelar", color = Color.Red, fontSize = 14.sp)
                }
            }
        }
    }
}
