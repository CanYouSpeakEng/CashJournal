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
    val cycleLabel: String = "",
    val budgetMode: BudgetMode = BudgetMode.FIXED_NOMINAL,
    val budgetPercent: Int = 10,
    val dailyBurnRate: Long = 0L,
    val survivalDays: Int = 0,
    val survivalMonths: Float = 0f,
    val isCalibrating: Boolean = false,
    val calibrationDaysNeeded: Int = 0
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
        val daysLeft = Math.round(diffMillis.toDouble() / (24 * 60 * 60 * 1000.0)).toInt().coerceAtLeast(0)
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

        val cycleCal = Calendar.getInstance(calendar.timeZone).apply { timeInMillis = cycle.startMillis }
        val cycleYear = cycleCal.get(Calendar.YEAR)
        val cycleMonth = cycleCal.get(Calendar.MONTH) + 1
        val cycleMonthKey = "%04d-%02d".format(Locale.US, cycleYear, cycleMonth)

        // Starting spent applies if it matches calendar month, active cycle start month, or empty key
        val startingSpent = if (plan.monthKey.isEmpty() || plan.monthKey == cycleMonthKey || plan.monthKey == todayMonthKey) {
            plan.startingSpent
        } else {
            0L
        }

        // Expenses for current cycle
        val currentCycleExpenses = transactions.filter { tx ->
            tx.type == TransactionType.EXPENSE && tx.date >= cycle.startMillis && tx.date <= cycle.endMillis
        }.sumOf { it.amount }

        val effectiveBudget = when (plan.budgetMode) {
            BudgetMode.PERCENTAGE_OF_FUNDS -> {
                if (totalNetWorth > 0L) {
                    val pct = plan.budgetPercent.coerceIn(1, 100)
                    (totalNetWorth.toDouble() * (pct.toDouble() / 100.0)).toLong()
                } else {
                    0L
                }
            }
            BudgetMode.FIXED_NOMINAL -> plan.budget
        }

        val totalSpent = startingSpent + currentCycleExpenses
        val remaining = if (effectiveBudget > totalSpent) effectiveBudget - totalSpent else 0L

        val divisor = if (cycle.daysLeft > 0) cycle.daysLeft else 1
        val hasNoFunds = !hasAccounts || totalNetWorth <= 0L
        val perDay = if (hasNoFunds) 0L else floor(remaining.toDouble() / divisor).toLong()

        val percent = if (effectiveBudget > 0) (totalSpent.toFloat() / effectiveBudget.toFloat()) else 0f
        val displayPct = min(100, (percent * 100).roundToInt())

        val status = when {
            hasNoFunds -> SafeToSpendStatus.NO_FUNDS
            effectiveBudget <= 0L -> SafeToSpendStatus.SET_BUDGET
            remaining <= 0L -> SafeToSpendStatus.RECALIBRATE
            percent >= 0.70f -> SafeToSpendStatus.NEAR_LIMIT
            else -> SafeToSpendStatus.SAFE
        }

        // Capital Burn-Rate Survival Runway Metric
        val nowCal = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nowMillis = nowCal.timeInMillis
        val elapsedMillis = (nowMillis - cycle.startMillis).coerceAtLeast(0L)
        val daysElapsedInCycle = ((elapsedMillis / (24 * 60 * 60 * 1000L)) + 1).toInt().coerceAtLeast(1)

        val windowStartMillis = nowMillis - (30L * 24 * 60 * 60 * 1000L)
        val rollingExpenseTxs = transactions.filter {
            it.type == TransactionType.EXPENSE && it.date in windowStartMillis..calendar.timeInMillis
        }

        val hasAnyExpenses = rollingExpenseTxs.isNotEmpty() || startingSpent > 0L || currentCycleExpenses > 0L
        val trailingDays = if (rollingExpenseTxs.isNotEmpty()) {
            val earliest = rollingExpenseTxs.minOf { it.date }
            (((nowMillis - earliest) / (24 * 60 * 60 * 1000L)) + 1).toInt().coerceAtLeast(1)
        } else {
            0
        }

        val effectiveHistoryDays = maxOf(daysElapsedInCycle, trailingDays)
        val isCalibrating = hasAnyExpenses && effectiveHistoryDays < 5
        val calibrationDaysNeeded = if (isCalibrating) (5 - effectiveHistoryDays).coerceAtLeast(1) else 0

        val (dailyBurnRate, survivalDays, survivalMonths) = if (!hasAnyExpenses || isCalibrating) {
            Triple(0L, 0, 0f)
        } else {
            val effectiveWindowDays = minOf(30, effectiveHistoryDays)
            val rollingExpenses = rollingExpenseTxs.sumOf { it.amount }
            val effectiveSpent = maxOf(rollingExpenses, startingSpent + currentCycleExpenses)
            val burnRate = if (effectiveSpent > 0L && effectiveWindowDays > 0) {
                (effectiveSpent.toDouble() / effectiveWindowDays.toDouble()).toLong()
            } else {
                0L
            }

            val (days, months) = if (burnRate > 0L && totalNetWorth > 0L) {
                val daysExact = totalNetWorth.toDouble() / burnRate.toDouble()
                val m = (daysExact / 30.0).toFloat()
                Pair(daysExact.toInt(), m)
            } else {
                Pair(0, 0f)
            }
            Triple(burnRate, days, months)
        }

        return SafeToSpendState(
            safeToSpendToday = perDay,
            remaining = remaining,
            spentThisMonth = totalSpent,
            budget = effectiveBudget,
            daysLeft = cycle.daysLeft,
            isLastDay = cycle.isLastDay,
            percentSpent = percent,
            displayPercent = displayPct,
            status = status,
            cycleLabel = cycle.label,
            budgetMode = plan.budgetMode,
            budgetPercent = plan.budgetPercent,
            dailyBurnRate = dailyBurnRate,
            survivalDays = survivalDays,
            survivalMonths = survivalMonths,
            isCalibrating = isCalibrating,
            calibrationDaysNeeded = calibrationDaysNeeded
        )
    }
}
