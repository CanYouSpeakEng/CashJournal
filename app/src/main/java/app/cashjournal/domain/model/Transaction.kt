package app.cashjournal.domain.model

import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: Category = Category.OTHER,
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: Long, // Minor units (always positive in storage, signed on display)
    val feeAmount: Long = 0L, // Minor units for transfer fee (always non-negative)
    val date: Long = System.currentTimeMillis(),
    val iconKey: String = "Tag",
    val accentKey: AccentKey = AccentKey.COBALT,
    val sourceAccountId: String? = null,
    val destinationAccountId: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
