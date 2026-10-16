package app.cashjournal

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent?.getBooleanExtra("seed", false) == true) {
            viewModel.seedExampleData()
        }

        setContent {
            CashJournalTheme {
                val state by viewModel.uiState.collectAsState()

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

                // App Lock State: locked if pinEnabled and pinHash is not empty
                var isAppLocked by rememberSaveable(state.userPreferences.pinEnabled) {
                    mutableStateOf(state.userPreferences.pinEnabled && state.userPreferences.pinHash.isNotEmpty())
                }

                val lifecycleOwner = this@MainActivity
                var lastBackgroundTime by rememberSaveable { mutableStateOf(0L) }

                DisposableEffect(lifecycleOwner, state.userPreferences.pinEnabled) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_STOP -> {
                                lastBackgroundTime = System.currentTimeMillis()
                            }
                            Lifecycle.Event.ON_START -> {
                                if (state.userPreferences.pinEnabled && state.userPreferences.pinHash.isNotEmpty()) {
                                    val elapsed = System.currentTimeMillis() - lastBackgroundTime
                                    if (lastBackgroundTime > 0L && elapsed > 15_000L) {
                                        isAppLocked = true
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                if (!state.userPreferences.onboardingComplete) {
                    OnboardingScreen(
                        onFinish = { language, currencyCode, name, railOnRight, shakeToSwap, loadExample, weatherOption, cityName, cityLat, cityLon ->
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
                                prefs.updateWeatherOption(weatherOptionKey, cityName, cityLat, cityLon)
                                prefs.updateOnboardingComplete(true)
                            }
                            if (loadExample) {
                                viewModel.seedExampleData()
                            }
                            viewModel.refreshWeather()
                        }
                    )
                } else if (isAppLocked && state.userPreferences.pinEnabled && state.userPreferences.pinHash.isNotEmpty()) {
                    AppLockScreen(
                        pinHash = state.userPreferences.pinHash,
                        biometricsEnabled = state.userPreferences.biometricsEnabled,
                        onUnlock = { isAppLocked = false },
                        onBiometricRequest = if (state.userPreferences.biometricsEnabled) {
                            {
                                BiometricAuthHelper.promptBiometric(
                                    activity = this@MainActivity,
                                    title = "Unlock CashJournal",
                                    subtitle = "Confirm biometric to unlock",
                                    onSuccess = { isAppLocked = false },
                                    onError = { /* fallback to PIN */ }
                                )
                            }
                        } else null
                    )
                } else {
                    MainAppScaffold(
                        viewModel = viewModel,
                        onLockApp = {
                            if (state.userPreferences.pinEnabled && state.userPreferences.pinHash.isNotEmpty()) {
                                isAppLocked = true
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("seed", false)) {
            viewModel.seedExampleData()
        }
    }
}
