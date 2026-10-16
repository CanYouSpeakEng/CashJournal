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
    val updatedAt: Long = System.currentTimeMillis()
)
