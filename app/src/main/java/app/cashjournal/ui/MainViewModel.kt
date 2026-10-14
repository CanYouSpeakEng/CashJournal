package app.cashjournal.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.cashjournal.data.ExampleDataSeeder
import app.cashjournal.data.local.AppDatabase
import app.cashjournal.data.local.entity.AccountEntity
import app.cashjournal.data.local.entity.TransactionEntity
import app.cashjournal.data.preferences.UserPreferences
import app.cashjournal.data.preferences.UserPreferencesRepository
import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.MonthlyPlan
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

enum class SafeToSpendStatus {
    SAFE,
    NEAR_LIMIT,
    RECALIBRATE,
    SET_BUDGET
}

data class SafeToSpendState(
    val safeToSpendToday: Long = 0L,
    val remaining: Long = 0L,
    val spentThisMonth: Long = 0L,
    val budget: Long = 0L,
    val daysLeft: Int = 1,
    val isLastDay: Boolean = false,
    val percentSpent: Float = 0f,
    val displayPercent: Int = 0,
    val status: SafeToSpendStatus = SafeToSpendStatus.SAFE
)

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
    val toastMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val preferencesRepository = UserPreferencesRepository(application)

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    private val _selectedAccount = MutableStateFlow<Account?>(null)
    private val _activeSheet = MutableStateFlow<ActiveSheet?>(null)
    private val _hideBalances = MutableStateFlow(false)
    private val _isReorderMode = MutableStateFlow(false)
    private val _toastMessage = MutableStateFlow<String?>(null)

    val currentScreen = _currentScreen.asStateFlow()
    val activeSheet = _activeSheet.asStateFlow()
    val toastMessage = _toastMessage.asStateFlow()

    private val accountsFlow = database.accountDao().getAllAccountsFlow()
    private val transactionsFlow = database.transactionDao().getAllTransactionsFlow()
    private val monthlyPlanFlow = database.monthlyPlanDao().getPlanFlow()
    private val preferencesFlow = preferencesRepository.userPreferencesFlow

    val uiState: StateFlow<MainUiState> = combine(
        accountsFlow,
        transactionsFlow,
        monthlyPlanFlow,
        preferencesFlow,
        _currentScreen,
        _selectedAccount,
        _activeSheet,
        _hideBalances,
        _isReorderMode,
        _toastMessage
    ) { args ->
        val rawAccounts = (args[0] as List<*>).filterIsInstance<AccountEntity>().map { it.toDomain() }
        val rawTransactions = (args[1] as List<*>).filterIsInstance<TransactionEntity>().map { it.toDomain() }
        val rawPlanEntity = args[2] as? app.cashjournal.data.local.entity.MonthlyPlanEntity
        val plan = rawPlanEntity?.toDomain() ?: MonthlyPlan()
        val prefs = args[3] as UserPreferences
        val screen = args[4] as AppScreen
        val account = args[5] as? Account
        val sheet = args[6] as? ActiveSheet
        val hideBal = args[7] as Boolean
        val reorder = args[8] as Boolean
        val toast = args[9] as? String

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
            toastMessage = toast
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        // Check if database needs initial seeding
        viewModelScope.launch {
            val existing = database.accountDao().getAllAccountsFlow()
            // In onCreate / start, we will let onboarding seed or check if empty
        }
    }

    private fun calculateSafeToSpend(plan: MonthlyPlan, transactions: List<Transaction>): SafeToSpendState {
        val cal = Calendar.getInstance()
        val todayMonthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(cal.time)

        // Starting spent only if applies to current month
        val startingSpent = if (plan.monthKey == todayMonthKey) plan.startingSpent else 0L

        // Expenses for current month
        val currentMonthExpenses = transactions.filter { tx ->
            tx.type == TransactionType.EXPENSE && isCurrentMonth(tx.date, cal)
        }.sumOf { it.amount }

        val totalSpent = startingSpent + currentMonthExpenses
        val budget = plan.budget
        val remaining = if (budget > totalSpent) budget - totalSpent else 0L

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val daysLeft = daysInMonth - dayOfMonth
        val isLastDay = daysLeft <= 0

        val divisor = if (daysLeft > 0) daysLeft else 1
        val perDay = floor(remaining.toDouble() / divisor).toLong()

        val percent = if (budget > 0) (totalSpent.toFloat() / budget.toFloat()) else 0f
        val displayPct = min(100, (percent * 100).roundToInt())

        val status = when {
            budget <= 0L -> SafeToSpendStatus.SET_BUDGET
            remaining <= 0L -> SafeToSpendStatus.RECALIBRATE
            percent >= 0.70f -> SafeToSpendStatus.NEAR_LIMIT
            else -> SafeToSpendStatus.SAFE
        }

        return SafeToSpendState(
            safeToSpendToday = perDay,
            remaining = remaining,
            spentThisMonth = totalSpent,
            budget = budget,
            daysLeft = if (daysLeft > 0) daysLeft else 0,
            isLastDay = isLastDay,
            percentSpent = percent,
            displayPercent = displayPct,
            status = status
        )
    }

    private fun isCurrentMonth(timestamp: Long, cal: Calendar): Boolean {
        val txCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return txCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                txCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
    }

    // ------------------------------------------------------------------------
    // Navigation & UI Actions
    // ------------------------------------------------------------------------
    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen != AppScreen.ACCOUNT_DETAIL) {
            _selectedAccount.value = null
        }
    }

    fun openAccountDetail(account: Account) {
        _selectedAccount.value = account
        _currentScreen.value = AppScreen.ACCOUNT_DETAIL
    }

    fun openSheet(sheet: ActiveSheet) {
        _activeSheet.value = sheet
    }

    fun dismissSheet() {
        _activeSheet.value = null
    }

    fun toggleHideBalances() {
        val next = !_hideBalances.value
        _hideBalances.value = next
        if (!next) {
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

    // ------------------------------------------------------------------------
    // Data Actions
    // ------------------------------------------------------------------------
    fun seedExampleData() {
        viewModelScope.launch {
            ExampleDataSeeder.seed(database)
        }
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

        if (sourceAccountId == null) {
            showToast("Select an account")
            return
        }

        if (type == TransactionType.TRANSFER) {
            if (destAccountId == null || destAccountId == sourceAccountId) {
                showToast("Select a different destination account")
                return
            }
        }

        viewModelScope.launch {
            val detectedCategory = if (type == TransactionType.TRANSFER) {
                Category.OTHER
            } else {
                Category.detectFromNote(note)
            }

            val title = when (type) {
                TransactionType.EXPENSE -> if (note.isNotBlank()) note else detectedCategory.defaultName
                TransactionType.INCOME -> if (note.isNotBlank()) note else "Income"
                TransactionType.TRANSFER -> {
                    val src = database.accountDao().getAccountById(sourceAccountId)?.name ?: "Source"
                    val dst = database.accountDao().getAccountById(destAccountId ?: "")?.name ?: "Destination"
                    "$src → $dst"
                }
            }

            val iconKey = when (type) {
                TransactionType.TRANSFER -> "ArrowsLeftRight"
                TransactionType.INCOME -> "ArrowDownLeft"
                TransactionType.EXPENSE -> detectedCategory.key
            }

            val accentKey = when (type) {
                TransactionType.TRANSFER -> AccentKey.COBALT
                TransactionType.INCOME -> AccentKey.EMERALD
                TransactionType.EXPENSE -> {
                    val srcAcc = database.accountDao().getAccountById(sourceAccountId)
                    AccentKey.fromKey(srcAcc?.accentKey)
                }
            }

            val tx = TransactionEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                category = detectedCategory.key,
                type = type.name,
                amount = amount,
                date = System.currentTimeMillis(),
                iconKey = iconKey,
                accentKey = accentKey.name,
                sourceAccountId = sourceAccountId,
                destinationAccountId = destAccountId,
                updatedAt = System.currentTimeMillis()
            )

            // Atomic balance updates
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
            val currentAccounts = database.accountDao().getAccountById("acc-bca-main") // just checking
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
                app.cashjournal.data.local.entity.MonthlyPlanEntity(
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

    fun clearAllData() {
        viewModelScope.launch {
            ExampleDataSeeder.clearAll(database)
            showToast("All data cleared")
        }
    }
}
