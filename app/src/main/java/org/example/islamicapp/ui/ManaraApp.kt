package org.example.islamicapp.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
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
import org.example.islamicapp.R
import org.example.islamicapp.core.datastore.AppThemeMode
import org.example.islamicapp.core.ui.theme.AppTheme
import org.example.islamicapp.feature.prayertimes.ui.home.HomeScreen
import org.example.islamicapp.feature.prayertimes.ui.location.LocationScreen
import org.example.islamicapp.feature.prayertimes.ui.settings.PrayerSettingsScreen
import org.example.islamicapp.feature.adhkar.ui.AdhkarScreen
import org.example.islamicapp.feature.hadith.ui.HadithScreen
import org.example.islamicapp.feature.learn.ui.LearnScreen
import org.example.islamicapp.feature.prayertimes.ui.times.PrayerTimesScreen
import org.example.islamicapp.feature.qibla.ui.QiblaScreen
import org.example.islamicapp.feature.quran.ui.BookmarksScreen
import org.example.islamicapp.feature.ramadan.ui.RamadanScreen
import org.example.islamicapp.feature.quran.ui.QuranReaderScreen
import org.example.islamicapp.feature.quran.ui.SearchScreen
import org.example.islamicapp.feature.quran.ui.SurahListScreen
import org.example.islamicapp.feature.settings.AboutScreen
import org.example.islamicapp.feature.settings.PrivacyScreen
import org.example.islamicapp.feature.settings.SettingsScreen
import org.example.islamicapp.feature.tasbih.ui.TasbihScreen
import org.example.islamicapp.feature.zakat.ui.ZakatScreen

private data class Tab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

private val tabs = listOf(
    Tab("home", R.string.tab_home, Icons.Default.Home),
    Tab("quran", R.string.tab_quran, Icons.AutoMirrored.Filled.MenuBook),
    Tab("times", R.string.tab_times, Icons.Default.Schedule),
    Tab("qibla", R.string.tab_qibla, Icons.Default.Explore),
    Tab("more", R.string.tab_more, Icons.Default.MoreHoriz),
)

private const val READER_ROUTE = "quran/reader"
private const val SEARCH_ROUTE = "quran/search"
private const val BOOKMARKS_ROUTE = "quran/bookmarks"
private const val SETTINGS_ROUTE = "settings"
private const val PRAYER_SETTINGS_ROUTE = "settings/prayer"
private const val ABOUT_ROUTE = "settings/about"
private const val PRIVACY_ROUTE = "settings/privacy"
private const val HADITH_ROUTE = "hadith"
private const val ADHKAR_ROUTE = "adhkar"
private const val TASBIH_ROUTE = "tasbih"
private const val RAMADAN_ROUTE = "ramadan"
private const val ZAKAT_ROUTE = "zakat"
private const val LEARN_ROUTE = "learn"

@Composable
fun ManaraApp(
    modifier: Modifier = Modifier,
    initialRoute: String = "home",
    onLanguageChanged: () -> Unit = {},
) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = hiltViewModel()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val preferences by viewModel.appPreferences.collectAsStateWithLifecycle()

    // Route to the tab requested by an App Shortcut (cold start or onNewIntent).
    LaunchedEffect(initialRoute) {
        if (initialRoute != "home") {
            navController.navigate(initialRoute) {
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val darkTheme = when (preferences.themeMode) {
        AppThemeMode.System -> isSystemInDarkTheme()
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }

    AppTheme(
        darkTheme = darkTheme,
        dynamicColor = preferences.dynamicColor,
    ) {
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
                    MoreScreen(
                        onOpenSettings = { navController.navigate(SETTINGS_ROUTE) },
                        onOpenHadith = { navController.navigate(HADITH_ROUTE) },
                        onOpenAdhkar = { navController.navigate(ADHKAR_ROUTE) },
                        onOpenTasbih = { navController.navigate(TASBIH_ROUTE) },
                        onOpenRamadan = { navController.navigate(RAMADAN_ROUTE) },
                        onOpenZakat = { navController.navigate(ZAKAT_ROUTE) },
                        onOpenLearn = { navController.navigate(LEARN_ROUTE) },
                    )
                }
                composable(SETTINGS_ROUTE) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onOpenPrayerSettings = { navController.navigate(PRAYER_SETTINGS_ROUTE) },
                        onOpenAbout = { navController.navigate(ABOUT_ROUTE) },
                        onOpenPrivacy = { navController.navigate(PRIVACY_ROUTE) },
                        onLanguageChanged = onLanguageChanged,
                    )
                }
                composable(HADITH_ROUTE) {
                    HadithScreen(onBack = { navController.popBackStack() })
                }
                composable(ADHKAR_ROUTE) {
                    AdhkarScreen(onBack = { navController.popBackStack() })
                }
                composable(TASBIH_ROUTE) {
                    TasbihScreen(onBack = { navController.popBackStack() })
                }
                composable(RAMADAN_ROUTE) {
                    RamadanScreen(onBack = { navController.popBackStack() })
                }
                composable(ZAKAT_ROUTE) {
                    ZakatScreen(onBack = { navController.popBackStack() })
                }
                composable(LEARN_ROUTE) {
                    LearnScreen(onBack = { navController.popBackStack() })
                }
                composable(PRAYER_SETTINGS_ROUTE) {
                    PrayerSettingsScreen(onOpenLocation = { navController.navigate("location") })
                }
                composable(ABOUT_ROUTE) {
                    AboutScreen(onBack = { navController.popBackStack() })
                }
                composable(PRIVACY_ROUTE) {
                    PrivacyScreen(onBack = { navController.popBackStack() })
                }
                composable("location") {
                    LocationScreen(onSaved = { navController.popBackStack() })
                }
            }
        }
    }
}
