import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.ui.StringResourcesProvider
import com.diverger.mig_android_sdk.ui.UnifiedReservationScreen
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import compose.icons.FeatherIcons
import compose.icons.feathericons.XCircle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    reservation: Reservation,
    onDismiss: () -> Unit
) {
    MIGAndroidSDKTheme {

        val context = LocalContext.current

        val viewModel: ReservationDetailViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReservationDetailViewModel(
                        stringResourcesProvider = StringResourcesProvider(context),
                        reservation = reservation
                    ) as T
                }
            }
        )


            val isFlipped by viewModel.isFlipped.collectAsState()
            val rotationY by viewModel.rotationY.collectAsState()

            val animatedRotation by animateFloatAsState(targetValue = rotationY)

            val scope = rememberCoroutineScope()
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = sheetState,
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
                            Icon(FeatherIcons.XCircle, contentDescription = stringResource(R.string.close_label), tint = Color.White)
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
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .height(60.dp)
                    ) {
                        val title = when {
                            isFlipped -> stringResource(R.string.details_label).uppercase()
                            else -> stringResource(R.string.terms_of_use_label).uppercase()
                        }
                        Text(title, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }

@Composable
fun ReservationQrCard(viewModel: ReservationDetailViewModel) {
    val qrText = viewModel.getQRValue().orEmpty()

    // Genera el Bitmap del QR solo cuando cambia qrText:
    val qrBitmap: Bitmap? = remember(qrText) {
        runCatching {
            val size = 300
            val bitMatrix = QRCodeWriter().encode(qrText, BarcodeFormat.QR_CODE, size, size)
            Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bmp ->
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                    }
                }
            }
        }.getOrNull()
    }

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
        Text(
            stringResource(R.string.booking_confirmed).uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            stringResource(R.string.booking_details_location, viewModel.getReservationLocation().uppercase()),
            color = Color.White.copy(0.7f),
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            stringResource(R.string.booking_details_platform, viewModel.getReservationConsole().uppercase()),
            color = Color.White.copy(0.7f),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            stringResource(R.string.booking_details_date, viewModel.getFormattedDate().uppercase()),
            color = Color.White.copy(0.7f),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            stringResource(R.string.booking_details_time, viewModel.getFormattedTimes().uppercase()),
            color = Color.White.copy(0.7f),
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 5.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mostramos el QR generado
        qrBitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Código QR de la reserva",
                modifier = Modifier
                    .size(250.dp)
                    .background(Color.White, shape = RoundedCornerShape(10.dp))
                    .padding(16.dp)
            )
        }
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
        Text(text = stringResource(R.string.terms_of_use_label).uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.height(20.dp))

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("https://premig.randomkesports.com/_next/static/media/reserve-rules.e49650ad.png")
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.terms_of_use_label),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(0.dp))
                .background(Color.Transparent)
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
//        ReservationDetailScreen(
//            reservation = reservation,
//            onDismiss = onDismiss
//        )

        UnifiedReservationScreen(
            isTeamReservation = false,
            reservation = reservation,
            onDismiss = onDismiss
        )

    }
}