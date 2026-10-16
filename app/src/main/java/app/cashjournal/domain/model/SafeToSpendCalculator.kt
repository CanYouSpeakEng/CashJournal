package app.cashjournal.domain.model

import java.text.SimpleDateFormat
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
    val status: SafeToSpendStatus = SafeToSpendStatus.SAFE,
    val cycleLabel: String = ""
)

data class CycleRange(
    val startMillis: Long,
    val endMillis: Long,
    val label: String,
    val daysLeft: Int,
    val isLastDay: Boolean
)

object SafeToSpendCalculator {

    fun getCycleRange(
        cycleStartDay: Int = 1,
        calendar: Calendar = Calendar.getInstance()
    ): CycleRange {
        val clampedDay = cycleStartDay.coerceIn(1, 28)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val startCal = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (clampedDay == 1) {
            startCal.set(Calendar.DAY_OF_MONTH, 1)
        } else if (dayOfMonth >= clampedDay) {
            startCal.set(Calendar.DAY_OF_MONTH, clampedDay)
        } else {
            startCal.add(Calendar.MONTH, -1)
            startCal.set(Calendar.DAY_OF_MONTH, clampedDay)
        }

        val endCal = (startCal.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            if (clampedDay == 1) {
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            } else {
                set(Calendar.DAY_OF_MONTH, clampedDay)
                add(Calendar.MILLISECOND, -1)
            }
        }

        val startMillis = startCal.timeInMillis
        val endMillis = endCal.timeInMillis

        val nowCal = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endDayCal = (endCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = endDayCal.timeInMillis - nowCal.timeInMillis
        val daysLeft = (diffMillis / (24 * 60 * 60 * 1000L)).toInt().coerceAtLeast(0)
        val isLastDay = daysLeft <= 0

        val label = if (clampedDay == 1) {
            SimpleDateFormat("MMMM yyyy", Locale.US).apply { timeZone = calendar.timeZone }.format(startCal.time)
        } else {
            val startStr = SimpleDateFormat("d MMM", Locale.US).apply { timeZone = calendar.timeZone }.format(startCal.time)
            val endStr = SimpleDateFormat("d MMM yyyy", Locale.US).apply { timeZone = calendar.timeZone }.format(endCal.time)
            "$startStr \u2013 $endStr"
        }

        return CycleRange(
            startMillis = startMillis,
            endMillis = endMillis,
            label = label,
            daysLeft = if (daysLeft <= 0) 1 else daysLeft,
            isLastDay = isLastDay
        )
    }

    fun isCurrentMonth(date: Long, calendar: Calendar): Boolean {
        val txCal = Calendar.getInstance(calendar.timeZone).apply { timeInMillis = date }
        return txCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                txCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
    }

    fun isCurrentCycle(date: Long, cycleStartDay: Int = 1, calendar: Calendar = Calendar.getInstance()): Boolean {
        val range = getCycleRange(cycleStartDay, calendar)
        return date in range.startMillis..range.endMillis
    }

    fun calculate(
        plan: MonthlyPlan,
        transactions: List<Transaction>,
        calendar: Calendar = Calendar.getInstance(),
        totalNetWorth: Long = 100_000_000L,
        hasAccounts: Boolean = true,
        cycleStartDay: Int = 1
    ): SafeToSpendState {
        val cycle = getCycleRange(cycleStartDay, calendar)

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val todayMonthKey = "%04d-%02d".format(Locale.US, year, month)

        // Starting spent only if applies to current month/cycle
        val startingSpent = if (plan.monthKey == todayMonthKey || plan.monthKey.isEmpty()) plan.startingSpent else 0L

        // Expenses for current cycle
        val currentCycleExpenses = transactions.filter { tx ->
            tx.type == TransactionType.EXPENSE && tx.date >= cycle.startMillis && tx.date <= cycle.endMillis
        }.sumOf { it.amount }

        val totalSpent = startingSpent + currentCycleExpenses
        val budget = plan.budget
        val remaining = if (budget > totalSpent) budget - totalSpent else 0L

        val divisor = if (cycle.daysLeft > 0) cycle.daysLeft else 1
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
            daysLeft = cycle.daysLeft,
            isLastDay = cycle.isLastDay,
            percentSpent = percent,
            displayPercent = displayPct,
            status = status,
            cycleLabel = cycle.label
        )
    }
}
