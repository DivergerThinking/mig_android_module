package com.diverger.mig_android_sdk.ui.competitions

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.R
import com.diverger.mig_android_sdk.data.Competition
import com.diverger.mig_android_sdk.data.CompetitionsApi
import com.diverger.mig_android_sdk.data.Split
import com.diverger.mig_android_sdk.data.Tournament
import com.diverger.mig_android_sdk.data.TournamentInscriptionRequest
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.data.getUnsafeOkHttpClient
import kotlinx.coroutines.launch
import com.diverger.mig_android_sdk.support.EnvironmentManager
import com.diverger.mig_android_sdk.support.localizedString
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.ArrowRightCircle
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Trophy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

// ─── Main screen ─────────────────────────────────────────────────────────────

@Composable
fun CompetitionDetailScreen(
    navController: NavController,
    competitionId: String,
    viewModel: CompetitionDetailViewModel = viewModel(),
) {
    val competition by viewModel.competition.collectAsState()
    val selectedSplit by viewModel.selectedSplit.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    LaunchedEffect(competitionId) {
        viewModel.fetchCompetition(competitionId)
    }

    MIGAndroidSDKTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            competition?.let { comp ->
                Column(modifier = Modifier.fillMaxSize()) {

                    // Banner
                    Box(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                        val bannerPath = selectedSplit?.banner?.takeIf { it.isNotBlank() } ?: comp.game?.banner
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("${EnvironmentManager.getAssetsBaseUrl()}$bannerPath")
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.7f), Color.Black)
                                )
                            )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                            }
                            Text(
                                text = comp.title.uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f).padding(end = 16.dp)
                            )
                        }
                    }

                    // Split chips
                    val splits = comp.splits ?: emptyList()
                    if (splits.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            splits.forEach { split ->
                                val isSelected = selectedSplit?.id == split.id
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) Color.Cyan else Color.White.copy(alpha = 0.12f),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .clickable { viewModel.selectSplit(split) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = split.name,
                                        color = if (isSelected) Color.Black else Color.White,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Tab content
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        SelectedTabContent(competition = comp, selectedTab = selectedTab, selectedSplit = selectedSplit)
                    }

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

// ─── Tab dispatcher ───────────────────────────────────────────────────────────

@Composable
fun SelectedTabContent(competition: Competition, selectedTab: Int, selectedSplit: Split?) {
    when (selectedTab) {
        0 -> OverviewTabContent(selectedSplit?.overview ?: competition.overview)
        1 -> DetailsTabContent()
        2 -> RulesTabContent(rules = selectedSplit?.rules ?: competition.rules, pdfFile = competition.pdfFile)
        3 -> ContactTabContent(contact = selectedSplit?.contact ?: competition.contact)
        4 -> TournamentTab(selectedSplit)
    }
}

// ─── Tab 0 · Overview ─────────────────────────────────────────────────────────

@Composable
fun OverviewTabContent(overview: String?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = localizedString("competitions.about", R.string.tournament_about_this_competition),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Text(
            text = if (overview.isNullOrBlank()) localizedString("competitions.subheadings.notFound", R.string.no_information_available) else cleanHtml(overview),
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ─── Tab 1 · Details (expandable accordion) ───────────────────────────────────

private data class DetailItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
fun DetailsTabContent() {
    val items = listOf(
        DetailItem(Icons.Default.Computer,  "Formato de torneos",
            "Cada torneo se juega en formato competitivo adaptado al juego. Incluye fases clasificatorias y eliminatorias según el número de participantes.",
            Color(0xFF2D59D9)),
        DetailItem(Icons.Default.Star,      "Sistema de puntos",
            "Los puntos se acumulan por cada partida jugada y se reflejan en la clasificación general.",
            Color(0xFFBF268C)),
        DetailItem(Icons.Default.DateRange, "Splits y temporada",
            "La temporada está dividida en splits. Cada split tiene su propio ranking y premio final.",
            Color(0xFF7326BF)),
        DetailItem(Icons.Default.Group,     "Participación",
            "La liga está abierta a jugadores de todos los niveles.",
            Color(0xFF268C40))
    )

    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = localizedString("competitions.details", R.string.tournament_details),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        items.forEachIndexed { index, item ->
            ExpandableDetailCell(
                item = item,
                isExpanded = expandedIndex == index,
                onToggle = { expandedIndex = if (expandedIndex == index) null else index }
            )
        }
    }
}

@Composable
private fun ExpandableDetailCell(item: DetailItem, isExpanded: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(item.color.copy(alpha = 0.45f), Color.Black.copy(alpha = 0.75f))
                )
            )
            .border(1.dp, item.color.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(item.color, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Text(
                text = item.title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        // Expanded content
        AnimatedVisibility(visible = isExpanded, enter = expandVertically(), exit = shrinkVertically()) {
            Text(
                text = item.description,
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }
    }
}

// ─── Tab 2 · Rules ────────────────────────────────────────────────────────────

@Composable
fun RulesTabContent(rules: String?, pdfFile: String?) {
    val context = LocalContext.current
    val pdfUrl = pdfFile?.takeIf { it.isNotBlank() }
        ?.let { "${EnvironmentManager.getAssetsBaseUrl()}$it" }
    var expandedSection by remember { mutableStateOf<String?>(null) }

    val ruleItems = listOf(
        Icons.Default.SmokeFree     to stringResource(R.string.rule_smoke_free),
        Icons.Default.Fastfood      to stringResource(R.string.rule_no_food),
        Icons.Default.DirectionsRun to stringResource(R.string.rule_no_running),
        Icons.Default.VolumeOff     to stringResource(R.string.rule_respect_environment),
        Icons.Default.Delete        to stringResource(R.string.rule_keep_clean),
        Icons.Default.CloudOff      to stringResource(R.string.rule_no_downloads)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        RulesTitleRow(pdfUrl = pdfUrl, context = context, modifier = Modifier)
        Spacer(modifier = Modifier.height(12.dp))

        if (pdfUrl != null) {
            RulesAccordionRow(
                label = localizedString("rules.section.text", R.string.rules_section_text),
                icon = Icons.Default.Description,
                isExpanded = expandedSection == "text",
                onToggle = { expandedSection = if (expandedSection == "text") null else "text" }
            )
            AnimatedVisibility(visible = expandedSection == "text", enter = expandVertically(), exit = shrinkVertically()) {
                RulesGrid(ruleItems = ruleItems, modifier = Modifier.padding(vertical = 10.dp))
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
            RulesAccordionRow(
                label = localizedString("rules.section.pdf", R.string.rules_section_pdf),
                icon = Icons.Default.PictureAsPdf,
                isExpanded = expandedSection == "pdf",
                onToggle = { expandedSection = if (expandedSection == "pdf") null else "pdf" }
            )
            AnimatedVisibility(visible = expandedSection == "pdf", enter = expandVertically(), exit = shrinkVertically()) {
                PdfViewer(
                    url = pdfUrl,
                    modifier = Modifier.fillMaxWidth().height(460.dp).padding(top = 8.dp, bottom = 16.dp)
                )
            }
        } else {
            RulesGrid(ruleItems = ruleItems)
        }
    }
}

@Composable
private fun RulesGrid(ruleItems: List<Pair<androidx.compose.ui.graphics.vector.ImageVector, String>>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ruleItems.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { (icon, text) ->
                    RuleCard(icon = icon, text = text, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RuleCard(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Text(text, color = Color.White, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RulesTitleRow(pdfUrl: String?, context: Context, modifier: Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Text(
            text = localizedString("competitions.rules", R.string.tournament_rules),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        if (pdfUrl != null) {
            Box(
                modifier = Modifier
                    .background(Color.Cyan, RoundedCornerShape(20.dp))
                    .clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl)))
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Text(localizedString("pdf.download", R.string.pdf_download), color = Color.Black, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                }
            }
        }
    }
}

@Composable
private fun RulesAccordionRow(label: String, icon: ImageVector, isExpanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(16.dp))
        Text(label, color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
        Icon(
            if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}

// ─── Tab 3 · Contact ──────────────────────────────────────────────────────────

private val FALLBACK_EMAIL = "info@madridingame.es"
private val FALLBACK_ADDRESS = "Casa de Campo. Avenida Principal.\n3.28011. Madrid"

private fun extractEmail(text: String): String? =
    Regex("[A-Z0-9a-z._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}").find(text)?.value

private fun extractDiscordUrl(text: String): String? =
    Regex("https?://[^\\s]*discord[^\\s]*", RegexOption.IGNORE_CASE).find(text)?.value

@Composable
fun ContactTabContent(contact: String?) {
    val decoded = contact?.let { cleanHtml(it) } ?: ""
    val email = extractEmail(decoded) ?: FALLBACK_EMAIL
    val discordUrl = contact?.let { extractDiscordUrl(it) }

    val textBeforeEmail = if (extractEmail(decoded) != null)
        decoded.substringBefore(email).trim()
    else decoded

    val textAfterEmail = if (extractEmail(decoded) != null) {
        decoded.substringAfter(email)
            .let { if (discordUrl != null) it.replace(discordUrl, "") else it }
            .trim()
    } else ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = localizedString("competitions.contact", R.string.tournament_contact),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ContactCard(
            email = email,
            textBefore = textBeforeEmail.ifBlank { FALLBACK_ADDRESS },
            textAfter = textAfterEmail,
            discordUrl = discordUrl
        )
    }
}

@Composable
private fun ContactCard(email: String, textBefore: String, textAfter: String, discordUrl: String?) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF6B0DAD), Color(0xFFD400C7))
                )
            )
            .padding(horizontal = 24.dp, vertical = 44.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.contact_card_title),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Text(
                text = textBefore,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            // Email button
            Box(
                modifier = Modifier
                    .background(Color(0xFF9EE8E0), RoundedCornerShape(50.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:$email"))
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 28.dp, vertical = 12.dp)
            ) {
                Text(
                    text = email,
                    color = Color(0xFF1A1A2E),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (textAfter.isNotBlank()) {
                Text(
                    text = textAfter,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            // Discord button
            if (discordUrl != null) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF5966D9), RoundedCornerShape(50.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(discordUrl))
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Forum, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Text(stringResource(R.string.contact_join_discord), color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Text(
                text = FALLBACK_ADDRESS,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Tab 4 · Tournaments ──────────────────────────────────────────────────────

@Composable
fun TournamentTab(selectedSplit: Split?) {
    val tournaments = selectedSplit?.tournaments ?: emptyList()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var selectedTournament by remember { mutableStateOf<Tournament?>(null) }
    var nickname by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    if (showDialog && selectedTournament != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Color(0xFF1C1C1C),
            title = {
                Text(
                    text = "Antes de continuar…",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "¿Con qué alias competirás en el torneo?",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Cyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = Color.Cyan
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Lo usamos para rastrear tus stats automáticamente.",
                        color = Color.White.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isSubmitting,
                    onClick = {
                        val tournament = selectedTournament ?: return@Button
                        val user = UserManager.getUser()
                        val request = TournamentInscriptionRequest(
                            alias = nickname,
                            idUser = user?.id,
                            idTournament = tournament.id,
                            idTeam = UserManager.getSelectedTeam()?.id,
                            email = user?.email
                        )
                        isSubmitting = true
                        scope.launch {
                            try {
                                android.util.Log.d("TournamentInscription", "Sending POST: $request")
                                CompetitionsApi.createTournamentInscription(request)
                                android.util.Log.d("TournamentInscription", "✅ Inscription created successfully")
                            } catch (e: Exception) {
                                android.util.Log.e("TournamentInscription", "❌ Error: ${e.message}")
                            }
                            isSubmitting = false
                            showDialog = false
                            val link = tournament.link?.takeIf { it.isNotBlank() }
                                ?: "https://webesports.madridingame.es/esports-center/"
                            android.util.Log.d("TournamentInscription", "Opening URL: $link")
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("Continuar e Inscribirme", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = stringResource(R.string.close_label), color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        item {
            Text(
                text = localizedString("competitions.tournaments", R.string.tournaments_title),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        if (tournaments.isEmpty()) {
            item {
                Text(
                    text = localizedString("competitions.subheadings.notFound", R.string.no_tournaments_available),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            items(tournaments) { tournament ->
                TournamentCard(tournament) {
                    nickname = UserManager.getUser()?.username ?: ""
                    selectedTournament = tournament
                    showDialog = true
                }
            }
        }
    }
}

@Composable
fun TournamentCard(tournament: Tournament, onSubscribeTapped: () -> Unit) {
    val (day, month) = getDayAndMonth(tournament.tournamentDate)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(20.dp))
            .padding(8.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clickable { onSubscribeTapped() }
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            DateBox(day = day, month = month)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(tournament.name.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 4.dp))
                Text("${tournament.status?.capitalize()}", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSubscribeTapped() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(localizedString("competitions.events.eventLayoutSignIn", R.string.tournament_enroll), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(FeatherIcons.ArrowRightCircle, contentDescription = null, tint = Color.White)
        }
    }
}

// ─── Bottom nav ───────────────────────────────────────────────────────────────

@Composable
fun BottomNavigationTabs(viewModel: CompetitionDetailViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.93f), Color.Black.copy(alpha = 0.82f))))
    ) {
        NavigationBar(modifier = Modifier.fillMaxWidth(), containerColor = Color.Transparent, contentColor = Color.White) {
            val tabs = listOf(
                localizedString("competitions.overview", R.string.overview_section_label)   to Icons.Default.Info,
                localizedString("competitions.details", R.string.details_section_label)    to Icons.Default.List,
                localizedString("competitions.rules", R.string.rules_section_label)        to Icons.Default.Rule,
                localizedString("competitions.contact", R.string.contact_section_label)    to Icons.Default.Email,
                localizedString("competitions.tournaments", R.string.tournament_section_label) to FontAwesomeIcons.Solid.Trophy
            )
            tabs.forEachIndexed { index, (title, icon) ->
                NavigationBarItem(
                    modifier = Modifier.size(22.dp),
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                    icon = { Icon(icon, contentDescription = title, tint = if (selectedTab == index) Color.Cyan else Color.White) },
                    label = { Text(title, color = if (selectedTab == index) Color.Cyan else Color.White) },
                    selected = selectedTab == index,
                    onClick = { viewModel.selectTab(index) }
                )
            }
        }
    }
}

// ─── Shared composables ───────────────────────────────────────────────────────

@Composable
fun DateBox(day: String, month: String) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .background(Brush.linearGradient(listOf(Color(0xFFFF007F), Color(0xFF8000FF))), shape = RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(day, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(month.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1)
        }
    }
}

@Composable
fun ScrollableTextContent(content: String) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text(text = cleanHtml(content), color = Color.White, style = MaterialTheme.typography.bodyLarge)
    }
}

// ─── PDF Viewer ───────────────────────────────────────────────────────────────

@Composable
fun PdfViewer(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var pages by remember(url) { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember(url) { mutableStateOf(true) }
    var loadFailed by remember(url) { mutableStateOf(false) }

    val widthPx = with(density) {
        (context.resources.displayMetrics.widthPixels - 48.dp.roundToPx()).coerceAtLeast(100)
    }

    LaunchedEffect(url) {
        withContext(Dispatchers.IO) {
            try {
                val client = getUnsafeOkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val bytes = response.body?.bytes() ?: throw Exception("Empty body")
                response.close()

                val tempFile = File(context.cacheDir, "pdf_${url.hashCode()}.pdf")
                tempFile.writeBytes(bytes)

                val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)

                val rendered = (0 until renderer.pageCount).map { i ->
                    val page = renderer.openPage(i)
                    val scale = widthPx.toFloat() / page.width
                    val bmp = Bitmap.createBitmap(widthPx, (page.height * scale).toInt(), Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    bmp
                }
                renderer.close()
                pfd.close()

                withContext(Dispatchers.Main) {
                    pages = rendered
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadFailed = true
                    isLoading = false
                }
            }
        }
    }

    DisposableEffect(url) {
        onDispose { pages.forEach { it.recycle() } }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f)),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator(color = Color.Cyan, modifier = Modifier.padding(32.dp))
            loadFailed -> Text(
                text = localizedString("competitions.subheadings.notFound", R.string.no_information_available),
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(24.dp)
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                pages.forEach { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ─── Pure helpers ─────────────────────────────────────────────────────────────

fun getDayAndMonth(dateString: String?): Pair<String, String> {
    if (dateString.isNullOrEmpty()) return Pair("?", "?")
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateString)
        Pair(
            SimpleDateFormat("dd", Locale.getDefault()).format(date ?: return Pair("?", "?")),
            SimpleDateFormat("MMMM", Locale("es", "ES")).format(date)
        )
    } catch (e: Exception) { Pair("?", "?") }
}

fun getFormattedDate(dateString: String?, context: Context): String {
    if (dateString.isNullOrEmpty()) return context.getString(R.string.unknown_date)
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(dateString)
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date ?: return context.getString(R.string.invalid_date))
    } catch (e: Exception) { context.getString(R.string.invalid_date) }
}
