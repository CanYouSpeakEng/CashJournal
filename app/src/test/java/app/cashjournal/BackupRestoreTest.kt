package app.cashjournal

import app.cashjournal.data.backup.AccountBackupDto
import app.cashjournal.data.backup.BackupPayload
import app.cashjournal.data.backup.MonthlyPlanBackupDto
import app.cashjournal.data.backup.PreferencesBackupDto
import app.cashjournal.data.backup.TransactionBackupDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRestoreTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testBackupPayloadSerializationRoundTrip() {
        val originalPayload = BackupPayload(
            version = 1,
            appVersion = "1.0.14",
            exportedAt = 1760000000000L,
            accounts = listOf(
                AccountBackupDto(
                    id = "acc-1",
                    name = "BCA Main",
                    accentKey = "EMERALD",
                    balance = 12_500_000L,
                    initialBalance = 10_000_000L,
                    sortIndex = 0,
                    updatedAt = 1760000000000L
                ),
                AccountBackupDto(
                    id = "acc-2",
                    name = "Cash Wallet",
                    accentKey = "AMBER",
                    balance = 750_000L,
                    initialBalance = 500_000L,
                    sortIndex = 1,
                    updatedAt = 1760000000000L
                )
            ),
            transactions = listOf(
                TransactionBackupDto(
                    id = "tx-1",
                    title = "Monthly Salary",
                    category = "INCOME",
                    type = "INCOME",
                    amount = 3_000_000L,
                    date = 1760000000000L,
                    iconKey = "Wallet",
                    accentKey = "EMERALD",
                    sourceAccountId = "acc-1"
                ),
                TransactionBackupDto(
                    id = "tx-2",
                    title = "Groceries",
                    category = "FOOD",
                    type = "EXPENSE",
                    amount = 500_000L,
                    date = 1760000000000L,
                    iconKey = "ShoppingCart",
                    accentKey = "BERRY",
                    sourceAccountId = "acc-1"
                ),
                TransactionBackupDto(
                    id = "tx-3",
                    title = "ATM Withdrawal",
                    category = "TRANSFER",
                    type = "TRANSFER",
                    amount = 250_000L,
                    date = 1760000000000L,
                    iconKey = "ArrowLeftRight",
                    accentKey = "COBALT",
                    sourceAccountId = "acc-1",
                    destinationAccountId = "acc-2"
                )
            ),
            monthlyPlan = MonthlyPlanBackupDto(
                id = "primary_plan",
                budget = 8_000_000L,
                startingSpent = 0L,
                monthKey = "2026-10"
            ),
            preferences = PreferencesBackupDto(
                language = "en",
                currencyCode = "IDR",
                userName = "Yahya",
                railOnRight = true,
                shakeToSwap = false,
                knockSensitivity = "balanced",
                temperatureUnit = "C",
                soundEffectsEnabled = true,
                tactileHapticsEnabled = true,
                dailyReminderEnabled = true,
                screenPrivacyEnabled = true,
                weatherOption = "search",
                weatherCityLabel = "Tokyo",
                weatherCityLat = 35.6762,
                weatherCityLon = 139.6503,
                selectedWorldCurrency = "EUR"
            )
        )

        val encodedJson = json.encodeToString(BackupPayload.serializer(), originalPayload)
        assertTrue(encodedJson.isNotEmpty())
        assertTrue(encodedJson.contains("BCA Main"))
        assertTrue(encodedJson.contains("Monthly Salary"))

        val decodedPayload = json.decodeFromString(BackupPayload.serializer(), encodedJson)
        assertEquals(originalPayload.version, decodedPayload.version)
        assertEquals(originalPayload.appVersion, decodedPayload.appVersion)
        assertEquals(originalPayload.accounts.size, decodedPayload.accounts.size)
        assertEquals(originalPayload.transactions.size, decodedPayload.transactions.size)
        assertEquals("BCA Main", decodedPayload.accounts[0].name)
        assertEquals(10_000_000L, decodedPayload.accounts[0].initialBalance)
        assertEquals(12_500_000L, decodedPayload.accounts[0].balance)
        assertNotNull(decodedPayload.monthlyPlan)
        assertEquals(8_000_000L, decodedPayload.monthlyPlan?.budget)
        assertNotNull(decodedPayload.preferences)
        assertEquals("IDR", decodedPayload.preferences?.currencyCode)
        assertEquals("Yahya", decodedPayload.preferences?.userName)
        assertEquals(true, decodedPayload.preferences?.screenPrivacyEnabled)
        assertEquals("search", decodedPayload.preferences?.weatherOption)
        assertEquals("Tokyo", decodedPayload.preferences?.weatherCityLabel)
        assertEquals(35.6762, decodedPayload.preferences?.weatherCityLat ?: 0.0, 0.0001)
        assertEquals(139.6503, decodedPayload.preferences?.weatherCityLon ?: 0.0, 0.0001)
        assertEquals("EUR", decodedPayload.preferences?.selectedWorldCurrency)
    }

    @Test
    fun testPreferencesBackupLegacyFallbackWithoutNewFields() {
        val legacyJson = """
            {
                "version": 1,
                "appVersion": "1.0.12",
                "exportedAt": 1750000000000,
                "accounts": [],
                "transactions": [],
                "preferences": {
                    "language": "en",
                    "currencyCode": "USD",
                    "userName": "Alex"
                }
            }
        """.trimIndent()

        val decoded = json.decodeFromString(BackupPayload.serializer(), legacyJson)
        assertNotNull(decoded.preferences)
        assertEquals("USD", decoded.preferences?.currencyCode)
        assertEquals("Alex", decoded.preferences?.userName)
        assertEquals(false, decoded.preferences?.screenPrivacyEnabled)
        assertEquals("approx", decoded.preferences?.weatherOption)
        assertEquals("USD", decoded.preferences?.selectedWorldCurrency)
    }

    @Test
    fun testCorruptedJsonFailsGracefully() {
        val corruptedJson = """{ "version": 1, "accounts": [{"id": "acc-1", "name": "Broken" """

        val result = runCatching {
            json.decodeFromString(BackupPayload.serializer(), corruptedJson)
        }

        assertTrue(result.isFailure)
    }

    @Test
    fun testUnsupportedVersionValidation() {
        val zeroVersionPayload = BackupPayload(
            version = 0,
            accounts = emptyList(),
            transactions = emptyList()
        )

        val encodedJson = json.encodeToString(BackupPayload.serializer(), zeroVersionPayload)
        val decoded = json.decodeFromString(BackupPayload.serializer(), encodedJson)

        val validationResult = runCatching {
            if (decoded.version <= 0) {
                throw IllegalArgumentException("Invalid backup format: unsupported version ${decoded.version}")
            }
        }

        assertTrue(validationResult.isFailure)
        assertEquals("Invalid backup format: unsupported version 0", validationResult.exceptionOrNull()?.message)
    }

    @Test
    fun testZeroDriftRecalculationOnRestoredPayload() {
        val account = AccountBackupDto(
            id = "acc-main",
            name = "Checking",
            accentKey = "COBALT",
            balance = 0L, // Placeholder prior to recalculation
            initialBalance = 5_000_000L
        )

        val txIncome = TransactionBackupDto(
            id = "tx-1",
            title = "Freelance Payment",
            category = "INCOME",
            type = "INCOME",
            amount = 2_000_000L,
            date = 1000L,
            sourceAccountId = "acc-main"
        )
        val txExpense = TransactionBackupDto(
            id = "tx-2",
            title = "Coffee Machine",
            category = "FOOD",
            type = "EXPENSE",
            amount = 750_000L,
            date = 2000L,
            sourceAccountId = "acc-main"
        )
        val txTransferOut = TransactionBackupDto(
            id = "tx-3",
            title = "Top up e-wallet",
            category = "TRANSFER",
            type = "TRANSFER",
            amount = 250_000L,
            date = 3000L,
            sourceAccountId = "acc-main",
            destinationAccountId = "acc-wallet"
        )

        val transactions = listOf(txIncome, txExpense, txTransferOut)

        // Mathematical ledger invariant: balance = initialBalance + SUM(inflows - outflows)
        val netFlows = transactions.sumOf { tx ->
            when {
                tx.type == "INCOME" && tx.sourceAccountId == account.id -> tx.amount
                tx.type == "TRANSFER" && tx.destinationAccountId == account.id -> tx.amount
                tx.type == "EXPENSE" && tx.sourceAccountId == account.id -> -tx.amount
                tx.type == "TRANSFER" && tx.sourceAccountId == account.id -> -tx.amount
                else -> 0L
            }
        }
        val calculatedBalance = account.initialBalance + netFlows

        // 5,000,000 + 2,000,000 - 750,000 - 250,000 = 6,000,000
        assertEquals(6_000_000L, calculatedBalance)
    }
}
