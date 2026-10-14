package app.cashjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.cashjournal.data.local.entity.MonthlyPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyPlanDao {
    @Query("SELECT * FROM monthly_plans WHERE id = :id LIMIT 1")
    fun getPlanFlow(id: String = "primary_plan"): Flow<MonthlyPlanEntity?>

    @Query("SELECT * FROM monthly_plans WHERE id = :id LIMIT 1")
    suspend fun getPlan(id: String = "primary_plan"): MonthlyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plan: MonthlyPlanEntity)

    @Update
    suspend fun update(plan: MonthlyPlanEntity)

    @Query("DELETE FROM monthly_plans")
    suspend fun deleteAll()
}
