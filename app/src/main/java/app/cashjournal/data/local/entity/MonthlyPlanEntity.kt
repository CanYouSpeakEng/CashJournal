package app.cashjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.cashjournal.domain.model.MonthlyPlan

@Entity(tableName = "monthly_plans")
data class MonthlyPlanEntity(
    @PrimaryKey
    val id: String = "primary_plan",
    val budget: Long,
    val startingSpent: Long,
    val monthKey: String
) {
    fun toDomain(): MonthlyPlan = MonthlyPlan(
        id = id,
        budget = budget,
        startingSpent = startingSpent,
        monthKey = monthKey
    )

    companion object {
        fun fromDomain(plan: MonthlyPlan): MonthlyPlanEntity = MonthlyPlanEntity(
            id = plan.id,
            budget = plan.budget,
            startingSpent = plan.startingSpent,
            monthKey = plan.monthKey
        )
    }
}
