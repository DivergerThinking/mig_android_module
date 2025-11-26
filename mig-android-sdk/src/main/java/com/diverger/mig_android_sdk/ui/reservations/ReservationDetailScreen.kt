package com.diverger.mig_android_sdk.ui.reservations

import ReservationDetailViewModel
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.diverger.mig_android_sdk.support.StringResourcesProvider
import com.diverger.mig_android_sdk.support.generateQrBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.XCircle
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.UserCircle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ReservationDetailScreen(
    isTeamReservation: Boolean,
    reservation: Reservation? = null,
    training: EventModel? = null,
    onDismiss: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isFlipped = remember { mutableStateOf(false) }

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

            val showFlipCard = isTeamReservation && training?.type == "centre" || !isTeamReservation

            if (showFlipCard) {
                val configuration = LocalConfiguration.current
                val screenHeightDp: Dp = configuration.screenHeightDp.dp
                val desiredHeight = screenHeightDp * 2 / 3

                FlippableCard(
                    isFlipped = isFlipped.value,
                    onFlip = { isFlipped.value = !isFlipped.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(desiredHeight),
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
                UnifiedQrCard(
                    isTeamReservation = isTeamReservation,
                    reservation = reservation,
                    training = training
                )
            }

            if (showFlipCard) {
                Button(
                    onClick = { isFlipped.value = !isFlipped.value },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.CenterHorizontally)
                        .height(60.dp)
                ) {
                    AnimatedContent(
                        targetState = isFlipped.value,
                        transitionSpec = { fadeIn() with fadeOut() }
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
private fun GradientReservationCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(colors = listOf(Color.Magenta, Color.Red)),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun ReservationHeader(
    title: String = stringResource(R.string.booking_confirmed),
) {
    Text(
        title.uppercase(),
        color = Color.White,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(bottom = 5.dp)
    )
}

@Composable
private fun ReservationDetailRow(text: String) {
    Text(
        text,
        color = Color.White,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 5.dp)
    )
}

@Composable
private fun RemoteQrImage(url: String, contentDescription: String) {

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Color.White, RoundedCornerShape(10.dp))
    ) {
        UnsafeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            placeholder = rememberVectorPainter(Icons.Default.QrCode),
            error = rememberVectorPainter(Icons.Default.BrokenImage),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun loadBitmapFromResource(@DrawableRes resId: Int): Bitmap? {
    val context = LocalContext.current
    return remember(resId) {
        runCatching {
            val drawable = context.getDrawable(resId) ?: return@runCatching null
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }.getOrNull()
    }
}

@Composable
fun LocalQrImageWithLogo(reservation: Reservation, @DrawableRes logoResId: Int) {
    val logoBitmap = loadBitmapFromResource(logoResId)

    LocalQrImage(
        reservation = reservation,
        logoBitmap = logoBitmap
    )
}

@Composable
private fun LocalQrImage(
    reservation: Reservation,
    qrSize: Int = 500,
    logoBitmap: Bitmap? = null // pass user logo bitmap here if available
) {
    val qrText = reservation.qrValue.orEmpty()

    val qrBitmap: Bitmap? = generateQrBitmap(qrText, qrSize, logoBitmap)

    qrBitmap?.let { bmp ->
        BitmapQrImage(
            bitmap = bmp,
            qrSize = qrSize,
            contentDescription = "Código QR de la reserva"
            )
    }
}

@Composable
private fun BitmapQrImage(bitmap: Bitmap, qrSize: Int, contentDescription: String) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(Color.White, RoundedCornerShape(10.dp))
            .size(qrSize.dp)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun UnifiedQrCard(
    isTeamReservation: Boolean,
    reservation: Reservation? = null,
    training: EventModel? = null,
) {
    val context = LocalContext.current.applicationContext

    val key = reservation?.id?.toString() ?: training?.id?.toString() ?: ""

    val viewModel: ReservationDetailViewModel = viewModel(
        key = key,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReservationDetailViewModel(
                    stringResourcesProvider = StringResourcesProvider(context),
                    training = if (isTeamReservation) training else null,
                    reservation = if (isTeamReservation) null else reservation
                ) as T
            }
        }
    )

    GradientReservationCard {
        ReservationHeader()

        Spacer(Modifier.height(10.dp))

        ReservationDetailRow(
            stringResource(
                R.string.booking_details_location,
                viewModel.getReservationLocation().uppercase()
            )
        )

        if (!isTeamReservation) {
            ReservationDetailRow(
                stringResource(
                    R.string.booking_details_platform,
                    viewModel.getReservationConsole().uppercase()
                )
            )
        }
        ReservationDetailRow(
            stringResource(
                R.string.booking_details_date,
                viewModel.getFormattedDate(LocalContext.current).uppercase()
            )
        )

        val time = viewModel.getFormattedTimes().uppercase()
        val stringResource =
            if (time.contains(",")) R.string.booking_details_time else R.string.booking_details_time_singular
        ReservationDetailRow(
            stringResource(
                stringResource,
                time
            )
        )


        Spacer(modifier = Modifier.height(14.dp))

        if (isTeamReservation) {
            if (training?.type == "virtual") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.QrCode,
                        contentDescription = "QR",
                        tint = Color.White,
                        modifier = Modifier.size(180.dp)
                    )
                    Text(stringResource(R.string.no_qr_code_required), color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            } else {
                RemoteQrImage(
                    url = "${EnvironmentManager.getAssetsBaseUrl()}${training?.reserves?.firstOrNull()?.qrImage.orEmpty()}",
                    contentDescription = stringResource(R.string.qr_code)
                )
            }
        } else {
            reservation?.let { reservation ->
                if (reservation.qrImage != null) {
                    RemoteQrImage(
                        url = "${EnvironmentManager.getAssetsBaseUrl()}${reservation.qrImage}",
                        contentDescription = stringResource(R.string.qr_code)
                    )
                } else {
                    UserManager.qr_logo?.let { logo ->
                        LocalQrImageWithLogo(reservation, logo)
                    } ?: LocalQrImage(reservation)
                }
            }
        }

        training?.let { training ->
            if ((training.players?.size ?: 0) > 0) {
                Spacer(Modifier.height(16.dp))
                PlayersCarousel(training)
                Spacer(Modifier.height(16.dp))
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun ReservationRulesCard() {
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
                    .background(Color.Transparent),
                contentScale = ContentScale.Crop,
                placeholder = rememberVectorPainter(Icons.Rounded.Person),
                error = rememberVectorPainter(Icons.Default.BrokenImage)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationBottomSheet(
    reservation: Reservation,
    onDismiss: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) {
        ReservationDetailScreen(
            isTeamReservation = false,
            reservation = reservation,
            onDismiss = onDismiss
        )
    }
}
