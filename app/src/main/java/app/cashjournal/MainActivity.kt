package app.cashjournal

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.cashjournal.ui.MainAppScaffold
import app.cashjournal.ui.MainViewModel
import app.cashjournal.ui.onboarding.AppLanguage
import app.cashjournal.ui.onboarding.OnboardingScreen
import app.cashjournal.ui.theme.CashJournalTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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

                if (!state.userPreferences.onboardingComplete) {
                    OnboardingScreen(
                        onFinish = { language, currencyCode, name, railOnRight, loadExample ->
                            viewModel.updatePreferences { prefs ->
                                prefs.updateLanguage(if (language == AppLanguage.INDONESIAN) "in" else "en")
                                prefs.updateCurrencyCode(currencyCode)
                                prefs.updateUserName(name)
                                prefs.updateRailOnRight(railOnRight)
                                prefs.updateOnboardingComplete(true)
                            }
                            if (loadExample) {
                                viewModel.seedExampleData()
                            }
                        }
                    )
                } else {
                    MainAppScaffold(viewModel = viewModel)
                }
            }
        }
    }
}
