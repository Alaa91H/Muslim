package org.example.islamicapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Schedule
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.example.islamicapp.BuildConfig
import org.example.islamicapp.R
import org.example.islamicapp.feature.adhkar.ui.AdhkarScreen
import org.example.islamicapp.feature.hadith.ui.HadithScreen
import org.example.islamicapp.feature.learn.ui.LearnScreen
import org.example.islamicapp.feature.prayertimes.ui.home.HomeScreen
import org.example.islamicapp.feature.prayertimes.ui.location.LocationScreen
import org.example.islamicapp.feature.prayertimes.ui.settings.PrayerSettingsScreen
import org.example.islamicapp.feature.prayertimes.ui.times.PrayerTimesScreen
import org.example.islamicapp.feature.qibla.ui.QiblaScreen
import org.example.islamicapp.feature.quran.ui.BookmarksScreen
import org.example.islamicapp.feature.quran.ui.QuranReaderScreen
import org.example.islamicapp.feature.quran.ui.SearchScreen
import org.example.islamicapp.feature.quran.ui.SurahListScreen
import org.example.islamicapp.feature.ramadan.ui.RamadanScreen
import org.example.islamicapp.feature.settings.ui.AboutScreen
import org.example.islamicapp.feature.tasbih.ui.TasbihScreen
import org.example.islamicapp.feature.zakat.ui.ZakatScreen

private data class Tab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

// Five core tabs (Material guidance); every other feature lives in "More".
private val tabs = listOf(
    Tab("home", R.string.tab_home, Icons.Default.Home),
    Tab("quran", R.string.tab_quran, Icons.AutoMirrored.Filled.MenuBook),
    Tab("times", R.string.tab_times, Icons.Default.Schedule),
    Tab("qibla", R.string.tab_qibla, Icons.Default.Explore),
    Tab("more", R.string.tab_more, Icons.Default.Apps),
)

private const val READER_ROUTE = "quran/reader"
private const val SEARCH_ROUTE = "quran/search"
private const val BOOKMARKS_ROUTE = "quran/bookmarks"

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
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            val onTab = tabs.any { currentDestination?.route == it.route }
            if (onTab) {
                NavigationBar {
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
            composable("quran") {
                SurahListScreen(
                    onOpenSurah = { number -> navController.navigate("$READER_ROUTE/$number") },
                    onOpenSearch = { navController.navigate(SEARCH_ROUTE) },
                    onOpenBookmarks = { navController.navigate(BOOKMARKS_ROUTE) },
                    onResumeReading = { surah, global ->
                        navController.navigate("$READER_ROUTE/$surah?ayah=$global")
                    },
                )
            }
            composable(SEARCH_ROUTE) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenAyah = { surah, global ->
                        navController.navigate("$READER_ROUTE/$surah?ayah=$global")
                    },
                )
            }
            composable(BOOKMARKS_ROUTE) {
                BookmarksScreen(
                    onBack = { navController.popBackStack() },
                    onOpenAyah = { surah, global ->
                        navController.navigate("$READER_ROUTE/$surah?ayah=$global")
                    },
                )
            }
            composable(
                route = "$READER_ROUTE/{surahNumber}?ayah={ayah}",
                arguments = listOf(
                    navArgument("surahNumber") { type = NavType.IntType },
                    navArgument("ayah") {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                ),
            ) {
                QuranReaderScreen(onBack = { navController.popBackStack() })
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
            composable("more") {
                MoreScreen(onOpen = { route -> navController.navigate(route) })
            }
            composable("adhkar") {
                AdhkarScreen(onBack = { navController.popBackStack() })
            }
            composable("tasbih") {
                TasbihScreen()
            }
            composable("hadith") {
                HadithScreen()
            }
            composable("learn") {
                LearnScreen()
            }
            composable("ramadan") {
                RamadanScreen(
                    latitude = location?.latitude,
                    longitude = location?.longitude,
                )
            }
            composable("zakat") {
                ZakatScreen()
            }
            composable("settings") {
                PrayerSettingsScreen(onOpenLocation = { navController.navigate("location") })
            }
            composable("about") {
                AboutScreen(versionName = BuildConfig.VERSION_NAME)
            }
            composable("location") {
                LocationScreen(onSaved = { navController.popBackStack() })
            }
        }
    }
}
