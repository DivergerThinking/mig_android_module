package com.diverger.mig_android_sdk.ui.reservations

import ReservationDetailViewModel
import android.app.Activity
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
import com.diverger.mig_android_sdk.ui.components.GradientCard
import com.diverger.mig_android_sdk.ui.components.HolographicFlippableCard
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.XCircle
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.UserCircle

// ═══════════════════════════════════════════════════════════════
// Standalone screen (creates its own ModalBottomSheet)
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    isTeamReservation: Boolean,
    reservation: Reservation? = null,
    training: EventModel? = null,
    onDismiss: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) {
        ReservationDetailContent(
            isTeamReservation = isTeamReservation,
            reservation = reservation,
            training = training,
            onDismiss = onDismiss
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Pure content (no sheet — use when caller provides its own)
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReservationDetailContent(
    isTeamReservation: Boolean,
    reservation: Reservation? = null,
    training: EventModel? = null,
    onDismiss: () -> Unit,
) {
    var targetAngle by remember { mutableFloatStateOf(0f) }
    val isFlipped = (((targetAngle / 180f).toInt()) % 2) != 0
    val context = LocalContext.current

    // Brightness to max
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val original = activity?.window?.attributes?.screenBrightness ?: -1f
        activity?.window?.attributes = activity?.window?.attributes?.apply {
            screenBrightness = 1f
        }
        onDispose {
            activity?.window?.attributes = activity?.window?.attributes?.apply {
                screenBrightness = original
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Close button
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onDismiss) {
                Icon(
                    FeatherIcons.XCircle,
                    contentDescription = stringResource(R.string.close_label),
                    tint = Color.Gray.copy(alpha = 0.4f)
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

        val showFlipCard =
            (isTeamReservation && training?.type == "centre") || !isTeamReservation

        if (showFlipCard) {
            val configuration = LocalConfiguration.current
            val desiredHeight: Dp = configuration.screenHeightDp.dp * 7 / 10

            HolographicFlippableCard(
                targetAngle = targetAngle,
                onAngleChange = { targetAngle = it },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(desiredHeight),
                front = {
                    UnifiedQrCardContent(
                        isTeamReservation = isTeamReservation,
                        reservation = reservation,
                        training = training
                    )
                },
                back = { ReservationRulesCardContent() }
            )
        } else {
            GradientCard {
                UnifiedQrCardContent(
                    isTeamReservation = isTeamReservation,
                    reservation = reservation,
                    training = training
                )
            }
        }

        if (showFlipCard) {
            Button(
                onClick = { targetAngle += if (isFlipped) 180f else -180f },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan),
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.CenterHorizontally)
                    .height(60.dp)
            ) {
                AnimatedContent(
                    targetState = isFlipped,
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

// ═══════════════════════════════════════════════════════════════
// Card content helpers
// ═══════════════════════════════════════════════════════════════

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
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 5.dp)
    )
}

@Composable
private fun RemoteQrImage(url: String, contentDescription: String) {
    var isLoading by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1f)
            .background(Color.White, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
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
            onLoading = { isLoading = true },
            onSuccess = { isLoading = false }
        )
        if (isLoading) {
            CircularProgressIndicator(color = Color.Cyan)
        }
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
    LocalQrImage(reservation = reservation, logoBitmap = logoBitmap)
}

@Composable
private fun LocalQrImage(
    reservation: Reservation,
    qrSize: Int = 500,
    logoBitmap: Bitmap? = null,
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
            .fillMaxWidth(0.9f)
            .aspectRatio(1f)
            .background(Color.White, RoundedCornerShape(10.dp))
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Front face content
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ColumnScope.UnifiedQrCardContent(
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
                @Suppress("UNCHECKED_CAST")
                return ReservationDetailViewModel(
                    stringResourcesProvider = StringResourcesProvider(context),
                    training = if (isTeamReservation) training else null,
                    reservation = if (isTeamReservation) null else reservation
                ) as T
            }
        }
    )

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
    val timeStringRes =
        if (time.contains(",")) R.string.booking_details_time
        else R.string.booking_details_time_singular
    ReservationDetailRow(stringResource(timeStringRes, time))

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
                Text(
                    stringResource(R.string.no_qr_code_required),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        } else {
            RemoteQrImage(
                url = "${EnvironmentManager.getAssetsBaseUrl()}${training?.reserves?.firstOrNull()?.qrImage.orEmpty()}",
                contentDescription = stringResource(R.string.qr_code)
            )
        }
    } else {
        reservation?.let { res ->
            if (res.qrImage != null) {
                RemoteQrImage(
                    url = "${EnvironmentManager.getAssetsBaseUrl()}${res.qrImage}",
                    contentDescription = stringResource(R.string.qr_code)
                )
            } else {
                UserManager.qr_logo?.let { logo ->
                    LocalQrImageWithLogo(res, logo)
                } ?: LocalQrImage(res)
            }
        }
    }

    training?.let { t ->
        if ((t.players?.size ?: 0) > 0) {
            Spacer(Modifier.height(16.dp))
            PlayersCarousel(t)
            Spacer(Modifier.height(16.dp))
        }
    }

    Spacer(Modifier.height(32.dp))
}

// ═══════════════════════════════════════════════════════════════
// Back face content
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ColumnScope.ReservationRulesCardContent() {
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

// ═══════════════════════════════════════════════════════════════
// Players
// ═══════════════════════════════════════════════════════════════

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

// ═══════════════════════════════════════════════════════════════
// Bottom Sheet wrapper (individual reservations)
// ═══════════════════════════════════════════════════════════════

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
        ReservationDetailContent(
            isTeamReservation = false,
            reservation = reservation,
            onDismiss = onDismiss
        )
    }
}