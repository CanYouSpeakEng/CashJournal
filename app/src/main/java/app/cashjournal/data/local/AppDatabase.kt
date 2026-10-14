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

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        MonthlyPlanEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun monthlyPlanDao(): MonthlyPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cashjournal_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
