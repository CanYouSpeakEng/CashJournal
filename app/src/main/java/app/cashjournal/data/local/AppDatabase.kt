package app.cashjournal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.cashjournal.data.local.dao.AccountDao
import app.cashjournal.data.local.dao.ChangeHistoryDao
import app.cashjournal.data.local.dao.MonthlyPlanDao
import app.cashjournal.data.local.dao.RecurringTransactionDao
import app.cashjournal.data.local.dao.TransactionDao
import app.cashjournal.data.local.entity.AccountEntity
import app.cashjournal.data.local.entity.ChangeHistoryEntity
import app.cashjournal.data.local.entity.MonthlyPlanEntity
import app.cashjournal.data.local.entity.RecurringTransactionEntity
import app.cashjournal.data.local.entity.TransactionEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        MonthlyPlanEntity::class,
        RecurringTransactionEntity::class,
        ChangeHistoryEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun monthlyPlanDao(): MonthlyPlanDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun changeHistoryDao(): ChangeHistoryDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add accountType column to accounts table
                db.execSQL("ALTER TABLE accounts ADD COLUMN accountType TEXT NOT NULL DEFAULT 'BANK_DEBIT'")

                // 2. Automatically infer types for existing accounts based on name keywords
                db.execSQL("""
                    UPDATE accounts 
                    SET accountType = 'CASH' 
                    WHERE LOWER(name) LIKE '%cash%' OR LOWER(name) LIKE '%tunai%' OR LOWER(name) LIKE '%dompet%' OR LOWER(name) LIKE '%pocket%' OR LOWER(name) LIKE '%saku%' OR LOWER(name) LIKE '%wallet%'
                """.trimIndent())

                db.execSQL("""
                    UPDATE accounts 
                    SET accountType = 'E_WALLET' 
                    WHERE LOWER(name) LIKE '%gopay%' OR LOWER(name) LIKE '%ovo%' OR LOWER(name) LIKE '%dana%' OR LOWER(name) LIKE '%shopeepay%' OR LOWER(name) LIKE '%linkaja%' OR LOWER(name) LIKE '%ewallet%' OR LOWER(name) LIKE '%e-wallet%' OR LOWER(name) LIKE '%paypal%'
                """.trimIndent())

                db.execSQL("""
                    UPDATE accounts 
                    SET accountType = 'CREDIT_CARD' 
                    WHERE LOWER(name) LIKE '%credit%' OR LOWER(name) LIKE '%kredit%' OR LOWER(name) LIKE '% cc %' OR LOWER(name) LIKE 'cc %' OR LOWER(name) LIKE '% cc' OR LOWER(name) = 'cc'
                """.trimIndent())

                db.execSQL("""
                    UPDATE accounts 
                    SET accountType = 'SAVINGS' 
                    WHERE LOWER(name) LIKE '%saving%' OR LOWER(name) LIKE '%tabungan%' OR LOWER(name) LIKE '%invest%' OR LOWER(name) LIKE '%bibit%' OR LOWER(name) LIKE '%deposito%' OR LOWER(name) LIKE '%darurat%' OR LOWER(name) LIKE '%vault%' OR LOWER(name) LIKE '%emergency%' OR LOWER(name) LIKE '%fund%'
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE monthly_plans ADD COLUMN budgetMode TEXT NOT NULL DEFAULT 'FIXED_NOMINAL'")
                db.execSQL("ALTER TABLE monthly_plans ADD COLUMN budgetPercent INTEGER NOT NULL DEFAULT 10")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `recurring_transactions` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `frequency` TEXT NOT NULL,
                        `dueDay` INTEGER NOT NULL,
                        `sourceAccountId` TEXT,
                        `destinationAccountId` TEXT,
                        `isActive` INTEGER NOT NULL,
                        `autoLog` INTEGER NOT NULL,
                        `lastLoggedDate` INTEGER,
                        `note` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_sourceAccountId` ON `recurring_transactions` (`sourceAccountId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_isActive` ON `recurring_transactions` (`isActive`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_recurring_transactions_dueDay` ON `recurring_transactions` (`dueDay`)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN feeAmount INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN creditLimit INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN statementDueDay INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN statementPaymentMode TEXT NOT NULL DEFAULT 'TARGET'")
                db.execSQL("ALTER TABLE accounts ADD COLUMN targetPaymentAmount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN investmentType TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE accounts ADD COLUMN unitsHeld REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN pricePerUnit INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN previousPricePerUnit INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE accounts ADD COLUMN priceUpdatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add splitGroupId column and index to transactions table
                db.execSQL("ALTER TABLE transactions ADD COLUMN splitGroupId TEXT DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_splitGroupId ON transactions(splitGroupId)")

                // 2. Create change_history table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `change_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `actionType` TEXT NOT NULL,
                        `entityType` TEXT NOT NULL,
                        `entityId` TEXT NOT NULL,
                        `summary` TEXT NOT NULL,
                        `previousStateJson` TEXT,
                        `newStateJson` TEXT,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_change_history_timestamp ON change_history(timestamp)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashjournal_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
