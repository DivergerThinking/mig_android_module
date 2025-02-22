package com.diverger.mig_android_sdk.ui.competitions

import CompetitionDetailViewModel
import android.text.Html
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.diverger.mig_android_sdk.data.Competition
import com.diverger.mig_android_sdk.data.Split
import com.diverger.mig_android_sdk.data.Tournament
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CompetitionDetailScreen(
    navController: NavController,
    competitionId: String,
    viewModel: CompetitionDetailViewModel = viewModel()
) {
    val competition by viewModel.competition.collectAsState()
    var selectedSplit by remember { mutableStateOf<Split?>(null) }
    var showTournamentPopup by remember { mutableStateOf(false) }

    LaunchedEffect(competitionId) {
        viewModel.fetchCompetition(competitionId)
    }

    competition?.let { comp ->
        selectedSplit = selectedSplit ?: comp.splits?.firstOrNull()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
        ) {
            // 🔙 **Barra Superior con Botón de Atrás y Título**
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                }
                Text(
                    text = comp.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { showTournamentPopup = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                ) {
                    Text("Torneos", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 📌 **Selector de Splits**
            DropdownMenuComponent(
                selectedSplit = selectedSplit,
                splits = comp.splits ?: emptyList(),
                onSplitSelected = { selectedSplit = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 📄 **Pestañas con Información**
            TabLayout(competition = comp)

            // 🎟️ **Popup de Torneos**
            if (showTournamentPopup) {
                TournamentPopup(
                    tournaments = selectedSplit?.tournaments ?: emptyList(),
                    onDismiss = { showTournamentPopup = false }
                )
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Cyan)
        }
    }
}

// 📌 **Dropdown de Splits**
@Composable
fun DropdownMenuComponent(selectedSplit: Split?, splits: List<Split>, onSplitSelected: (Split) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
            Text(text = selectedSplit?.name ?: "Selecciona un Split", color = Color.White)
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            splits.forEach { split ->
                DropdownMenuItem(
                    text = { Text(split.name, color = Color.Black) },
                    onClick = {
                        onSplitSelected(split)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 📄 **Tab Layout**
@Composable
fun TabLayout(competition: Competition) {
    val tabs = listOf("Overview", "Detalles", "Reglas", "Contacto")
    var selectedTab by remember { mutableStateOf(0) }

    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, color = Color.DarkGray) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(state = rememberLazyListState(), modifier = Modifier.fillMaxSize()) {
            item {
                val content = when (selectedTab) {
                    0 -> competition.overview.takeUnless { it.isNullOrBlank() } ?: "Sin información"
                    1 -> competition.details.takeUnless { it.isNullOrBlank() } ?: "Sin información"
                    2 -> competition.rules.takeUnless { it.isNullOrBlank() } ?: "Sin reglas definidas"
                    3 -> competition.contact.takeUnless { it.isNullOrBlank() } ?: "Sin contacto disponible"
                    else -> "No hay datos disponibles"
                }
                InfoSection(content)
            }
        }
    }
}

// 📄 **Sección de Información (Scrollable)**
@Composable
fun InfoSection(content: String) {
    Text(
        text = Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString(),
        color = Color.White,
        modifier = Modifier.padding(8.dp)
    )
}

// 🎟️ **Popup de Torneos**
@Composable
fun TournamentPopup(tournaments: List<Tournament>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true),
        containerColor = Color.DarkGray,
        title = { Text("Torneos", color = Color.White) },
        text = {
            LazyColumn {
                items(tournaments) { tournament ->
                    TournamentItem(tournament)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)) {
                Text("Cerrar", color = Color.White)
            }
        }
    )
}

// 🏆 **Item de Torneo**
@Composable
fun TournamentItem(tournament: Tournament) {
    val formattedDate = formatDate(tournament.tournamentDate)
    val context = LocalContext.current

    Column(modifier = Modifier.padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Yellow)
            Spacer(modifier = Modifier.width(8.dp))
            Text(tournament.name, color = Color.White)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text("Fecha: $formattedDate", color = Color.Cyan)

        tournament.link?.takeIf { it.isNotBlank() }?.let { link ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Acceder al Torneo",
                color = Color.Yellow,
                modifier = Modifier.clickable {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Manejo de error si no se puede abrir el enlace
                        Toast.makeText(context, "No se puede abrir el enlace", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } ?: run {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Enlace no disponible", color = Color.Gray)
        }

        Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
    }
}