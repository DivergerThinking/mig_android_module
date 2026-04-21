package com.diverger.mig_android_sdk.ui.competitions

import android.content.Context
import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.Competition
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.diverger.mig_android_sdk.support.LocalizationManager
import com.diverger.mig_android_sdk.support.localizedString
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
            Text(
                localizedString("competitions.heading", R.string.competitions_label),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(bottom = 10.dp)
            )

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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
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
                    text = selectedYear.ifEmpty { localizedString("competitions.season", R.string.select_season) },
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Cyan)
                }
            }
            DropdownMenu(
                expanded = expanded,
                modifier = Modifier
                    .background(Color.Black)
                    .border(1.dp, Color.White, RoundedCornerShape(12.dp)),
                onDismissRequest = { expanded = false }
            ) {
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

@Composable
fun CompetitionLeagueSection(league: LeagueModel, navController: NavController) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = league.title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        if (league.seriesTitle.isNotBlank()) {
            Text(
                text = league.seriesTitle,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (league.description.isNotBlank()) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = league.description,
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier
                        .background(Color.Cyan.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isDescriptionExpanded) localizedString("dashboard.profile.seeLess", R.string.see_less) else localizedString("dashboard.profile.seeMore", R.string.see_more),
                        color = Color.Cyan,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Icon(
                        imageVector = if (isDescriptionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Cyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val chunked = league.competitions.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            chunked.forEach { pair ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    pair.forEach { competition ->
                        CompetitionCard(
                            competition = competition,
                            modifier = Modifier.weight(1f)
                        ) {
                            navController.navigate("competition_detail/${competition.id}")
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CompetitionCard(competition: Competition, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val imageUrl = "${EnvironmentManager.getAssetsBaseUrl()}${competition.game?.image ?: ""}"
    val gameType = competition.game?.type
    val modalityText = when (gameType) {
        "in-person" -> localizedString("competition.modality.hybrid", R.string.modality_in_person)
        "online"    -> localizedString("competition.modality.online", R.string.modality_online)
        else        -> null
    }
    val modalityColor = if (gameType == "online") Color(0xFF00C853) else Color.Cyan

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = competition.game?.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    competition.game?.name?.let { name ->
                        Text(
                            text = name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )
                    }
                    competition.game?.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Text(
                                text = cleanHtml(desc),
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                maxLines = 4
                            )
                        }
                    }
                }

                modalityText?.let { label ->
                    Box(
                        modifier = Modifier
                            .background(modalityColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = label,
                            color = modalityColor,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}

data class LeagueModel(
    val title: String,
    val seriesTitle: String,
    val description: String,
    val competitions: List<Competition>
)

fun CompetitionsViewModel.getLeaguesWithCompetitions(context: Context): List<LeagueModel> {
    val allCompetitions = competitions.value
    val list: MutableList<LeagueModel> = mutableListOf()

    val esm = allCompetitions.filter { it.type == "esm" }
    val junior = allCompetitions.filter { it.type == "junior" }
    val stormCircuit = allCompetitions.filter { it.type == "stormCircuit" }
    val other = allCompetitions.filter { it.type == "other" }

    if (esm.isNotEmpty()) {
        list.add(LeagueModel(
            title = LocalizationManager.string("competitions.municipalLeague") ?: context.getString(R.string.local_league_title),
            seriesTitle = context.getString(R.string.local_league_description),
            description = LocalizationManager.string("competitions.subheadings.townLeague") ?: context.getString(R.string.local_league_overview),
            competitions = esm
        ))
    }
    if (junior.isNotEmpty()) {
        list.add(LeagueModel(
            title = LocalizationManager.string("competitions.juniorLeague") ?: context.getString(R.string.junior_league_title),
            seriesTitle = context.getString(R.string.junior_league_description),
            description = LocalizationManager.string("competitions.subheadings.junior") ?: context.getString(R.string.junior_league_overview),
            competitions = junior
        ))
    }
    if (stormCircuit.isNotEmpty()) {
        list.add(LeagueModel(
            title = LocalizationManager.string("competitions.stormCircuit") ?: context.getString(R.string.storm_circuit_title),
            seriesTitle = context.getString(R.string.storm_circuit_description),
            description = LocalizationManager.string("competitions.subheadings.storm") ?: context.getString(R.string.storm_circuit_overview),
            competitions = stormCircuit
        ))
    }
    if (other.isNotEmpty()) {
        list.add(LeagueModel(
            title = LocalizationManager.string("competitions.otherLeague") ?: context.getString(R.string.other_competition_title),
            seriesTitle = "",
            description = context.getString(R.string.other_competition_description),
            competitions = other
        ))
    }

    return list
}

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

fun cleanHtml(html: String): String {
    return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
}
