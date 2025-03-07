package com.diverger.mig_android_sdk.ui.teams.playersTeam

import PlayersTeamViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diverger.mig_android_sdk.data.TeamUser

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
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerItem(player: TeamUser) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ícono de avatar en lugar de imagen
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Avatar",
            tint = Color.White,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Información del jugador
        Column(modifier = Modifier.weight(1f)) {
            Text(player.userId.username, color = Color.White, style = MaterialTheme.typography.bodyLarge)
            Text("Rol: ${player.role?.name ?: "Sin rol"}", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        }
    }
}