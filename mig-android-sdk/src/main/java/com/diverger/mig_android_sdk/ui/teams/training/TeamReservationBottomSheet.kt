package com.diverger.mig_android_sdk.ui.teams.training

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.PlayerModel
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.diverger.mig_android_sdk.ui.competitions.formatDateFromShort
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.User
import compose.icons.feathericons.XCircle
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.UserCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamReservationBottomSheet(
    training: EventModel,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isFlipped = remember { mutableStateOf(false) }
    val rotationY by animateFloatAsState(targetValue = if (isFlipped.value) 180f else 0f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔙 **Botón de Cerrar**
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onDismiss) {
                    Icon(FeatherIcons.XCircle, contentDescription = "Cerrar", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Madrid in Game", color = Color.White, style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(10.dp))

            // 📌 **Tarjeta QR**
            if (training.type == "centre") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(
                            Brush.linearGradient(colors = listOf(Color.Magenta, Color.Red)),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { _, dragAmount ->
                                if (dragAmount > 50) {
                                    isFlipped.value = !isFlipped.value
                                }
                            }
                        }
                ) {
                    if (isFlipped.value) {
                        ReservationRulesCard()
                    } else {
                        ReservationQrCard(training)
                    }
                }
            } else {
                ReservationQrCard(training)
            }

            /*Spacer(modifier = Modifier.height(16.dp))

            // 📌 **Lista de jugadores**
            PlayersCarousel(training)*/

            Spacer(modifier = Modifier.height(16.dp))

            // 📌 **Botón de Normas SOLO en Presencial**
            if (training.type == "centre") {
                Button(
                    onClick = { isFlipped.value = !isFlipped.value },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                    modifier = Modifier.align(Alignment.CenterHorizontally).height(60.dp)
                ) {
                    val title = if (isFlipped.value) "DETALLES" else "NORMAS DE USO"
                    Text(title, color = Color.Black)
                }
            }
        }
    }
}

// 📌 **Tarjeta del Código QR**
@Composable
private fun ReservationQrCard(training: EventModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(colors = listOf(Color.Magenta, Color.Red)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("RESERVA CONFIRMADA", color = Color.White, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(10.dp))

        Text("Localización: ${if (training.type == "virtual") "Virtual" else "Centro"}", color = Color.White.copy(0.8f) ,fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 5.dp))
        Text("Fecha: ${formatDateFromShort(training.startDate)}", color = Color.White.copy(0.8f) ,fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 5.dp))
        Text("Hora: ${training.time.slice(0..4)}", color = Color.White.copy(0.8f) ,fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 5.dp))

        Spacer(modifier = Modifier.height(14.dp))

        if (training.type == "virtual") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.QrCode, contentDescription = "QR", tint = Color.White, modifier = Modifier.size(100.dp))
                Text("No se requiere QR", color = Color.White)
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${EnvironmentManager.getAssetsBaseUrl()}${training.reserves?.firstOrNull()?.qrImage ?: ""}")
                    .crossfade(true)
                    .build(),
                contentDescription = "Código QR",
                placeholder = rememberVectorPainter(Icons.Default.QrCode),
                error       = rememberVectorPainter(Icons.Default.BrokenImage),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White, shape = RoundedCornerShape(10.dp))
                    .padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📌 **Lista de jugadores**
        PlayersCarousel(training)
    }
}

// 📌 **Tarjeta de Normas de Uso**
@Composable
private fun ReservationRulesCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(colors = listOf(Color.Magenta, Color.Red)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("NORMAS DE USO", color = Color.White, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://premig.randomkesports.com/_next/static/media/reserve-rules.e49650ad.png")
                .crossfade(true)
                .build(),
            contentDescription = "Normas de Uso",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        )
    }
}

// 📌 **Lista de jugadores**
@Composable
private fun PlayersCarousel(training: EventModel) {
    Column(
        modifier = Modifier.
        fillMaxWidth()
    ) {
        Text("Players", color = Color.White, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow {
            items(training.players ?: emptyList()) { player ->
                PlayerAvatar(player.userId!!)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

// 📌 **Avatar del jugador con fallback**
@Composable
private fun PlayerAvatar(player: PlayerModel) {
    Box(modifier = Modifier.size(50.dp)) {
        if (player.avatar != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("${EnvironmentManager.getAssetsBaseUrl()}${player.avatar}")
                .crossfade(true)
                .build(),
            contentDescription = "Avatar",
            modifier = Modifier.size(50.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
            /*error = {
                Icon(FeatherIcons.User, contentDescription = "Avatar", tint = Color.White, modifier = Modifier.size(35.dp))
            }*/
        ) } else {
            PlaceholderIcon()
    }
    }
}

// 🔹 **Fallback a Icono si no hay imagen**
@Composable
private fun PlaceholderIcon() {
    Icon(
        imageVector = FontAwesomeIcons.Solid.UserCircle,
        contentDescription = "Usuario sin avatar",
        tint = Color.White,
        modifier = Modifier.size(35.dp)
    )
}