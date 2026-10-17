package app.cashjournal.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.RecurrenceFrequency
import app.cashjournal.domain.model.RecurringTransaction
import app.cashjournal.domain.model.TransactionType

@Entity(
    tableName = "recurring_transactions",
    indices = [
        Index(value = ["sourceAccountId"]),
        Index(value = ["isActive"]),
        Index(value = ["dueDay"])
    ]
)
data class RecurringTransactionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val type: String,
    val amount: Long,
    val frequency: String,
    val dueDay: Int,
    val sourceAccountId: String?,
    val destinationAccountId: String?,
    val isActive: Boolean,
    val autoLog: Boolean,
    val lastLoggedDate: Long?,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): RecurringTransaction = RecurringTransaction(
        id = id,
        title = title,
        category = Category.fromKey(category),
        type = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.EXPENSE },
        amount = amount,
        frequency = RecurrenceFrequency.fromKey(frequency),
        dueDay = dueDay,
        sourceAccountId = sourceAccountId,
        destinationAccountId = destinationAccountId,
        isActive = isActive,
        autoLog = autoLog,
        lastLoggedDate = lastLoggedDate,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(rt: RecurringTransaction): RecurringTransactionEntity = RecurringTransactionEntity(
            id = rt.id,
            title = rt.title,
            category = rt.category.key,
            type = rt.type.name,
            amount = rt.amount,
            frequency = rt.frequency.key,
            dueDay = rt.dueDay,
            sourceAccountId = rt.sourceAccountId,
            destinationAccountId = rt.destinationAccountId,
            isActive = rt.isActive,
            autoLog = rt.autoLog,
            lastLoggedDate = rt.lastLoggedDate,
            note = rt.note,
            createdAt = rt.createdAt,
            updatedAt = rt.updatedAt
        )
    }
}
