package com.diverger.mig_android_sdk.ui.competitions

import android.content.Context
import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.Competition
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CompetitionsScreen(navController: NavController, viewModel: CompetitionsViewModel = viewModel()) {
    val competitions by viewModel.competitions.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val availableYears by viewModel.availableYears.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    MIGAndroidSDKTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
        ) {
            // 📌 Título
            Text(
                stringResource(R.string.competitions_label),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // 📅 Dropdown de selección de año (similar a iOS)
            DropdownMenuComponent(
                selectedYear = selectedYear,
                options = availableYears,
                onYearSelected = { viewModel.updateSelectedYear(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Cyan)
                }
            } else if (!errorMessage.isNullOrEmpty()) {
                Text(text = errorMessage!!, color = Color.Red, fontWeight = FontWeight.Bold)
            } else {
                val leagues = viewModel.getLeaguesWithCompetitions(context = LocalContext.current)

                LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    items(leagues) { league ->
                        CompetitionLeagueSection(league, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuComponent(selectedYear: String, options: List<String>, onYearSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .border(1.dp, Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { expanded = !expanded }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedYear.ifEmpty { stringResource(R.string.select_season) },
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {expanded = !expanded}) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir", tint = Color.Cyan)
                }
            }

            DropdownMenu(expanded = expanded,
                modifier = Modifier
                    .background(Color.Black)
                    .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                , onDismissRequest = { expanded = false }) {
                options.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year, color = Color.White) },
                        onClick = {
                            onYearSelected(year)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// 📌 **Sección de una liga con su carrusel de competiciones**
@Composable
fun CompetitionLeagueSection(league: LeagueModel, navController: NavController) {
    Column {
        Text(
            text = league.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        Text(
            text = league.description,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        Text(
            text = league.overView,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )

        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
            items(league.competitions) { competition ->
                CompetitionCard(competition) {
                    navController.navigate("competition_detail/${competition.id}")
                }
            }
        }
    }
}

// 📌 **Tarjeta de una competición en el carrusel**
@Composable
fun CompetitionCard(competition: Competition, onClick: () -> Unit) {
    val imageUrl = "${EnvironmentManager.getAssetsBaseUrl()}${competition.game?.image ?: ""}"

    Card(
        modifier = Modifier
            //.width(200.dp)
            //.height(300.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        //colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen de ${competition.title}",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            )

            /*Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = competition.title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Inicio: ${formatDate(competition.startDate)}",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }*/
        }
    }
}

// 📌 **Modelo de liga con competiciones**
data class LeagueModel(
    val title: String,
    val description: String,
    val overView: String,
    val competitions: List<Competition>
)

// 📌 **Función para organizar las competiciones en ligas**
fun CompetitionsViewModel.getLeaguesWithCompetitions(context: Context): List<LeagueModel> {
    val allCompetitions = competitions.value

    val list: MutableList<LeagueModel> = mutableListOf()

    val esm = allCompetitions.filter { it.game?.type == "esm" }
    val junior = allCompetitions.filter { it.game?.type == "junior" }
    val stormCircuit = allCompetitions.filter { it.game?.type == "stormCircuit" }
    val other = allCompetitions.filter { it.game?.type == "other" }

   if (esm.isNotEmpty()) {
       list.add(LeagueModel(
           title = context.getString(R.string.local_league_title),
           description = context.getString(R.string.local_league_description),
           overView = context.getString(R.string.local_league_overview),
           competitions = allCompetitions.filter { it.game?.type == "esm" }
       ))
   }

    if (junior.isNotEmpty()) {
        list.add(LeagueModel(
            title = context.getString(R.string.junior_league_title),
            description = context.getString(R.string.junior_league_description),
            overView = context.getString(R.string.junior_league_overview),
            competitions = allCompetitions.filter { it.game?.type == "junior" }
        ))
    }

    if (stormCircuit.isNotEmpty()) {
        list.add(LeagueModel(
            title = context.getString(R.string.storm_circuit_title),
            description = context.getString(R.string.storm_circuit_description),
            overView = context.getString(R.string.storm_circuit_overview),
            competitions = allCompetitions.filter { it.game?.type == "stormCircuit" }
        ))
    }

    if (other.isNotEmpty()) {
        list.add(LeagueModel(
            title = context.getString(R.string.other_competition_title),
            description = context.getString(R.string.other_competition_description),
            overView = "",
            competitions = allCompetitions.filter { it.game?.type == "other" }
        ))
    }

    return list
}

// 📅 **Función para formatear la fecha a dd/MM/yyyy**
fun formatDate(dateString: String?, context: Context): String {
    if (dateString.isNullOrEmpty()) return context.getString(R.string.unknown_date)

    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return context.getString(R.string.invalid_date))
    } catch (e: Exception) {
        context.getString(R.string.invalid_date)
    }
}

// 📅 **Función para formatear la fecha a dd/MM/yyyy desde yyy-MM-dd**
fun formatDateFromShort(dateString: String?, context: Context): String {
    if (dateString.isNullOrEmpty()) return context.getString(R.string.unknown_date)

    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return context.getString(R.string.invalid_date))
    } catch (e: Exception) {
        context.getString(R.string.invalid_date)
    }
}

// 📝 **Función para limpiar el HTML del overview**
fun cleanHtml(html: String): String {
    return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
}