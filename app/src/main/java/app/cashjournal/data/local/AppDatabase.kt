package app.cashjournal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.cashjournal.data.local.dao.AccountDao
import app.cashjournal.data.local.dao.MonthlyPlanDao
import app.cashjournal.data.local.dao.TransactionDao
import app.cashjournal.data.local.entity.AccountEntity
import app.cashjournal.data.local.entity.MonthlyPlanEntity
import app.cashjournal.data.local.entity.TransactionEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        MonthlyPlanEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun monthlyPlanDao(): MonthlyPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add initialBalance column to accounts table
                db.execSQL("ALTER TABLE accounts ADD COLUMN initialBalance INTEGER NOT NULL DEFAULT 0")

                // 2. Reconstruct initialBalance for existing accounts: initialBalance = balance - net_flows
                db.execSQL("""
                    UPDATE accounts 
                    SET initialBalance = balance - (
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
                    )
                """.trimIndent())

                // 3. Add indices on transactions table for fast lookups
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_sourceAccountId ON transactions(sourceAccountId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_destinationAccountId ON transactions(destinationAccountId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_date ON transactions(date)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashjournal_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
