package org.example.islamicapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.islamicapp.R
import org.example.islamicapp.feature.prayertimes.ui.home.HomeScreen
import org.example.islamicapp.feature.prayertimes.ui.location.LocationScreen
import org.example.islamicapp.feature.prayertimes.ui.settings.PrayerSettingsScreen
import org.example.islamicapp.feature.prayertimes.ui.times.PrayerTimesScreen
import org.example.islamicapp.feature.qibla.ui.QiblaScreen

private data class Tab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

private val tabs = listOf(
    Tab("home", R.string.tab_home, Icons.Default.Home),
    Tab("times", R.string.tab_times, Icons.Default.Schedule),
    Tab("qibla", R.string.tab_qibla, Icons.Default.Explore),
    Tab("settings", R.string.tab_settings, Icons.Default.Settings),
)

@Composable
fun ManaraApp(
    initialRoute: String = "home",
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = hiltViewModel()
    val location by viewModel.location.collectAsStateWithLifecycle()

    // Route to the tab requested by an App Shortcut (cold start or onNewIntent).
    LaunchedEffect(initialRoute) {
        if (initialRoute != "home") {
            navController.navigate(initialRoute) {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                HomeScreen(onSelectLocation = { navController.navigate("location") })
            }
            composable("times") {
                PrayerTimesScreen()
            }
            composable("qibla") {
                val selected = location
                if (selected == null) {
                    HomeScreen(onSelectLocation = { navController.navigate("location") })
                } else {
                    QiblaScreen(
                        latitude = selected.latitude,
                        longitude = selected.longitude,
                        locationName = selected.name,
                    )
                }
            }
            composable("settings") {
                PrayerSettingsScreen(onOpenLocation = { navController.navigate("location") })
            }
            composable("location") {
                LocationScreen(onSaved = { navController.popBackStack() })
            }
        }
    }
}
