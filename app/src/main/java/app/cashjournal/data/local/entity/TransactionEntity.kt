package app.cashjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["sourceAccountId"]),
        Index(value = ["destinationAccountId"]),
        Index(value = ["date"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val type: String,
    val amount: Long,
    val feeAmount: Long = 0L,
    val date: Long,
    val iconKey: String,
    val accentKey: String,
    val sourceAccountId: String?,
    val destinationAccountId: String?,
    val updatedAt: Long
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        title = title,
        category = Category.fromKey(category),
        type = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.EXPENSE },
        amount = amount,
        feeAmount = feeAmount,
        date = date,
        iconKey = iconKey,
        accentKey = AccentKey.fromKey(accentKey),
        sourceAccountId = sourceAccountId,
        destinationAccountId = destinationAccountId,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(tx: Transaction): TransactionEntity = TransactionEntity(
            id = tx.id,
            title = tx.title,
            category = tx.category.key,
            type = tx.type.name,
            amount = tx.amount,
            feeAmount = tx.feeAmount,
            date = tx.date,
            iconKey = tx.iconKey,
            accentKey = tx.accentKey.name,
            sourceAccountId = tx.sourceAccountId,
            destinationAccountId = tx.destinationAccountId,
            updatedAt = tx.updatedAt
        )
    }
}
