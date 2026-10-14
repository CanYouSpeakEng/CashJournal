package app.cashjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.AccentKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val accentKey: String,
    val balance: Long,
    val sortIndex: Int,
    val updatedAt: Long
) {
    fun toDomain(): Account = Account(
        id = id,
        name = name,
        accentKey = AccentKey.fromKey(accentKey),
        balance = balance,
        sortIndex = sortIndex,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(account: Account): AccountEntity = AccountEntity(
            id = account.id,
            name = account.name,
            accentKey = account.accentKey.name,
            balance = account.balance,
            sortIndex = account.sortIndex,
            updatedAt = account.updatedAt
        )
    }
}
