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
import androidx.compose.ui.draw.alpha
import app.cashjournal.ui.theme.Slate100

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent?.getBooleanExtra("seed", false) == true) {
            try {
                viewModel.seedExampleData()
            } catch (_: Exception) {}
        }

        setContent {
            CashJournalTheme {
                val state by viewModel.uiState.collectAsState()
                val isAppLocked by viewModel.isAppLocked.collectAsState()
                val isVeiled by viewModel.isVeiled.collectAsState()

                // Synchronize lock state with PIN configuration
                val hasPin = state.userPreferences.pinEnabled && state.userPreferences.pinHash.isNotEmpty()
                val locked = (isAppLocked || isVeiled) && hasPin

                // Screen Privacy (FLAG_SECURE)
                LaunchedEffect(state.userPreferences.screenPrivacyEnabled) {
                    try {
                        if (state.userPreferences.screenPrivacyEnabled) {
                            window.setFlags(
                                WindowManager.LayoutParams.FLAG_SECURE,
                                WindowManager.LayoutParams.FLAG_SECURE
                            )
                        } else {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                    } catch (_: Exception) {}
                }

                // Daily Reminder scheduling
                LaunchedEffect(state.userPreferences.dailyReminderEnabled) {
                    try {
                        if (state.userPreferences.dailyReminderEnabled) {
                            app.cashjournal.receiver.DailyReminderScheduler.scheduleDailyReminder(applicationContext)
                        } else {
                            app.cashjournal.receiver.DailyReminderScheduler.cancelDailyReminder(applicationContext)
                        }
                    } catch (_: Exception) {}
                }

                // Immersive Sticky Fullscreen
                val insetsController = remember(window) {
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
                LaunchedEffect(state.userPreferences.immersiveModeEnabled) {
                    try {
                        if (state.userPreferences.immersiveModeEnabled) {
                            insetsController.hide(WindowInsetsCompat.Type.systemBars())
                        } else {
                            insetsController.show(WindowInsetsCompat.Type.systemBars())
                        }
                    } catch (_: Exception) {}
                }

                if (!state.isPreferencesLoaded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Slate100)
                    )
                } else if (!state.userPreferences.onboardingComplete) {
                    OnboardingScreen(
                        hasExistingAccounts = state.accounts.isNotEmpty(),
                        onCancel = {
                            viewModel.updatePreferences { prefs ->
                                prefs.updateOnboardingComplete(true)
                            }
                        },
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
                                prefs.updateLastSeenVersion(app.cashjournal.util.AppVersionProvider.getVersionName(applicationContext))
                            }
                            if (loadExample) {
                                viewModel.seedExampleData()
                            }
                            viewModel.refreshWeather()
                        }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MainAppScaffold(
                            viewModel = viewModel,
                            onLockApp = {
                                if (hasPin) {
                                    viewModel.lockAppNow()
                                }
                            },
                            isLocked = locked,
                            modifier = if (locked) Modifier.alpha(0f) else Modifier
                        )

                        if (locked) {
                            AppLockScreen(
                                pinHash = state.userPreferences.pinHash,
                                biometricsEnabled = state.userPreferences.biometricsEnabled,
                                autoPromptBiometrics = isAppLocked && !isVeiled,
                                onUnlock = { viewModel.unlockApp() },
                                languageCode = state.userPreferences.language,
                                hapticsEnabled = state.userPreferences.tactileHapticsEnabled,
                                onBiometricRequest = if (state.userPreferences.biometricsEnabled) {
                                    {
                                        val isIndonesian = state.userPreferences.language.equals("in", ignoreCase = true)
                                        BiometricAuthHelper.promptBiometric(
                                            activity = this@MainActivity,
                                            title = if (isIndonesian) "Buka CashJournal" else "Unlock CashJournal",
                                            subtitle = if (isIndonesian) "Konfirmasi biometrik untuk membuka" else "Confirm biometric to unlock",
                                            onSuccess = { viewModel.unlockApp() },
                                            onError = { /* fallback to PIN */ }
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppForegrounded(viewModel.hasPinConfigured())
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppBackgrounded(viewModel.hasPinConfigured())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("seed", false)) {
            viewModel.seedExampleData()
        }
    }
}
