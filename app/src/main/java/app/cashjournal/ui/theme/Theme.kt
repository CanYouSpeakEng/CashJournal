package app.cashjournal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = CashColors.Cobalt,
    onPrimary = CashColors.White,
    primaryContainer = CashColors.CobaltLight,
    onPrimaryContainer = CashColors.CobaltDark,
    secondary = CashColors.Slate500,
    onSecondary = CashColors.White,
    secondaryContainer = CashColors.Slate100,
    onSecondaryContainer = CashColors.Charcoal,
    tertiary = CashColors.Emerald,
    onTertiary = CashColors.White,
    background = CashColors.Slate50,
    onBackground = CashColors.Charcoal,
    surface = CashColors.White,
    onSurface = CashColors.Charcoal,
    surfaceVariant = CashColors.Slate100,
    onSurfaceVariant = CashColors.TextMuted,
    outline = CashColors.Slate200,
    error = CashColors.Berry,
    onError = CashColors.White
)

@Composable
fun CashJournalTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
