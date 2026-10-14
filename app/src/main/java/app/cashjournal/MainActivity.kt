package app.cashjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.cashjournal.ui.preview.Phase3ComponentsScreen
import app.cashjournal.ui.theme.CashJournalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CashJournalTheme {
                Phase3ComponentsScreen()
            }
        }
    }
}
