package app.cashjournal.domain.model

data class MonthlyPlan(
    val id: String = "primary_plan",
    val budget: Long = 750000000L, // 7.500.000 IDR (or equivalent minor units)
    val startingSpent: Long = 325000000L, // 3.250.000 IDR (or equivalent minor units)
    val monthKey: String = "" // e.g. "2026-10"
)
