package app.cashjournal.domain.model

enum class BudgetMode {
    FIXED_NOMINAL,
    PERCENTAGE_OF_FUNDS;

    companion object {
        fun fromKey(key: String?): BudgetMode {
            return when (key?.uppercase()) {
                "PERCENTAGE_OF_FUNDS", "PERCENTAGE", "PERCENT" -> PERCENTAGE_OF_FUNDS
                else -> FIXED_NOMINAL
            }
        }
    }
}

data class MonthlyPlan(
    val id: String = "primary_plan",
    val budget: Long = 0L,
    val startingSpent: Long = 0L,
    val monthKey: String = "", // e.g. "2026-10"
    val budgetMode: BudgetMode = BudgetMode.FIXED_NOMINAL,
    val budgetPercent: Int = 10 // Percentage of total net worth/funds (1..100)
)
