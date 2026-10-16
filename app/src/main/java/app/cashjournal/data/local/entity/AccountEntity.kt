package app.cashjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val accentKey: String,
    val accountType: String = "BANK_DEBIT",
    val balance: Long,
    val initialBalance: Long = 0L,
    val sortIndex: Int,
    val updatedAt: Long
) {
    fun toDomain(): Account = Account(
        id = id,
        name = name,
        accentKey = AccentKey.fromKey(accentKey),
        accountType = AccountType.fromKey(accountType),
        balance = balance,
        initialBalance = initialBalance,
        sortIndex = sortIndex,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(account: Account): AccountEntity = AccountEntity(
            id = account.id,
            name = account.name,
            accentKey = account.accentKey.name,
            accountType = account.accountType.name,
            balance = account.balance,
            initialBalance = account.initialBalance,
            sortIndex = account.sortIndex,
            updatedAt = account.updatedAt
        )
    }
}
