package com.diverger.mig_android_sdk.ui.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.diverger.mig_android_sdk.ui.teams.playersTeam.PlayersTeamScreen
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.PeopleCarry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(user: com.diverger.mig_android_sdk.data.User) {
    val navController = rememberNavController()

    MIGAndroidSDKTheme {
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
}

@Composable
fun TeamBottomNavigation(navController: NavHostController) {
    val items = listOf(
        TeamNavItem("training", "Entrenamiento", Icons.Default.DateRange),
        TeamNavItem("players", "Jugadores", FontAwesomeIcons.Solid.PeopleCarry)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.98f), Color.Black.copy(alpha = 0.87f))
                )
            )
    ) {
    NavigationBar (
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                modifier = Modifier.size(22.dp),
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                icon = { Icon(imageVector = item.icon, contentDescription = item.label, tint = if (currentRoute == item.route) Color.Cyan else Color.White) },
                label = { Text(item.label, color = if (currentRoute == item.route) Color.Cyan else Color.White) },
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) }
            )
        }
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
