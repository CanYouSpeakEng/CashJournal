package app.cashjournal.domain.model

import java.util.UUID

data class Account(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val accentKey: AccentKey = AccentKey.COBALT,
    val accountType: AccountType = AccountType.BANK_DEBIT,
    val balance: Long = 0L, // Minor units
    val initialBalance: Long = 0L,
    val sortIndex: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val creditLimit: Long = 0L,
    val statementDueDay: Int = 0,
    val statementPaymentMode: String = "TARGET",
    val targetPaymentAmount: Long = 0L,
    val investmentType: String? = null,
    val unitsHeld: Double = 0.0,
    val pricePerUnit: Long = 0L,
    val previousPricePerUnit: Long = 0L,
    val priceUpdatedAt: Long = 0L
) {
    val availableCredit: Long
        get() = if (accountType == AccountType.CREDIT_CARD && creditLimit > 0L) {
            (creditLimit + balance).coerceAtLeast(0L)
        } else {
            0L
        }
}
