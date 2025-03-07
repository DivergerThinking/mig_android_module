package com.diverger.mig_android_sdk.ui.teams.news

import NewsViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.diverger.mig_android_sdk.data.NewsModel
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme

@Composable
fun NewsScreen(viewModel: NewsViewModel = viewModel()) {
    val allNews by viewModel.allNews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedNews by remember { mutableStateOf<NewsModel?>(null) }

    MIGAndroidSDKTheme {
        Column(
            Modifier.background(Color.Black)
        ) {
            Text(
                "NOTICIAS",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                modifier = Modifier.padding(bottom = 10.dp, start = 16.dp),
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                when {
                    isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                    allNews.isEmpty() -> Text("No hay noticias disponibles", color = Color.White, modifier = Modifier.align(Alignment.Center))
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp).padding(horizontal = 5.dp)) {
                            items(allNews) { news ->
                                NewsItem(news, onNewsSelected = { selectedNews = it })
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // 📌 Pantalla de detalles de noticia en BottomSheet
    selectedNews?.let {
        NewsDetailScreen(news = it, onDismiss = { selectedNews = null })
    }
}

@Composable
fun NewsItem(news: NewsModel, onNewsSelected: (NewsModel) -> Unit) {
    Card (
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNewsSelected(news) }
                .background(Color.White.copy(0.1f))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://webesports.madridingame.es/cms/assets/${news.image ?: ""}")
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagen de noticia",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(news.title ?: "Sin título", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(6.dp))
                Text(news.body ?: "Sin descripción", color = Color.Gray, style = MaterialTheme.typography.bodySmall, maxLines = 3)
            }

            Icon(Icons.Default.ArrowForward, contentDescription = "Ver más", tint = Color.White)
        }
    }
}
