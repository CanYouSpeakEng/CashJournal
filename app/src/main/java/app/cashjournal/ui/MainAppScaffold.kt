package app.cashjournal.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.components.GreetingBar
import app.cashjournal.ui.components.NavTab
import app.cashjournal.ui.components.SideRail
import app.cashjournal.ui.components.ToastPill
import app.cashjournal.ui.screens.AccountDetailScreen
import app.cashjournal.ui.screens.AccountsScreen
import app.cashjournal.ui.screens.AnalyticsScreen
import app.cashjournal.ui.screens.DashboardScreen
import app.cashjournal.ui.screens.LedgerScreen
import app.cashjournal.ui.screens.SettingsScreen
import app.cashjournal.ui.sheets.LogTransactionSheet
import app.cashjournal.ui.sheets.MainCurrencySheet
import app.cashjournal.ui.sheets.NewAccountSheet
import app.cashjournal.ui.sheets.WeatherDetailSheet
import app.cashjournal.ui.sheets.WorldCurrenciesSheet
import app.cashjournal.ui.theme.Slate100
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Sun
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val railOnRight = state.userPreferences.railOnRight
    val isAccountDetail = state.currentScreen == AppScreen.ACCOUNT_DETAIL

    // Auto-dismiss toast
    LaunchedEffect(state.toastMessage) {
        if (state.toastMessage != null) {
            delay(2200)
            viewModel.clearToast()
        }
    }

    val currentNavTab = when (state.currentScreen) {
        AppScreen.DASHBOARD -> NavTab.DASHBOARD
        AppScreen.LEDGER -> NavTab.LEDGER
        AppScreen.ACCOUNTS, AppScreen.ACCOUNT_DETAIL -> NavTab.ACCOUNTS
        AppScreen.ANALYTICS -> NavTab.ANALYTICS
        AppScreen.SETTINGS -> NavTab.SETTINGS
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Main Content Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (railOnRight) 20.dp else 78.dp,
                    end = if (railOnRight) 78.dp else 20.dp,
                    top = 44.dp
                )
        ) {
            // Greeting Bar
            if (isAccountDetail && state.selectedAccount != null) {
                GreetingBar(
                    greeting = "ACCOUNT",
                    name = state.selectedAccount!!.name,
                    weatherIcon = PhosphorIcons.Regular.Sun,
                    temperature = "24°C",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                GreetingBar(
                    name = state.userPreferences.userName,
                    weatherIcon = PhosphorIcons.Regular.Sun,
                    temperature = "24°C",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Content with Animated Transition
            AnimatedContent(
                targetState = state.currentScreen,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally { width -> if (targetState.ordinal > initialState.ordinal) width / 4 else -width / 4 })
                        .togetherWith(fadeOut() + slideOutHorizontally { width -> if (targetState.ordinal > initialState.ordinal) -width / 4 else width / 4 })
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
                        onAddAccountClick = { viewModel.openSheet(ActiveSheet.NEW_ACCOUNT) }
                    )

                    AppScreen.LEDGER -> LedgerScreen(
                        state = state,
                        onLogTransactionClick = { viewModel.openSheet(ActiveSheet.LOG_TRANSACTION) }
                    )

                    AppScreen.ACCOUNTS -> AccountsScreen(
                        state = state,
                        onAccountClick = { viewModel.openAccountDetail(it) },
                        onAddAccountClick = { viewModel.openSheet(ActiveSheet.NEW_ACCOUNT) },
                        onMoveAccount = { idx, up -> viewModel.moveAccount(idx, up) },
                        onToggleReorderMode = { viewModel.setReorderMode(it) },
                        onOpenWorldCurrencies = { viewModel.openSheet(ActiveSheet.WORLD_CURRENCIES) }
                    )

                    AppScreen.ACCOUNT_DETAIL -> {
                        state.selectedAccount?.let { acc ->
                            AccountDetailScreen(
                                account = acc,
                                state = state
                            )
                        }
                    }

                    AppScreen.ANALYTICS -> AnalyticsScreen(
                        state = state,
                        onBudgetChange = { viewModel.updateBudget(it) }
                    )

                    AppScreen.SETTINGS -> SettingsScreen(
                        state = state,
                        onLanguageChange = { viewModel.updatePreferences { prefs -> prefs.updateLanguage(it) } },
                        onOpenMainCurrency = { viewModel.openSheet(ActiveSheet.MAIN_CURRENCY) },
                        onPinChange = { viewModel.updatePreferences { prefs -> prefs.updatePinEnabled(it) } },
                        onBiometricsChange = { viewModel.updatePreferences { prefs -> prefs.updateBiometricsEnabled(it) } },
                        onScreenPrivacyChange = { viewModel.updatePreferences { prefs -> prefs.updateScreenPrivacyEnabled(it) } },
                        onSoundEffectsChange = { viewModel.updatePreferences { prefs -> prefs.updateSoundEffectsEnabled(it) } },
                        onTactileHapticsChange = { viewModel.updatePreferences { prefs -> prefs.updateTactileHapticsEnabled(it) } },
                        onDailyReminderChange = { viewModel.updatePreferences { prefs -> prefs.updateDailyReminderEnabled(it) } },
                        onRailSideChange = { viewModel.updatePreferences { prefs -> prefs.updateRailOnRight(it) } },
                        onShakeToSwapChange = { viewModel.updatePreferences { prefs -> prefs.updateShakeToSwap(it) } },
                        onOpenWeatherDetail = { viewModel.openSheet(ActiveSheet.WEATHER_DETAIL) },
                        onTemperatureUnitChange = { viewModel.updatePreferences { prefs -> prefs.updateTemperatureUnit(it) } },
                        onToast = { viewModel.showToast(it) },
                        onClearAllData = { viewModel.clearAllData() }
                    )
                }
            }
        }

        // Side Rail (Floating, 12dp from screen edge, vertically centered)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (!railOnRight) 12.dp else 0.dp,
                    end = if (railOnRight) 12.dp else 0.dp
                ),
            contentAlignment = if (railOnRight) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            SideRail(
                currentTab = currentNavTab,
                onTabSelected = { tab ->
                    when (tab) {
                        NavTab.DASHBOARD -> viewModel.setScreen(AppScreen.DASHBOARD)
                        NavTab.LEDGER -> viewModel.setScreen(AppScreen.LEDGER)
                        NavTab.ACCOUNTS -> viewModel.setScreen(AppScreen.ACCOUNTS)
                        NavTab.ANALYTICS -> viewModel.setScreen(AppScreen.ANALYTICS)
                        NavTab.SETTINGS -> viewModel.setScreen(AppScreen.SETTINGS)
                    }
                },
                onQuickAddClick = { viewModel.openSheet(ActiveSheet.LOG_TRANSACTION) },
                balancesVisible = !state.hideBalances,
                onToggleBalances = { viewModel.toggleHideBalances() },
                isNestedScreen = isAccountDetail,
                onBackClick = if (isAccountDetail) { { viewModel.setScreen(AppScreen.ACCOUNTS) } } else null
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

        // Modal Bottom Sheets
        state.activeSheet?.let { sheet ->
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { viewModel.dismissSheet() },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = null
            ) {
                when (sheet) {
                    ActiveSheet.LOG_TRANSACTION -> LogTransactionSheet(
                        accounts = state.accounts,
                        currencyCode = state.userPreferences.currencyCode,
                        onSaveTransaction = { type, src, dst, note, amount ->
                            viewModel.logTransaction(type, src, dst, note, amount)
                        },
                        onDismiss = { viewModel.dismissSheet() }
                    )

                    ActiveSheet.NEW_ACCOUNT -> NewAccountSheet(
                        currencyCode = state.userPreferences.currencyCode,
                        onCreateAccount = { name, bal, accent ->
                            viewModel.createAccount(name, bal, accent)
                        },
                        onDismiss = { viewModel.dismissSheet() }
                    )

                    ActiveSheet.MAIN_CURRENCY -> MainCurrencySheet(
                        currentCurrencyCode = state.userPreferences.currencyCode,
                        onSelectCurrency = { code ->
                            viewModel.updatePreferences { it.updateCurrencyCode(code) }
                            viewModel.dismissSheet()
                            viewModel.showToast("Main currency changed to $code")
                        },
                        onDismiss = { viewModel.dismissSheet() }
                    )

                    ActiveSheet.WORLD_CURRENCIES -> WorldCurrenciesSheet(
                        totalNetWorthIdr = state.totalNetWorth,
                        selectedCode = state.userPreferences.selectedWorldCurrency,
                        onSelectCode = { code ->
                            viewModel.updatePreferences { it.updateSelectedWorldCurrency(code) }
                            viewModel.dismissSheet()
                            viewModel.showToast("Currency changed to $code")
                        },
                        onDismiss = { viewModel.dismissSheet() }
                    )

                    ActiveSheet.WEATHER_DETAIL -> WeatherDetailSheet(
                        currentOption = state.userPreferences.weatherOption,
                        onSelectOption = { opt ->
                            viewModel.updatePreferences { it.updateWeatherOption(opt) }
                            viewModel.dismissSheet()
                        },
                        onDismiss = { viewModel.dismissSheet() }
                    )
                }
            }
        }
    }
}

