import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.MIGAndroidSDKScreen
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme
import compose.icons.FeatherIcons
import compose.icons.feathericons.XCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    reservation: Reservation,
    onDismiss: () -> Unit
) {
    MIGAndroidSDKTheme {
            val viewModel: ReservationDetailViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReservationDetailViewModel(reservation) as T
                }
            })

            val isFlipped by viewModel.isFlipped.collectAsState()
            val rotationY by viewModel.rotationY.collectAsState()

            val animatedRotation by animateFloatAsState(targetValue = rotationY)

            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val bottomSheetState = rememberModalBottomSheetState()

            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = bottomSheetState,
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Black
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // 🔙 **Botón de Cerrar**
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(FeatherIcons.XCircle, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }

                    // 📌 **Título**
                    Text(
                        text = "MADRID IN GAME",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 📌 **Tarjeta Giratoria**
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures { _, dragAmount ->
                                    if (dragAmount > 50) {
                                        viewModel.flipCard()
                                    }
                                }
                            }
                    ) {
                        if (isFlipped) {
                            ReservationRulesCard()
                        } else {
                            ReservationQrCard(viewModel)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 📌 **Botón de Reglas**
                    Button(
                        onClick = { viewModel.flipCard() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                        modifier = Modifier.align(Alignment.CenterHorizontally).height(60.dp)
                    ) {
                        val title = when {
                            isFlipped -> "DETALLES"
                            else -> "NORMAS DE USO"
                        }
                        Text(title, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }

// 📌 **Tarjeta del Código QR**
@Composable
fun ReservationQrCard(viewModel: ReservationDetailViewModel) {
    val reservation by viewModel.reservation.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(800.dp)
            .background(
                Brush.linearGradient(colors = listOf(Color.Magenta, Color.Red)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("RESERVA CONFIRMADA", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 5.dp))
        Text("LOCALIZACIÓN: ${viewModel.getReservationLocation().uppercase()}", color = Color.White.copy(0.7f) ,fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 5.dp))
        Text("PLATAFORMA: ${viewModel.getReservationConsole().uppercase()}", color = Color.White.copy(0.7f) ,fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 5.dp))
        Text("FECHA: ${viewModel.getFormattedDate()}", color = Color.White.copy(0.7f) ,fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 5.dp))
        Text("HORAS: ${viewModel.getFormattedTimes()}", color = Color.White.copy(0.7f) ,fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 5.dp))

        Spacer(modifier = Modifier.height(20.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://webesports.madridingame.es/cms/assets/${reservation.qrImage ?: "1ff27f6e-a8ef-44f4-b21c-323e87c543bd"}")
                .crossfade(true)
                .build(),
            contentDescription = "Código QR",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(200.dp)
                .background(Color.White, shape = RoundedCornerShape(10.dp))
                .padding(16.dp)
        )
    }
}

// 📌 **Tarjeta de Normas de Uso**
@Composable
fun ReservationRulesCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(800.dp)
            .background(
                Brush.linearGradient(colors = listOf(Color.Magenta, Color.Red)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "NORMAS DE USO",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://webesports.madridingame.es/_next/static/media/reserve-rules.e49650ad.png")
                .crossfade(true)
                .build(),
            contentDescription = "Normas de Uso",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(200.dp)
                .background(Color.White, shape = RoundedCornerShape(10.dp))
                .padding(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationBottomSheet(
    reservation: Reservation,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true) // Se abre completamente

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState, // Estado para control de expansión
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) {
        ReservationDetailScreen(
            reservation = reservation,
            onDismiss = onDismiss
        )
    }
}