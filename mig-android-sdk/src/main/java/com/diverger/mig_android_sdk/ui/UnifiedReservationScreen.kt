package com.diverger.mig_android_sdk.ui

import ReservationDetailViewModel
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.PlayerModel
import com.diverger.mig_android_sdk.data.Reservation
import com.diverger.mig_android_sdk.data.UnsafeAsyncImage
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.XCircle
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.UserCircle


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun UnifiedReservationScreen(
    isTeamReservation: Boolean,
    reservation: Reservation? = null,
    training: EventModel? = null,
    onDismiss: () -> Unit,
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
            // Botón de cerrar
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        FeatherIcons.XCircle,
                        contentDescription = stringResource(R.string.close_label),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "MADRID IN GAME",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tarjeta giratoria solo si es de tipo equipo y es tipo centro
            val showFlipCard = isTeamReservation && training?.type == "centre" || !isTeamReservation

            if (showFlipCard) {

                FlippableCard(
                    isFlipped = isFlipped.value,
                    onFlip = { isFlipped.value = !isFlipped.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    front = {
                        UnifiedQrCard(
                            isTeamReservation = isTeamReservation,
                            reservation = reservation,
                            training = training
                        )
                    },
                    back = { ReservationRulesCard() }
                )

            } else {
                // Si no es tarjeta giratoria, solo mostrar QR
                UnifiedQrCard(
                    isTeamReservation = isTeamReservation,
                    reservation = reservation,
                    training = training
                )
            }

            Spacer(Modifier.height(16.dp))

            // Lista de jugadores solo para entrenamiento tipo equipo
            if (isTeamReservation && training != null && (training.players?.size ?: 0) > 0) {
                PlayersCarousel(training)
                Spacer(Modifier.height(16.dp))
            }

            if (showFlipCard) {
                Button(
                    onClick = { isFlipped.value = !isFlipped.value },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(60.dp)
                ) {
                    AnimatedContent(
                        targetState = isFlipped.value,
                        transitionSpec = {
                            fadeIn() with fadeOut()
                        }
                    ) { flipped ->
                        val title = if (flipped)
                            stringResource(R.string.details_label).uppercase()
                        else
                            stringResource(R.string.terms_of_use_label).uppercase()

                        Text(title, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun UnifiedQrCard(
    isTeamReservation: Boolean,
    reservation: Reservation? = null,
    training: EventModel? = null,
) {

    val context = LocalContext.current.applicationContext

    if (isTeamReservation) {
        // Equipo - entrenamiento
        training ?: return


        val viewModel: ReservationDetailViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReservationDetailViewModel(
                        stringResourcesProvider = StringResourcesProvider(context),
                        training = training
                    ) as T
                }
            }
        )

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
            Text(
                stringResource(R.string.booking_confirmed),
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )

                    Spacer(modifier = Modifier.height(10.dp))

//            Text(
//                stringResource(
//                    R.string.booking_details_location,
//                    if (training.type == "virtual") stringResource(R.string.virtual_label) else stringResource(
//                        R.string.center_label
//                    )
//                ),
//                color = Color.White.copy(0.8f),
//                fontWeight = FontWeight.ExtraBold,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.padding(bottom = 5.dp)
//            )

                    Text(
                        stringResource(
                            R.string.booking_details_location,
                            viewModel.getReservationLocation().uppercase()
                        ),
                        color = Color.White.copy(0.7f),
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    Text(
                        stringResource(
                            R.string.booking_details_date,
                            viewModel.getFormattedDate().uppercase()
                        ),
                        color = Color.White.copy(0.7f),
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    Text(
                        stringResource(
                            R.string.booking_details_time,
                            viewModel.getFormattedTimes().uppercase()
                        ),
                        color = Color.White.copy(0.7f),
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

            if (training.type == "virtual") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = "QR",
                        tint = Color.White,
                        modifier = Modifier.size(100.dp)
                    )
                    Text(stringResource(R.string.no_qr_code_required), color = Color.White)
                }
            } else {
                UnsafeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("${EnvironmentManager.getAssetsBaseUrl()}${training.reserves?.firstOrNull()?.qrImage.orEmpty()}")
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.qr_code),
                    placeholder = rememberVectorPainter(Icons.Default.QrCode),
                    error = rememberVectorPainter(Icons.Default.BrokenImage),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, shape = RoundedCornerShape(10.dp))
                        .padding(16.dp)
                )
            }
        }
    } else {
        // Reserva estándar
        val reservation = reservation ?: return

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

        // QR bitmap generation
        val qrText = reservation.qrValue.orEmpty()
        val qrBitmap: Bitmap? = remember(qrText) {
            runCatching {
                val size = 300
                val bitMatrix = QRCodeWriter().encode(qrText, BarcodeFormat.QR_CODE, size, size)
                Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bmp ->
                    for (x in 0 until size) {
                        for (y in 0 until size) {
                            bmp.setPixel(
                                x,
                                y,
                                if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                            )
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
                stringResource(
                    R.string.booking_details_location,
                    viewModel.getReservationLocation().uppercase()
                ),
                color = Color.White.copy(0.7f),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(
                stringResource(
                    R.string.booking_details_platform,
                    viewModel.getReservationConsole().uppercase()
                ),
                color = Color.White.copy(0.7f),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(
                stringResource(
                    R.string.booking_details_date,
                    viewModel.getFormattedDate().uppercase()
                ),
                color = Color.White.copy(0.7f),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(
                stringResource(
                    R.string.booking_details_time,
                    viewModel.getFormattedTimes().uppercase()
                ),
                color = Color.White.copy(0.7f),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 5.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

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
}

@Composable
fun ReservationRulesCard() {
    // Única implementación, idéntica en ambos casos
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
        Text(
            stringResource(R.string.terms_of_use_label).uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        UnsafeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("${EnvironmentManager.getBaseServerUrl()}/_next/static/media/reserve-rules.e49650ad.png")
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.terms_of_use_label),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(0.dp))
                .background(Color.Transparent),
            error = rememberVectorPainter(Icons.Default.BrokenImage)
        )
    }
}

@Composable
fun PlayersCarousel(training: EventModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.players_label),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow {
            items(training.players ?: emptyList()) { player ->
                PlayerAvatar(player.userId!!)
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun PlayerAvatar(player: PlayerModel) {
    Box(modifier = Modifier.size(50.dp)) {
        if (player.avatar != null) {
            UnsafeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${EnvironmentManager.getAssetsBaseUrl()}${player.avatar}")
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                ,
                contentScale = ContentScale.Crop,
                placeholder = rememberVectorPainter(Icons.Rounded.Person),
                error= rememberVectorPainter(Icons.Default.BrokenImage)
            )
        } else {
            PlaceholderIcon()
        }
    }
}

@Composable
fun PlaceholderIcon() {
    Icon(
        imageVector = FontAwesomeIcons.Solid.UserCircle,
        contentDescription = stringResource(R.string.no_avatar_user),
        tint = Color.White,
        modifier = Modifier.size(35.dp)
    )
}

@Composable
fun FlippableCard(
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    val rotationYValue by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600)
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotationYValue
                cameraDistance = 8 * density
            }
            .background(
                Brush.linearGradient(colors = listOf(Color.Magenta, Color.Red)),
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) {
                        onFlip()
                    }
                }
            }
    ) {
        if (rotationYValue <= 90f) {
            Box { front() }
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) { back() }
        }
    }
}