package app.cashjournal.domain.model

import java.util.Calendar
import java.util.Locale
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

enum class SafeToSpendStatus {
    SAFE,
    NEAR_LIMIT,
    RECALIBRATE,
    SET_BUDGET,
    NO_FUNDS
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

object SafeToSpendCalculator {

    fun isCurrentMonth(date: Long, calendar: Calendar): Boolean {
        val txCal = Calendar.getInstance(calendar.timeZone).apply { timeInMillis = date }
        return txCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                txCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
    }

    fun calculate(
        plan: MonthlyPlan,
        transactions: List<Transaction>,
        calendar: Calendar = Calendar.getInstance(),
        totalNetWorth: Long = 100_000_000L,
        hasAccounts: Boolean = true
    ): SafeToSpendState {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val todayMonthKey = "%04d-%02d".format(Locale.US, year, month)

        // Starting spent only if applies to current month
        val startingSpent = if (plan.monthKey == todayMonthKey) plan.startingSpent else 0L

        // Expenses for current month
        val currentMonthExpenses = transactions.filter { tx ->
            tx.type == TransactionType.EXPENSE && isCurrentMonth(tx.date, calendar)
        }.sumOf { it.amount }

        val totalSpent = startingSpent + currentMonthExpenses
        val budget = plan.budget
        val remaining = if (budget > totalSpent) budget - totalSpent else 0L

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val daysLeft = daysInMonth - dayOfMonth
        val isLastDay = daysLeft <= 0

        val divisor = if (daysLeft > 0) daysLeft else 1
        val hasNoFunds = !hasAccounts || totalNetWorth <= 0L
        val perDay = if (hasNoFunds) 0L else floor(remaining.toDouble() / divisor).toLong()

        val percent = if (budget > 0) (totalSpent.toFloat() / budget.toFloat()) else 0f
        val displayPct = min(100, (percent * 100).roundToInt())

        val status = when {
            hasNoFunds -> SafeToSpendStatus.NO_FUNDS
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
            daysLeft = if (daysLeft <= 0) 1 else daysLeft,
            isLastDay = isLastDay,
            percentSpent = percent,
            displayPercent = displayPct,
            status = status
        )
    }
}
