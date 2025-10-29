package com.diverger.mig_android_sdk

import CompetitionDetailScreen
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.diverger.mig_android_sdk.data.User
import com.diverger.mig_android_sdk.data.MadridInGameUserData
import com.diverger.mig_android_sdk.data.UserManager
import com.diverger.mig_android_sdk.ui.competitions.CompetitionsScreen
import com.diverger.mig_android_sdk.ui.profile.ProfileScreen
import com.diverger.mig_android_sdk.ui.reservations.IndividualReservationsScreen
import com.diverger.mig_android_sdk.ui.teams.TeamsScreen
import com.diverger.mig_android_sdk.ui.theme.MIGAndroidSDKTheme
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.XCircle
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Trophy
import compose.icons.fontawesomeicons.solid.UserCircle
import compose.icons.fontawesomeicons.solid.Users
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MadridInGameAndroidModule(email: String,
                              userName: String? = null,
                              dni: String? = null,
                              accessToken: String,
                              logoMIG: Int? = null,
                              qrMiddleLogo: Int? = null) {
    val madridInGameUserData = MadridInGameUserData(
        email = email,
        userName = userName ?: "",
        dni = dni,
        logoMIG = logoMIG,
        qrMiddleLogo = qrMiddleLogo
    )
    MIGAndroidSDKScreen(madridInGameUserData, accessToken = accessToken)
}

@Composable
fun MIGAndroidSDKScreen(madridInGameUserData: MadridInGameUserData,
                        accessToken: String) {
    val viewModel: MIGSDKViewModel = viewModel()
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(madridInGameUserData.email, accessToken) {
        viewModel.initializeUser(
            madridInGameUserData,
            accessToken = accessToken
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            errorMessage != null -> {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(stringResource(R.string.module_load_error), color = MaterialTheme.colorScheme.error)
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
fun MainScreen(user: User) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("dashboard") }

    val activity = LocalActivity.current

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
                    },
                    actions = {
                        MIGAndroidSDKTheme {
                            TextButton(
                                onClick = { activity?.finish() }
                            ) {
                                Text(
                                    text = stringResource(R.string.exit),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
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
    val items = mutableListOf(
        DrawerItem("dashboard", stringResource(R.string.dashboard), Icons.Filled.Home),
        DrawerItem("competitions",
            stringResource(R.string.competitions), FontAwesomeIcons.Solid.Trophy)
    )

    if (UserManager.getUser()?.teams?.isNotEmpty() == true) {
        items.add(1, DrawerItem("teams",
            stringResource(R.string.teams), FontAwesomeIcons.Solid.Users))
    }

    MIGAndroidSDKTheme {
        ModalDrawerSheet(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            drawerContainerColor = Color.Black,
            drawerShape = RoundedCornerShape(1.dp),

            ) {
            Spacer(modifier = Modifier.height(32.dp))

            /*Text(
                text = "Menú",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
            )*/

//            Text(
//                text = "x",
//                color = Color.White,
//                style = MaterialTheme.typography.headlineMedium,
//                modifier = Modifier
//                    .padding(start = 16.dp, bottom = 16.dp)
//                    .clickable {
//                        scope.launch { drawerState.close() }
//                    }
//            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = {
                    scope.launch { drawerState.close() }
                }) {
                    Icon(FeatherIcons.XCircle, contentDescription = "Cerrar", tint = Color.White)
                }
            }

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
fun DashboardNavigation(user: User, modifier: Modifier) {
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
    user: User,
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
        BottomNavItem("individualReservations",
            stringResource(R.string.bookings), Icons.Default.DateRange),
        BottomNavItem("profile", stringResource(R.string.profile), FontAwesomeIcons.Solid.UserCircle)
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.93f),
                        Color.Black.copy(alpha = 0.82f)
                    )
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
