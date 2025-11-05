package com.diverger.mig_android_sdk.ui.teams.playersTeam

import PlayersTeamViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.data.TeamUser
import com.diverger.mig_android_sdk.data.UnsafeAsyncImage
import com.diverger.mig_android_sdk.support.EnvironmentManager

@Composable
fun PlayersTeamScreen(viewModel: PlayersTeamViewModel = viewModel()) {
    val teamName by viewModel.teamName.collectAsState()
    val teamPlayers by viewModel.teamPlayers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            }
            errorMessage != null -> {
                Text(errorMessage ?: "Error desconocido", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        text = if (teamName.isEmpty()) "Sin equipo" else "JUGADORES DE ${teamName.uppercase()}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        items(teamPlayers) { player ->
                            PlayerItem(player)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerItem(player: TeamUser) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!player.userId.avatar.isNullOrEmpty()) {
                UnsafeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("${EnvironmentManager.getAssetsBaseUrl()}${player.userId.avatar}")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Avatar de ${player.userId.username}",
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape),
                    error = rememberVectorPainter(Icons.Default.Person)
                )
            } else {
                // Si no hay avatar, mostrar el icono predeterminado
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(68.dp)
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            // Información del jugador
            Column(modifier = Modifier.weight(1f)) {
                Text(player.userId.username, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(6.dp))
                Text("${player.role?.name ?: "Sin rol"}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}