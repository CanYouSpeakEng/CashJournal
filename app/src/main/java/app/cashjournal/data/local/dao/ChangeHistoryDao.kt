package app.cashjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.cashjournal.data.local.entity.ChangeHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChangeHistoryDao {
    @Query("SELECT * FROM change_history ORDER BY timestamp DESC, id DESC LIMIT 50")
    fun getRecentHistoryFlow(): Flow<List<ChangeHistoryEntity>>

    @Query("SELECT * FROM change_history ORDER BY timestamp DESC, id DESC LIMIT 50")
    suspend fun getRecentHistory(): List<ChangeHistoryEntity>

    @Query("SELECT * FROM change_history ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun getLastAction(): ChangeHistoryEntity?

    @Query("SELECT * FROM change_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ChangeHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ChangeHistoryEntity): Long

    @Query("DELETE FROM change_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM change_history WHERE id NOT IN (SELECT id FROM change_history ORDER BY timestamp DESC, id DESC LIMIT 50)")
    suspend fun pruneOldHistory()

    @Query("DELETE FROM change_history")
    suspend fun deleteAll()
}
