package app.cashjournal.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.cashjournal.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY sortIndex ASC")
    fun getAllAccountsFlow(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<AccountEntity>)

    @Update
    suspend fun update(account: AccountEntity)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()

    @Query("UPDATE accounts SET balance = balance + :amountDelta, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBalance(id: String, amountDelta: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE accounts 
        SET balance = initialBalance + (
            SELECT COALESCE(SUM(
                CASE 
                    WHEN type = 'INCOME' AND sourceAccountId = accounts.id THEN amount
                    WHEN type = 'TRANSFER' AND destinationAccountId = accounts.id THEN amount
                    WHEN type = 'EXPENSE' AND sourceAccountId = accounts.id THEN -amount
                    WHEN type = 'TRANSFER' AND sourceAccountId = accounts.id THEN -amount
                    ELSE 0 
                END
            ), 0)
            FROM transactions 
            WHERE sourceAccountId = accounts.id OR destinationAccountId = accounts.id
        ),
        updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun recalculateAccountBalance(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE accounts 
        SET balance = initialBalance + (
            SELECT COALESCE(SUM(
                CASE 
                    WHEN type = 'INCOME' AND sourceAccountId = accounts.id THEN amount
                    WHEN type = 'TRANSFER' AND destinationAccountId = accounts.id THEN amount
                    WHEN type = 'EXPENSE' AND sourceAccountId = accounts.id THEN -amount
                    WHEN type = 'TRANSFER' AND sourceAccountId = accounts.id THEN -amount
                    ELSE 0 
                END
            ), 0)
            FROM transactions 
            WHERE sourceAccountId = accounts.id OR destinationAccountId = accounts.id
        ),
        updatedAt = :updatedAt
    """)
    suspend fun recalculateAllAccountBalances(updatedAt: Long = System.currentTimeMillis())
}
