package com.diverger.mig_android_sdk.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.EventModel
import com.diverger.mig_android_sdk.data.PlayerModel
import com.diverger.mig_android_sdk.data.UnsafeAsyncImage
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.diverger.mig_android_sdk.ui.competitions.formatDateFromShort
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.Edit
import compose.icons.feathericons.Eye
import compose.icons.feathericons.Monitor
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Building
import compose.icons.fontawesomeicons.solid.UserCircle

// ═══════════════════════════════════════════════════════════════
// Options enum (matches iOS TeamReservationCellComponentOptionSelected)
// ═══════════════════════════════════════════════════════════════

enum class TrainingCellOption {
    RemoveCell, SeeDetails, EditBooking
}

// ═══════════════════════════════════════════════════════════════
// Main cell — use this from any screen
// showDeleteAndEditOption: controls visibility of delete & edit
// (matches iOS showDeleteAndEditOption)
// ═══════════════════════════════════════════════════════════════

@Composable
fun TrainingCell(
    training: EventModel,
    showDeleteAndEditOption: Boolean = false,
    onAction: (TrainingCellOption) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            IconDateAndRemoveBanner(training, showDeleteAndEditOption, onAction)
            Spacer(Modifier.height(10.dp))
            ReservationLocation(training)
            Spacer(Modifier.height(10.dp))

            if (!training.players.isNullOrEmpty()) {
                PlayersCarousel(training)
                Spacer(Modifier.height(16.dp))
            }

            if (!training.notes.isNullOrEmpty()) {
                Spacer(Modifier.height(10.dp))
                NotesSection(training.notes)
            }

            ActionButtons(showDeleteAndEditOption, onAction)
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Icon + Date + Delete button (matches iOS iconDateAndRemoveBannerComponent)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun IconDateAndRemoveBanner(
    training: EventModel,
    showDeleteAndEditOption: Boolean,
    onAction: (TrainingCellOption) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = when (training.type.lowercase()) {
                "virtual" -> FeatherIcons.Monitor
                "centre" -> FontAwesomeIcons.Solid.Building
                else -> FeatherIcons.Monitor
            },
            contentDescription = stringResource(R.string.booking_type),
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${formatDateFromShort(training.startDate, context = LocalContext.current)} - ${training.time.slice(0..4)}",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.weight(1f))

        // Delete button — matches iOS minus.circle red
        if (showDeleteAndEditOption) {
            IconButton(
                onClick = { onAction(TrainingCellOption.RemoveCell) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.RemoveCircle,
                    contentDescription = "Eliminar",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Location label (matches iOS showReservationLocation)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ReservationLocation(training: EventModel) {
    Text(
        text = "${UserManager.selectedTeam.value?.name ?: stringResource(R.string.no_team)} - ${training.type}",
        color = Color.White.copy(0.8f),
        style = MaterialTheme.typography.labelMedium
    )
}

// ═══════════════════════════════════════════════════════════════
// Players carousel (matches iOS playersCarouselComponent)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun PlayersCarousel(training: EventModel) {
    Column {
        Text(
            "Players",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 4.dp, start = 2.dp)
        )
        LazyRow(
            modifier = Modifier.padding(7.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(training.players ?: emptyList()) { player ->
                player.userId?.let { PlayerAvatar(it) }
            }
        }
    }
}

@Composable
private fun PlayerAvatar(player: PlayerModel) {
    Box(
        modifier = Modifier.size(50.dp),
        contentAlignment = Alignment.Center
    ) {
        val imageUrl = player.avatar.takeIf { it?.isNotEmpty() ?: false }
            ?.let { "${EnvironmentManager.getAssetsBaseUrl()}$it" }

        if (imageUrl != null) {
            UnsafeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = FontAwesomeIcons.Solid.UserCircle,
                contentDescription = stringResource(R.string.no_avatar_user),
                tint = Color.Gray,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════
// Notes section (matches iOS descriptionComponent)
// ═══════════════════════════════════════════════════════════════

@Composable
private fun NotesSection(notes: String) {
    Column(modifier = Modifier.padding(7.dp)) {
        Text(
            "Notas",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
        Text(
            notes,
            color = Color.White.copy(0.8f),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 3
        )
    }
}

// ═══════════════════════════════════════════════════════════════
// Action buttons (matches iOS buttonsComponent)
// "Ver reserva" always + "Editar reserva" when allowed
// ═══════════════════════════════════════════════════════════════

@Composable
private fun ActionButtons(
    showDeleteAndEditOption: Boolean,
    onAction: (TrainingCellOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        // "Ver reserva" — always visible (matches iOS eye + "Ver reserva")
        TextButton(onClick = { onAction(TrainingCellOption.SeeDetails) }) {
            Icon(
                FeatherIcons.Eye,
                contentDescription = "Ver reserva",
                tint = Color.Cyan,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                stringResource(R.string.see_booking),
                color = Color.Cyan,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.weight(1f))

        // "Editar reserva" — only when allowed (matches iOS pencil + "Editar reserva")
        if (showDeleteAndEditOption) {
            TextButton(onClick = { onAction(TrainingCellOption.EditBooking) }) {
                Icon(
                    FeatherIcons.Edit,
                    contentDescription = "Editar reserva",
                    tint = Color.Red,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    stringResource(R.string.edit_booking),
                    color = Color.Red,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.weight(1f))
        }
    }
}