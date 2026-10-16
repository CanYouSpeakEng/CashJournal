package app.cashjournal

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.cashjournal.ui.MainAppScaffold
import app.cashjournal.ui.MainViewModel
import app.cashjournal.ui.onboarding.AppLanguage
import app.cashjournal.ui.onboarding.OnboardingScreen
import app.cashjournal.ui.onboarding.WeatherOption
import app.cashjournal.ui.screens.AppLockScreen
import app.cashjournal.ui.security.BiometricAuthHelper
import app.cashjournal.ui.theme.CashJournalTheme
import app.cashjournal.ui.theme.Slate100

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private var lastBackgroundTimestamp: Long = 0L
    private val isAppLocked = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent?.getBooleanExtra("seed", false) == true) {
            viewModel.seedExampleData()
        }

        setContent {
            CashJournalTheme {
                val state by viewModel.uiState.collectAsState()

                // Synchronize lock state with PIN configuration
                val hasPin = state.userPreferences.pinEnabled && state.userPreferences.pinHash.isNotEmpty()
                val locked = isAppLocked.value && hasPin

                // Screen Privacy (FLAG_SECURE)
                LaunchedEffect(state.userPreferences.screenPrivacyEnabled) {
                    if (state.userPreferences.screenPrivacyEnabled) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }

                // Daily Reminder scheduling
                LaunchedEffect(state.userPreferences.dailyReminderEnabled) {
                    if (state.userPreferences.dailyReminderEnabled) {
                        app.cashjournal.receiver.DailyReminderScheduler.scheduleDailyReminder(applicationContext)
                    } else {
                        app.cashjournal.receiver.DailyReminderScheduler.cancelDailyReminder(applicationContext)
                    }
                }

                // Immersive Sticky Fullscreen
                val insetsController = remember(window) {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
                LaunchedEffect(state.userPreferences.immersiveModeEnabled) {
                    if (state.userPreferences.immersiveModeEnabled) {
                        insetsController.hide(WindowInsetsCompat.Type.systemBars())
                    } else {
                        insetsController.show(WindowInsetsCompat.Type.systemBars())
                    }
                }

                if (!state.isPreferencesLoaded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Slate100)
                    )
                } else if (!state.userPreferences.onboardingComplete) {
                    OnboardingScreen(
                        onFinish = { language, currencyCode, name, railOnRight, shakeToSwap, knockSensitivity, loadExample, weatherOption, cityName, cityLat, cityLon ->
                            val weatherOptionKey = when (weatherOption) {
                                WeatherOption.APPROX_LOCATION -> "approx"
                                WeatherOption.SEARCH_CITY -> "search"
                                WeatherOption.NOT_NOW -> "not_now"
                            }
                            viewModel.updatePreferences { prefs ->
                                prefs.updateLanguage(if (language == AppLanguage.INDONESIAN) "in" else "en")
                                prefs.updateCurrencyCode(currencyCode)
                                prefs.updateUserName(name)
                                prefs.updateRailOnRight(railOnRight)
                                prefs.updateShakeToSwap(shakeToSwap)
                                prefs.updateKnockSensitivity(knockSensitivity)
                                prefs.updateWeatherOption(weatherOptionKey, cityName, cityLat, cityLon)
                                prefs.updateOnboardingComplete(true)
                                prefs.updateLastSeenVersion(BuildConfig.VERSION_NAME)
                            }
                            if (loadExample) {
                                viewModel.seedExampleData()
                            }
                            viewModel.refreshWeather()
                        }
                    )
                } else if (locked) {
                    AppLockScreen(
                        pinHash = state.userPreferences.pinHash,
                        biometricsEnabled = state.userPreferences.biometricsEnabled,
                        onUnlock = { isAppLocked.value = false },
                        languageCode = state.userPreferences.language,
                        hapticsEnabled = state.userPreferences.tactileHapticsEnabled,
                        onBiometricRequest = if (state.userPreferences.biometricsEnabled) {
                            {
                                val isIndonesian = state.userPreferences.language.equals("in", ignoreCase = true)
                                BiometricAuthHelper.promptBiometric(
                                    activity = this@MainActivity,
                                    title = if (isIndonesian) "Buka CashJournal" else "Unlock CashJournal",
                                    subtitle = if (isIndonesian) "Konfirmasi biometrik untuk membuka" else "Confirm biometric to unlock",
                                    onSuccess = { isAppLocked.value = false },
                                    onError = { /* fallback to PIN */ }
                                )
                            }
                        } else null
                    )
                } else {
                    MainAppScaffold(
                        viewModel = viewModel,
                        onLockApp = {
                            if (hasPin) {
                                isAppLocked.value = true
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (lastBackgroundTimestamp > 0L) {
            val elapsed = System.currentTimeMillis() - lastBackgroundTimestamp
            if (elapsed > 15_000L) {
                isAppLocked.value = true
            }
        }
    }

    override fun onStop() {
        super.onStop()
        lastBackgroundTimestamp = System.currentTimeMillis()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("seed", false)) {
            viewModel.seedExampleData()
        }
    }
}
