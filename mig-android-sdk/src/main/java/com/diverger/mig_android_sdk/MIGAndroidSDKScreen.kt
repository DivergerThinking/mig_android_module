package com.diverger.mig_android_sdk

import CompetitionDetailScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.diverger.mig_android_sdk.data.Competition
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.ui.competitions.CompetitionsScreen
import com.diverger.mig_android_sdk.ui.dashboard.DashboardScreen
import com.diverger.mig_android_sdk.ui.profile.ProfileScreen
import com.diverger.mig_android_sdk.ui.reservations.IndividualReservationsScreen
import com.diverger.mig_android_sdk.ui.teams.TeamsScreen
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.PeopleCarry
import compose.icons.fontawesomeicons.solid.Trophy
import compose.icons.fontawesomeicons.solid.UserCircle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MIGAndroidSDKScreen(email: String) {
    val viewModel: MIGSDKViewModel = viewModel()
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(email) {
        viewModel.initializeUser(email)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            errorMessage != null -> {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text("Error al cargar el módulo", color = MaterialTheme.colorScheme.error)
                    Text(errorMessage.orEmpty(), color = MaterialTheme.colorScheme.onBackground)
                }
            }
            user != null -> {
                MainScreen(user!!)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(user: com.diverger.mig_android_sdk.data.User) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("dashboard") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController, drawerState, scope, currentScreen) { selectedScreen ->
                currentScreen = selectedScreen
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black // Fondo negro
                    ),
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menú", tint = Color.White)
                        }
                    }
                )
            }
        ) { innerPadding ->
            if (currentScreen == "dashboard") {
                DashboardNavigation(user, Modifier.padding(innerPadding))
            } else {
                NavigationGraph(navController, Modifier.padding(innerPadding), user, currentScreen)
            }
        }
    }
}

@Composable
fun DrawerContent(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    val items = listOf(
        DrawerItem("dashboard", "Dashboard", Icons.Filled.Home),
        DrawerItem("teams", "Equipos", FontAwesomeIcons.Solid.PeopleCarry),
        DrawerItem("competitions", "Competiciones", FontAwesomeIcons.Solid.Trophy)
    )

    MIGAndroidSDKTheme {
        ModalDrawerSheet(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            drawerContainerColor = Color.Black,
            drawerShape = RoundedCornerShape(1.dp),

            ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Menú",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
            )

            LazyColumn {
                items(items) { item ->
                    val isSelected = item.route == currentScreen

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp, // 🔹 Borde blanco en el seleccionado
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                color = Color.Transparent, // 🔹 Fondo transparente
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Button(
                            onClick = {
                                scope.launch { drawerState.close() }
                                onScreenSelected(item.route)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent, // 🔹 Fondo transparente para evitar color sólido
                                contentColor = Color.White // 🔹 Texto e iconos en blanco
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = Color.White, // 🔹 Iconos en blanco
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = Color.White // 🔹 Texto en blanco
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardNavigation(user: com.diverger.mig_android_sdk.data.User, modifier: Modifier) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(navController, startDestination = "individualReservations", modifier = Modifier.padding(innerPadding)) {
            //composable("dashboard") { DashboardScreen() }
            composable("individualReservations") { IndividualReservationsScreen(userId = user.id) }
            composable("profile") { ProfileScreen() }
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    modifier: Modifier,
    user: com.diverger.mig_android_sdk.data.User,
    screen: String
) {
    NavHost(navController, startDestination = screen, modifier = modifier) {
        composable("competitions") { CompetitionsScreen(navController) }
        composable("competition_detail/{competitionId}") { backStackEntry ->
            val competitionId = backStackEntry.arguments?.getString("competitionId") ?: return@composable
            CompetitionDetailScreen(navController, competitionId)
        }
        composable("teams") { TeamsScreen(user) }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        //BottomNavItem("dashboard", "Dashboard", Icons.Default.Home),
        BottomNavItem("individualReservations", "Reservas", Icons.Default.DateRange),
        BottomNavItem("profile", "Perfil", FontAwesomeIcons.Solid.UserCircle)
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

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
            containerColor = Color.Transparent, // ✅ Hace que el fondo de NavigationBar sea transparente
            contentColor = Color.White // ✅ Cambia el color de los íconos y texto
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    modifier = Modifier.size(22.dp),
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (currentRoute == item.route) Color.Cyan else Color.White
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            color = if (currentRoute == item.route) Color.Cyan else Color.White
                        )
                    },
                    selected = currentRoute == item.route,
                    onClick = { navController.navigate(item.route) }
                )
            }
        }
    }
}

data class DrawerItem(val route: String, val label: String, val icon: ImageVector)
data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)
