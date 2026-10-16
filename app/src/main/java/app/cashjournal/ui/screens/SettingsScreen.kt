package app.cashjournal.ui.screens

import android.net.Uri
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.platform.LocalView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import app.cashjournal.BuildConfig
import app.cashjournal.domain.model.SafeToSpendCalculator
import kotlin.math.roundToInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.FragmentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import app.cashjournal.R
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.MainUiState
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.SegmentItem
import app.cashjournal.ui.components.SegmentedControl
import app.cashjournal.ui.components.ToggleSwitch
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.onboarding.AppLanguage
import app.cashjournal.ui.security.BiometricAuthHelper
import app.cashjournal.ui.sheets.PinSetupSheet
import app.cashjournal.ui.sheets.PinVerifyOrRemoveSheet
import app.cashjournal.ui.theme.Berry
import app.cashjournal.ui.theme.CashColors
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowCounterClockwise
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Lock

@Composable
fun SettingsScreen(
    state: MainUiState,
    onLanguageChange: (String) -> Unit,
    onOpenMainCurrency: () -> Unit,
    onBiometricsChange: (Boolean) -> Unit,
    onScreenPrivacyChange: (Boolean) -> Unit,
    onSoundEffectsChange: (Boolean) -> Unit,
    onTactileHapticsChange: (Boolean) -> Unit,
    onDailyReminderChange: (Boolean) -> Unit,
    onRailSideChange: (Boolean) -> Unit,
    onShakeToSwapChange: (Boolean) -> Unit,
    onKnockSensitivityChange: (String) -> Unit = {},
    onOpenWeatherDetail: () -> Unit,
    onTemperatureUnitChange: (String) -> Unit,
    onToast: (String) -> Unit,
    onClearAllData: () -> Unit,
    onSetPin: (String) -> Unit = {},
    onRemovePin: () -> Unit = {},
    onLockAppNow: () -> Unit = {},
    onResetOnboarding: () -> Unit = {},
    onExportBackup: (Uri) -> Unit = {},
    onRestoreBackup: (Uri) -> Unit = {},
    onCycleStartDayChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showCycleDialog by remember { mutableStateOf(false) }
    var showPinSetupSheet by remember { mutableStateOf(false) }
    var showPinRemoveSheet by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { onExportBackup(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onRestoreBackup(it) }
    }

    val currency = Currency.findByCode(state.userPreferences.currencyCode)
    val strings = AppStrings.get(state.userPreferences.language)
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = if (state.userPreferences.language == "in") "Hapus semua data?" else "Delete all data?",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
            },
            text = {
                Text(
                    text = if (state.userPreferences.language == "in") "Menghapus semua akun dan transaksi di ponsel ini. Tindakan ini tidak dapat dibatalkan." else "Removes all accounts and transactions on this phone. This can't be undone.",
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
                        text = if (state.userPreferences.language == "in") "Hapus semuanya" else "Delete everything",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = if (state.userPreferences.language == "in") "Batal" else "Cancel",
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

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = {
                Text(
                    text = strings.restoreBackupDialogTitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
            },
            text = {
                Text(
                    text = strings.restoreBackupDialogDesc,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 14.sp,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreDialog = false
                        importLauncher.launch(arrayOf("application/json", "application/octet-stream", "*/*"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = strings.confirmRestoreAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(
                        text = strings.cancelAction,
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

    if (showCycleDialog) {
        var selectedDay by remember { mutableStateOf(state.userPreferences.cycleStartDay.toFloat()) }
        val cyclePreview = remember(selectedDay) {
            SafeToSpendCalculator.getCycleRange(selectedDay.roundToInt()).label
        }

        AlertDialog(
            onDismissRequest = { showCycleDialog = false },
            title = {
                Text(
                    text = strings.cycleStartDayDialogTitle,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = strings.cycleStartDayDialogDesc,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 13.sp,
                        color = Slate600
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedDay.roundToInt() == 1) strings.cycleStartDayCalendarMonth else strings.cycleStartDaySubtitleFormat.format(selectedDay.roundToInt()),
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Cobalt
                        )
                        Text(
                            text = cyclePreview,
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }

                    Slider(
                        value = selectedDay,
                        onValueChange = { selectedDay = it },
                        valueRange = 1f..28f,
                        steps = 26,
                        colors = SliderDefaults.colors(
                            thumbColor = Cobalt,
                            activeTrackColor = Cobalt,
                            inactiveTrackColor = Slate200
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCycleDialog = false
                        onCycleStartDayChange(selectedDay.roundToInt())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (state.userPreferences.language == "in") "Simpan" else "Save",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCycleDialog = false }) {
                    Text(
                        text = strings.cancelAction,
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
            .padding(bottom = 68.dp)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                val mistHeightPx = 64.dp.toPx()
                val mistStartRatio = ((size.height - mistHeightPx) / size.height).coerceIn(0f, 1f)
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black,
                            mistStartRatio to Color.Black,
                            mistStartRatio + (1f - mistStartRatio) * 0.4f to Color.Black.copy(alpha = 0.6f),
                            mistStartRatio + (1f - mistStartRatio) * 0.75f to Color.Black.copy(alpha = 0.15f),
                            1.0f to Color.Transparent
                        )
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Subtitle
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = strings.settingsTitle,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Charcoal
            )
            Text(
                text = strings.settingsSubtitle,
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
                    text = strings.sectionLanguage,
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
                        text = strings.sectionMainCurrency,
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

        // 3. Billing & Payday Cycle Card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCycleDialog = true }
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
                        text = strings.cycleStartDayTitle.uppercase(),
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp,
                        color = Slate500
                    )
                    val currentCycleDay = state.userPreferences.cycleStartDay
                    val subtitleText = if (currentCycleDay == 1) {
                        strings.cycleStartDayCalendarMonth
                    } else {
                        strings.cycleStartDaySubtitleFormat.format(currentCycleDay)
                    }
                    Text(
                        text = subtitleText,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Charcoal
                    )
                }

                Icon(
                    imageVector = PhosphorIcons.Regular.CaretRight,
                    contentDescription = "Change Cycle",
                    tint = Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 4. Security & Access Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = strings.sectionSecurity,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )

                SettingToggleRow(
                    title = strings.pinLockTitle,
                    subtitle = if (state.userPreferences.pinEnabled) (if (state.userPreferences.language == "in") "Terkunci otomatis setelah 15 dtk" else "App locked after 15s in background") else strings.pinLockSubtitle,
                    checked = state.userPreferences.pinEnabled,
                    onCheckedChange = { targetChecked ->
                        if (targetChecked) {
                            showPinSetupSheet = true
                        } else {
                            showPinRemoveSheet = true
                        }
                    },
                    hapticsEnabled = state.userPreferences.tactileHapticsEnabled
                )

                SettingToggleRow(
                    title = strings.biometricsTitle,
                    subtitle = if (state.userPreferences.biometricsEnabled) (if (state.userPreferences.language == "in") "Sidik jari / Wajah aktif" else "Fingerprint / Face unlock active") else strings.biometricsSubtitle,
                    checked = state.userPreferences.biometricsEnabled,
                    onCheckedChange = { targetChecked ->
                        if (targetChecked) {
                            if (!state.userPreferences.pinEnabled || state.userPreferences.pinHash.isEmpty()) {
                                onToast(strings.toastPinSetupFirst)
                                showPinSetupSheet = true
                            } else {
                                if (activity != null) {
                                    BiometricAuthHelper.promptBiometric(
                                        activity = activity,
                                        title = if (state.userPreferences.language == "in") "Aktifkan Biometrik" else "Enable Biometrics",
                                        subtitle = if (state.userPreferences.language == "in") "Konfirmasi identitas biometrik untuk mengaktifkan" else "Confirm biometric identity to enable",
                                        onSuccess = {
                                            onBiometricsChange(true)
                                        },
                                        onError = {
                                            onToast(strings.toastBiometricCancelled)
                                        }
                                    )
                                } else {
                                    onBiometricsChange(true)
                                }
                            }
                        } else {
                            onBiometricsChange(false)
                        }
                    },
                    hapticsEnabled = state.userPreferences.tactileHapticsEnabled
                )

                SettingToggleRow(
                    title = strings.screenPrivacyTitle,
                    subtitle = strings.screenPrivacySubtitle,
                    checked = state.userPreferences.screenPrivacyEnabled,
                    onCheckedChange = onScreenPrivacyChange,
                    hapticsEnabled = state.userPreferences.tactileHapticsEnabled
                )

                if (state.userPreferences.pinEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate100)
                            .clickable {
                                onLockAppNow()
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = strings.lockAppNowTitle,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )
                            Text(
                                text = strings.lockAppNowSubtitle,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }

                        Icon(
                            imageVector = PhosphorIcons.Regular.Lock,
                            contentDescription = "Lock",
                            tint = Cobalt,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 4. Sensory & Feedback Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = strings.sectionSensory,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )

                SettingToggleRow(
                    title = strings.soundEffectsTitle,
                    checked = state.userPreferences.soundEffectsEnabled,
                    onCheckedChange = onSoundEffectsChange,
                    hapticsEnabled = state.userPreferences.tactileHapticsEnabled
                )

                SettingToggleRow(
                    title = strings.tactileHapticsTitle,
                    checked = state.userPreferences.tactileHapticsEnabled,
                    onCheckedChange = onTactileHapticsChange,
                    hapticsEnabled = state.userPreferences.tactileHapticsEnabled
                )

                SettingToggleRow(
                    title = strings.dailyReminderTitle,
                    checked = state.userPreferences.dailyReminderEnabled,
                    onCheckedChange = onDailyReminderChange,
                    hapticsEnabled = state.userPreferences.tactileHapticsEnabled
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
                    text = strings.sectionSidebar,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )

                val railItems = listOf(
                    SegmentItem(false, if (state.userPreferences.language == "in") "Kiri" else "Left", Cobalt),
                    SegmentItem(true, if (state.userPreferences.language == "in") "Kanan" else "Right", Cobalt)
                )

                SegmentedControl(
                    items = railItems,
                    selectedValue = state.userPreferences.railOnRight,
                    onValueSelected = onRailSideChange,
                    hapticsEnabled = state.userPreferences.tactileHapticsEnabled
                )

                SettingToggleRow(
                    title = strings.knockToSwapTitle,
                    subtitle = if (state.userPreferences.language == "in") "Ketuk perangkat untuk pindah sisi" else "Knock device or shake to swap",
                    checked = state.userPreferences.shakeToSwap,
                    onCheckedChange = onShakeToSwapChange,
                    hapticsEnabled = state.userPreferences.tactileHapticsEnabled
                )

                if (state.userPreferences.shakeToSwap) {
                    Column(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = strings.knockSensitivityTitle,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate500
                        )

                        val sensitivityItems = listOf(
                            SegmentItem("gentle", strings.knockSensitivityLow, Cobalt),
                            SegmentItem("balanced", strings.knockSensitivityMed, Cobalt),
                            SegmentItem("firm", strings.knockSensitivityHigh, Cobalt)
                        )

                        SegmentedControl(
                            items = sensitivityItems,
                            selectedValue = state.userPreferences.knockSensitivity,
                            onValueSelected = onKnockSensitivityChange
                        )
                    }
                }
            }
        }

        // 6. Weather Card
        val weatherLocationSubtitle = remember(state.userPreferences, state.weatherData, strings) {
            if (state.userPreferences.weatherCityLabel.isNotBlank()) {
                String.format(strings.weatherCitySubtitle, state.userPreferences.weatherCityLabel)
            } else if (state.weatherData?.locationLabel.isNullOrBlank()) {
                strings.weatherApproxSubtitle
            } else {
                state.weatherData?.locationLabel ?: strings.weatherApproxSubtitle
            }
        }
        val weatherUpdatedText = remember(state.weatherData, strings) {
            val fetchedAt = state.weatherData?.fetchedAt ?: 0L
            if (fetchedAt <= 0L) {
                strings.weatherPending
            } else {
                val diffMin = ((System.currentTimeMillis() - fetchedAt) / 60000L).coerceAtLeast(0L)
                if (diffMin < 1L) {
                    strings.weatherUpdatedJustNow
                } else {
                    String.format(strings.weatherUpdatedMinsAgo, diffMin)
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = strings.sectionWeather,
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
                            text = strings.weatherLocationTitle,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Charcoal
                        )
                        Text(
                            text = weatherLocationSubtitle,
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
                    text = weatherUpdatedText,
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
                    text = strings.sectionData,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Slate500
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            exportLauncher.launch("cashjournal_backup_$timestamp.json")
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.exportJsonTitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Charcoal
                    )
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretRight,
                        contentDescription = "Export Backup",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRestoreDialog = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.importJsonTitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Charcoal
                    )
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretRight,
                        contentDescription = "Restore Backup",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToast(strings.comingSoonToast) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.exportExcelTitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Charcoal
                    )
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretRight,
                        contentDescription = "Export Excel",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onResetOnboarding() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.replayOnboardingTitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Cobalt
                    )
                    Icon(
                        imageVector = PhosphorIcons.Regular.ArrowCounterClockwise,
                        contentDescription = "Replay Onboarding",
                        tint = Cobalt,
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
                        text = strings.clearAllDataTitle,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_cashjournal_logo),
                    contentDescription = "CashJournal Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = strings.sectionAbout,
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
                        text = "CashJournal v${BuildConfig.VERSION_NAME} (Native Android Build)",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }
        }
    }

    if (showPinSetupSheet) {
        PinSetupSheet(
            onDismiss = { showPinSetupSheet = false },
            onPinCreated = { pin ->
                onSetPin(pin)
                showPinSetupSheet = false
            },
            hapticsEnabled = state.userPreferences.tactileHapticsEnabled
        )
    }

    if (showPinRemoveSheet) {
        PinVerifyOrRemoveSheet(
            storedHash = state.userPreferences.pinHash,
            onDismiss = { showPinRemoveSheet = false },
            onSuccess = {
                onRemovePin()
                showPinRemoveSheet = false
            },
            hapticsEnabled = state.userPreferences.tactileHapticsEnabled
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    hapticsEnabled: Boolean = true
) {
    val view = LocalView.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (hapticsEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                onCheckedChange(!checked)
            },
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
            onCheckedChange = onCheckedChange,
            hapticsEnabled = hapticsEnabled
        )
    }
}
