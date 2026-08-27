package org.muslim.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import org.muslim.app.BuildConfig
import org.muslim.app.R
import org.muslim.app.crash.CrashReportDialog
import org.muslim.app.core.common.prayer.Prayer
import org.muslim.app.core.datastore.AppThemeMode
import org.muslim.app.core.ui.theme.AppTheme
import org.muslim.app.feature.prayertimes.ui.home.HomeScreen
import org.muslim.app.feature.prayertimes.ui.location.LocationScreen
import org.muslim.app.feature.prayertimes.ui.settings.PrayerSettingsScreen
import org.muslim.app.feature.adhkar.ui.AdhkarScreen
import org.muslim.app.feature.hadith.ui.HadithScreen
import org.muslim.app.feature.learn.ui.LearnScreen
import org.muslim.app.feature.learn.ui.FamilyLifeScreen
import org.muslim.app.feature.learn.ui.FuneralWillScreen
import org.muslim.app.feature.learn.ui.NooraniNewMuslimScreen
import org.muslim.app.feature.learn.ui.TravelerExpatsScreen
import org.muslim.app.feature.qibla.ui.QiblaScreen
import org.muslim.app.feature.quran.ui.BookmarksScreen
import org.muslim.app.feature.ramadan.ui.HabitTrackerScreen
import org.muslim.app.feature.ramadan.ui.RamadanScreen
import org.muslim.app.feature.quran.ui.QuranDownloadsScreen
import org.muslim.app.feature.quran.ui.QuranReaderScreen
import org.muslim.app.feature.quran.ui.SurahListScreen
import org.muslim.app.feature.reference.ui.IslamicHistoryScreen
import org.muslim.app.feature.reference.ui.ReferenceScreen
import org.muslim.app.feature.scholarlibrary.ui.ScholarBookDetailScreen
import org.muslim.app.feature.scholarlibrary.ui.ScholarLibraryScreen
import org.muslim.app.feature.scholarlibrary.ui.ScholarStudyDeskScreen
import org.muslim.app.feature.settings.AccessibilityScreen
import org.muslim.app.feature.settings.AboutScreen
import org.muslim.app.feature.settings.NotificationSettingsScreen
import org.muslim.app.feature.settings.PermissionsScreen
import org.muslim.app.feature.settings.PrivacyScreen
import org.muslim.app.feature.settings.SettingsScreen
import org.muslim.app.feature.settings.SmartDevicesScreen
import org.muslim.app.feature.settings.update.UpdateScreen
import org.muslim.app.feature.tasbih.ui.TasbihScreen
import org.muslim.app.feature.finance.ui.IslamicFinanceScreen
import org.muslim.app.feature.zakat.ui.ZakatScreen

private data class Tab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

private val tabs = listOf(
    Tab("home", R.string.tab_home, Icons.Default.Home),
    Tab("quran", R.string.tab_quran, Icons.AutoMirrored.Filled.MenuBook),
    Tab("qibla", R.string.tab_qibla, Icons.Default.Explore),
    Tab("more", R.string.tab_more, Icons.Default.MoreHoriz),
)

/** Returns [preferred] if it is one of the real tab routes, else "home". */
private fun startDestinationFor(preferred: String): String =
    if (tabs.any { it.route == preferred }) preferred else "home"

private const val READER_ROUTE = "quran/reader"
private const val BOOKMARKS_ROUTE = "quran/bookmarks"
private const val SETTINGS_ROUTE = "settings"
private const val ACCESSIBILITY_ROUTE = "accessibility"
private const val SMART_DEVICES_ROUTE = "settings/smart-devices"
private const val PRAYER_SETTINGS_ROUTE = "settings/prayer"
private const val NOTIFICATIONS_ROUTE = "settings/notifications"
private const val PERMISSIONS_ROUTE = "settings/permissions"
private const val MORE_ORDER_ROUTE = "settings/more-order"
private const val ABOUT_ROUTE = "settings/about"
private const val PRIVACY_ROUTE = "settings/privacy"
private const val UPDATE_ROUTE = "settings/update"
private const val HADITH_ROUTE = "hadith"
private const val ADHKAR_ROUTE = "adhkar"
private const val TASBIH_ROUTE = "tasbih"
private const val RAMADAN_ROUTE = "ramadan"
private const val HABITS_ROUTE = "habits"
private const val ZAKAT_ROUTE = "zakat"
private const val ISLAMIC_FINANCE_ROUTE = "finance"
private const val LEARN_ROUTE = "learn"
private const val FAMILY_LIFE_ROUTE = "learn/family-life"
private const val FUNERAL_WILL_ROUTE = "learn/funeral-will"
private const val NOORANI_NEW_MUSLIM_ROUTE = "learn/noorani-new-muslim"
private const val TRAVELER_EXPAT_ROUTE = "learn/traveler-expat"
private const val REFERENCE_ROUTE = "reference"
private const val ISLAMIC_HISTORY_ROUTE = "history"
private const val QURAN_DOWNLOADS_ROUTE = "quran/downloads"
private const val SCHOLAR_LIBRARY_ROUTE = "scholar-library"
private const val SCHOLAR_LIBRARY_BOOK_ROUTE = "scholar-library/book"
private const val SCHOLAR_LIBRARY_STUDY_ROUTE = "scholar-library/study"

@Composable
fun MuslimApp(
    modifier: Modifier = Modifier,
    initialRoute: String = "home",
    /**
     * The user-chosen start tab, snapshotted once at cold start (see
     * [org.muslim.app.MainActivity]). It is read on the very first composition
     * and never re-read reactively, so changing it in Settings does **not**
     * navigate to it — the choice applies on the next app launch only.
     */
    initialStartTab: String = "home",
    onLanguageChanged: () -> Unit = {},
) {
    val navController = rememberNavController()
    val viewModel: MainViewModel = hiltViewModel()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val preferences by viewModel.appPreferences.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
        highContrast = preferences.accessibilityHighContrast,
        accessibilityReadingMode = preferences.accessibilityReadingMode,
        colorPalette = preferences.colorPalette,
        cardCornerStyle = preferences.cardCornerStyle,
    ) {
        Scaffold(
            modifier = modifier,
            floatingActionButton = {
                if (preferences.voiceNavigationEnabled) {
                    VoiceNavigationButton(onTarget = { target ->
                        when (target) {
                            is VoiceNavigationTarget.Route -> navController.navigate(target.route) {
                                launchSingleTop = true
                            }
                            is VoiceNavigationTarget.Reader -> navController.navigate(
                                "$READER_ROUTE/${target.surahNumber}",
                            ) {
                                launchSingleTop = true
                            }
                        }
                    })
                }
            },
            bottomBar = {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination
                val onTab = tabs.any { currentDestination?.route == it.route }
                if (onTab) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 1.dp,
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                        ) {
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
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                // The user-chosen start tab (default: prayer-times home), validated
                // against the real tab routes so a stale value can never crash.
                startDestination = startDestinationFor(initialStartTab),
                navController = navController,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable("home") {
                    HomeScreen(
                        onSelectLocation = { navController.navigate("location") },
                        onConfigurePrayerAlert = { prayer ->
                            navController.navigate("$PRAYER_SETTINGS_ROUTE?alertPrayer=${prayer.name}")
                        },
                    )
                }
                composable("quran") {
                    SurahListScreen(
                        onOpenSurah = { number -> navController.navigate("$READER_ROUTE/$number") },
                        onPlaySurah = { number -> navController.navigate("$READER_ROUTE/$number?autoplay=true") },
                        onOpenBookmarks = { navController.navigate(BOOKMARKS_ROUTE) },
                        onResumeReading = { surah, global ->
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
                    route = "$READER_ROUTE/{surahNumber}?ayah={ayah}&autoplay={autoplay}",
                    arguments = listOf(
                        navArgument("surahNumber") { type = NavType.IntType },
                        navArgument("ayah") {
                            type = NavType.IntType
                            defaultValue = -1
                        },
                        navArgument("autoplay") {
                            type = NavType.BoolType
                            defaultValue = false
                        },
                    ),
                ) {
                    QuranReaderScreen(
                            onBack = { navController.popBackStack() },
                            onOpenDownloads = { navController.navigate(QURAN_DOWNLOADS_ROUTE) },
                        )
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
                        onOpenHabits = { navController.navigate(HABITS_ROUTE) },
                        onOpenZakat = { navController.navigate(ZAKAT_ROUTE) },
                        onOpenIslamicFinance = { navController.navigate(ISLAMIC_FINANCE_ROUTE) },
                        onOpenLearn = { navController.navigate(LEARN_ROUTE) },
                        onOpenFamily = { navController.navigate(FAMILY_LIFE_ROUTE) },
                        onOpenFuneralWill = { navController.navigate(FUNERAL_WILL_ROUTE) },
                        onOpenNoorani = { navController.navigate(NOORANI_NEW_MUSLIM_ROUTE) },
                        onOpenTraveler = { navController.navigate(TRAVELER_EXPAT_ROUTE) },
                        onOpenReference = { navController.navigate(REFERENCE_ROUTE) },
                        onOpenIslamicHistory = { navController.navigate(ISLAMIC_HISTORY_ROUTE) },
                        onOpenScholarLibrary = { navController.navigate(SCHOLAR_LIBRARY_ROUTE) },
                        onOpenAccessibility = { navController.navigate(ACCESSIBILITY_ROUTE) },
                        onOpenDownloads = { navController.navigate(QURAN_DOWNLOADS_ROUTE) },
                        sectionOrder = preferences.moreSectionOrder,
                        hiddenSections = preferences.hiddenMoreSections,
                    )
                }
                composable(SETTINGS_ROUTE) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onOpenPrayerSettings = { navController.navigate(PRAYER_SETTINGS_ROUTE) },
                        onOpenNotifications = { navController.navigate(NOTIFICATIONS_ROUTE) },
                        onOpenPermissions = { navController.navigate(PERMISSIONS_ROUTE) },
                        onOpenAbout = { navController.navigate(ABOUT_ROUTE) },
                        onOpenPrivacy = { navController.navigate(PRIVACY_ROUTE) },
                        onOpenMoreOrder = { navController.navigate(MORE_ORDER_ROUTE) },
                        onOpenUpdates = { navController.navigate(UPDATE_ROUTE) },
                        onOpenAccessibility = { navController.navigate(ACCESSIBILITY_ROUTE) },
                        onOpenSmartDevices = { navController.navigate(SMART_DEVICES_ROUTE) },
                        onLanguageChanged = onLanguageChanged,
                    )
                }
                composable(ACCESSIBILITY_ROUTE) {
                    AccessibilityScreen(onBack = { navController.popBackStack() })
                }
                composable(SMART_DEVICES_ROUTE) {
                    SmartDevicesScreen(onBack = { navController.popBackStack() })
                }
                composable(MORE_ORDER_ROUTE) {
                    MoreOrderScreen(onBack = { navController.popBackStack() })
                }
                composable(NOTIFICATIONS_ROUTE) {
                    NotificationSettingsScreen(onBack = { navController.popBackStack() })
                }
                composable(PERMISSIONS_ROUTE) {
                    PermissionsScreen(onBack = { navController.popBackStack() })
                }
                composable(SCHOLAR_LIBRARY_ROUTE) {
                    ScholarLibraryScreen(
                        onBack = { navController.popBackStack() },
                        onOpenBook = { bookId -> navController.navigate("$SCHOLAR_LIBRARY_BOOK_ROUTE/$bookId") },
                        onOpenStudyDesk = { navController.navigate(SCHOLAR_LIBRARY_STUDY_ROUTE) },
                    )
                }
                composable(
                    route = "$SCHOLAR_LIBRARY_BOOK_ROUTE/{bookId}",
                    arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
                ) { entry ->
                    ScholarBookDetailScreen(
                        bookId = entry.arguments?.getString("bookId").orEmpty(),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(SCHOLAR_LIBRARY_STUDY_ROUTE) {
                    ScholarStudyDeskScreen(onBack = { navController.popBackStack() })
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
                composable(HABITS_ROUTE) {
                    HabitTrackerScreen(onBack = { navController.popBackStack() })
                }
                composable(ZAKAT_ROUTE) {
                    ZakatScreen(onBack = { navController.popBackStack() })
                }
                composable(ISLAMIC_FINANCE_ROUTE) {
                    IslamicFinanceScreen(onBack = { navController.popBackStack() })
                }
                composable(LEARN_ROUTE) {
                    LearnScreen(onBack = { navController.popBackStack() })
                }
                composable(FAMILY_LIFE_ROUTE) {
                    FamilyLifeScreen(onBack = { navController.popBackStack() })
                }
                composable(FUNERAL_WILL_ROUTE) {
                    FuneralWillScreen(onBack = { navController.popBackStack() })
                }
                composable(NOORANI_NEW_MUSLIM_ROUTE) {
                    NooraniNewMuslimScreen(onBack = { navController.popBackStack() })
                }
                composable(TRAVELER_EXPAT_ROUTE) {
                    TravelerExpatsScreen(
                        onBack = { navController.popBackStack() },
                        onOpenPrayerSettings = { navController.navigate(PRAYER_SETTINGS_ROUTE) },
                    )
                }
                composable(REFERENCE_ROUTE) {
                    ReferenceScreen(onBack = { navController.popBackStack() })
                }
                composable(ISLAMIC_HISTORY_ROUTE) {
                    IslamicHistoryScreen(onBack = { navController.popBackStack() })
                }
                composable(QURAN_DOWNLOADS_ROUTE) {
                    QuranDownloadsScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    route = "$PRAYER_SETTINGS_ROUTE?alertPrayer={alertPrayer}",
                    arguments = listOf(
                        navArgument("alertPrayer") {
                            type = NavType.StringType
                            defaultValue = ""
                        },
                    ),
                ) { entry ->
                    val initialCustomizingPrayer = entry.arguments
                        ?.getString("alertPrayer")
                        ?.let { name -> runCatching { Prayer.valueOf(name) }.getOrNull() }
                    PrayerSettingsScreen(
                        onOpenLocation = { navController.navigate("location") },
                        initialCustomizingPrayer = initialCustomizingPrayer,
                    )
                }
                composable(ABOUT_ROUTE) {
                    AboutScreen(
                        onBack = { navController.popBackStack() },
                        versionName = BuildConfig.VERSION_NAME,
                    )
                }
                composable(PRIVACY_ROUTE) {
                    PrivacyScreen(onBack = { navController.popBackStack() })
                }
                composable(UPDATE_ROUTE) {
                    UpdateScreen(onBack = { navController.popBackStack() })
                }
                composable("location") {
                    LocationScreen(onSaved = { navController.popBackStack() })
                }
            }
        }
        CrashReportDialog()
    }
}
