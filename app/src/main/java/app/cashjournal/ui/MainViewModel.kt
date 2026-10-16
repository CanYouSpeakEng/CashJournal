package app.cashjournal.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import app.cashjournal.data.backup.BackupManager
import app.cashjournal.data.ExampleDataSeeder
import app.cashjournal.data.currency.CachedExchangeRates
import app.cashjournal.data.currency.CurrencyConverter
import app.cashjournal.data.currency.ExchangeRateService
import app.cashjournal.data.local.AppDatabase
import app.cashjournal.data.local.entity.AccountEntity
import app.cashjournal.data.local.entity.MonthlyPlanEntity
import app.cashjournal.data.local.entity.TransactionEntity
import app.cashjournal.data.location.LocationProvider
import app.cashjournal.data.preferences.UserPreferences
import app.cashjournal.data.preferences.UserPreferencesRepository
import app.cashjournal.data.weather.GeocodingCity
import app.cashjournal.data.weather.WeatherData
import app.cashjournal.data.weather.WeatherService
import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.MonthlyPlan
import app.cashjournal.domain.model.SafeToSpendCalculator
import app.cashjournal.domain.model.SafeToSpendState
import app.cashjournal.domain.model.SafeToSpendStatus
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.ui.sensory.ChimeGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.i18n.ScreenStrings

enum class AppScreen {
    DASHBOARD,
    LEDGER,
    ACCOUNTS,
    ACCOUNT_DETAIL,
    ANALYTICS,
    SETTINGS
}

enum class ActiveSheet {
    LOG_TRANSACTION,
    NEW_ACCOUNT,
    MAIN_CURRENCY,
    WORLD_CURRENCIES,
    WEATHER_DETAIL,
    EDIT_TRANSACTION,
    RECONCILE_BALANCE,
    RELEASE_NOTES
}

typealias SafeToSpendStatus = app.cashjournal.domain.model.SafeToSpendStatus
typealias SafeToSpendState = app.cashjournal.domain.model.SafeToSpendState


data class MainUiState(
    val isPreferencesLoaded: Boolean = false,
    val currentScreen: AppScreen = AppScreen.DASHBOARD,
    val selectedAccount: Account? = null,
    val activeSheet: ActiveSheet? = null,
    val selectedTransaction: Transaction? = null,
    val selectedReconcileAccount: Account? = null,
    val hideBalances: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val totalNetWorth: Long = 0L,
    val safeToSpend: SafeToSpendState = SafeToSpendState(),
    val monthlyPlan: MonthlyPlan = MonthlyPlan(),
    val userPreferences: UserPreferences = UserPreferences(),
    val isReorderMode: Boolean = false,
    val toastMessage: String? = null,
    val weatherData: WeatherData? = null,
    val cachedRates: CachedExchangeRates? = null,
    val isRatesOffline: Boolean = false,
    val citySearchResults: List<GeocodingCity> = emptyList(),
    val isSearchingCity: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val preferencesRepository = UserPreferencesRepository(application)
    private val weatherService = WeatherService()
    private val exchangeRateService = ExchangeRateService()
    private val locationProvider = LocationProvider(application)
    private val backupManager = BackupManager(database, preferencesRepository)

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    private val _selectedAccount = MutableStateFlow<Account?>(null)
    private val _activeSheet = MutableStateFlow<ActiveSheet?>(null)
    private val _selectedTransaction = MutableStateFlow<Transaction?>(null)
    private val _selectedReconcileAccount = MutableStateFlow<Account?>(null)
    private val _hideBalances = MutableStateFlow(false)
    private val _isReorderMode = MutableStateFlow(false)
    private val _toastMessage = MutableStateFlow<String?>(null)
    private val _isRatesOffline = MutableStateFlow(false)
    private val _citySearchResults = MutableStateFlow<List<GeocodingCity>>(emptyList())
    private val _isSearchingCity = MutableStateFlow(false)

    val currentScreen = _currentScreen.asStateFlow()
    val activeSheet = _activeSheet.asStateFlow()
    val toastMessage = _toastMessage.asStateFlow()

    private val accountsFlow = database.accountDao().getAllAccountsFlow()
    private val transactionsFlow = database.transactionDao().getAllTransactionsFlow()
    private val monthlyPlanFlow = database.monthlyPlanDao().getPlanFlow()
    private val preferencesFlow = preferencesRepository.userPreferencesFlow
    private val weatherFlow = preferencesRepository.cachedWeatherFlow
    private val ratesFlow = preferencesRepository.cachedRatesFlow

    val uiState: StateFlow<MainUiState> = combine(
        combine(accountsFlow, transactionsFlow, monthlyPlanFlow, preferencesFlow) { accs, txs, planEntity, prefs ->
            val domainAccs = accs.map { it.toDomain() }
            val domainTxs = txs.map { it.toDomain() }
            val domainPlan = planEntity?.toDomain() ?: MonthlyPlan()
            Quad(domainAccs, domainTxs, domainPlan, prefs)
        },
        combine(weatherFlow, ratesFlow, _isRatesOffline, _citySearchResults, _isSearchingCity) { weather, rates, offline, cities, searching ->
            Pent(weather, rates, offline, cities, searching)
        },
        combine(
            _currentScreen,
            _selectedAccount,
            _activeSheet,
            _selectedTransaction,
            _selectedReconcileAccount,
            _hideBalances,
            _isReorderMode,
            _toastMessage
        ) { args ->
            args
        }
    ) { group1, group2, group3 ->
        val (rawAccounts, rawTransactions, plan, prefs) = group1
        val (weather, rates, isOffline, cityResults, isSearching) = group2

        val screen = group3[0] as AppScreen
        val rawSelectedAccount = group3[1] as? Account
        val account = rawSelectedAccount?.let { sel -> rawAccounts.find { it.id == sel.id } ?: sel }
        val sheet = group3[2] as? ActiveSheet
        val selectedTx = group3[3] as? Transaction
        val rawReconcileAcc = group3[4] as? Account
        val reconcileAcc = rawReconcileAcc?.let { sel -> rawAccounts.find { it.id == sel.id } ?: sel }
        val hideBal = group3[5] as Boolean
        val reorder = group3[6] as Boolean
        val toast = group3[7] as? String

        val totalWorth = rawAccounts.sumOf { it.balance }
        val recentTx = rawTransactions.take(5)

        // Calculate Safe to Spend
        val safeState = calculateSafeToSpend(plan, rawTransactions, totalWorth, rawAccounts.isNotEmpty(), prefs.cycleStartDay)

        MainUiState(
            isPreferencesLoaded = true,
            currentScreen = screen,
            selectedAccount = account,
            activeSheet = sheet,
            selectedTransaction = selectedTx,
            selectedReconcileAccount = reconcileAcc,
            hideBalances = hideBal,
            accounts = rawAccounts,
            transactions = rawTransactions,
            recentTransactions = recentTx,
            totalNetWorth = totalWorth,
            safeToSpend = safeState,
            monthlyPlan = plan,
            userPreferences = prefs,
            isReorderMode = reorder,
            toastMessage = toast,
            weatherData = weather,
            cachedRates = rates,
            isRatesOffline = isOffline,
            citySearchResults = cityResults,
            isSearchingCity = isSearching
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        // Automatically check if rates or weather need refresh on startup
        viewModelScope.launch {
            val prefs = preferencesFlow.first()
            val cachedRates = ratesFlow.first()
            if (exchangeRateService.shouldAutoRefresh(cachedRates?.fetchedAt ?: 0L)) {
                refreshRates(manual = false)
            }
            if (prefs.weatherOption != "not_now") {
                refreshWeather()
            }
            if (prefs.onboardingComplete && prefs.lastSeenVersion != app.cashjournal.BuildConfig.VERSION_NAME) {
                _activeSheet.value = ActiveSheet.RELEASE_NOTES
            }
        }
    }

    private fun calculateSafeToSpend(
        plan: MonthlyPlan,
        transactions: List<Transaction>,
        totalNetWorth: Long = 0L,
        hasAccounts: Boolean = true,
        cycleStartDay: Int = 1
    ): SafeToSpendState {
        return SafeToSpendCalculator.calculate(
            plan = plan,
            transactions = transactions,
            totalNetWorth = totalNetWorth,
            hasAccounts = hasAccounts,
            cycleStartDay = cycleStartDay
        )
    }

    fun updateCycleStartDay(day: Int) {
        viewModelScope.launch {
            preferencesRepository.updateCycleStartDay(day)
        }
    }


    fun seedExampleData() {
        viewModelScope.launch {
            ExampleDataSeeder.seed(database)
        }
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen != AppScreen.ACCOUNTS) {
            _isReorderMode.value = false
        }
    }

    fun openAccountDetail(account: Account) {
        _selectedAccount.value = account
        _currentScreen.value = AppScreen.ACCOUNT_DETAIL
    }

    fun openSheet(sheet: ActiveSheet) {
        _activeSheet.value = sheet
        if (sheet == ActiveSheet.WORLD_CURRENCIES) {
            // Check if rates cache is older than 12 hours per Section 10
            val lastFetched = uiState.value.cachedRates?.fetchedAt ?: 0L
            if (exchangeRateService.shouldAutoRefresh(lastFetched)) {
                refreshRates(manual = false)
            }
        }
    }

    fun dismissSheet() {
        _activeSheet.value = null
        _selectedTransaction.value = null
        _selectedReconcileAccount.value = null
        _citySearchResults.value = emptyList()
    }

    fun dismissReleaseNotes() {
        viewModelScope.launch {
            preferencesRepository.updateLastSeenVersion(app.cashjournal.BuildConfig.VERSION_NAME)
        }
        dismissSheet()
    }

    fun showEditTransaction(transaction: Transaction) {
        _selectedTransaction.value = transaction
        _activeSheet.value = ActiveSheet.EDIT_TRANSACTION
    }

    fun showReconcileSheet(account: Account) {
        _selectedReconcileAccount.value = account
        _activeSheet.value = ActiveSheet.RECONCILE_BALANCE
    }

    private fun currentStrings(): ScreenStrings = AppStrings.get(uiState.value.userPreferences.language)

    fun toggleHideBalances() {
        _hideBalances.value = !_hideBalances.value
        val strings = currentStrings()
        showToast(if (_hideBalances.value) strings.toastBalancesMasked else strings.toastBalancesRevealed)
    }

    fun setReorderMode(enabled: Boolean) {
        _isReorderMode.value = enabled
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun logTransaction(
        type: TransactionType,
        sourceAccountId: String?,
        destAccountId: String?,
        note: String,
        amount: Long,
        date: Long = System.currentTimeMillis(),
        category: Category? = null
    ) {
        val strings = currentStrings()
        if (amount <= 0) {
            showToast(strings.toastEnterAmount)
            return
        }

        if (sourceAccountId.isNullOrBlank()) {
            showToast(strings.toastSelectAccount)
            return
        }

        if (type == TransactionType.TRANSFER && (destAccountId == null || destAccountId == sourceAccountId)) {
            showToast(strings.toastSelectDifferentDestAccount)
            return
        }

        val finalCategory = category ?: Category.detectFromNote(note)

        viewModelScope.launch {
            val sourceAcc = database.accountDao().getAccountById(sourceAccountId)
            val destAcc = destAccountId?.let { database.accountDao().getAccountById(it) }

            val title = when (type) {
                TransactionType.EXPENSE -> note.ifBlank { finalCategory.defaultName }
                TransactionType.INCOME -> note.ifBlank { "Income" }
                TransactionType.TRANSFER -> "${sourceAcc?.name ?: "Source"} \u2192 ${destAcc?.name ?: "Destination"}"
            }

            val txCategory = if (type == TransactionType.TRANSFER) "TRANSFER" else finalCategory.key
            val iconKey = when (type) {
                TransactionType.EXPENSE -> finalCategory.key
                TransactionType.INCOME -> "ArrowDownLeft"
                TransactionType.TRANSFER -> "ArrowsLeftRight"
            }
            val accentKey = when (type) {
                TransactionType.EXPENSE -> finalCategory.defaultAccent.name
                TransactionType.INCOME -> "EMERALD"
                TransactionType.TRANSFER -> "COBALT"
            }

            val tx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                sourceAccountId = sourceAccountId,
                destinationAccountId = destAccountId,
                type = type.name,
                amount = amount,
                title = title,
                category = txCategory,
                date = date,
                iconKey = iconKey,
                accentKey = accentKey,
                updatedAt = System.currentTimeMillis()
            )

            database.withTransaction {
                database.transactionDao().insert(tx)
                database.accountDao().recalculateAccountBalance(sourceAccountId)
                destAccountId?.let { database.accountDao().recalculateAccountBalance(it) }
            }

            ChimeGenerator.playChime(uiState.value.userPreferences.soundEffectsEnabled, viewModelScope)
            dismissSheet()
            showToast(strings.toastTransactionLogged)
        }
    }

    fun editTransaction(
        id: String,
        title: String,
        amount: Long,
        category: Category,
        type: TransactionType,
        sourceAccountId: String?,
        destAccountId: String?,
        date: Long
    ) {
        val strings = currentStrings()
        if (amount <= 0) {
            showToast(strings.toastEnterAmount)
            return
        }

        if (sourceAccountId.isNullOrBlank()) {
            showToast(strings.toastSelectAccount)
            return
        }

        if (type == TransactionType.TRANSFER && (destAccountId == null || destAccountId == sourceAccountId)) {
            showToast(strings.toastSelectDifferentDestAccount)
            return
        }

        viewModelScope.launch {
            val existing = database.transactionDao().getTransactionById(id) ?: return@launch
            val sourceAcc = database.accountDao().getAccountById(sourceAccountId)

            val txCategory = if (type == TransactionType.TRANSFER) "TRANSFER" else category.key
            val iconKey = when (type) {
                TransactionType.EXPENSE -> category.key
                TransactionType.INCOME -> "ArrowDownLeft"
                TransactionType.TRANSFER -> "ArrowsLeftRight"
            }
            val accentKey = when (type) {
                TransactionType.EXPENSE -> category.defaultAccent.name
                TransactionType.INCOME -> "EMERALD"
                TransactionType.TRANSFER -> "COBALT"
            }

            val updatedTx = existing.copy(
                title = title,
                category = txCategory,
                type = type.name,
                amount = amount,
                date = date,
                iconKey = iconKey,
                accentKey = accentKey,
                sourceAccountId = sourceAccountId,
                destinationAccountId = destAccountId,
                updatedAt = System.currentTimeMillis()
            )

            val affectedAccounts = setOfNotNull(
                existing.sourceAccountId,
                existing.destinationAccountId,
                sourceAccountId,
                destAccountId
            )

            database.withTransaction {
                database.transactionDao().update(updatedTx)
                affectedAccounts.forEach { accId ->
                    database.accountDao().recalculateAccountBalance(accId)
                }
            }

            ChimeGenerator.playChime(uiState.value.userPreferences.soundEffectsEnabled, viewModelScope)
            dismissSheet()
            val strings = AppStrings.get(uiState.value.userPreferences.language)
            showToast(strings.transactionUpdatedToast)
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            val existing = database.transactionDao().getTransactionById(id) ?: return@launch
            val affectedAccounts = setOfNotNull(existing.sourceAccountId, existing.destinationAccountId)

            database.withTransaction {
                database.transactionDao().delete(existing)
                affectedAccounts.forEach { accId ->
                    database.accountDao().recalculateAccountBalance(accId)
                }
            }

            dismissSheet()
            val strings = AppStrings.get(uiState.value.userPreferences.language)
            showToast(strings.transactionDeletedToast)
        }
    }

    fun reconcileBalance(accountId: String, actualBalance: Long, note: String) {
        viewModelScope.launch {
            val account = database.accountDao().getAccountById(accountId) ?: return@launch
            val delta = actualBalance - account.balance
            val strings = AppStrings.get(uiState.value.userPreferences.language)

            if (delta == 0L) {
                dismissSheet()
                showToast(strings.balanceReconciledToast)
                return@launch
            }

            val isPositive = delta > 0L
            val txType = if (isPositive) TransactionType.INCOME else TransactionType.EXPENSE
            val txAmount = abs(delta)
            val title = note.ifBlank { strings.defaultAdjustmentTitle }

            val tx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                sourceAccountId = accountId,
                destinationAccountId = null,
                type = txType.name,
                amount = txAmount,
                title = title,
                category = Category.OTHER.key,
                date = System.currentTimeMillis(),
                iconKey = if (isPositive) "ArrowDownLeft" else "Tag",
                accentKey = if (isPositive) "EMERALD" else "COBALT",
                updatedAt = System.currentTimeMillis()
            )

            database.withTransaction {
                database.transactionDao().insert(tx)
                database.accountDao().recalculateAccountBalance(accountId)
            }

            ChimeGenerator.playChime(uiState.value.userPreferences.soundEffectsEnabled, viewModelScope)
            dismissSheet()
            showToast(strings.balanceReconciledToast)
        }
    }

    fun createAccount(name: String, startingBalance: Long, accentKey: AccentKey) {
        val strings = currentStrings()
        if (name.isBlank()) {
            showToast(strings.toastAccountNameRequired)
            return
        }

        viewModelScope.launch {
            val account = AccountEntity(
                id = UUID.randomUUID().toString(),
                name = name.take(28),
                accentKey = accentKey.name,
                balance = startingBalance.coerceAtLeast(0L),
                initialBalance = startingBalance.coerceAtLeast(0L),
                sortIndex = (uiState.value.accounts.size),
                updatedAt = System.currentTimeMillis()
            )
            database.accountDao().insert(account)
            dismissSheet()
            showToast(strings.toastAccountCreatedFormat.format(name.take(28)))
        }
    }

    fun moveAccount(index: Int, moveUp: Boolean) {
        val currentAccounts = uiState.value.accounts.toMutableList()
        val targetIndex = if (moveUp) index - 1 else index + 1
        if (targetIndex in currentAccounts.indices) {
            val item = currentAccounts.removeAt(index)
            currentAccounts.add(targetIndex, item)
            viewModelScope.launch {
                val updated = currentAccounts.mapIndexed { idx, acc ->
                    AccountEntity.fromDomain(acc.copy(sortIndex = idx))
                }
                database.accountDao().insertAll(updated)
            }
        }
    }

    fun updateBudget(newBudget: Long) {
        viewModelScope.launch {
            ChimeGenerator.playChime(uiState.value.userPreferences.soundEffectsEnabled, viewModelScope)
            val current = uiState.value.monthlyPlan
            val monthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
            database.monthlyPlanDao().upsert(
                MonthlyPlanEntity(
                    id = current.id,
                    budget = newBudget.coerceAtLeast(0L),
                    startingSpent = current.startingSpent,
                    monthKey = monthKey
                )
            )
        }
    }

    fun updatePreferences(transform: suspend (UserPreferencesRepository) -> Unit) {
        viewModelScope.launch {
            transform(preferencesRepository)
            val updated = preferencesRepository.userPreferencesFlow.first()
            ChimeGenerator.playChime(updated.soundEffectsEnabled, viewModelScope)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            preferencesRepository.setPin(pin)
            showToast(currentStrings().toastPinSecured)
        }
    }

    fun removePin() {
        viewModelScope.launch {
            preferencesRepository.removePin()
            showToast(currentStrings().toastPinDisabled)
        }
    }

    fun updateBiometrics(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateBiometricsEnabled(enabled)
            val strings = currentStrings()
            showToast(if (enabled) strings.toastBiometricsEnabled else strings.toastBiometricsDisabled)
        }
    }

    fun toggleRailSide() {
        viewModelScope.launch {
            val current = uiState.value.userPreferences.railOnRight
            preferencesRepository.updateRailOnRight(!current)
        }
    }

    // --- Weather Services ---

    fun refreshWeather() {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            if (prefs.weatherOption == "not_now") return@launch

            val isFahrenheit = prefs.temperatureUnit.equals("F", ignoreCase = true)

            if (prefs.weatherOption == "search" && prefs.weatherCityLat != 0.0) {
                val result = weatherService.fetchWeather(
                    latitude = prefs.weatherCityLat,
                    longitude = prefs.weatherCityLon,
                    isFahrenheit = isFahrenheit,
                    locationLabel = prefs.weatherCityLabel.ifBlank { "Selected city" }
                )
                result.onSuccess { preferencesRepository.updateWeatherCache(it) }
            } else {
                // Approximate location
                val coords = locationProvider.getCoarseLocation()
                val lat = coords?.first ?: -6.20 // Default to Jakarta if emulator location off
                val lon = coords?.second ?: 106.81
                val result = weatherService.fetchWeather(
                    latitude = lat,
                    longitude = lon,
                    isFahrenheit = isFahrenheit,
                    locationLabel = "Approximate location"
                )
                result.onSuccess { preferencesRepository.updateWeatherCache(it) }
            }
        }
    }

    fun searchCity(query: String) {
        viewModelScope.launch {
            _isSearchingCity.value = true
            val lang = preferencesRepository.userPreferencesFlow.first().language
            val result = weatherService.searchCity(query, lang)
            result.onSuccess { _citySearchResults.value = it }
            _isSearchingCity.value = false
        }
    }

    fun selectCity(city: GeocodingCity) {
        viewModelScope.launch {
            preferencesRepository.updateWeatherOption("search", city.name, city.latitude, city.longitude)
            _citySearchResults.value = emptyList()
            refreshWeather()
            dismissSheet()
            showToast(currentStrings().toastWeatherLocationSet.format(city.name))
        }
    }

    fun setWeatherOption(option: String) {
        viewModelScope.launch {
            preferencesRepository.updateWeatherOption(option)
            if (option == "not_now") {
                // Kept in preferences
            } else if (option == "approx") {
                refreshWeather()
            }
        }
    }

    // --- Exchange Rate Services ---

    fun refreshRates(manual: Boolean) {
        viewModelScope.launch {
            val strings = currentStrings()
            val cached = preferencesRepository.cachedRatesFlow.first()
            if (manual && !exchangeRateService.canManualRefresh(cached?.fetchedAt ?: 0L)) {
                showToast(strings.toastRatesRecentlyUpdated)
                return@launch
            }

            val currentCode = preferencesRepository.userPreferencesFlow.first().currencyCode
            val result = exchangeRateService.fetchRates(currentCode)

            result.onSuccess { freshRates ->
                preferencesRepository.updateRatesCache(freshRates)
                _isRatesOffline.value = false
                if (manual) {
                    showToast(strings.toastRatesUpdated)
                }
            }.onFailure {
                _isRatesOffline.value = true
                if (manual) {
                    showToast(strings.toastRatesOffline)
                }
            }
        }
    }

    fun changeMainCurrency(targetCode: String) {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            val currentCode = prefs.currencyCode
            if (currentCode.equals(targetCode, ignoreCase = true)) {
                dismissSheet()
                return@launch
            }

            val strings = currentStrings()
            val cached = preferencesRepository.cachedRatesFlow.first()
            if (cached == null || cached.rates.isEmpty()) {
                showToast(strings.toastConnectInternetCurrency)
                return@launch
            }

            val sourceDecimals = Currency.getDecimals(currentCode)
            val targetDecimals = Currency.getDecimals(targetCode)

            val currentAccounts = database.accountDao().getAllAccountsFlow().first()
            val convertedAccounts = currentAccounts.map { acc ->
                val newBal = CurrencyConverter.convertMinorUnits(
                    amountMinor = acc.balance,
                    sourceCode = currentCode,
                    targetCode = targetCode,
                    baseCode = cached.baseCode,
                    rates = cached.rates,
                    sourceDecimals = sourceDecimals,
                    targetDecimals = targetDecimals
                )
                val newInitialBal = CurrencyConverter.convertMinorUnits(
                    amountMinor = acc.initialBalance,
                    sourceCode = currentCode,
                    targetCode = targetCode,
                    baseCode = cached.baseCode,
                    rates = cached.rates,
                    sourceDecimals = sourceDecimals,
                    targetDecimals = targetDecimals
                )
                acc.copy(
                    balance = newBal,
                    initialBalance = newInitialBal,
                    updatedAt = System.currentTimeMillis()
                )
            }

            val currentTx = database.transactionDao().getAllTransactionsFlow().first()
            val convertedTx = currentTx.map { tx ->
                val newAmt = CurrencyConverter.convertMinorUnits(
                    amountMinor = tx.amount,
                    sourceCode = currentCode,
                    targetCode = targetCode,
                    baseCode = cached.baseCode,
                    rates = cached.rates,
                    sourceDecimals = sourceDecimals,
                    targetDecimals = targetDecimals
                )
                tx.copy(amount = newAmt, updatedAt = System.currentTimeMillis())
            }

            val currentPlan = database.monthlyPlanDao().getPlan()
            val convertedPlan = currentPlan?.let { p ->
                val newBudget = CurrencyConverter.convertMinorUnits(
                    amountMinor = p.budget,
                    sourceCode = currentCode,
                    targetCode = targetCode,
                    baseCode = cached.baseCode,
                    rates = cached.rates,
                    sourceDecimals = sourceDecimals,
                    targetDecimals = targetDecimals
                )
                val newStartingSpent = CurrencyConverter.convertMinorUnits(
                    amountMinor = p.startingSpent,
                    sourceCode = currentCode,
                    targetCode = targetCode,
                    baseCode = cached.baseCode,
                    rates = cached.rates,
                    sourceDecimals = sourceDecimals,
                    targetDecimals = targetDecimals
                )
                p.copy(budget = newBudget, startingSpent = newStartingSpent)
            }

            database.withTransaction {
                database.accountDao().insertAll(convertedAccounts)
                database.transactionDao().insertAll(convertedTx)
                convertedPlan?.let { database.monthlyPlanDao().upsert(it) }
                database.accountDao().recalculateAllAccountBalances()
            }

            preferencesRepository.updateCurrencyCode(targetCode)
            dismissSheet()
            showToast(strings.currencyChangedToast.format(targetCode))

            // Automatically re-fetch rates using the new base currency
            refreshRates(manual = false)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            database.withTransaction {
                database.accountDao().deleteAll()
                database.transactionDao().deleteAll()
                database.monthlyPlanDao().deleteAll()
            }
            setScreen(AppScreen.DASHBOARD)
            showToast(currentStrings().allDataDeletedToast)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            preferencesRepository.updateOnboardingComplete(false)
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val outputStream = getApplication<Application>().contentResolver.openOutputStream(uri)
                val lang = preferencesRepository.userPreferencesFlow.first().language
                val strings = AppStrings.get(lang)
                if (outputStream == null) {
                    showToast(strings.backupExportFailed)
                    return@launch
                }
                val result = backupManager.exportToStream(outputStream)
                if (result.isSuccess) {
                    showToast(strings.backupExportSuccess)
                } else {
                    showToast(strings.backupExportFailed)
                }
            } catch (e: Exception) {
                val lang = preferencesRepository.userPreferencesFlow.first().language
                showToast(AppStrings.get(lang).backupExportFailed)
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val lang = preferencesRepository.userPreferencesFlow.first().language
                val strings = AppStrings.get(lang)
                if (inputStream == null) {
                    showToast(strings.backupImportFailed)
                    return@launch
                }
                val result = backupManager.restoreFromStream(inputStream)
                if (result.isSuccess) {
                    val res = result.getOrThrow()
                    setScreen(AppScreen.DASHBOARD)
                    showToast(strings.backupImportSuccess.format(res.accountsRestored, res.transactionsRestored))
                } else {
                    showToast(strings.backupImportFailed)
                }
            } catch (e: Exception) {
                val lang = preferencesRepository.userPreferencesFlow.first().language
                showToast(AppStrings.get(lang).backupImportFailed)
            }
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private data class Pent<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
