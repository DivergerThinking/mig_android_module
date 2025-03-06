import CompetitionDetailViewModel
import android.text.Html
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.data.Competition
import com.diverger.mig_android_sdk.data.Split
import com.diverger.mig_android_sdk.data.Tournament
import com.diverger.mig_android_sdk.ui.competitions.cleanHtml
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Trophy
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CompetitionDetailScreen(
    navController: NavController,
    competitionId: String,
    viewModel: CompetitionDetailViewModel = viewModel()
) {
    val competition by viewModel.competition.collectAsState()
    val selectedSplit by viewModel.selectedSplit.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    LaunchedEffect(competitionId) {
        viewModel.fetchCompetition(competitionId)
    }

    MIGAndroidSDKTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            competition?.let { comp ->
                Column(modifier = Modifier
                    //.fillMaxHeight()
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)) {

                    // 🔙 **Barra Superior**
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                        }
                        Text(
                            text = comp.title.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 📌 **Banner**
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://premig.randomkesports.com/cms/assets/${comp.game?.banner}")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.Gray, shape = RoundedCornerShape(30.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 📌 **Dropdown de Splits**
                    DropdownMenuComponent(
                        selectedSplit = selectedSplit,
                        splits = comp.splits ?: emptyList(),
                        onSplitSelected = { viewModel.selectSplit(it) }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    when (selectedTab) {
                        0 -> Text(text = "SOBRE ESTA COMPETICIÓN...", color = Color.White, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        1 -> Text(text = "DETALLES", color = Color.White, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        2 -> Text(text = "REGLAS", color = Color.White, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        3 -> Text(text = "CONTACTO", color = Color.White, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        4 -> Text(text = "TORNEOS", color = Color.White, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    }

                    //Spacer(modifier = Modifier.height(16.dp))

                    // 📌 **Contenido de la pestaña seleccionada (Scroll habilitado)**
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // 🔹 IMPORTANTE para que la bottom bar se muestre bien
                            //.verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        SelectedTabContent(competition = comp, selectedTab = selectedTab)
                    }

                    // 📌 **Tab Layout en la parte inferior**
                    BottomNavigationTabs(viewModel)
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Cyan)
                }
            }
        }
    }
}

// 📌 **Dropdown de Splits**
@Composable
fun DropdownMenuComponent(selectedSplit: Split?, splits: List<Split>, onSplitSelected: (Split) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .border(1.dp, Color.White, RoundedCornerShape(22.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedSplit?.name ?: "Selecciona un Split",
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir", tint = Color.Cyan)
                }
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.Black)
                .border(1.dp, Color.White, RoundedCornerShape(12.dp))) {
                splits.forEach { split ->
                    DropdownMenuItem(
                        text = { Text(split.name, color = Color.White) },
                        onClick = {
                            onSplitSelected(split)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedTabContent(competition: Competition, selectedTab: Int) {
    when (selectedTab) {
        0 -> Text(text = cleanHtml(competition.overview ?: "Sin información"), color = Color.White, modifier = Modifier.padding(16.dp))
        1 -> Text(text = cleanHtml(competition.details ?: "Sin información"), color = Color.White, modifier = Modifier.padding(16.dp))
        2 -> Text(text = cleanHtml(competition.rules ?: "Sin reglas definidas"), color = Color.White, modifier = Modifier.padding(16.dp))
        3 -> Text(text = cleanHtml(competition.contact ?: "Sin contacto disponible"), color = Color.White, modifier = Modifier.padding(16.dp))
        4 -> TournamentTab(competition)
    }
}

// 📌 **Tab Layout en la parte inferior**
@Composable
fun BottomNavigationTabs(viewModel: CompetitionDetailViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.93f), Color.Black.copy(alpha = 0.82f))
                )
            )
    ) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) {
        val tabs = listOf(
            "Overview" to Icons.Default.Info,
            "Detalles" to Icons.Default.List,
            "Reglas" to Icons.Default.Rule,
            "Contacto" to Icons.Default.Email,
            "Torneos" to FontAwesomeIcons.Solid.Trophy
        )

        tabs.forEachIndexed { index, (title, icon) ->
            NavigationBarItem(
                modifier = Modifier.size(22.dp),
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                icon = {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = if (selectedTab == index) Color.Cyan else Color.White
                    )
                },
                label = {
                    Text(
                        title,
                        color = if (selectedTab == index) Color.Cyan else Color.White
                    )
                },
                selected = selectedTab == index,
                onClick = { viewModel.selectTab(index) }
            )
        }
    }
}
}

// 📌 **Pestaña de Torneos**
@Composable
fun TournamentTab(competition: Competition) {
    val selectedSplit by remember { mutableStateOf(competition.splits?.firstOrNull()) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (selectedSplit?.tournaments.isNullOrEmpty()) {
            item {
                Text(
                    text = "No hay torneos disponibles",
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(selectedSplit?.tournaments ?: emptyList()) { tournament ->
                TournamentCard(tournament)
            }
        }
    }
}

@Composable
fun TournamentCard(tournament: Tournament) {
    val formattedDate = getFormattedDate(tournament.tournamentDate)
    val (day, month) = getDayAndMonth(tournament.tournamentDate)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
            .padding(8.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clickable {
                tournament.link?.let { link ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                } ?: Toast.makeText(context, "Enlace no disponible", Toast.LENGTH_SHORT).show()
            }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 📆 **Fecha en cuadro con gradiente**
            DateBox(day = day, month = month)

            Spacer(modifier = Modifier.width(16.dp))

            // 🎮 **Nombre del torneo y estado**
            Column {
                Text(
                    text = tournament.name.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "${tournament.status?.capitalize()}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 📌 **Botón de inscripción**
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    tournament.link?.let { link ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                        context.startActivity(intent)
                    } ?: Toast.makeText(context, "Enlace no disponible", Toast.LENGTH_SHORT).show()
                },
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "INSCRÍBETE",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

// 📌 **Componente de fecha con Gradiente (Igual que en iOS)**
@Composable
fun DateBox(day: String, month: String) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .background(
                Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFF8000FF))), // 🎨 Rosa a Morado
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = month.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

// 📌 **Función para obtener el Día y Mes separados**
fun getDayAndMonth(dateString: String?): Pair<String, String> {
    if (dateString.isNullOrEmpty()) return Pair("?", "?")

    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString)

        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMMM", Locale("es", "ES"))

        Pair(dayFormat.format(date ?: return Pair("?", "?")), monthFormat.format(date))
    } catch (e: Exception) {
        Pair("?", "?")
    }
}

// 📌 **Función para formatear la fecha en "DD/MM/YYYY"**
fun getFormattedDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Fecha desconocida"

    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return "Fecha inválida")
    } catch (e: Exception) {
        "Fecha inválida"
    }
}