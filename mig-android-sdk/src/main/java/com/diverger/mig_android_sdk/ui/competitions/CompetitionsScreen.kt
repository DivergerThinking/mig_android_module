package com.diverger.mig_android_sdk.ui.competitions

import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.diverger.mig_android_sdk.data.Competition
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CompetitionsScreen(navController: NavController, viewModel: CompetitionsViewModel = viewModel()) {
    val competitions by viewModel.competitions.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val availableYears by viewModel.availableYears.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // 📌 Título
        Text(
            "COMPETICIONES",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .shadow(5.dp, shape = RoundedCornerShape(10.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 📅 Dropdown de selección de año
        DropdownMenuComponent(selectedYear, availableYears, onYearSelected = { viewModel.updateSelectedYear(it) })

        Spacer(modifier = Modifier.height(16.dp))

        // 📌 Contenido de competiciones (Loading, Error o Lista de competiciones)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                isLoading -> CircularProgressIndicator(color = Color.Cyan) // 🔄 Indicador de carga
                !errorMessage.isNullOrEmpty() -> Text(text = errorMessage!!, color = Color.Red, fontWeight = FontWeight.Bold)
                competitions.isEmpty() -> Text("No hay competiciones disponibles", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(competitions) { competition ->
                        CompetitionItem(competition) {
                            navController.navigate("competition_detail/${competition.id}")
                        }
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
            .background(Color.Gray.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedYear.ifEmpty { "Selecciona un año" },
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir", tint = Color.White)
                }
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year, color = Color.Black) },
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
fun CompetitionItem(competition: Competition, onClick: () -> Unit) {
    val formattedDate = formatDate(competition.startDate)
    val overviewText = cleanHtml(competition.overview ?: "Sin descripción disponible")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = competition.title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fecha: $formattedDate",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = overviewText,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// 📅 **Función para formatear la fecha a dd/MM/yyyy**
fun formatDate(dateString: String?): String {
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

// 📅 **Función para formatear la fecha a dd/MM/yyyy desde yyy-MM-dd**
fun formatDateFromShort(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Fecha desconocida"

    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: return "Fecha inválida")
    } catch (e: Exception) {
        "Fecha inválida"
    }
}

// 📝 **Función para limpiar el HTML del overview**
fun cleanHtml(html: String): String {
    return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
}
