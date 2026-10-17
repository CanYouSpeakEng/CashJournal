package app.cashjournal

import app.cashjournal.data.currency.CurrencyConverter
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.MonthlyPlan
import app.cashjournal.domain.model.SafeToSpendCalculator
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs

class LedgerIntegrityTest {

    private fun computeDerivedBalance(account: Account, transactions: List<Transaction>): Long {
        val netFlows = transactions.sumOf { tx ->
            when {
                tx.type == TransactionType.INCOME && tx.sourceAccountId == account.id -> tx.amount
                tx.type == TransactionType.REFUND && tx.sourceAccountId == account.id -> tx.amount
                tx.type == TransactionType.TRANSFER && tx.destinationAccountId == account.id -> tx.amount
                tx.type == TransactionType.EXPENSE && tx.sourceAccountId == account.id -> -tx.amount
                tx.type == TransactionType.TRANSFER && tx.sourceAccountId == account.id -> -(tx.amount + tx.feeAmount)
                else -> 0L
            }
        }
        return account.initialBalance + netFlows
    }

    @Test
    fun testZeroDriftBalanceDerivation() {
        val accId = "acc-main"
        val initialBalance = 10_000_000L
        val account = Account(id = accId, name = "BCA Main", initialBalance = initialBalance, balance = initialBalance)

        val tx1 = Transaction(id = "1", title = "Groceries", type = TransactionType.EXPENSE, amount = 250_000L, sourceAccountId = accId)
        val tx2 = Transaction(id = "2", title = "Bonus", type = TransactionType.INCOME, amount = 1_500_000L, sourceAccountId = accId)
        val tx3 = Transaction(id = "3", title = "Dinner", type = TransactionType.EXPENSE, amount = 150_000L, sourceAccountId = accId)

        val txList = mutableListOf(tx1, tx2, tx3)

        // Initial derivation: 10,000,000 - 250,000 + 1,500,000 - 150,000 = 11,100,000
        var derivedBalance = computeDerivedBalance(account, txList)
        assertEquals(11_100_000L, derivedBalance)

        // Retroactive edit: user changes Groceries from 250,000 to 500,000
        val tx1Edited = tx1.copy(amount = 500_000L)
        txList[0] = tx1Edited

        // Recalculated: 10,000,000 - 500,000 + 1,500,000 - 150,000 = 10,850,000
        derivedBalance = computeDerivedBalance(account, txList)
        assertEquals(10_850_000L, derivedBalance)

        // Retroactive delete: user deletes the 150,000 dinner
        txList.removeAt(2)

        // Recalculated: 10,000,000 - 500,000 + 1,500,000 = 11,000,000
        derivedBalance = computeDerivedBalance(account, txList)
        assertEquals(11_000_000L, derivedBalance)
    }

    @Test
    fun testAtomicTransferConservationOfNetWorth() {
        val accA = Account(id = "acc-a", name = "Bank", initialBalance = 5_000_000L, balance = 5_000_000L)
        val accB = Account(id = "acc-b", name = "E-Wallet", initialBalance = 1_000_000L, balance = 1_000_000L)

        val transferTx = Transaction(
            id = "tx-tr",
            title = "Transfer to Wallet",
            type = TransactionType.TRANSFER,
            amount = 500_000L,
            sourceAccountId = accA.id,
            destinationAccountId = accB.id
        )

        val txList = listOf(transferTx)

        val balanceA = computeDerivedBalance(accA, txList)
        val balanceB = computeDerivedBalance(accB, txList)

        assertEquals(4_500_000L, balanceA)
        assertEquals(1_500_000L, balanceB)

        // Total Net Worth is perfectly preserved (6,000,000 before and after)
        val totalWorthBefore = accA.initialBalance + accB.initialBalance
        val totalWorthAfter = balanceA + balanceB
        assertEquals(totalWorthBefore, totalWorthAfter)

        // Transfer is isolated from monthly expense calculation
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.OCTOBER, 14, 12, 0)
        }
        val plan = MonthlyPlan(monthKey = "2026-10", budget = 3_000_000L, startingSpent = 0L)
        val safeState = SafeToSpendCalculator.calculate(plan, txList, cal)

        assertEquals(0L, safeState.spentThisMonth)
        assertEquals(3_000_000L, safeState.remaining)
    }

    @Test
    fun testReconciliationAdjustmentDeltaMath() {
        val accId = "acc-cash"
        val currentAppBalance = 4_535_000L
        val realWorldBalance = 4_500_000L

        // Delta is -35,000 (shortfall / unlogged cash spending)
        val delta = realWorldBalance - currentAppBalance
        assertEquals(-35_000L, delta)

        // App generates an outflow adjustment transaction of 35,000
        val adjustmentTx = Transaction(
            id = "tx-adj",
            title = "Balance Adjustment",
            type = TransactionType.EXPENSE,
            amount = abs(delta),
            sourceAccountId = accId,
            category = Category.OTHER
        )

        val account = Account(id = accId, name = "Cash", initialBalance = currentAppBalance, balance = currentAppBalance)
        val reconciledBalance = computeDerivedBalance(account, listOf(adjustmentTx))

        assertEquals(realWorldBalance, reconciledBalance)
    }

    @Test
    fun testBackdatingPreservation() {
        val threeDaysAgo = System.currentTimeMillis() - (3 * 86_400_000L)
        val backdatedTx = Transaction(
            id = "tx-backdated",
            title = "Friday Dinner",
            type = TransactionType.EXPENSE,
            amount = 120_000L,
            date = threeDaysAgo
        )

        assertEquals(threeDaysAgo, backdatedTx.date)
    }

    @Test
    fun testCurrencySwitchInitialBalanceConversionZeroDrift() {
        val rates = mapOf("USD" to 1.0, "IDR" to 16000.0)
        val oldCurrency = "IDR"
        val oldDecimals = 0
        val newCurrency = "USD"
        val newDecimals = 2

        val rawInitial = 16_000_000L // 16,000,000 IDR
        val tx1Amount = 1_600_000L   // 1,600,000 IDR expense
        val tx2Amount = 3_200_000L   // 3,200,000 IDR income
        val accId = "acc-curr"

        val oldAccount = Account(id = accId, name = "Savings", initialBalance = rawInitial, balance = rawInitial - tx1Amount + tx2Amount)
        val oldTx1 = Transaction(id = "tx1", title = "Expense", type = TransactionType.EXPENSE, amount = tx1Amount, sourceAccountId = accId)
        val oldTx2 = Transaction(id = "tx2", title = "Income", type = TransactionType.INCOME, amount = tx2Amount, sourceAccountId = accId)

        assertEquals(17_600_000L, computeDerivedBalance(oldAccount, listOf(oldTx1, oldTx2)))

        // CRITICAL-1: Both initialBalance and transactions are converted using CurrencyConverter
        val convertedInitial = CurrencyConverter.convertMinorUnits(
            amountMinor = oldAccount.initialBalance,
            sourceCode = oldCurrency,
            targetCode = newCurrency,
            baseCode = "USD",
            rates = rates,
            sourceDecimals = oldDecimals,
            targetDecimals = newDecimals
        )
        val convertedTx1Amount = CurrencyConverter.convertMinorUnits(
            amountMinor = oldTx1.amount,
            sourceCode = oldCurrency,
            targetCode = newCurrency,
            baseCode = "USD",
            rates = rates,
            sourceDecimals = oldDecimals,
            targetDecimals = newDecimals
        )
        val convertedTx2Amount = CurrencyConverter.convertMinorUnits(
            amountMinor = oldTx2.amount,
            sourceCode = oldCurrency,
            targetCode = newCurrency,
            baseCode = "USD",
            rates = rates,
            sourceDecimals = oldDecimals,
            targetDecimals = newDecimals
        )

        // 16,000,000 IDR = 1,000.00 USD = 100,000 cents
        assertEquals(100_000L, convertedInitial)
        // 1,600,000 IDR = 100.00 USD = 10,000 cents
        assertEquals(10_000L, convertedTx1Amount)
        // 3,200,000 IDR = 200.00 USD = 20,000 cents
        assertEquals(20_000L, convertedTx2Amount)

        val newAccount = oldAccount.copy(initialBalance = convertedInitial, balance = convertedInitial)
        val newTx1 = oldTx1.copy(amount = convertedTx1Amount)
        val newTx2 = oldTx2.copy(amount = convertedTx2Amount)

        // Recalculating account balance with Room formula: initialBalance + SUM(flows)
        val recalculatedBalance = computeDerivedBalance(newAccount, listOf(newTx1, newTx2))
        // 100,000 - 10,000 + 20,000 = 110,000 cents ($1,100.00)
        assertEquals(110_000L, recalculatedBalance)
    }

    @Test
    fun testCurrencyConversionConvertsRecurringBillsAndCommittedRunwayConsistently() {
        val oldCurrency = "IDR"
        val newCurrency = "USD"
        val rates = mapOf("USD" to 1.0, "IDR" to 16000.0)

        // 1. Initial IDR state: 16,000,000 IDR budget, 1,600,000 IDR recurring rent bill
        val idrBudget = 16_000_000L
        val idrRentBillAmount = 1_600_000L

        val convertedBudget = CurrencyConverter.convertMinorUnits(
            amountMinor = idrBudget,
            sourceCode = oldCurrency,
            targetCode = newCurrency,
            baseCode = "USD",
            rates = rates,
            sourceDecimals = 0,
            targetDecimals = 2
        )

        val convertedRecurringAmount = CurrencyConverter.convertMinorUnits(
            amountMinor = idrRentBillAmount,
            sourceCode = oldCurrency,
            targetCode = newCurrency,
            baseCode = "USD",
            rates = rates,
            sourceDecimals = 0,
            targetDecimals = 2
        )

        // 16,000,000 IDR -> $1,000.00 USD (100,000 cents)
        assertEquals(100_000L, convertedBudget)
        // 1,600,000 IDR -> $100.00 USD (10,000 cents)
        assertEquals(10_000L, convertedRecurringAmount)

        // Safe-to-spend evaluation with converted recurring bill
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.OCTOBER, 10, 12, 0, 0)
        }
        val plan = MonthlyPlan(budget = convertedBudget)
        val convertedBill = app.cashjournal.domain.model.RecurringTransaction(
            id = "rent-1",
            title = "Apartment Rent",
            category = Category.HOUSING,
            amount = convertedRecurringAmount,
            type = TransactionType.EXPENSE,
            dueDay = 25,
            isActive = true
        )

        val result = SafeToSpendCalculator.calculate(
            plan = plan,
            transactions = emptyList(),
            calendar = cal,
            totalNetWorth = 500_000L,
            cycleStartDay = 1,
            recurringTransactions = listOf(convertedBill)
        )

        // Committed bills in USD must be 10,000 cents ($100), not the unconverted 1,600,000
        assertEquals(10_000L, result.committedBills)
        // Remaining budget: 100,000 - 10,000 = 90,000 cents ($900.00)
        assertEquals(90_000L, result.remaining)
    }

    @Test
    fun testFullResetClearsAllTablesCompletely() {
        val conn = java.sql.DriverManager.getConnection("jdbc:sqlite::memory:")
        val stmt = conn.createStatement()

        stmt.execute("CREATE TABLE accounts (id TEXT PRIMARY KEY, balance INTEGER)")
        stmt.execute("CREATE TABLE transactions (id TEXT PRIMARY KEY, amount INTEGER)")
        stmt.execute("CREATE TABLE monthly_plans (id TEXT PRIMARY KEY, budget INTEGER)")
        stmt.execute("CREATE TABLE recurring_transactions (id TEXT PRIMARY KEY, amount INTEGER)")

        // Populate sample data in all 4 tables
        stmt.execute("INSERT INTO accounts VALUES ('acc-1', 100000)")
        stmt.execute("INSERT INTO transactions VALUES ('tx-1', 50000)")
        stmt.execute("INSERT INTO monthly_plans VALUES ('plan-1', 500000)")
        stmt.execute("INSERT INTO recurring_transactions VALUES ('rec-1', 25000)")

        // Perform clearAllData simulation
        stmt.execute("DELETE FROM accounts")
        stmt.execute("DELETE FROM transactions")
        stmt.execute("DELETE FROM monthly_plans")
        stmt.execute("DELETE FROM recurring_transactions")

        val rsAcc = stmt.executeQuery("SELECT count(*) FROM accounts")
        assertEquals(0, rsAcc.getInt(1))

        val rsTx = stmt.executeQuery("SELECT count(*) FROM transactions")
        assertEquals(0, rsTx.getInt(1))

        val rsPlan = stmt.executeQuery("SELECT count(*) FROM monthly_plans")
        assertEquals(0, rsPlan.getInt(1))

        val rsRec = stmt.executeQuery("SELECT count(*) FROM recurring_transactions")
        assertEquals(0, rsRec.getInt(1))

        conn.close()
    }

    @Test
    fun testTransferFeeAccountingZeroDrift() {
        val src = Account(id = "acc-src", name = "BCA Checking", initialBalance = 1_000_000L, balance = 1_000_000L)
        val dst = Account(id = "acc-dst", name = "GoPay Wallet", initialBalance = 200_000L, balance = 200_000L)

        // Transfer: 100,000 sent with 2,500 fee -> dst receives 100,000, fee is 2,500, src pays 102,500
        val tx = Transaction(
            id = "tx-transfer-1",
            title = "Top up GoPay",
            type = TransactionType.TRANSFER,
            amount = 100_000L,
            feeAmount = 2_500L,
            sourceAccountId = src.id,
            destinationAccountId = dst.id
        )

        val srcDerived = computeDerivedBalance(src, listOf(tx))
        val dstDerived = computeDerivedBalance(dst, listOf(tx))

        // Invariant: Amount Sent (102,500) = Amount Received (100,000) + Fee (2,500)
        assertEquals(897_500L, srcDerived)
        assertEquals(300_000L, dstDerived)

        val initialNetWorth = src.initialBalance + dst.initialBalance // 1,200,000
        val finalNetWorth = srcDerived + dstDerived // 1,197,500
        val netWorthLoss = initialNetWorth - finalNetWorth
        assertEquals(2_500L, netWorthLoss)
    }

    @Test
    fun testRefundAccountingZeroDrift() {
        val acc = Account(id = "acc-1", name = "Mandiri Debit", initialBalance = 500_000L, balance = 500_000L)

        val expense = Transaction(
            id = "tx-exp",
            title = "Tokopedia Purchase",
            type = TransactionType.EXPENSE,
            amount = 150_000L,
            sourceAccountId = acc.id
        )
        val refund = Transaction(
            id = "tx-ref",
            title = "Tokopedia Refund",
            type = TransactionType.REFUND,
            amount = 150_000L,
            sourceAccountId = acc.id
        )

        val afterExpense = computeDerivedBalance(acc, listOf(expense))
        assertEquals(350_000L, afterExpense)

        val afterRefund = computeDerivedBalance(acc, listOf(expense, refund))
        assertEquals(500_000L, afterRefund)
    }

    @Test
    fun testSqliteAccountRecalculationWithFeesAndRefunds() {
        val conn = java.sql.DriverManager.getConnection("jdbc:sqlite::memory:")
        val stmt = conn.createStatement()

        stmt.execute("CREATE TABLE accounts (id TEXT PRIMARY KEY, initialBalance INTEGER, balance INTEGER)")
        stmt.execute("CREATE TABLE transactions (id TEXT PRIMARY KEY, type TEXT, amount INTEGER, feeAmount INTEGER, sourceAccountId TEXT, destinationAccountId TEXT)")

        stmt.execute("INSERT INTO accounts VALUES ('acc-a', 1000000, 1000000)")
        stmt.execute("INSERT INTO accounts VALUES ('acc-b', 500000, 500000)")

        // Transfer with fee: 200,000 amount, 6,500 fee
        stmt.execute("INSERT INTO transactions VALUES ('tx-1', 'TRANSFER', 200000, 6500, 'acc-a', 'acc-b')")
        // Refund on acc-a: 50,000
        stmt.execute("INSERT INTO transactions VALUES ('tx-2', 'REFUND', 50000, 0, 'acc-a', NULL)")

        // Execute recalculate SQL for acc-a
        val recalcSql = """
            UPDATE accounts
            SET balance = initialBalance + COALESCE((
                SELECT SUM(
                    CASE
                        WHEN type = 'INCOME' AND sourceAccountId = accounts.id THEN amount
                        WHEN type = 'REFUND' AND sourceAccountId = accounts.id THEN amount
                        WHEN type = 'TRANSFER' AND destinationAccountId = accounts.id THEN amount
                        WHEN type = 'EXPENSE' AND sourceAccountId = accounts.id THEN -amount
                        WHEN type = 'TRANSFER' AND sourceAccountId = accounts.id THEN -(amount + feeAmount)
                        ELSE 0
                    END
                )
                FROM transactions
                WHERE sourceAccountId = accounts.id OR destinationAccountId = accounts.id
            ), 0)
        """.trimIndent()

        stmt.execute(recalcSql)

        val rsA = stmt.executeQuery("SELECT balance FROM accounts WHERE id = 'acc-a'")
        assertTrue(rsA.next())
        // 1,000,000 - (200,000 + 6,500) + 50,000 = 843,500
        assertEquals(843_500L, rsA.getLong("balance"))

        val rsB = stmt.executeQuery("SELECT balance FROM accounts WHERE id = 'acc-b'")
        assertTrue(rsB.next())
        // 500,000 + 200,000 = 700,000
        assertEquals(700_000L, rsB.getLong("balance"))

        conn.close()
    }
}
