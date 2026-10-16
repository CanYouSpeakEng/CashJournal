package app.cashjournal

import app.cashjournal.domain.model.BudgetMode
import app.cashjournal.domain.model.MonthlyPlan
import app.cashjournal.domain.model.SafeToSpendCalculator
import app.cashjournal.domain.model.SafeToSpendStatus
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SafeToSpendTest {

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
    fun testBriefSection16AcceptanceCheck() {
        // Build Brief v2 Section 16 Acceptance Check:
        // Date: October 14, 2026
        // Budget: 7,500,000 IDR
        // Starting spent: 3,250,000 IDR
        // Expenses this month: 1,695,000 IDR
        // Total spent: 4,945,000 IDR
        // Remaining: 2,555,000 IDR
        // Days left: 17 days (31 - 14)
        // Safe-to-spend: 150,294 / day
        // Ring: 66% (4,945,000 / 7,500,000 = 65.93% -> 66%)
        // Status: Safe
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 14)

        val plan = MonthlyPlan(
            monthKey = "2026-10",
            budget = 7500000L,
            startingSpent = 3250000L
        )

        val tx = Transaction(
            title = "Monthly Expenses",
            amount = 1695000L,
            type = TransactionType.EXPENSE,
            date = cal.timeInMillis
        )

        val result = SafeToSpendCalculator.calculate(plan, listOf(tx), cal)

        assertEquals(4945000L, result.spentThisMonth)
        assertEquals(2555000L, result.remaining)
        assertEquals(17, result.daysLeft)
        assertEquals(150294L, result.safeToSpendToday)
        assertEquals(66, result.displayPercent)
        assertEquals(SafeToSpendStatus.SAFE, result.status)
        assertFalse(result.isLastDay)
    }

    @Test
    fun testLeapYearFebruaryDaysLeft() {
        // Leap year: 2024 (29 days in Feb)
        // Date: Feb 14 -> 29 - 14 = 15 days left
        val cal2024 = createUtcCalendar(2024, Calendar.FEBRUARY, 14)
        val plan2024 = MonthlyPlan(monthKey = "2024-02", budget = 1500000L)
        val result2024 = SafeToSpendCalculator.calculate(plan2024, emptyList(), cal2024)
        assertEquals(15, result2024.daysLeft)
        assertEquals(100000L, result2024.safeToSpendToday)

        // Non-leap year: 2023 (28 days in Feb)
        // Date: Feb 14 -> 28 - 14 = 14 days left
        val cal2023 = createUtcCalendar(2023, Calendar.FEBRUARY, 14)
        val plan2023 = MonthlyPlan(monthKey = "2023-02", budget = 1400000L)
        val result2023 = SafeToSpendCalculator.calculate(plan2023, emptyList(), cal2023)
        assertEquals(14, result2023.daysLeft)
        assertEquals(100000L, result2023.safeToSpendToday)
    }

    @Test
    fun testExpensesDeductionAndThresholds() {
        // Budget = 1,000,000
        // Day = 15 of April (30 days) -> 15 days left
        val cal = createUtcCalendar(2026, Calendar.APRIL, 15)
        val txTime = cal.timeInMillis

        // 1. Safe status (< 70% spent)
        val plan = MonthlyPlan(monthKey = "2026-04", budget = 1000000L)
        val tx1 = Transaction(title = "Groceries", amount = 500000L, type = TransactionType.EXPENSE, date = txTime)
        val result1 = SafeToSpendCalculator.calculate(plan, listOf(tx1), cal)
        assertEquals(500000L, result1.spentThisMonth)
        assertEquals(500000L, result1.remaining)
        assertEquals(50, result1.displayPercent)
        assertEquals(SafeToSpendStatus.SAFE, result1.status)

        // 2. Near limit status (>= 70% spent, per brief: 79% is near limit)
        val tx2 = Transaction(title = "Shopping", amount = 250000L, type = TransactionType.EXPENSE, date = txTime)
        val result2 = SafeToSpendCalculator.calculate(plan, listOf(tx1, tx2), cal)
        assertEquals(750000L, result2.spentThisMonth)
        assertEquals(250000L, result2.remaining)
        assertEquals(75, result2.displayPercent)
        assertEquals(SafeToSpendStatus.NEAR_LIMIT, result2.status)

        // 3. Recalibrate status (100% or overspent)
        val tx3 = Transaction(title = "Dinner", amount = 300000L, type = TransactionType.EXPENSE, date = txTime)
        val result3 = SafeToSpendCalculator.calculate(plan, listOf(tx1, tx2, tx3), cal)
        assertEquals(1050000L, result3.spentThisMonth)
        assertEquals(0L, result3.remaining)
        assertEquals(SafeToSpendStatus.RECALIBRATE, result3.status)
    }

    @Test
    fun testLastDayOfMonth() {
        // Last day of month (April 30) -> daysLeft = 0, divisor = 1, isLastDay = true
        val cal = createUtcCalendar(2026, Calendar.APRIL, 30)
        val plan = MonthlyPlan(monthKey = "2026-04", budget = 500000L)
        val result = SafeToSpendCalculator.calculate(plan, emptyList(), cal)

        assertTrue(result.isLastDay)
        assertEquals(1, result.daysLeft) // Clamped to 1 for display
        assertEquals(500000L, result.safeToSpendToday)
    }

    @Test
    fun testZeroBudgetRequiresSetting() {
        val cal = createUtcCalendar(2026, Calendar.APRIL, 1)
        val plan = MonthlyPlan(monthKey = "2026-04", budget = 0L)
        val result = SafeToSpendCalculator.calculate(plan, emptyList(), cal)
        assertEquals(SafeToSpendStatus.SET_BUDGET, result.status)
    }

    @Test
    fun testCycleRangeDay1() {
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 14)
        val cycle = SafeToSpendCalculator.getCycleRange(cycleStartDay = 1, calendar = cal)
        assertEquals("October 2026", cycle.label)
        assertEquals(17, cycle.daysLeft)
        assertFalse(cycle.isLastDay)
    }

    @Test
    fun testCycleRangeMidMonthBeforeCycleDay() {
        // Current day: 14 Oct 2026, Cycle start: 25th -> Cycle is 25 Sep - 24 Oct 2026
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 14)
        val cycle = SafeToSpendCalculator.getCycleRange(cycleStartDay = 25, calendar = cal)
        assertEquals("25 Sep \u2013 24 Oct 2026", cycle.label)
        assertEquals(10, cycle.daysLeft) // 24 - 14 = 10
        assertFalse(cycle.isLastDay)
    }

    @Test
    fun testCycleRangeMidMonthAfterCycleDay() {
        // Current day: 26 Oct 2026, Cycle start: 25th -> Cycle is 25 Oct - 24 Nov 2026
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 26)
        val cycle = SafeToSpendCalculator.getCycleRange(cycleStartDay = 25, calendar = cal)
        assertEquals("25 Oct \u2013 24 Nov 2026", cycle.label)
        assertEquals(29, cycle.daysLeft)
        assertFalse(cycle.isLastDay)
    }

    @Test
    fun testCycleRangeClamping() {
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 14)
        val cycleMin = SafeToSpendCalculator.getCycleRange(cycleStartDay = 0, calendar = cal)
        assertEquals("October 2026", cycleMin.label)

        val cycleMax = SafeToSpendCalculator.getCycleRange(cycleStartDay = 31, calendar = cal)
        assertEquals("28 Sep \u2013 27 Oct 2026", cycleMax.label)
    }

    @Test
    fun testCustomCycleExpensesFiltering() {
        // Cycle starts on 25th. Today is 14 Oct 2026.
        // Active range: 25 Sep 2026 - 24 Oct 2026
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 14)

        val plan = MonthlyPlan(
            monthKey = "2026-10",
            budget = 5000000L
        )

        // Tx 1: 20 Sep (before cycle) -> should be excluded
        val txBefore = Transaction(
            title = "Old Tx",
            amount = 1000000L,
            type = TransactionType.EXPENSE,
            date = createUtcCalendar(2026, Calendar.SEPTEMBER, 20).timeInMillis
        )
        // Tx 2: 28 Sep (in cycle) -> should be included
        val txInCycle1 = Transaction(
            title = "Groceries",
            amount = 500000L,
            type = TransactionType.EXPENSE,
            date = createUtcCalendar(2026, Calendar.SEPTEMBER, 28).timeInMillis
        )
        // Tx 3: 10 Oct (in cycle) -> should be included
        val txInCycle2 = Transaction(
            title = "Utilities",
            amount = 300000L,
            type = TransactionType.EXPENSE,
            date = createUtcCalendar(2026, Calendar.OCTOBER, 10).timeInMillis
        )

        val result = SafeToSpendCalculator.calculate(
            plan = plan,
            transactions = listOf(txBefore, txInCycle1, txInCycle2),
            calendar = cal,
            cycleStartDay = 25
        )

        assertEquals(800000L, result.spentThisMonth)
        assertEquals(4200000L, result.remaining)
        assertEquals(10, result.daysLeft)
        // 4,200,000 / 10 = 420,000 per day
        assertEquals(420000L, result.safeToSpendToday)
        assertEquals(SafeToSpendStatus.SAFE, result.status)
    }

    @Test
    fun testStartingSpentPreservedAcrossMonthBoundaryWithCustomCycle() {
        // Today is November 2, 2026. Custom cycle starts on the 25th.
        // Active cycle range is 25 Oct 2026 - 24 Nov 2026.
        // Cycle start month is 2026-10, while current calendar month is 2026-11.
        val cal = createUtcCalendar(2026, Calendar.NOVEMBER, 2)

        // Plan configured for this cycle with monthKey matching cycle start month ("2026-10")
        val plan = MonthlyPlan(
            monthKey = "2026-10",
            budget = 5000000L,
            startingSpent = 1500000L
        )

        // Transaction within active cycle in November
        val txNov = Transaction(
            title = "Early Nov Groceries",
            amount = 500000L,
            type = TransactionType.EXPENSE,
            date = createUtcCalendar(2026, Calendar.NOVEMBER, 1).timeInMillis
        )

        val result = SafeToSpendCalculator.calculate(
            plan = plan,
            transactions = listOf(txNov),
            calendar = cal,
            cycleStartDay = 25
        )

        // startingSpent must NOT be reset to 0 even though calendar month is November
        assertEquals(2000000L, result.spentThisMonth) // 1,500,000 starting + 500,000 tx
        assertEquals(3000000L, result.remaining)
    }

    @Test
    fun testPercentageOfFundsBudgetMode() {
        // Net worth: 50,000,000 IDR
        // Budget percent: 10%
        // Computed budget = 5,000,000 IDR
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 15)
        val plan = MonthlyPlan(
            monthKey = "2026-10",
            budget = 0L,
            budgetMode = BudgetMode.PERCENTAGE_OF_FUNDS,
            budgetPercent = 10
        )

        val tx = Transaction(
            title = "Living Expenses",
            amount = 1500000L,
            type = TransactionType.EXPENSE,
            date = cal.timeInMillis
        )

        val result = SafeToSpendCalculator.calculate(
            plan = plan,
            transactions = listOf(tx),
            calendar = cal,
            totalNetWorth = 50000000L
        )

        assertEquals(BudgetMode.PERCENTAGE_OF_FUNDS, result.budgetMode)
        assertEquals(10, result.budgetPercent)
        assertEquals(5000000L, result.budget) // 10% of 50M
        assertEquals(1500000L, result.spentThisMonth)
        assertEquals(3500000L, result.remaining)
        assertEquals(30, result.displayPercent) // 1.5M / 5M = 30%
        assertEquals(SafeToSpendStatus.SAFE, result.status)
    }

    @Test
    fun testPercentageOfFundsZeroOrNegativeNetWorth() {
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 15)
        val plan = MonthlyPlan(
            budgetMode = BudgetMode.PERCENTAGE_OF_FUNDS,
            budgetPercent = 15
        )

        val resultZero = SafeToSpendCalculator.calculate(
            plan = plan,
            transactions = emptyList(),
            calendar = cal,
            totalNetWorth = 0L
        )

        assertEquals(0L, resultZero.budget)
        assertEquals(SafeToSpendStatus.NO_FUNDS, resultZero.status)
        assertEquals(0L, resultZero.safeToSpendToday)

        val resultNegative = SafeToSpendCalculator.calculate(
            plan = plan,
            transactions = emptyList(),
            calendar = cal,
            totalNetWorth = -5000000L
        )

        assertEquals(0L, resultNegative.budget)
        assertEquals(SafeToSpendStatus.NO_FUNDS, resultNegative.status)
    }

    @Test
    fun testCapitalSurvivalRunwayCalculation() {
        // Oct 10, 2026 with cycle starting on 1st -> 10 days elapsed (Oct 1..10)
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 10)
        val plan = MonthlyPlan(
            monthKey = "2026-10",
            budget = 10000000L
        )

        // Total expenses in cycle = 3,000,000 IDR
        val tx = Transaction(
            title = "Food and Utilities",
            amount = 3000000L,
            type = TransactionType.EXPENSE,
            date = cal.timeInMillis
        )

        // Total net worth = 60,000,000 IDR
        val result = SafeToSpendCalculator.calculate(
            plan = plan,
            transactions = listOf(tx),
            calendar = cal,
            totalNetWorth = 60000000L,
            cycleStartDay = 1
        )

        // Daily burn rate = 3,000,000 / 10 days = 300,000 / day
        assertEquals(300000L, result.dailyBurnRate)

        // Survival days = 60,000,000 / 300,000 = 200 days
        assertEquals(200, result.survivalDays)

        // Survival months = 200 / 30 = 6.666... -> ~6.67 months
        assertTrue(result.survivalMonths in 6.6f..6.7f)
    }

    @Test
    fun testCapitalSurvivalRunwayZeroBurn() {
        val cal = createUtcCalendar(2026, Calendar.OCTOBER, 10)
        val plan = MonthlyPlan(budget = 5000000L)

        val result = SafeToSpendCalculator.calculate(
            plan = plan,
            transactions = emptyList(),
            calendar = cal,
            totalNetWorth = 50000000L
        )

        assertEquals(0L, result.dailyBurnRate)
        assertEquals(0, result.survivalDays)
        assertEquals(0f, result.survivalMonths, 0.001f)
    }

    @Test
    fun testBudgetModeEnumFromKey() {
        assertEquals(BudgetMode.FIXED_NOMINAL, BudgetMode.fromKey("FIXED_NOMINAL"))
        assertEquals(BudgetMode.PERCENTAGE_OF_FUNDS, BudgetMode.fromKey("PERCENTAGE_OF_FUNDS"))
        assertEquals(BudgetMode.PERCENTAGE_OF_FUNDS, BudgetMode.fromKey("percentage"))
        assertEquals(BudgetMode.PERCENTAGE_OF_FUNDS, BudgetMode.fromKey("percent"))
        assertEquals(BudgetMode.FIXED_NOMINAL, BudgetMode.fromKey(null))
        assertEquals(BudgetMode.FIXED_NOMINAL, BudgetMode.fromKey("unknown"))
    }
}
