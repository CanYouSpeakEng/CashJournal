package app.cashjournal.domain.model

data class MonthlyPlan(
    val id: String = "primary_plan",
    val budget: Long = 0L,
    val startingSpent: Long = 0L,
    val monthKey: String = "" // e.g. "2026-10"
)
