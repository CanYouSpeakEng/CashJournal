package app.cashjournal

import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.MonthlyPlan
import app.cashjournal.domain.model.SafeToSpendCalculator
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.abs

class LedgerIntegrityTest {

    private fun computeDerivedBalance(account: Account, transactions: List<Transaction>): Long {
        val netFlows = transactions.sumOf { tx ->
            when {
                tx.type == TransactionType.INCOME && tx.sourceAccountId == account.id -> tx.amount
                tx.type == TransactionType.TRANSFER && tx.destinationAccountId == account.id -> tx.amount
                tx.type == TransactionType.EXPENSE && tx.sourceAccountId == account.id -> -tx.amount
                tx.type == TransactionType.TRANSFER && tx.sourceAccountId == account.id -> -tx.amount
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
}
