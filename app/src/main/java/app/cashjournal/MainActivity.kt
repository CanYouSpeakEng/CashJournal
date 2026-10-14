package app.cashjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cashjournal.ui.onboarding.OnboardingScreen
import app.cashjournal.ui.preview.Phase3ComponentsScreen
import app.cashjournal.ui.theme.CashJournalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashJournalTheme {
                var onboardingDone by remember { mutableStateOf(false) }

                if (!onboardingDone) {
                    OnboardingScreen(
                        onFinish = { _, _, _, _, _ ->
                            onboardingDone = true
                        }
                    )
                } else {
                    Phase3ComponentsScreen()
                }
            }
        }
    }
}
