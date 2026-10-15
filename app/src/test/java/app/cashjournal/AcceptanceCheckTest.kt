package app.cashjournal

import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.MonthlyPlan
import app.cashjournal.domain.model.SafeToSpendCalculator
import app.cashjournal.domain.model.SafeToSpendStatus
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.roundToInt

/**
 * Direct unit test verification of Section 16 Acceptance Checks
 * from docs/antigravity_native_build_brief_v2.md
 */
class AcceptanceCheckTest {

    private fun createUtcCalendar(year: Int, monthZeroIndexed: Int, day: Int): Calendar {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthZeroIndexed)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
        }
    }

    @Test
    fun testExampleDataAccountsNetWorthAndPercentages() {
        val bca = Account(id = "1", name = "BCA Main", balance = 12_500_000L)
        val mandiri = Account(id = "2", name = "Mandiri", balance = 4_200_000L)
        val gopay = Account(id = "3", name = "GoPay", balance = 950_000L)
        val bibit = Account(id = "4", name = "Bibit Vault", balance = 800_000L)

        val accounts = listOf(bca, mandiri, gopay, bibit)
        val totalNetWorth = accounts.sumOf { it.balance }

        // Section 16: Total net worth: "Rp 18.450.000". Metadata: "4 accounts"
        assertEquals(18_450_000L, totalNetWorth)
        assertEquals(4, accounts.size)

        // Section 16: Account percentages: BCA Main 68%, Mandiri 23%, GoPay 5%, Bibit Vault 4%
        val bcaPct = (bca.balance.toDouble() / totalNetWorth * 100).roundToInt()
        val mandiriPct = (mandiri.balance.toDouble() / totalNetWorth * 100).roundToInt()
        val gopayPct = (gopay.balance.toDouble() / totalNetWorth * 100).roundToInt()
        val bibitPct = (bibit.balance.toDouble() / totalNetWorth * 100).roundToInt()

        assertEquals(68, bcaPct)
        assertEquals(23, mandiriPct)
        assertEquals(5, gopayPct)
        assertEquals(4, bibitPct)
    }

    @Test
    fun testSection16AcceptanceCheckOnOctober14() {
        // Section 16: Example data, main currency IDR, on October 14, 2026:
        // - Spent: "Rp 4.945.000" (3.250.000 starting plus 1.695.000 from this month's expenses)
        // - Safe-to-spend: "Rp 150.294 / day". Ring 66%. Status "Safe". Subline "17 days left · Rp 2.555.000 remaining"
        // - Budget card: "Budget Rp 7.500.000", "Spent Rp 4.945.000", "Days left: 17 days"
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 14)
        val plan = MonthlyPlan(
            monthKey = "2026-10",
            budget = 7_500_000L,
            startingSpent = 3_250_000L
        )

        // Expenses in example data:
        // 1. Artisan Coffee: 28,000
        // 3. City Subway Pass: 14,000
        // 4. Lunch & Dining: 35,000
        // 6. Electricity Bill: 250,000
        // 7. Ride to Office: 22,000
        // 9. Apartment Rent: 1,200,000
        // 10. Online Shopping: 146,000
        // Total = 1,695,000
        val expenses = listOf(
            Transaction(id = "1", title = "Artisan Coffee", amount = 28_000L, type = TransactionType.EXPENSE, date = cal.timeInMillis),
            Transaction(id = "2", title = "Tech Corp Salary", amount = 4_500_000L, type = TransactionType.INCOME, date = cal.timeInMillis),
            Transaction(id = "3", title = "City Subway Pass", amount = 14_000L, type = TransactionType.EXPENSE, date = cal.timeInMillis),
            Transaction(id = "4", title = "Lunch & Dining", amount = 35_000L, type = TransactionType.EXPENSE, date = cal.timeInMillis),
            Transaction(id = "5", title = "Portfolio Dividend", amount = 185_000L, type = TransactionType.INCOME, date = cal.timeInMillis),
            Transaction(id = "6", title = "Electricity Bill", amount = 250_000L, type = TransactionType.EXPENSE, date = cal.timeInMillis),
            Transaction(id = "7", title = "Ride to Office", amount = 22_000L, type = TransactionType.EXPENSE, date = cal.timeInMillis),
            Transaction(id = "8", title = "BCA Main to GoPay", amount = 500_000L, type = TransactionType.TRANSFER, date = cal.timeInMillis),
            Transaction(id = "9", title = "Apartment Rent", amount = 1_200_000L, type = TransactionType.EXPENSE, date = cal.timeInMillis),
            Transaction(id = "10", title = "Online Shopping", amount = 146_000L, type = TransactionType.EXPENSE, date = cal.timeInMillis)
        )

        val result = SafeToSpendCalculator.calculate(plan, expenses, cal)

        assertEquals(4_945_000L, result.spentThisMonth)
        assertEquals(2_555_000L, result.remaining)
        assertEquals(17, result.daysLeft)
        assertEquals(150_294L, result.safeToSpendToday)
        assertEquals(66, result.displayPercent)
        assertEquals(SafeToSpendStatus.SAFE, result.status)
    }

    @Test
    fun testSection16LogExpenseNearLimit() {
        // Section 16: Log an expense of Rp 1.000.000 from BCA Main:
        // Spent "Rp 5.945.000", per day "Rp 91.470", ring 79%, status "Near Limit"
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 14)
        val plan = MonthlyPlan(
            monthKey = "2026-10",
            budget = 7_500_000L,
            startingSpent = 3_250_000L
        )

        val previousExpenses = 1_695_000L
        val newExpense = 1_000_000L
        val txList = listOf(
            Transaction(id = "1", title = "Previous Expenses", amount = previousExpenses, type = TransactionType.EXPENSE, date = cal.timeInMillis),
            Transaction(id = "2", title = "New Expense", amount = newExpense, type = TransactionType.EXPENSE, date = cal.timeInMillis)
        )

        val result = SafeToSpendCalculator.calculate(plan, txList, cal)

        assertEquals(5_945_000L, result.spentThisMonth)
        assertEquals(1_555_000L, result.remaining)
        assertEquals(17, result.daysLeft)
        assertEquals(91_470L, result.safeToSpendToday)
        assertEquals(79, result.displayPercent)
        assertEquals(SafeToSpendStatus.NEAR_LIMIT, result.status)
    }

    @Test
    fun testSection16TransferPreservesSpent() {
        // Section 16: Transfer Rp 500.000 from BCA Main to GoPay:
        // balances move, Spent does not change.
        var bcaBalance = 12_500_000L
        var gopayBalance = 950_000L
        val initialNetWorth = bcaBalance + gopayBalance

        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 14)
        val plan = MonthlyPlan(
            monthKey = "2026-10",
            budget = 7_500_000L,
            startingSpent = 3_250_000L
        )

        val baseTransactions = listOf(
            Transaction(id = "1", title = "Expenses", amount = 1_695_000L, type = TransactionType.EXPENSE, date = cal.timeInMillis)
        )

        val resultBefore = SafeToSpendCalculator.calculate(plan, baseTransactions, cal)
        assertEquals(4_945_000L, resultBefore.spentThisMonth)

        // Execute transfer of 500.000
        val transferAmount = 500_000L
        bcaBalance -= transferAmount
        gopayBalance += transferAmount

        val updatedNetWorth = bcaBalance + gopayBalance
        assertEquals(initialNetWorth, updatedNetWorth)
        assertEquals(12_000_000L, bcaBalance)
        assertEquals(1_450_000L, gopayBalance)

        val transferTx = Transaction(
            id = "tx-transfer",
            title = "BCA Main to GoPay",
            amount = transferAmount,
            type = TransactionType.TRANSFER,
            date = cal.timeInMillis
        )
        val resultAfter = SafeToSpendCalculator.calculate(plan, baseTransactions + transferTx, cal)

        // Spent MUST NOT change
        assertEquals(4_945_000L, resultAfter.spentThisMonth)
        assertEquals(resultBefore.remaining, resultAfter.remaining)
        assertEquals(resultBefore.safeToSpendToday, resultAfter.safeToSpendToday)
        assertEquals(resultBefore.status, resultAfter.status)
    }
}
