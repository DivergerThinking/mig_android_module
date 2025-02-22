package com.diverger.mig_android_sdk.ui.teams

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.diverger.mig_android_sdk.ui.teams.playersTeam.PlayersTeamScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(user: com.diverger.mig_android_sdk.data.User) {
    val navController = rememberNavController()

    Scaffold(
        /*topBar = {
            TopAppBar(
                title = { Text("Equipo") }
            )
        },*/
        bottomBar = { TeamBottomNavigation(navController) }
    ) { innerPadding ->
        TeamNavigationGraph(navController, Modifier.padding(innerPadding), user)
    }
}

@Composable
fun TeamBottomNavigation(navController: NavHostController) {
    val items = listOf(
        TeamNavItem("training", "Entrenamiento", Icons.Filled.DateRange),
        TeamNavItem("players", "Jugadores", Icons.AutoMirrored.Filled.List)
    )

    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}

@Composable
fun TeamNavigationGraph(navController: NavHostController, modifier: Modifier, user: com.diverger.mig_android_sdk.data.User) {
    NavHost(navController, startDestination = "training", modifier = modifier) {
        composable("training") { TeamTrainingScreen() }
        composable("players") { PlayersTeamScreen() }
    }
}

// Modelo para los ítems de la Bottom Bar
data class TeamNavItem(val route: String, val label: String, val icon: ImageVector)
