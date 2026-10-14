package app.cashjournal.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.MainUiState
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.SegmentItem
import app.cashjournal.ui.components.SegmentedControl
import app.cashjournal.ui.components.ToggleSwitch
import app.cashjournal.ui.onboarding.AppLanguage
import app.cashjournal.ui.theme.Berry
import app.cashjournal.ui.theme.CashColors
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretRight

@Composable
fun SettingsScreen(
    state: MainUiState,
    onLanguageChange: (String) -> Unit,
    onOpenMainCurrency: () -> Unit,
    onPinChange: (Boolean) -> Unit,
    onBiometricsChange: (Boolean) -> Unit,
    onScreenPrivacyChange: (Boolean) -> Unit,
    onSoundEffectsChange: (Boolean) -> Unit,
    onTactileHapticsChange: (Boolean) -> Unit,
    onDailyReminderChange: (Boolean) -> Unit,
    onRailSideChange: (Boolean) -> Unit,
    onShakeToSwapChange: (Boolean) -> Unit,
    onOpenWeatherDetail: () -> Unit,
    onTemperatureUnitChange: (String) -> Unit,
    onToast: (String) -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val currency = Currency.findByCode(state.userPreferences.currencyCode)

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete all data?",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
            },
            text = {
                Text(
                    text = "Removes all accounts and transactions on this phone. This can't be undone.",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 14.sp,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Berry),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Delete everything",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate600
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Subtitle
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Settings",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Charcoal
            )
            Text(
                text = "Preferences, security, and device data",
                fontFamily = PlusJakartaSansFamily,
                fontSize = 13.sp,
                color = Slate500
            )
        }

        // 1. Language Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "LANGUAGE",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )

                val languageItems = listOf(
                    SegmentItem("en", "English", Cobalt),
                    SegmentItem("in", "Bahasa Indonesia", Cobalt)
                )

                SegmentedControl(
                    items = languageItems,
                    selectedValue = state.userPreferences.language,
                    onValueSelected = onLanguageChange
                )
            }
        }

        // 2. Main Currency Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenMainCurrency() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "MAIN CURRENCY",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = Slate500
                    )
                    Text(
                        text = "${currency.name} (${currency.code})",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Charcoal
                    )
                }

                Icon(
                    imageVector = PhosphorIcons.Regular.CaretRight,
                    contentDescription = "Change",
                    tint = Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 3. Security & Access Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "SECURITY & ACCESS",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )

                SettingToggleRow(
                    title = "PIN Code",
                    checked = state.userPreferences.pinEnabled,
                    onCheckedChange = onPinChange
                )

                SettingToggleRow(
                    title = "Biometrics",
                    checked = state.userPreferences.biometricsEnabled,
                    onCheckedChange = onBiometricsChange
                )

                SettingToggleRow(
                    title = "Screen Privacy",
                    subtitle = "Blocks screenshots and app switcher preview",
                    checked = state.userPreferences.screenPrivacyEnabled,
                    onCheckedChange = onScreenPrivacyChange
                )
            }
        }

        // 4. Sensory & Feedback Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "SENSORY & FEEDBACK",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )

                SettingToggleRow(
                    title = "Sound Effects",
                    checked = state.userPreferences.soundEffectsEnabled,
                    onCheckedChange = onSoundEffectsChange
                )

                SettingToggleRow(
                    title = "Tactile Haptics",
                    checked = state.userPreferences.tactileHapticsEnabled,
                    onCheckedChange = onTactileHapticsChange
                )

                SettingToggleRow(
                    title = "Daily Reminder",
                    checked = state.userPreferences.dailyReminderEnabled,
                    onCheckedChange = onDailyReminderChange
                )
            }
        }

        // 5. Sidebar Placement Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "SIDEBAR PLACEMENT",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )

                val railItems = listOf(
                    SegmentItem(false, "Left", Cobalt),
                    SegmentItem(true, "Right", Cobalt)
                )

                SegmentedControl(
                    items = railItems,
                    selectedValue = state.userPreferences.railOnRight,
                    onValueSelected = onRailSideChange
                )

                SettingToggleRow(
                    title = "Shake to Swap Sides",
                    subtitle = "Optional, uses motion sensor",
                    checked = state.userPreferences.shakeToSwap,
                    onCheckedChange = onShakeToSwapChange
                )
            }
        }

        // 6. Weather Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "WEATHER",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWeatherDetail() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Weather Location",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Charcoal
                        )
                        Text(
                            text = "Approximate location",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }

                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretRight,
                        contentDescription = "Edit Location",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Temperature Unit °C / °F
                val tempItems = listOf(
                    SegmentItem("C", "°C", Cobalt),
                    SegmentItem("F", "°F", Cobalt)
                )

                SegmentedControl(
                    items = tempItems,
                    selectedValue = state.userPreferences.temperatureUnit,
                    onValueSelected = onTemperatureUnitChange
                )

                Text(
                    text = "Updated 4 min ago",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 11.sp,
                    color = Slate500
                )
            }
        }

        // 7. Data & Portability Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "DATA & PORTABILITY",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToast("Coming soon") },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Export Excel",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Charcoal
                    )
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretRight,
                        contentDescription = "Export",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToast("Coming soon") },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Smart Import",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Charcoal
                    )
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretRight,
                        contentDescription = "Import",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clear all data",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Berry
                    )
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretRight,
                        contentDescription = "Clear",
                        tint = Berry,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 8. About Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ABOUT",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )
                Text(
                    text = "Weather data by Open-Meteo.com",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = Slate600
                )
                Text(
                    text = "Rates by Exchange Rate API",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 12.sp,
                    color = Slate600
                )
                Text(
                    text = "CashJournal v1.0.0 (Native Android Build)",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    color = Slate500
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Charcoal
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 11.sp,
                    color = Slate500
                )
            }
        }

        ToggleSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
