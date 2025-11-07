package com.diverger.mig_android_sdk.ui.reservations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.ui.competitions.formatDateFromShort
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.Eye
import compose.icons.feathericons.MinusCircle
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Desktop
import compose.icons.fontawesomeicons.solid.Gamepad
import compose.icons.fontawesomeicons.solid.Keyboard
import compose.icons.fontawesomeicons.solid.Laptop
import compose.icons.fontawesomeicons.solid.MobileAlt

enum class IndividualReservationsCellOptions {
    SEE_RESERVATION,
    CANCEL_RESERVATION
}

@Composable
fun ReservationItem(reservation: Reservation, onReservationPressed: (IndividualReservationsCellOptions, Reservation) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (reservation.slot.space.translations.first().device.lowercase()) {
                        "pc", "desktop" -> FontAwesomeIcons.Solid.Desktop
                        "playstation", "ps5", "ps4", "console" -> FontAwesomeIcons.Solid.Gamepad
                        "xbox", "xbox series x", "xbox one" -> FontAwesomeIcons.Solid.Gamepad
                        "nintendo switch", "switch" -> FontAwesomeIcons.Solid.Gamepad
                        "laptop" -> FontAwesomeIcons.Solid.Laptop
                        "mobile", "tablet", "ipad", "smartphone" -> FontAwesomeIcons.Solid.MobileAlt
                        "keyboard" -> FontAwesomeIcons.Solid.Keyboard
                        else -> FontAwesomeIcons.Solid.Gamepad // Default
                    },
                    contentDescription = "Reserva en ${reservation.slot.space}",
                    tint = Color.Cyan,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Información de la reserva
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            R.string.booking_info_slot,
                            reservation.slot.space.translations.first().device.uppercase()
                        ),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    Text(
                        text = formatDateFromShort(reservation.date),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    Text(
                        text = reservation.times.joinToString { it.time },
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                    )
                }

                // Botones de acción
                Column(horizontalAlignment = Alignment.End) {
                    /*TextButton(onClick = { onReservationPressed(IndividualReservationsCellOptions.SEE_RESERVATION, reservation) }) {
                        Text("Ver reserva", color = Color.Cyan, fontSize = 14.sp)
                    }*/
                    IconButton(
                        onClick = { onReservationPressed(IndividualReservationsCellOptions.CANCEL_RESERVATION, reservation) }
                    ) {
                        Icon(
                            imageVector = FeatherIcons.MinusCircle,
                            contentDescription = "Cancelar reserva",
                            tint = Color.Red
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(imageVector = FeatherIcons.Eye,
                    contentDescription = "Ver Reserva",
                    tint = Color.Cyan,
                    modifier = Modifier.size(18.dp))
                TextButton(onClick = { onReservationPressed(IndividualReservationsCellOptions.SEE_RESERVATION, reservation) }) {
                    Text("VER RESERVA", color = Color.Cyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
