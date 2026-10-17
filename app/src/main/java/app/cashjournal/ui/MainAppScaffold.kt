package app.cashjournal.ui

import android.app.Activity
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import app.cashjournal.domain.model.Transaction
import app.cashjournal.data.weather.TemperatureConverter
import app.cashjournal.data.weather.WeatherIconMapper
import app.cashjournal.ui.components.AmbientField
import app.cashjournal.ui.components.GreetingBar
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.components.NavTab
import app.cashjournal.ui.components.SideRail
import app.cashjournal.ui.components.ToastPill
import app.cashjournal.ui.screens.AccountDetailScreen
import app.cashjournal.ui.screens.AccountsScreen
import app.cashjournal.ui.screens.AnalyticsScreen
import app.cashjournal.ui.screens.DashboardScreen
import app.cashjournal.ui.screens.LedgerScreen
import app.cashjournal.ui.screens.SettingsScreen
import app.cashjournal.ui.sensory.KnockSensitivity
import app.cashjournal.ui.sensory.ShakeDetector
import app.cashjournal.ui.sheets.EditAccountSheet
import app.cashjournal.ui.sheets.LogTransactionSheet
import app.cashjournal.ui.sheets.MainCurrencySheet
import app.cashjournal.ui.sheets.NewAccountSheet
import app.cashjournal.ui.sheets.RecurringBillsSheet
import app.cashjournal.ui.sheets.ReleaseNotesSheet
import app.cashjournal.ui.sheets.TransactionEditSheet
import app.cashjournal.ui.sheets.UpdateSheet
import app.cashjournal.ui.sheets.WeatherDetailSheet
import app.cashjournal.ui.sheets.WorldCurrenciesSheet
import app.cashjournal.ui.theme.Slate100
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Sun
import com.adamglin.phosphoricons.regular.Thermometer
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onLockApp: () -> Unit = {},
    isLocked: Boolean = false
) {
    val state by viewModel.uiState.collectAsState()
    val railOnRight = state.userPreferences.railOnRight
    val isAccountDetail = state.currentScreen == AppScreen.ACCOUNT_DETAIL
    val context = LocalContext.current
    val view = LocalView.current

    // Shake & Knock Detector
    val shakeDetector = remember {
        ShakeDetector(context) {
            viewModel.toggleRailSide()
        }
    }

    LaunchedEffect(state.userPreferences.knockSensitivity) {
        shakeDetector.sensitivity = KnockSensitivity.fromString(state.userPreferences.knockSensitivity)
    }

    DisposableEffect(state.userPreferences.shakeToSwap) {
        if (state.userPreferences.shakeToSwap) {
            val started = shakeDetector.start()
            if (!started) {
                viewModel.showToast("Shake sensor not available on this device")
                viewModel.updatePreferences { prefs -> prefs.updateShakeToSwap(false) }
            }
        } else {
            shakeDetector.stop()
        }
        onDispose {
            shakeDetector.stop()
        }
    }

    // Rail Move Spring physics per Build Brief v2 Section 15:
    // Leading edge stiffness 320, damping 23. Pill stretches and tilts up to 18°.
    val railBias by animateFloatAsState(
        targetValue = if (railOnRight) 1f else -1f,
        animationSpec = spring(stiffness = 320f, dampingRatio = 0.65f),
        label = "rail_bias"
    )

    val contentStartPadding by animateDpAsState(
        targetValue = if (railOnRight) 20.dp else 78.dp,
        animationSpec = spring(stiffness = 320f, dampingRatio = 0.65f),
        label = "content_start_pad"
    )

    val contentEndPadding by animateDpAsState(
        targetValue = if (railOnRight) 78.dp else 20.dp,
        animationSpec = spring(stiffness = 320f, dampingRatio = 0.65f),
        label = "content_end_pad"
    )

    val tiltAngle = remember { Animatable(0f) }
    var isInitialComposition by remember { mutableStateOf(true) }
    LaunchedEffect(railOnRight) {
        if (isInitialComposition) {
            isInitialComposition = false
            return@LaunchedEffect
        }
        if (state.userPreferences.tactileHapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        val targetTilt = if (railOnRight) -18f else 18f
        tiltAngle.snapTo(targetTilt)
        tiltAngle.animateTo(
            targetValue = 0f,
            animationSpec = spring(stiffness = 320f, dampingRatio = 0.65f)
        )
    }

    val activity = context as? Activity
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    BackHandler(enabled = true) {
        when {
            state.activeSheet != null -> {
                viewModel.dismissSheet()
            }
            state.currentScreen == AppScreen.ACCOUNT_DETAIL -> {
                viewModel.setScreen(AppScreen.ACCOUNTS)
            }
            state.currentScreen != AppScreen.DASHBOARD -> {
                viewModel.setScreen(AppScreen.DASHBOARD)
            }
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressTime < 2000L) {
                    activity?.finish()
                } else {
                    lastBackPressTime = now
                    val exitMsg = if (state.userPreferences.language == "in") "Tekan sekali lagi untuk keluar" else "Press back again to exit"
                    viewModel.showToast(exitMsg)
                }
            }
        }
    }

    // Map screen to NavTab
    val currentNavTab = when (state.currentScreen) {
        AppScreen.DASHBOARD -> NavTab.DASHBOARD
        AppScreen.LEDGER -> NavTab.LEDGER
        AppScreen.ACCOUNTS, AppScreen.ACCOUNT_DETAIL -> NavTab.ACCOUNTS
        AppScreen.ANALYTICS -> NavTab.ANALYTICS
        AppScreen.SETTINGS -> NavTab.SETTINGS
    }

    // Modal sheet state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isWeatherEnabled = state.userPreferences.weatherOption != "not_now"
    val weather = state.weatherData
    val weatherIcon = when {
        !isWeatherEnabled -> PhosphorIcons.Regular.Sun
        weather != null -> WeatherIconMapper.getIcon(weather.weatherCode, weather.isDay)
        else -> PhosphorIcons.Regular.Thermometer
    }
    val tempText = when {
        !isWeatherEnabled -> ""
        weather != null -> TemperatureConverter.format(weather.temperature, state.userPreferences.temperatureUnit)
        else -> "--"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Dynamic Ambient Field Background per Build Brief v2 Section 15
        AmbientField(
            accounts = state.accounts,
            railSideRight = railOnRight
        )

        // Main Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = contentStartPadding,
                    end = contentEndPadding,
                    top = 44.dp
                )
        ) {
            // Greeting Bar
            val greetingAccount = if (state.userPreferences.language.equals("in", ignoreCase = true)) "AKUN" else "ACCOUNT"
            if (isAccountDetail && state.selectedAccount != null) {
                GreetingBar(
                    greeting = greetingAccount,
                    name = state.selectedAccount!!.name,
                    weatherIcon = weatherIcon,
                    temperature = tempText,
                    weatherEnabled = isWeatherEnabled,
                    onWeatherClick = if (isWeatherEnabled) { { viewModel.openSheet(ActiveSheet.WEATHER_DETAIL) } } else null,
                    languageCode = state.userPreferences.language,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                GreetingBar(
                    name = state.userPreferences.userName,
                    weatherIcon = weatherIcon,
                    temperature = tempText,
                    weatherEnabled = isWeatherEnabled,
                    onWeatherClick = if (isWeatherEnabled) { { viewModel.openSheet(ActiveSheet.WEATHER_DETAIL) } } else null,
                    languageCode = state.userPreferences.language,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Content with Section 15 Animation:
            // "Screen switch: the new screen fades in from 0 to 100% and slides up 6dp over 350 ms,
            // curve (0.16, 1, 0.3, 1). The old screen disappears at once."
            val density = LocalDensity.current
            val slideOffsetPx = with(density) { 6.dp.roundToPx() }
            val screenCurve = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
            AnimatedContent(
                targetState = state.currentScreen,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(durationMillis = 350, easing = screenCurve)) +
                            slideInVertically(animationSpec = tween(durationMillis = 350, easing = screenCurve)) { slideOffsetPx })
                        .togetherWith(fadeOut(animationSpec = tween(durationMillis = 0)))
                },
                label = "screenTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetScreen ->
                when (targetScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        state = state,
                        onSeeAllAccounts = { viewModel.setScreen(AppScreen.ACCOUNTS) },
                        onAccountClick = { viewModel.openAccountDetail(it) },
                        onViewTransactions = { viewModel.setScreen(AppScreen.LEDGER) },
                        onSetBudgetClick = { viewModel.setScreen(AppScreen.ANALYTICS) },
                        onAddAccountClick = { viewModel.openSheet(ActiveSheet.NEW_ACCOUNT) },
                        onTransactionClick = { viewModel.showEditTransaction(it) },
                        onOpenRecurringBills = { viewModel.openRecurringBillsSheet() }
                    )

                    AppScreen.LEDGER -> LedgerScreen(
                        state = state,
                        onLogTransactionClick = { viewModel.openSheet(ActiveSheet.LOG_TRANSACTION) },
                        onTransactionClick = { viewModel.showEditTransaction(it) }
                    )

                    AppScreen.ACCOUNTS -> AccountsScreen(
                        state = state,
                        onAccountClick = { viewModel.openAccountDetail(it) },
                        onAddAccountClick = { viewModel.openSheet(ActiveSheet.NEW_ACCOUNT) },
                        onMoveAccount = { idx, up -> viewModel.moveAccount(idx, up) },
                        onToggleReorderMode = { viewModel.setReorderMode(it) },
                        onOpenWorldCurrencies = { viewModel.openSheet(ActiveSheet.WORLD_CURRENCIES) },
                        onSelectWorldCurrency = { code ->
                            viewModel.updatePreferences { prefs -> prefs.updateSelectedWorldCurrency(code) }
                        },
                        onRefreshRates = { viewModel.refreshRates(manual = true) }
                    )

                    AppScreen.ACCOUNT_DETAIL -> {
                        state.selectedAccount?.let { acc ->
                            AccountDetailScreen(
                                account = acc,
                                state = state,
                                onTransactionClick = { viewModel.showEditTransaction(it) },
                                onEditAccountClick = { viewModel.openEditAccount(acc) }
                            )
                        }
                    }

                    AppScreen.ANALYTICS -> AnalyticsScreen(
                        state = state,
                        onBudgetPlanChange = { mode, percent, nominal -> viewModel.updateBudgetPlan(mode, percent, nominal) },
                        onBudgetChange = { viewModel.updateBudget(it) }
                    )

                    AppScreen.SETTINGS -> SettingsScreen(
                        state = state,
                        onLanguageChange = { viewModel.updatePreferences { prefs -> prefs.updateLanguage(it) } },
                        onOpenMainCurrency = { viewModel.openSheet(ActiveSheet.MAIN_CURRENCY) },
                        onBiometricsChange = { viewModel.updateBiometrics(it) },
                        onScreenPrivacyChange = { viewModel.updatePreferences { prefs -> prefs.updateScreenPrivacyEnabled(it) } },
                        onSoundEffectsChange = { viewModel.updatePreferences { prefs -> prefs.updateSoundEffectsEnabled(it) } },
                        onTactileHapticsChange = { viewModel.updatePreferences { prefs -> prefs.updateTactileHapticsEnabled(it) } },
                        onDailyReminderChange = { viewModel.updatePreferences { prefs -> prefs.updateDailyReminderEnabled(it) } },
                        onRailSideChange = { viewModel.updatePreferences { prefs -> prefs.updateRailOnRight(it) } },
                        onShakeToSwapChange = { enabled ->
                            val supported = shakeDetector.isSupported
                            val screenStrings = AppStrings.get(state.userPreferences.language)
                            if (enabled && !supported) {
                                viewModel.showToast(screenStrings.toastShakeSensorUnavailable)
                                viewModel.updatePreferences { prefs -> prefs.updateShakeToSwap(false) }
                            } else {
                                viewModel.updatePreferences { prefs -> prefs.updateShakeToSwap(enabled) }
                                viewModel.showToast(if (enabled) screenStrings.toastShakeSensorActivated else screenStrings.toastShakeSensorDisabled)
                            }
                        },
                        onKnockSensitivityChange = { viewModel.updatePreferences { prefs -> prefs.updateKnockSensitivity(it) } },
                        onOpenWeatherDetail = { viewModel.openSheet(ActiveSheet.WEATHER_DETAIL) },
                        onTemperatureUnitChange = { viewModel.updatePreferences { prefs -> prefs.updateTemperatureUnit(it) } },
                        onToast = { viewModel.showToast(it) },
                        onClearAllData = { viewModel.clearAllData() },
                        onSetPin = { pin -> viewModel.setPin(pin) },
                        onRemovePin = { viewModel.removePin() },
                        onLockAppNow = onLockApp,
                        onResetOnboarding = { viewModel.resetOnboarding() },
                        onExportBackup = { uri -> viewModel.exportBackup(uri) },
                        onRestoreBackup = { uri -> viewModel.restoreBackup(uri) },
                        onCycleStartDayChange = { viewModel.updateCycleStartDay(it) },
                        onImmersiveModeChange = { viewModel.updatePreferences { prefs -> prefs.updateImmersiveMode(it) } },
                        onOpenReleaseNotes = { viewModel.openSheet(ActiveSheet.RELEASE_NOTES) },
                        onCheckForUpdates = { viewModel.checkForUpdates(manual = true) },
                        onOpenUpdateSheet = { viewModel.openUpdateSheet() },
                        onOpenRecurringBills = { viewModel.openRecurringBillsSheet() }
                    )
                }
            }
        }

        // Side Rail (Floating, 12dp from screen edge, spring transition across sides)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = BiasAlignment(railBias, 0f)
        ) {
            SideRail(
                currentTab = currentNavTab,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.DASHBOARD -> viewModel.setScreen(AppScreen.DASHBOARD)
                        NavTab.LEDGER -> viewModel.setScreen(AppScreen.LEDGER)
                        NavTab.ACCOUNTS -> viewModel.setScreen(AppScreen.ACCOUNTS)
                        NavTab.ANALYTICS -> viewModel.setScreen(AppScreen.ANALYTICS)
                        NavTab.SETTINGS -> {
                            viewModel.setScreen(AppScreen.SETTINGS)
                            if (state.isUpdateReadyToInstall) {
                                viewModel.openSheet(ActiveSheet.UPDATE_INFO)
                            }
                        }
                    }
                },
                onQuickAddClick = { viewModel.openSheet(ActiveSheet.LOG_TRANSACTION) },
                balancesVisible = !state.hideBalances,
                onToggleBalances = { viewModel.toggleHideBalances() },
                isNestedScreen = isAccountDetail,
                onBackClick = if (isAccountDetail) { { viewModel.setScreen(AppScreen.ACCOUNTS) } } else null,
                hapticsEnabled = state.userPreferences.tactileHapticsEnabled,
                railOnRight = railOnRight,
                languageCode = state.userPreferences.language,
                isUpdateReady = state.isUpdateReadyToInstall,
                modifier = Modifier.rotate(tiltAngle.value)
            )
        }

        // Toast Pill (6% from top, auto-dismisses in 2.2s)
        ToastPill(
            message = state.toastMessage ?: "",
            visible = state.toastMessage != null,
            onDismiss = { viewModel.clearToast() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        )

        // Modal Bottom Sheets (suppressed when app is locked/veiled to prevent WindowManager bleed-through)
        if (!isLocked) {
            state.activeSheet?.let { sheet ->
                ModalBottomSheet(
                    onDismissRequest = { viewModel.dismissSheet() },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    scrimColor = Color(0x4D0F172A), // 30% Charcoal per brief section 5
                    dragHandle = null
                ) {
                when (sheet) {
                    ActiveSheet.LOG_TRANSACTION -> LogTransactionSheet(
                        accounts = state.accounts,
                        currencyCode = state.userPreferences.currencyCode,
                        onSaveTransaction = { type, src, dst, note, amount, date, cat ->
                            viewModel.logTransaction(type, src, dst, note, amount, date, cat)
                        },
                        onDismiss = { viewModel.dismissSheet() },
                        soundEffectsEnabled = state.userPreferences.soundEffectsEnabled,
                        languageCode = state.userPreferences.language,
                        hapticsEnabled = state.userPreferences.tactileHapticsEnabled
                    )

                    ActiveSheet.EDIT_TRANSACTION -> {
                        state.selectedTransaction?.let { tx ->
                            TransactionEditSheet(
                                transaction = tx,
                                accounts = state.accounts,
                                currencyCode = state.userPreferences.currencyCode,
                                onUpdateTransaction = { id, title, amount, cat, type, src, dst, date ->
                                    viewModel.editTransaction(id, title, amount, cat, type, src, dst, date)
                                },
                                onDeleteTransaction = { id ->
                                    viewModel.deleteTransaction(id)
                                },
                                onDismiss = { viewModel.dismissSheet() },
                                soundEffectsEnabled = state.userPreferences.soundEffectsEnabled,
                                languageCode = state.userPreferences.language,
                                hapticsEnabled = state.userPreferences.tactileHapticsEnabled
                            )
                        }
                    }

                    ActiveSheet.NEW_ACCOUNT -> NewAccountSheet(
                        currencyCode = state.userPreferences.currencyCode,
                        onCreateAccount = { name, bal, accent, type ->
                            viewModel.createAccount(name, bal, accent, type)
                        },
                        onDismiss = { viewModel.dismissSheet() },
                        languageCode = state.userPreferences.language
                    )

                    ActiveSheet.EDIT_ACCOUNT -> {
                        state.selectedEditAccount?.let { acc ->
                            EditAccountSheet(
                                account = acc,
                                onSave = { name, accountType, accentKey, actualBalance, reconcileNote ->
                                    viewModel.updateAccountDetails(acc.id, name, accountType, accentKey, actualBalance, reconcileNote)
                                },
                                onDelete = {
                                    viewModel.deleteAccount(acc.id)
                                },
                                onDismiss = { viewModel.dismissSheet() },
                                canDelete = state.accounts.size > 1,
                                onCannotDeleteToast = {
                                    viewModel.showToast(AppStrings.get(state.userPreferences.language).deleteAccountCannotDeleteLast)
                                },
                                currencyCode = state.userPreferences.currencyCode,
                                languageCode = state.userPreferences.language
                            )
                        }
                    }

                    ActiveSheet.MAIN_CURRENCY -> MainCurrencySheet(
                        currentCurrencyCode = state.userPreferences.currencyCode,
                        onSelectCurrency = { code ->
                            viewModel.changeMainCurrency(code)
                        },
                        onDismiss = { viewModel.dismissSheet() },
                        languageCode = state.userPreferences.language
                    )

                    ActiveSheet.WORLD_CURRENCIES -> WorldCurrenciesSheet(
                        totalNetWorth = state.totalNetWorth,
                        mainCurrencyCode = state.userPreferences.currencyCode,
                        cachedRates = state.cachedRates,
                        isRatesOffline = state.isRatesOffline,
                        selectedCode = state.userPreferences.selectedWorldCurrency,
                        onSelectCode = { code ->
                            viewModel.updatePreferences { it.updateSelectedWorldCurrency(code) }
                            viewModel.dismissSheet()
                            val strings = AppStrings.get(state.userPreferences.language)
                            viewModel.showToast(String.format(strings.currencyChangedToast, code))
                        },
                        onDismiss = { viewModel.dismissSheet() },
                        languageCode = state.userPreferences.language
                    )

                    ActiveSheet.WEATHER_DETAIL -> WeatherDetailSheet(
                        currentOption = state.userPreferences.weatherOption,
                        citySearchResults = state.citySearchResults,
                        isSearchingCity = state.isSearchingCity,
                        onSearchCity = { query -> viewModel.searchCity(query) },
                        onSelectCity = { city ->
                            viewModel.selectCity(city)
                            viewModel.dismissSheet()
                        },
                        onSelectOption = { opt ->
                            viewModel.setWeatherOption(opt)
                            if (opt != "search") {
                                viewModel.dismissSheet()
                            }
                        },
                        onDismiss = { viewModel.dismissSheet() },
                        languageCode = state.userPreferences.language
                    )

                    ActiveSheet.RELEASE_NOTES -> ReleaseNotesSheet(
                        onDismiss = { viewModel.dismissReleaseNotes() },
                        languageCode = state.userPreferences.language,
                        hapticsEnabled = state.userPreferences.tactileHapticsEnabled
                    )

                    ActiveSheet.UPDATE_INFO -> UpdateSheet(
                        updateState = state.updateState,
                        onStartDownload = { viewModel.startUpdateDownload() },
                        onDownloadInBackground = { viewModel.dismissUpdateSheetToBackground() },
                        onInstallClick = { act -> viewModel.installDownloadedApk(act) },
                        onCheckAgain = { viewModel.checkForUpdates(manual = true) },
                        onDismiss = { viewModel.dismissSheet() },
                        languageCode = state.userPreferences.language
                    )

                    ActiveSheet.RECURRING_BILLS -> RecurringBillsSheet(
                        recurringBills = state.recurringTransactions,
                        accounts = state.accounts,
                        currencyCode = state.userPreferences.currencyCode,
                        cycleStartDay = state.userPreferences.cycleStartDay,
                        onAddBill = { title, amount, category, dueDay, sourceAccountId, frequency ->
                            viewModel.createRecurringBill(title, amount, category, dueDay, sourceAccountId, frequency)
                        },
                        onUpdateBill = { id, title, amount, category, dueDay, sourceAccountId, frequency, isActive ->
                            viewModel.updateRecurringBill(id, title, amount, category, dueDay, sourceAccountId, frequency, isActive)
                        },
                        onDeleteBill = { id -> viewModel.deleteRecurringBill(id) },
                        onToggleActive = { id, active -> viewModel.toggleRecurringBillActive(id, active) },
                        onMarkAsPaid = { id -> viewModel.markRecurringBillAsPaid(id) },
                        onDismiss = { viewModel.dismissSheet() },
                        languageCode = state.userPreferences.language,
                        hapticsEnabled = state.userPreferences.tactileHapticsEnabled
                    )
                }
            }
        }
        }
    }
}
