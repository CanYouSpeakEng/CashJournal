package app.cashjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.cashjournal.data.local.entity.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions ORDER BY dueDay ASC, createdAt ASC")
    fun getAllRecurringFlow(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions ORDER BY dueDay ASC, createdAt ASC")
    suspend fun getAllRecurring(): List<RecurringTransactionEntity>

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY dueDay ASC")
    fun getActiveRecurringFlow(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id LIMIT 1")
    suspend fun getRecurringById(id: String): RecurringTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurring: RecurringTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recurringList: List<RecurringTransactionEntity>)

    @Update
    suspend fun update(recurring: RecurringTransactionEntity)

    @Delete
    suspend fun delete(recurring: RecurringTransactionEntity)

    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM recurring_transactions")
    suspend fun deleteAll()

    @Query("UPDATE recurring_transactions SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateActiveState(id: String, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE recurring_transactions SET lastLoggedDate = :lastLoggedDate, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateLastLoggedDate(id: String, lastLoggedDate: Long, updatedAt: Long = System.currentTimeMillis())
}
