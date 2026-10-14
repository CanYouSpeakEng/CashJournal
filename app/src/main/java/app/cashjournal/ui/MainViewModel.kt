package app.cashjournal.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
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
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

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
    WEATHER_DETAIL
}

typealias SafeToSpendStatus = app.cashjournal.domain.model.SafeToSpendStatus
typealias SafeToSpendState = app.cashjournal.domain.model.SafeToSpendState


data class MainUiState(
    val currentScreen: AppScreen = AppScreen.DASHBOARD,
    val selectedAccount: Account? = null,
    val activeSheet: ActiveSheet? = null,
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

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    private val _selectedAccount = MutableStateFlow<Account?>(null)
    private val _activeSheet = MutableStateFlow<ActiveSheet?>(null)
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
        combine(_currentScreen, _selectedAccount, _activeSheet, _hideBalances, _isReorderMode, _toastMessage) { args ->
            args
        }
    ) { group1, group2, group3 ->
        val (rawAccounts, rawTransactions, plan, prefs) = group1
        val (weather, rates, isOffline, cityResults, isSearching) = group2

        val screen = group3[0] as AppScreen
        val account = group3[1] as? Account
        val sheet = group3[2] as? ActiveSheet
        val hideBal = group3[3] as Boolean
        val reorder = group3[4] as Boolean
        val toast = group3[5] as? String

        val totalWorth = rawAccounts.sumOf { it.balance }
        val recentTx = rawTransactions.take(5)

        // Calculate Safe to Spend
        val safeState = calculateSafeToSpend(plan, rawTransactions)

        MainUiState(
            currentScreen = screen,
            selectedAccount = account,
            activeSheet = sheet,
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
        }
    }

    private fun calculateSafeToSpend(plan: MonthlyPlan, transactions: List<Transaction>): SafeToSpendState {
        return SafeToSpendCalculator.calculate(plan, transactions)
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
        _citySearchResults.value = emptyList()
    }

    fun toggleHideBalances() {
        _hideBalances.value = !_hideBalances.value
        if (!_hideBalances.value) {
            showToast("Balances revealed")
        }
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
        amount: Long
    ) {
        if (amount <= 0) {
            showToast("Enter an amount")
            return
        }

        if (sourceAccountId.isNullOrBlank()) {
            showToast("Select an account")
            return
        }

        if (type == TransactionType.TRANSFER && (destAccountId == null || destAccountId == sourceAccountId)) {
            showToast("Select a different destination account")
            return
        }

        val detectedCategory = Category.detectFromNote(note)

        viewModelScope.launch {
            val sourceAcc = database.accountDao().getAccountById(sourceAccountId)
            val destAcc = destAccountId?.let { database.accountDao().getAccountById(it) }

            val title = when (type) {
                TransactionType.EXPENSE -> note.ifBlank { detectedCategory.defaultName }
                TransactionType.INCOME -> note.ifBlank { "Income" }
                TransactionType.TRANSFER -> "${sourceAcc?.name ?: "Source"} \u2192 ${destAcc?.name ?: "Destination"}"
            }

            val txCategory = if (type == TransactionType.TRANSFER) "TRANSFER" else detectedCategory.key
            val iconKey = when (type) {
                TransactionType.EXPENSE -> detectedCategory.key
                TransactionType.INCOME -> "ArrowDownLeft"
                TransactionType.TRANSFER -> "ArrowsLeftRight"
            }
            val accentKey = when (type) {
                TransactionType.EXPENSE -> sourceAcc?.accentKey ?: "COBALT"
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
                date = System.currentTimeMillis(),
                iconKey = iconKey,
                accentKey = accentKey,
                updatedAt = System.currentTimeMillis()
            )

            when (type) {
                TransactionType.EXPENSE -> {
                    database.accountDao().updateBalance(sourceAccountId, -amount)
                }
                TransactionType.INCOME -> {
                    database.accountDao().updateBalance(sourceAccountId, amount)
                }
                TransactionType.TRANSFER -> {
                    database.accountDao().updateBalance(sourceAccountId, -amount)
                    destAccountId?.let { database.accountDao().updateBalance(it, amount) }
                }
            }

            database.transactionDao().insert(tx)
            dismissSheet()
            showToast("Transaction logged")
        }
    }

    fun createAccount(name: String, startingBalance: Long, accentKey: AccentKey) {
        if (name.isBlank()) {
            showToast("Account name is required")
            return
        }

        viewModelScope.launch {
            val account = AccountEntity(
                id = UUID.randomUUID().toString(),
                name = name.take(28),
                accentKey = accentKey.name,
                balance = startingBalance.coerceAtLeast(0L),
                sortIndex = (uiState.value.accounts.size),
                updatedAt = System.currentTimeMillis()
            )
            database.accountDao().insert(account)
            dismissSheet()
            showToast("Account \"${name.take(28)}\" created")
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
            showToast("Weather location set to ${city.name}")
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
            val cached = preferencesRepository.cachedRatesFlow.first()
            if (manual && !exchangeRateService.canManualRefresh(cached?.fetchedAt ?: 0L)) {
                showToast("Rates already updated recently")
                return@launch
            }

            val currentCode = preferencesRepository.userPreferencesFlow.first().currencyCode
            val result = exchangeRateService.fetchRates(currentCode)

            result.onSuccess { freshRates ->
                preferencesRepository.updateRatesCache(freshRates)
                _isRatesOffline.value = false
                if (manual) {
                    showToast("Rates updated")
                }
            }.onFailure {
                _isRatesOffline.value = true
                if (manual) {
                    showToast("Unable to refresh rates (offline)")
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

            val cached = preferencesRepository.cachedRatesFlow.first()
            if (cached == null || cached.rates.isEmpty()) {
                showToast("Connect to the internet once to change your currency.")
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
                acc.copy(balance = newBal, updatedAt = System.currentTimeMillis())
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
            }

            preferencesRepository.updateCurrencyCode(targetCode)
            dismissSheet()
            showToast("Currency changed to $targetCode")

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
            showToast("All data deleted")
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private data class Pent<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
