package app.cashjournal.data

import app.cashjournal.data.local.AppDatabase
import app.cashjournal.data.local.entity.AccountEntity
import app.cashjournal.data.local.entity.MonthlyPlanEntity
import app.cashjournal.data.local.entity.TransactionEntity
import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExampleDataSeeder {

    suspend fun seed(database: AppDatabase) {
        val now = System.currentTimeMillis()
        val oneDayMs = 86_400_000L

        val bcaId = "acc-bca-main"
        val mandiriId = "acc-mandiri"
        val gopayId = "acc-gopay"
        val bibitId = "acc-bibit-vault"

        // 1. Seed Accounts
        val accounts = listOf(
            AccountEntity(
                id = bcaId,
                name = "BCA Main",
                accentKey = AccentKey.COBALT.name,
                accountType = "BANK_DEBIT",
                balance = 12_500_000L,
                initialBalance = 8_646_000L,
                sortIndex = 0,
                updatedAt = now
            ),
            AccountEntity(
                id = mandiriId,
                name = "Mandiri",
                accentKey = AccentKey.EMERALD.name,
                accountType = "BANK_DEBIT",
                balance = 4_200_000L,
                initialBalance = 5_650_000L,
                sortIndex = 1,
                updatedAt = now
            ),
            AccountEntity(
                id = gopayId,
                name = "GoPay",
                accentKey = AccentKey.PERSIMMON.name,
                accountType = "E_WALLET",
                balance = 950_000L,
                initialBalance = 549_000L,
                sortIndex = 2,
                updatedAt = now
            ),
            AccountEntity(
                id = bibitId,
                name = "Bibit Vault",
                accentKey = AccentKey.IRIS.name,
                accountType = "SAVINGS",
                balance = 800_000L,
                initialBalance = 615_000L,
                sortIndex = 3,
                updatedAt = now
            )
        )
        database.accountDao().insertAll(accounts)

        // 2. Seed Transactions
        val transactions = listOf(
            TransactionEntity(
                id = "tx-1",
                title = "Artisan Coffee",
                category = Category.FOOD.key,
                type = TransactionType.EXPENSE.name,
                amount = 28_000L,
                date = now - 3_600_000L, // Today
                iconKey = "Coffee",
                accentKey = AccentKey.PERSIMMON.name,
                sourceAccountId = gopayId,
                destinationAccountId = null,
                updatedAt = now
            ),
            TransactionEntity(
                id = "tx-2",
                title = "Tech Corp Salary",
                category = Category.SALARY.key,
                type = TransactionType.INCOME.name,
                amount = 4_500_000L,
                date = now - 7_200_000L, // Today
                iconKey = "ArrowDownLeft",
                accentKey = AccentKey.EMERALD.name,
                sourceAccountId = bcaId,
                destinationAccountId = null,
                updatedAt = now
            ),
            TransactionEntity(
                id = "tx-3",
                title = "City Subway Pass",
                category = Category.TRANSPORT.key,
                type = TransactionType.EXPENSE.name,
                amount = 14_000L,
                date = now - 10_800_000L, // Today
                iconKey = "Train",
                accentKey = AccentKey.IRIS.name,
                sourceAccountId = gopayId,
                destinationAccountId = null,
                updatedAt = now
            ),
            TransactionEntity(
                id = "tx-4",
                title = "Lunch & Dining",
                category = Category.FOOD.key,
                type = TransactionType.EXPENSE.name,
                amount = 35_000L,
                date = now - 14_400_000L, // Today
                iconKey = "Coffee",
                accentKey = AccentKey.PERSIMMON.name,
                sourceAccountId = gopayId,
                destinationAccountId = null,
                updatedAt = now
            ),
            TransactionEntity(
                id = "tx-5",
                title = "Portfolio Dividend",
                category = Category.INVESTMENT.key,
                type = TransactionType.INCOME.name,
                amount = 185_000L,
                date = now - oneDayMs, // Yesterday
                iconKey = "Sparkle",
                accentKey = AccentKey.EMERALD.name,
                sourceAccountId = bibitId,
                destinationAccountId = null,
                updatedAt = now
            ),
            TransactionEntity(
                id = "tx-6",
                title = "Electricity Bill",
                category = Category.UTILITIES.key,
                type = TransactionType.EXPENSE.name,
                amount = 250_000L,
                date = now - oneDayMs - 3_600_000L, // Yesterday
                iconKey = "Lightning",
                accentKey = AccentKey.SAFFRON.name,
                sourceAccountId = mandiriId,
                destinationAccountId = null,
                updatedAt = now
            ),
            TransactionEntity(
                id = "tx-7",
                title = "Ride to Office",
                category = Category.TRANSPORT.key,
                type = TransactionType.EXPENSE.name,
                amount = 22_000L,
                date = now - oneDayMs - 7_200_000L, // Yesterday
                iconKey = "Bicycle",
                accentKey = AccentKey.IRIS.name,
                sourceAccountId = gopayId,
                destinationAccountId = null,
                updatedAt = now
            ),
            TransactionEntity(
                id = "tx-8",
                title = "BCA Main → GoPay",
                category = Category.OTHER.key,
                type = TransactionType.TRANSFER.name,
                amount = 500_000L,
                date = now - 2 * oneDayMs, // 2 days ago
                iconKey = "ArrowsLeftRight",
                accentKey = AccentKey.COBALT.name,
                sourceAccountId = bcaId,
                destinationAccountId = gopayId,
                updatedAt = now
            ),
            TransactionEntity(
                id = "tx-9",
                title = "Apartment Rent",
                category = Category.HOUSING.key,
                type = TransactionType.EXPENSE.name,
                amount = 1_200_000L,
                date = now - 2 * oneDayMs - 3_600_000L, // 2 days ago
                iconKey = "House",
                accentKey = AccentKey.COBALT.name,
                sourceAccountId = mandiriId,
                destinationAccountId = null,
                updatedAt = now
            ),
            TransactionEntity(
                id = "tx-10",
                title = "Online Shopping",
                category = Category.SHOPPING.key,
                type = TransactionType.EXPENSE.name,
                amount = 146_000L,
                date = now - 2 * oneDayMs - 7_200_000L, // 2 days ago
                iconKey = "ShoppingBag",
                accentKey = AccentKey.BERRY.name,
                sourceAccountId = bcaId,
                destinationAccountId = null,
                updatedAt = now
            )
        )
        database.transactionDao().insertAll(transactions)
        database.accountDao().recalculateAllAccountBalances(now)

        // 3. Seed Monthly Plan
        val monthKey = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(now))
        database.monthlyPlanDao().upsert(
            MonthlyPlanEntity(
                id = "primary_plan",
                budget = 7_500_000L,
                startingSpent = 3_250_000L,
                monthKey = monthKey
            )
        )
    }

    suspend fun clearAll(database: AppDatabase) {
        database.transactionDao().deleteAll()
        database.accountDao().deleteAll()
        database.monthlyPlanDao().deleteAll()
    }
}
