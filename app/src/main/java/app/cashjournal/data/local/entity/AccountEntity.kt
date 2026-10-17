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
    val updatedAt: Long,
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
    fun toDomain(): Account = Account(
        id = id,
        name = name,
        accentKey = AccentKey.fromKey(accentKey),
        accountType = AccountType.fromKey(accountType),
        balance = balance,
        initialBalance = initialBalance,
        sortIndex = sortIndex,
        updatedAt = updatedAt,
        creditLimit = creditLimit,
        statementDueDay = statementDueDay,
        statementPaymentMode = statementPaymentMode,
        targetPaymentAmount = targetPaymentAmount,
        investmentType = investmentType,
        unitsHeld = unitsHeld,
        pricePerUnit = pricePerUnit,
        previousPricePerUnit = previousPricePerUnit,
        priceUpdatedAt = priceUpdatedAt
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
            updatedAt = account.updatedAt,
            creditLimit = account.creditLimit,
            statementDueDay = account.statementDueDay,
            statementPaymentMode = account.statementPaymentMode,
            targetPaymentAmount = account.targetPaymentAmount,
            investmentType = account.investmentType,
            unitsHeld = account.unitsHeld,
            pricePerUnit = account.pricePerUnit,
            previousPricePerUnit = account.previousPricePerUnit,
            priceUpdatedAt = account.priceUpdatedAt
        )
    }
}
