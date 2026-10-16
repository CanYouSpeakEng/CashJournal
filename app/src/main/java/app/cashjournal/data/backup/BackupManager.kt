package app.cashjournal.data.backup

import androidx.room.withTransaction
import app.cashjournal.data.local.AppDatabase
import app.cashjournal.data.local.entity.AccountEntity
import app.cashjournal.data.local.entity.MonthlyPlanEntity
import app.cashjournal.data.local.entity.TransactionEntity
import app.cashjournal.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

import app.cashjournal.BuildConfig

@Serializable
data class BackupPayload(
    val version: Int = 1,
    val appVersion: String = BuildConfig.VERSION_NAME,
    val exportedAt: Long = System.currentTimeMillis(),
    val accounts: List<AccountBackupDto> = emptyList(),
    val transactions: List<TransactionBackupDto> = emptyList(),
    val monthlyPlan: MonthlyPlanBackupDto? = null,
    val preferences: PreferencesBackupDto? = null
)

@Serializable
data class AccountBackupDto(
    val id: String,
    val name: String,
    val accentKey: String,
    val balance: Long,
    val initialBalance: Long = 0L,
    val sortIndex: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): AccountEntity = AccountEntity(
        id = id,
        name = name,
        accentKey = accentKey,
        balance = balance,
        initialBalance = initialBalance,
        sortIndex = sortIndex,
        updatedAt = updatedAt
    )

    companion object {
        fun fromEntity(entity: AccountEntity): AccountBackupDto = AccountBackupDto(
            id = entity.id,
            name = entity.name,
            accentKey = entity.accentKey,
            balance = entity.balance,
            initialBalance = entity.initialBalance,
            sortIndex = entity.sortIndex,
            updatedAt = entity.updatedAt
        )
    }
}

@Serializable
data class TransactionBackupDto(
    val id: String,
    val title: String,
    val category: String,
    val type: String,
    val amount: Long,
    val date: Long,
    val iconKey: String = "",
    val accentKey: String = "SLATE",
    val sourceAccountId: String? = null,
    val destinationAccountId: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): TransactionEntity = TransactionEntity(
        id = id,
        title = title,
        category = category,
        type = type,
        amount = amount,
        date = date,
        iconKey = iconKey,
        accentKey = accentKey,
        sourceAccountId = sourceAccountId,
        destinationAccountId = destinationAccountId,
        updatedAt = updatedAt
    )

    companion object {
        fun fromEntity(entity: TransactionEntity): TransactionBackupDto = TransactionBackupDto(
            id = entity.id,
            title = entity.title,
            category = entity.category,
            type = entity.type,
            amount = entity.amount,
            date = entity.date,
            iconKey = entity.iconKey,
            accentKey = entity.accentKey,
            sourceAccountId = entity.sourceAccountId,
            destinationAccountId = entity.destinationAccountId,
            updatedAt = entity.updatedAt
        )
    }
}

@Serializable
data class MonthlyPlanBackupDto(
    val id: String = "primary_plan",
    val budget: Long,
    val startingSpent: Long = 0L,
    val monthKey: String
) {
    fun toEntity(): MonthlyPlanEntity = MonthlyPlanEntity(
        id = id,
        budget = budget,
        startingSpent = startingSpent,
        monthKey = monthKey
    )

    companion object {
        fun fromEntity(entity: MonthlyPlanEntity): MonthlyPlanBackupDto = MonthlyPlanBackupDto(
            id = entity.id,
            budget = entity.budget,
            startingSpent = entity.startingSpent,
            monthKey = entity.monthKey
        )
    }
}

@Serializable
data class PreferencesBackupDto(
    val language: String? = "en",
    val currencyCode: String? = "IDR",
    val userName: String? = "",
    val cycleStartDay: Int? = 1,
    val railOnRight: Boolean? = true,
    val shakeToSwap: Boolean? = false,
    val knockSensitivity: String? = "balanced",
    val temperatureUnit: String? = "C",
    val soundEffectsEnabled: Boolean? = true,
    val tactileHapticsEnabled: Boolean? = true,
    val dailyReminderEnabled: Boolean? = true,
    val screenPrivacyEnabled: Boolean? = false,
    val weatherOption: String? = "approx",
    val weatherCityLabel: String? = "",
    val weatherCityLat: Double? = 0.0,
    val weatherCityLon: Double? = 0.0,
    val selectedWorldCurrency: String? = "USD",
    val immersiveModeEnabled: Boolean? = true
)

data class RestoreResult(
    val accountsRestored: Int,
    val transactionsRestored: Int
)

class BackupManager(
    private val database: AppDatabase,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun createBackupPayload(): BackupPayload = withContext(Dispatchers.IO) {
        val accounts = database.accountDao().getAllAccounts().map { AccountBackupDto.fromEntity(it) }
        val transactions = database.transactionDao().getAllTransactions().map { TransactionBackupDto.fromEntity(it) }
        val plan = database.monthlyPlanDao().getPlan()?.let { MonthlyPlanBackupDto.fromEntity(it) }
        val prefs = preferencesRepository.userPreferencesFlow.first()
        val prefsDto = PreferencesBackupDto(
            language = prefs.language,
            currencyCode = prefs.currencyCode,
            userName = prefs.userName,
            cycleStartDay = prefs.cycleStartDay,
            railOnRight = prefs.railOnRight,
            shakeToSwap = prefs.shakeToSwap,
            knockSensitivity = prefs.knockSensitivity,
            temperatureUnit = prefs.temperatureUnit,
            soundEffectsEnabled = prefs.soundEffectsEnabled,
            tactileHapticsEnabled = prefs.tactileHapticsEnabled,
            dailyReminderEnabled = prefs.dailyReminderEnabled,
            screenPrivacyEnabled = prefs.screenPrivacyEnabled,
            weatherOption = prefs.weatherOption,
            weatherCityLabel = prefs.weatherCityLabel,
            weatherCityLat = prefs.weatherCityLat,
            weatherCityLon = prefs.weatherCityLon,
            selectedWorldCurrency = prefs.selectedWorldCurrency,
            immersiveModeEnabled = prefs.immersiveModeEnabled
        )

        BackupPayload(
            version = 1,
            appVersion = BuildConfig.VERSION_NAME,
            exportedAt = System.currentTimeMillis(),
            accounts = accounts,
            transactions = transactions,
            monthlyPlan = plan,
            preferences = prefsDto
        )
    }

    suspend fun exportToStream(outputStream: OutputStream): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = createBackupPayload()
            val jsonString = json.encodeToString(BackupPayload.serializer(), payload)
            outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(jsonString)
                writer.flush()
            }
            payload.transactions.size
        }
    }

    suspend fun restoreFromStream(inputStream: InputStream): Result<RestoreResult> = withContext(Dispatchers.IO) {
        runCatching {
            val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val payload = json.decodeFromString(BackupPayload.serializer(), jsonString)

            if (payload.version <= 0) {
                throw IllegalArgumentException("Invalid backup format: unsupported version ${payload.version}")
            }

            database.withTransaction {
                database.accountDao().deleteAll()
                database.transactionDao().deleteAll()
                database.monthlyPlanDao().deleteAll()

                if (payload.accounts.isNotEmpty()) {
                    database.accountDao().insertAll(payload.accounts.map { it.toEntity() })
                }
                if (payload.transactions.isNotEmpty()) {
                    database.transactionDao().insertAll(payload.transactions.map { it.toEntity() })
                }
                payload.monthlyPlan?.let {
                    database.monthlyPlanDao().upsert(it.toEntity())
                }

                // Zero-Drift Recalculation: enforce mathematical truth across all accounts
                database.accountDao().recalculateAllAccountBalances()
            }

            // Restore preferences if present in backup payload
            payload.preferences?.let { p ->
                preferencesRepository.restorePreferences(
                    language = p.language,
                    currencyCode = p.currencyCode,
                    userName = p.userName,
                    cycleStartDay = p.cycleStartDay,
                    railOnRight = p.railOnRight,
                    shakeToSwap = p.shakeToSwap,
                    knockSensitivity = p.knockSensitivity,
                    temperatureUnit = p.temperatureUnit,
                    soundEffectsEnabled = p.soundEffectsEnabled,
                    tactileHapticsEnabled = p.tactileHapticsEnabled,
                    dailyReminderEnabled = p.dailyReminderEnabled,
                    screenPrivacyEnabled = p.screenPrivacyEnabled,
                    weatherOption = p.weatherOption,
                    weatherCityLabel = p.weatherCityLabel,
                    weatherCityLat = p.weatherCityLat,
                    weatherCityLon = p.weatherCityLon,
                    selectedWorldCurrency = p.selectedWorldCurrency,
                    immersiveModeEnabled = p.immersiveModeEnabled
                )
            }

            RestoreResult(
                accountsRestored = payload.accounts.size,
                transactionsRestored = payload.transactions.size
            )
        }
    }
}
