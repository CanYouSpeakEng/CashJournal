package app.cashjournal.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import app.cashjournal.BuildConfig
import app.cashjournal.data.backup.BackupManager
import app.cashjournal.data.backup.BackupCrypto
import app.cashjournal.data.backup.BackupFileType
import app.cashjournal.data.update.AppReleaseInfo
import app.cashjournal.data.update.AppUpdateService
import app.cashjournal.data.update.UpdateState
import app.cashjournal.util.AppVersionProvider
import java.io.File
import java.util.TimeZone
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
import app.cashjournal.domain.model.AccountType
import app.cashjournal.domain.model.BudgetMode
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.MonthlyPlan
import app.cashjournal.domain.model.RecurringTransaction
import app.cashjournal.domain.model.RecurrenceFrequency
import app.cashjournal.data.local.entity.RecurringTransactionEntity
import app.cashjournal.domain.model.SafeToSpendCalculator
import app.cashjournal.domain.model.SafeToSpendState
import app.cashjournal.domain.model.SafeToSpendStatus
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.data.csv.CsvDuplicateDetector
import app.cashjournal.data.csv.CsvExporter
import app.cashjournal.data.csv.CsvParser
import app.cashjournal.data.csv.CsvTokenizer
import app.cashjournal.data.csv.ParsedCsvTransaction
import app.cashjournal.ui.sensory.ChimeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    RELEASE_NOTES,
    EDIT_ACCOUNT,
    UPDATE_INFO,
    RECURRING_BILLS,
    CSV_IMPORT
}

typealias SafeToSpendStatus = app.cashjournal.domain.model.SafeToSpendStatus
typealias SafeToSpendState = app.cashjournal.domain.model.SafeToSpendState

data class CsvImportState(
    val targetAccountId: String,
    val items: List<ParsedCsvTransaction> = emptyList(),
    val fileName: String = ""
) {
    val totalFound: Int get() = items.size
    val duplicatesCount: Int get() = items.count { it.isDuplicate }
    val selectedCount: Int get() = items.count { it.isSelected }
}

data class MainUiState(
    val isPreferencesLoaded: Boolean = false,
    val currentScreen: AppScreen = AppScreen.DASHBOARD,
    val selectedAccount: Account? = null,
    val activeSheet: ActiveSheet? = null,
    val selectedTransaction: Transaction? = null,
    val selectedEditAccount: Account? = null,
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
    val isSearchingCity: Boolean = false,
    val updateState: UpdateState = UpdateState.Idle,
    val isUpdateReadyToInstall: Boolean = false,
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val csvImportState: CsvImportState? = null
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
    private val _selectedEditAccount = MutableStateFlow<Account?>(null)
    private val _hideBalances = MutableStateFlow(false)
    private val _isReorderMode = MutableStateFlow(false)
    private val _toastMessage = MutableStateFlow<String?>(null)
    private val _isRatesOffline = MutableStateFlow(false)
    private val _citySearchResults = MutableStateFlow<List<GeocodingCity>>(emptyList())
    private val _isSearchingCity = MutableStateFlow(false)

    // OTA Update State
    private val appUpdateService = AppUpdateService()
    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    private val _csvImportState = MutableStateFlow<CsvImportState?>(null)
    val updateState = _updateState.asStateFlow()

    // App Lock & Privacy Veil Lifecycle State
    private val _isAppLocked = MutableStateFlow(true)
    private val _isVeiled = MutableStateFlow(false)
    val isAppLocked = _isAppLocked.asStateFlow()
    val isVeiled = _isVeiled.asStateFlow()
    private var lastBackgroundTimestamp: Long = 0L
    private var isInstallingUpdate: Boolean = false
    private var installHandOffTimestamp: Long = 0L

    val currentScreen = _currentScreen.asStateFlow()
    val activeSheet = _activeSheet.asStateFlow()
    val toastMessage = _toastMessage.asStateFlow()

    private val accountsFlow = database.accountDao().getAllAccountsFlow()
    private val transactionsFlow = database.transactionDao().getAllTransactionsFlow()
    private val monthlyPlanFlow = database.monthlyPlanDao().getPlanFlow()
    private val preferencesFlow = preferencesRepository.userPreferencesFlow
    private val weatherFlow = preferencesRepository.cachedWeatherFlow
    private val ratesFlow = preferencesRepository.cachedRatesFlow
    private val recurringFlow = database.recurringTransactionDao().getAllRecurringFlow()

    val uiState: StateFlow<MainUiState> = combine(
        combine(
            accountsFlow.catch { emit(emptyList()) },
            transactionsFlow.catch { emit(emptyList()) },
            monthlyPlanFlow.catch { emit(null) },
            preferencesFlow.catch { emit(UserPreferences()) },
            recurringFlow.catch { emit(emptyList()) }
        ) { accs, txs, planEntity, prefs, recurringEntities ->
            val domainAccs = accs.map { it.toDomain() }
            val domainTxs = txs.map { it.toDomain() }
            val domainPlan = planEntity?.toDomain() ?: MonthlyPlan()
            val domainRecurring = recurringEntities.map { it.toDomain() }
            Pent(domainAccs, domainTxs, domainPlan, prefs, domainRecurring)
        },
        combine(
            weatherFlow.catch { emit(null) },
            ratesFlow.catch { emit(null) },
            _isRatesOffline,
            _citySearchResults,
            _isSearchingCity
        ) { weather, rates, offline, cities, searching ->
            Pent(weather, rates, offline, cities, searching)
        },
        combine(
            _currentScreen,
            _selectedAccount,
            _activeSheet,
            _selectedTransaction,
            _selectedEditAccount,
            _hideBalances,
            _isReorderMode,
            _toastMessage,
            _updateState,
            _csvImportState
        ) { args ->
            args
        }
    ) { group1, group2, group3 ->
        val (rawAccounts, rawTransactions, plan, prefs, rawRecurring) = group1
        val (weather, rates, isOffline, cityResults, isSearching) = group2

        val screen = group3[0] as AppScreen
        val rawSelectedAccount = group3[1] as? Account
        val account = rawSelectedAccount?.let { sel -> rawAccounts.find { it.id == sel.id } ?: sel }
        val sheet = group3[2] as? ActiveSheet
        val selectedTx = group3[3] as? Transaction
        val rawEditAcc = group3[4] as? Account
        val editAcc = rawEditAcc?.let { sel -> rawAccounts.find { it.id == sel.id } ?: sel }
        val hideBal = group3[5] as Boolean
        val reorder = group3[6] as Boolean
        val toast = group3[7] as? String
        val currentUpdateState = group3[8] as UpdateState
        val csvState = group3[9] as? CsvImportState
        val isUpdateReady = currentUpdateState is UpdateState.ReadyToInstall

        val totalWorth = rawAccounts.sumOf { it.balance }
        val liquidFunds = rawAccounts
            .filter { it.accountType != AccountType.CREDIT_CARD }
            .sumOf { it.balance }
            .coerceAtLeast(0L)
        val recentTx = rawTransactions.take(5)

        // Calculate Safe to Spend (using spendable liquid capital, excluding credit card debt)
        val safeState = calculateSafeToSpend(
            plan = plan,
            transactions = rawTransactions,
            totalNetWorth = liquidFunds,
            hasAccounts = rawAccounts.isNotEmpty(),
            cycleStartDay = prefs.cycleStartDay,
            recurringTransactions = rawRecurring
        )

        MainUiState(
            isPreferencesLoaded = true,
            currentScreen = screen,
            selectedAccount = account,
            activeSheet = sheet,
            selectedTransaction = selectedTx,
            selectedEditAccount = editAcc,
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
            isSearchingCity = isSearching,
            updateState = currentUpdateState,
            isUpdateReadyToInstall = isUpdateReady,
            recurringTransactions = rawRecurring,
            csvImportState = csvState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        // Automatically check if rates or weather need refresh on startup
        viewModelScope.launch {
            try {
                val prefs = preferencesFlow.first()
                val cachedRates = ratesFlow.first()
                if (exchangeRateService.shouldAutoRefresh(cachedRates?.fetchedAt ?: 0L) || cachedRates?.baseCode != "USD") {
                    refreshRates(manual = false)
                }
                if (prefs.weatherOption != "not_now") {
                    refreshWeather()
                }
                val currentRuntimeVersion = AppVersionProvider.getVersionName(application)
                withContext(Dispatchers.IO) {
                    appUpdateService.cleanStaleUpdateApks(application.cacheDir, currentRuntimeVersion)
                    database.transactionDao().sanitizeNonTransferDestinations()
                }
                if (prefs.onboardingComplete && prefs.lastSeenVersion != currentRuntimeVersion) {
                    _activeSheet.value = ActiveSheet.RELEASE_NOTES
                }
                checkDailyUpdateQuietly()
            } catch (_: Exception) {
                // Prevent background startup tasks from failing process initialization
            }
        }
    }

    private fun calculateSafeToSpend(
        plan: MonthlyPlan,
        transactions: List<Transaction>,
        totalNetWorth: Long = 0L,
        hasAccounts: Boolean = true,
        cycleStartDay: Int = 1,
        recurringTransactions: List<RecurringTransaction> = emptyList()
    ): SafeToSpendState {
        return SafeToSpendCalculator.calculate(
            plan = plan,
            transactions = transactions,
            totalNetWorth = totalNetWorth,
            hasAccounts = hasAccounts,
            cycleStartDay = cycleStartDay,
            recurringTransactions = recurringTransactions
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
            val cached = uiState.value.cachedRates
            val lastFetched = cached?.fetchedAt ?: 0L
            if (exchangeRateService.shouldAutoRefresh(lastFetched) || cached?.baseCode != "USD") {
                refreshRates(manual = false)
            }
        }
    }

    fun dismissSheet() {
        _activeSheet.value = null
        _selectedTransaction.value = null
        _selectedEditAccount.value = null
        _citySearchResults.value = emptyList()
    }

    fun onAppForegrounded(hasPin: Boolean) {
        val now = System.currentTimeMillis()
        val inGracePeriod = isInstallingUpdate && (now - installHandOffTimestamp < 60_000L)
        if (isInstallingUpdate && !inGracePeriod) {
            isInstallingUpdate = false
        }

        if (hasPin && lastBackgroundTimestamp > 0L && !inGracePeriod) {
            val elapsed = now - lastBackgroundTimestamp
            if (elapsed > 15_000L) {
                _isAppLocked.value = true
            }
        }
        _isVeiled.value = false
        checkDailyUpdateQuietly()
    }

    fun onAppBackgrounded(hasPin: Boolean) {
        lastBackgroundTimestamp = System.currentTimeMillis()
        if (hasPin) {
            if (!isInstallingUpdate) {
                _isVeiled.value = true
            }
        }
    }

    fun lockAppNow() {
        _isAppLocked.value = true
        _isVeiled.value = false
    }

    fun unlockApp() {
        _isAppLocked.value = false
        _isVeiled.value = false
    }

    fun hasPinConfigured(): Boolean {
        val prefs = uiState.value.userPreferences
        return prefs.pinEnabled && prefs.pinHash.isNotEmpty()
    }

    fun openEditAccount(account: Account) {
        _selectedEditAccount.value = account
        _activeSheet.value = ActiveSheet.EDIT_ACCOUNT
    }

    fun dismissReleaseNotes() {
        viewModelScope.launch {
            val currentRuntimeVersion = AppVersionProvider.getVersionName(getApplication())
            preferencesRepository.updateLastSeenVersion(currentRuntimeVersion)
        }
        dismissSheet()
    }

    fun openUpdateSheet() {
        val current = _updateState.value
        if (current is UpdateState.ReadyToInstall || current is UpdateState.Downloading || current is UpdateState.Available) {
            _activeSheet.value = ActiveSheet.UPDATE_INFO
        } else {
            checkForUpdates(manual = true)
        }
    }

    fun checkForUpdates(manual: Boolean = true) {
        viewModelScope.launch {
            if (manual) {
                _activeSheet.value = ActiveSheet.UPDATE_INFO
            }
            _updateState.value = UpdateState.Checking
            val currentRuntimeVersion = AppVersionProvider.getVersionName(getApplication())
            val result = appUpdateService.fetchLatestRelease(
                currentVersion = currentRuntimeVersion,
                isDebugBuild = BuildConfig.DEBUG
            )
            result.onSuccess { info ->
                if (info.isNewer) {
                    val cacheDir = getApplication<Application>().cacheDir
                    val updateFile = File(cacheDir, "updates/CashJournal-${info.versionName}.apk")
                    if (updateFile.exists() && updateFile.isFile && updateFile.length() > 0L && (info.apkSize <= 0L || updateFile.length() == info.apkSize)) {
                        _updateState.value = UpdateState.ReadyToInstall(info, updateFile)
                    } else {
                        _updateState.value = UpdateState.Available(info)
                    }
                    if (!manual) {
                        showToast(currentStrings().updateAvailableFormat.format(info.versionName))
                    }
                } else {
                    _updateState.value = UpdateState.UpToDate
                    withContext(Dispatchers.IO) {
                        appUpdateService.cleanStaleUpdateApks(getApplication<Application>().cacheDir, currentRuntimeVersion)
                    }
                }
            }.onFailure { err ->
                _updateState.value = UpdateState.Error(err.message ?: "Failed to connect to GitHub")
            }
        }
    }

    fun startUpdateDownload() {
        val current = _updateState.value
        val info = when (current) {
            is UpdateState.Available -> current.info
            is UpdateState.ReadyToInstall -> current.info
            else -> return
        }

        viewModelScope.launch {
            val cacheDir = getApplication<Application>().cacheDir
            val updateFile = File(cacheDir, "updates/CashJournal-${info.versionName}.apk")

            // Short-circuit: If the complete APK is already on disk, avoid downloading it again
            if (updateFile.exists() && updateFile.isFile && updateFile.length() > 0L && (info.apkSize <= 0L || updateFile.length() == info.apkSize)) {
                _updateState.value = UpdateState.ReadyToInstall(info, updateFile)
                showToast(currentStrings().updateReadyToInstall)
                return@launch
            }

            withContext(Dispatchers.IO) {
                appUpdateService.cleanStaleUpdateApks(cacheDir, AppVersionProvider.getVersionName(getApplication()))
                appUpdateService.purgeOtherUpdateApks(cacheDir, info.versionName)
            }

            _updateState.value = UpdateState.Downloading(info, 0f, 0L, info.apkSize)

            val result = appUpdateService.downloadApk(
                downloadUrl = info.apkDownloadUrl,
                destinationFile = updateFile
            ) { bytesDownloaded, totalBytes, progress ->
                _updateState.value = UpdateState.Downloading(info, progress, bytesDownloaded, totalBytes)
            }

            result.onSuccess { file ->
                _updateState.value = UpdateState.ReadyToInstall(info, file)
                showToast(currentStrings().updateReadyToInstall)
            }.onFailure { err ->
                _updateState.value = UpdateState.Error("Download failed: ${err.message}")
            }
        }
    }

    fun dismissUpdateSheetToBackground() {
        dismissSheet()
        showToast(currentStrings().downloadingInBackgroundToast)
    }

    fun installDownloadedApk(activity: Activity) {
        val current = _updateState.value
        val file = (current as? UpdateState.ReadyToInstall)?.apkFile ?: return
        try {
            isInstallingUpdate = true
            installHandOffTimestamp = System.currentTimeMillis()
            dismissSheet()

            val uri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val installerPackages = mutableSetOf(
                "com.google.android.packageinstaller",
                "com.android.packageinstaller",
                "com.samsung.android.packageinstaller"
            )
            val resInfoList = activity.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            for (resolveInfo in resInfoList) {
                installerPackages.add(resolveInfo.activityInfo.packageName)
            }
            for (pkgName in installerPackages) {
                try {
                    activity.grantUriPermission(
                        pkgName,
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (_: Exception) {}
            }

            activity.startActivity(intent)
            activity.finish()
        } catch (e: Exception) {
            isInstallingUpdate = false
            showToast("Failed to launch package installer: ${e.message}")
        }
    }

    fun checkDailyUpdateQuietly() {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferencesFlow.first()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Jakarta")
            }
            val todayWib = sdf.format(Date())
            if (prefs.lastUpdateCheckDateWib != todayWib) {
                preferencesRepository.updateLastUpdateCheckDateWib(todayWib)
                checkForUpdates(manual = false)
            }
        }
    }

    fun showEditTransaction(transaction: Transaction) {
        _selectedTransaction.value = transaction
        _activeSheet.value = ActiveSheet.EDIT_TRANSACTION
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

            val finalDestAccountId = if (type == TransactionType.TRANSFER) destAccountId else null

            val tx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                sourceAccountId = sourceAccountId,
                destinationAccountId = finalDestAccountId,
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
                finalDestAccountId?.let { database.accountDao().recalculateAccountBalance(it) }
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

            val finalDestAccountId = if (type == TransactionType.TRANSFER) destAccountId else null

            val updatedTx = existing.copy(
                title = title,
                category = txCategory,
                type = type.name,
                amount = amount,
                date = date,
                iconKey = iconKey,
                accentKey = accentKey,
                sourceAccountId = sourceAccountId,
                destinationAccountId = finalDestAccountId,
                updatedAt = System.currentTimeMillis()
            )

            val affectedAccounts = setOfNotNull(
                existing.sourceAccountId,
                existing.destinationAccountId,
                sourceAccountId,
                finalDestAccountId
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

    fun openRecurringBillsSheet() {
        openSheet(ActiveSheet.RECURRING_BILLS)
    }

    fun createRecurringBill(
        title: String,
        amount: Long,
        category: Category,
        dueDay: Int,
        sourceAccountId: String?,
        frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
        note: String? = null
    ) {
        val strings = currentStrings()
        if (title.isBlank()) {
            showToast(strings.toastBillNameRequired)
            return
        }
        if (amount <= 0) {
            showToast(strings.toastEnterAmount)
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entity = RecurringTransactionEntity(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                category = category.key,
                type = TransactionType.EXPENSE.name,
                amount = amount,
                frequency = frequency.name,
                dueDay = dueDay.coerceIn(1, 31),
                sourceAccountId = sourceAccountId,
                destinationAccountId = null,
                isActive = true,
                autoLog = false,
                lastLoggedDate = null,
                note = note?.trim(),
                createdAt = now,
                updatedAt = now
            )
            database.recurringTransactionDao().insert(entity)
            showToast(strings.billCreatedToast)
        }
    }

    fun updateRecurringBill(
        id: String,
        title: String,
        amount: Long,
        category: Category,
        dueDay: Int,
        sourceAccountId: String?,
        frequency: RecurrenceFrequency = RecurrenceFrequency.MONTHLY,
        isActive: Boolean = true,
        note: String? = null
    ) {
        val strings = currentStrings()
        if (title.isBlank()) {
            showToast(strings.toastBillNameRequired)
            return
        }
        if (amount <= 0) {
            showToast(strings.toastEnterAmount)
            return
        }
        viewModelScope.launch {
            val existing = database.recurringTransactionDao().getRecurringById(id) ?: return@launch
            val updated = existing.copy(
                title = title.trim(),
                amount = amount,
                category = category.key,
                dueDay = dueDay.coerceIn(1, 31),
                sourceAccountId = sourceAccountId,
                frequency = frequency.name,
                isActive = isActive,
                note = note?.trim(),
                updatedAt = System.currentTimeMillis()
            )
            database.recurringTransactionDao().update(updated)
            showToast(strings.billUpdatedToast)
        }
    }

    fun deleteRecurringBill(id: String) {
        val strings = currentStrings()
        viewModelScope.launch {
            database.recurringTransactionDao().deleteById(id)
            showToast(strings.billDeletedToast)
        }
    }

    fun toggleRecurringBillActive(id: String, isActive: Boolean) {
        viewModelScope.launch {
            database.recurringTransactionDao().updateActiveState(id, isActive)
        }
    }

    fun markRecurringBillAsPaid(id: String) {
        val strings = currentStrings()
        viewModelScope.launch {
            val billEntity = database.recurringTransactionDao().getRecurringById(id) ?: return@launch
            val rawAccounts = database.accountDao().getAllAccounts()
            val targetAccountId = billEntity.sourceAccountId?.takeIf { accId -> rawAccounts.any { it.id == accId } }
                ?: rawAccounts.firstOrNull()?.id

            if (targetAccountId == null) {
                showToast(strings.toastSelectAccount)
                return@launch
            }

            val category = Category.fromKey(billEntity.category)
            val now = System.currentTimeMillis()
            val tx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                sourceAccountId = targetAccountId,
                destinationAccountId = null,
                type = TransactionType.EXPENSE.name,
                amount = billEntity.amount,
                title = billEntity.title,
                category = category.key,
                date = now,
                iconKey = category.key,
                accentKey = category.defaultAccent.name,
                updatedAt = now
            )

            database.withTransaction {
                database.transactionDao().insert(tx)
                database.accountDao().recalculateAccountBalance(targetAccountId)
                database.recurringTransactionDao().updateLastLoggedDate(id, now)
            }

            ChimeGenerator.playChime(uiState.value.userPreferences.soundEffectsEnabled, viewModelScope)
            showToast(strings.billMarkedAsPaidToast)
        }
    }

    fun createAccount(
        name: String,
        startingBalance: Long,
        accentKey: AccentKey,
        accountType: AccountType = AccountType.BANK_DEBIT
    ) {
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
                accountType = accountType.name,
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

    fun updateAccountDetails(
        accountId: String,
        name: String,
        accountType: AccountType,
        accentKey: AccentKey,
        actualBalance: Long? = null,
        reconcileNote: String? = null
    ) {
        val strings = currentStrings()
        if (name.isBlank()) {
            showToast(strings.toastAccountNameRequired)
            return
        }

        viewModelScope.launch {
            val existing = database.accountDao().getAccountById(accountId) ?: return@launch
            val updated = existing.copy(
                name = name.take(28),
                accountType = accountType.name,
                accentKey = accentKey.name,
                updatedAt = System.currentTimeMillis()
            )

            val balanceChanged = actualBalance != null && actualBalance != existing.balance
            database.withTransaction {
                database.accountDao().update(updated)

                if (balanceChanged) {
                    val delta = actualBalance!! - existing.balance
                    val isPositive = delta > 0L
                    val txType = if (isPositive) TransactionType.INCOME else TransactionType.EXPENSE
                    val txAmount = abs(delta)
                    val title = if (!reconcileNote.isNullOrBlank()) reconcileNote else strings.defaultAdjustmentTitle

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
                    database.transactionDao().insert(tx)
                    database.accountDao().recalculateAccountBalance(accountId)
                }
            }

            if (_selectedAccount.value?.id == accountId) {
                val refreshed = database.accountDao().getAccountById(accountId)?.toDomain()
                if (refreshed != null) {
                    _selectedAccount.value = refreshed
                }
            }

            if (balanceChanged) {
                ChimeGenerator.playChime(uiState.value.userPreferences.soundEffectsEnabled, viewModelScope)
            }

            dismissSheet()
            showToast(if (balanceChanged) strings.accountUpdatedAndReconciledToast else strings.accountUpdatedToast)
        }
    }

    fun deleteAccount(accountId: String) {
        val strings = currentStrings()
        if (uiState.value.accounts.size <= 1) {
            showToast(strings.deleteAccountCannotDeleteLast)
            return
        }

        viewModelScope.launch {
            val existing = database.accountDao().getAccountById(accountId) ?: return@launch
            database.withTransaction {
                database.accountDao().delete(existing)
            }
            dismissSheet()
            if (_selectedAccount.value?.id == accountId) {
                _selectedAccount.value = null
                _currentScreen.value = AppScreen.ACCOUNTS
            }
            showToast(strings.accountDeletedToast)
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

    fun updateBudgetPlan(
        budgetMode: BudgetMode = uiState.value.monthlyPlan.budgetMode,
        budgetPercent: Int = uiState.value.monthlyPlan.budgetPercent,
        nominalBudget: Long = uiState.value.monthlyPlan.budget
    ) {
        viewModelScope.launch {
            ChimeGenerator.playChime(uiState.value.userPreferences.soundEffectsEnabled, viewModelScope)
            val current = uiState.value.monthlyPlan
            val monthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
            database.monthlyPlanDao().upsert(
                MonthlyPlanEntity(
                    id = current.id,
                    budget = nominalBudget.coerceAtLeast(0L),
                    startingSpent = current.startingSpent,
                    monthKey = monthKey,
                    budgetMode = budgetMode.name,
                    budgetPercent = budgetPercent.coerceIn(1, 100)
                )
            )
        }
    }

    fun updateBudget(newBudget: Long) {
        updateBudgetPlan(nominalBudget = newBudget)
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
            val isStaleOrNonUsd = cached == null || cached.baseCode != "USD"
            if (manual && !exchangeRateService.canManualRefresh(cached?.fetchedAt ?: 0L) && !isStaleOrNonUsd) {
                showToast(strings.toastRatesRecentlyUpdated)
                return@launch
            }

            // Always anchor rate queries to USD to guarantee full 6-digit floating-point precision
            // across all world currencies, bypassing the API's 2-significant-digit scientific notation
            // rounding truncation on low-unit currencies like IDR.
            val result = exchangeRateService.fetchRates("USD")

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

            val currentRecurring = database.recurringTransactionDao().getAllRecurring()
            val convertedRecurring = currentRecurring.map { rec ->
                val newAmt = CurrencyConverter.convertMinorUnits(
                    amountMinor = rec.amount,
                    sourceCode = currentCode,
                    targetCode = targetCode,
                    baseCode = cached.baseCode,
                    rates = cached.rates,
                    sourceDecimals = sourceDecimals,
                    targetDecimals = targetDecimals
                )
                rec.copy(amount = newAmt, updatedAt = System.currentTimeMillis())
            }

            database.withTransaction {
                database.accountDao().insertAll(convertedAccounts)
                database.transactionDao().insertAll(convertedTx)
                convertedPlan?.let { database.monthlyPlanDao().upsert(it) }
                database.recurringTransactionDao().insertAll(convertedRecurring)
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
                database.recurringTransactionDao().deleteAll()
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

    fun exportEncryptedBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            try {
                val outputStream = getApplication<Application>().contentResolver.openOutputStream(uri)
                val lang = preferencesRepository.userPreferencesFlow.first().language
                val strings = AppStrings.get(lang)
                if (outputStream == null) {
                    showToast(strings.backupExportFailed)
                    return@launch
                }
                val result = backupManager.exportToEncryptedStream(outputStream, password)
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

    suspend fun inspectBackupFile(uri: Uri): BackupFileType = withContext(Dispatchers.IO) {
        try {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri) ?: return@withContext BackupFileType.INVALID
            val buffer = ByteArray(64)
            val bytesRead = inputStream.use { it.read(buffer) }
            if (bytesRead <= 0) return@withContext BackupFileType.INVALID
            val validSlice = buffer.copyOf(bytesRead)
            backupManager.inspectBackup(validSlice)
        } catch (e: Exception) {
            BackupFileType.INVALID
        }
    }

    fun restoreBackup(
        uri: Uri,
        password: String? = null,
        onPasswordError: ((String) -> Unit)? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                val lang = preferencesRepository.userPreferencesFlow.first().language
                val strings = AppStrings.get(lang)
                if (inputStream == null) {
                    showToast(strings.backupImportFailed)
                    return@launch
                }
                val result = backupManager.restoreFromStream(inputStream, password)
                if (result.isSuccess) {
                    val res = result.getOrThrow()
                    setScreen(AppScreen.DASHBOARD)
                    showToast(strings.backupImportSuccess.format(res.accountsRestored, res.transactionsRestored))
                    onSuccess?.invoke()
                } else {
                    val ex = result.exceptionOrNull()
                    if (ex is BackupCrypto.InvalidPasswordException) {
                        onPasswordError?.invoke(strings.wrongPasswordError)
                    } else {
                        showToast(strings.backupImportFailed)
                    }
                }
            } catch (e: Exception) {
                val lang = preferencesRepository.userPreferencesFlow.first().language
                showToast(AppStrings.get(lang).backupImportFailed)
            }
        }
    }

    fun openCsvImport(uri: Uri, preselectedAccountId: String? = null) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val lang = preferencesRepository.userPreferencesFlow.first().language
                val strings = AppStrings.get(lang)
                if (inputStream == null) {
                    showToast(strings.csvImportEmptyToast)
                    return@launch
                }

                var fileName = "Statement.csv"
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1 && cursor.moveToFirst()) {
                            fileName = cursor.getString(nameIndex) ?: "Statement.csv"
                        }
                    }
                } catch (_: Exception) {}

                val rows = withContext(Dispatchers.IO) {
                    CsvTokenizer.parse(inputStream)
                }

                if (rows.isEmpty()) {
                    showToast(strings.csvImportEmptyToast)
                    return@launch
                }

                val mapping = CsvParser.detectMapping(rows[0])
                val currCode = preferencesRepository.userPreferencesFlow.first().currencyCode
                val parsed = CsvParser.parseRows(rows, mapping, currCode, skipHeader = true)

                if (parsed.isEmpty()) {
                    showToast(strings.csvImportEmptyToast)
                    return@launch
                }

                val accs = accountsFlow.first().map { it.toDomain() }
                val targetAccId = preselectedAccountId
                    ?: _selectedAccount.value?.id
                    ?: accs.firstOrNull()?.id
                    ?: return@launch

                val allTxs = transactionsFlow.first().map { it.toDomain() }
                val flagged = CsvDuplicateDetector.flagDuplicates(parsed, allTxs, targetAccId)

                _csvImportState.value = CsvImportState(
                    targetAccountId = targetAccId,
                    items = flagged,
                    fileName = fileName
                )
                openSheet(ActiveSheet.CSV_IMPORT)
            } catch (e: Exception) {
                val lang = preferencesRepository.userPreferencesFlow.first().language
                showToast(AppStrings.get(lang).csvImportEmptyToast)
            }
        }
    }

    fun setCsvImportTargetAccount(accountId: String) {
        val current = _csvImportState.value ?: return
        viewModelScope.launch {
            val allTxs = transactionsFlow.first().map { it.toDomain() }
            val reflagged = CsvDuplicateDetector.flagDuplicates(current.items, allTxs, accountId)
            _csvImportState.value = current.copy(
                targetAccountId = accountId,
                items = reflagged
            )
        }
    }

    fun toggleCsvImportItemSelection(itemId: String) {
        val current = _csvImportState.value ?: return
        val updated = current.items.map { item ->
            if (item.id == itemId) item.copy(isSelected = !item.isSelected) else item
        }
        _csvImportState.value = current.copy(items = updated)
    }

    fun setSelectAllCsvImportItems(select: Boolean) {
        val current = _csvImportState.value ?: return
        val updated = current.items.map { item ->
            item.copy(isSelected = select)
        }
        _csvImportState.value = current.copy(items = updated)
    }

    fun confirmCsvImport() {
        val current = _csvImportState.value ?: return
        val selectedItems = current.items.filter { it.isSelected }
        if (selectedItems.isEmpty()) return

        viewModelScope.launch {
            val entities = selectedItems.map { item ->
                val iconKey = when (item.type) {
                    TransactionType.EXPENSE -> item.category.key
                    TransactionType.INCOME -> "ArrowDownLeft"
                    TransactionType.TRANSFER -> "ArrowsLeftRight"
                }
                TransactionEntity(
                    id = UUID.randomUUID().toString(),
                    title = item.title,
                    amount = item.amount,
                    type = item.type.name,
                    category = item.category.key,
                    sourceAccountId = current.targetAccountId,
                    destinationAccountId = null,
                    date = item.parsedDateMillis,
                    iconKey = iconKey,
                    accentKey = item.category.defaultAccent.name,
                    updatedAt = System.currentTimeMillis()
                )
            }

            database.withTransaction {
                database.transactionDao().insertAll(entities)
                database.accountDao().recalculateAccountBalance(current.targetAccountId)
            }

            val lang = preferencesRepository.userPreferencesFlow.first().language
            val strings = AppStrings.get(lang)
            ChimeGenerator.playChime(uiState.value.userPreferences.soundEffectsEnabled, viewModelScope)
            _csvImportState.value = null
            dismissSheet()
            showToast(strings.csvImportSuccessToast.format(entities.size))
        }
    }

    fun exportCsvSpreadsheet(uri: Uri) {
        viewModelScope.launch {
            try {
                val outputStream = getApplication<Application>().contentResolver.openOutputStream(uri)
                val lang = preferencesRepository.userPreferencesFlow.first().language
                val strings = AppStrings.get(lang)
                if (outputStream == null) {
                    showToast(strings.csvExportFailedToast)
                    return@launch
                }
                val txs = transactionsFlow.first().map { it.toDomain() }
                val accs = accountsFlow.first().map { it.toDomain() }
                val currCode = preferencesRepository.userPreferencesFlow.first().currencyCode
                withContext(Dispatchers.IO) {
                    CsvExporter.exportToStream(txs, accs, currCode, outputStream)
                }
                showToast(strings.csvExportSuccessToast)
            } catch (e: Exception) {
                val lang = preferencesRepository.userPreferencesFlow.first().language
                showToast(AppStrings.get(lang).csvExportFailedToast)
            }
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private data class Pent<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
