package app.cashjournal

import app.cashjournal.data.backup.AccountBackupDto
import app.cashjournal.data.backup.BackupPayload
import app.cashjournal.data.local.entity.AccountEntity
import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.AccountType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountTypeTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testAccountTypeFromKey() {
        assertEquals(AccountType.CASH, AccountType.fromKey("CASH"))
        assertEquals(AccountType.BANK_DEBIT, AccountType.fromKey("BANK_DEBIT"))
        assertEquals(AccountType.E_WALLET, AccountType.fromKey("E_WALLET"))
        assertEquals(AccountType.CREDIT_CARD, AccountType.fromKey("CREDIT_CARD"))
        assertEquals(AccountType.SAVINGS, AccountType.fromKey("SAVINGS"))
        assertEquals(AccountType.BANK_DEBIT, AccountType.fromKey("UNKNOWN_TYPE"))
    }

    @Test
    fun testAccountTypeInferFromName() {
        assertEquals(AccountType.CASH, AccountType.inferFromName("Cash Wallet"))
        assertEquals(AccountType.CASH, AccountType.inferFromName("Uang Saku"))
        assertEquals(AccountType.CASH, AccountType.inferFromName("Dompet Utama"))

        assertEquals(AccountType.BANK_DEBIT, AccountType.inferFromName("BCA Payroll"))
        assertEquals(AccountType.BANK_DEBIT, AccountType.inferFromName("Bank Mandiri"))
        assertEquals(AccountType.BANK_DEBIT, AccountType.inferFromName("Checking Account"))

        assertEquals(AccountType.E_WALLET, AccountType.inferFromName("GoPay"))
        assertEquals(AccountType.E_WALLET, AccountType.inferFromName("Dana Saldo"))
        assertEquals(AccountType.E_WALLET, AccountType.inferFromName("ShopeePay"))

        assertEquals(AccountType.CREDIT_CARD, AccountType.inferFromName("BCA Credit Card"))
        assertEquals(AccountType.CREDIT_CARD, AccountType.inferFromName("Kartu Kredit Mandiri"))

        assertEquals(AccountType.SAVINGS, AccountType.inferFromName("Bibit Vault"))
        assertEquals(AccountType.SAVINGS, AccountType.inferFromName("Tabungan Impian"))
        assertEquals(AccountType.SAVINGS, AccountType.inferFromName("Emergency Fund"))
    }

    @Test
    fun testEntityDomainMapping() {
        val account = Account(
            id = "test-1",
            name = "GoPay Wallet",
            accentKey = AccentKey.COBALT,
            accountType = AccountType.E_WALLET,
            balance = 1_500_000L
        )

        val entity = AccountEntity.fromDomain(account)
        assertEquals("E_WALLET", entity.accountType)

        val domainFromEntity = entity.toDomain()
        assertEquals(AccountType.E_WALLET, domainFromEntity.accountType)
        assertEquals("GoPay Wallet", domainFromEntity.name)
        assertEquals(1_500_000L, domainFromEntity.balance)
    }

    @Test
    fun testBackupRoundTripWithAccountType() {
        val payload = BackupPayload(
            version = 1,
            appVersion = "1.0.19",
            exportedAt = 1760000000000L,
            accounts = listOf(
                AccountBackupDto(
                    id = "acc-e-wallet",
                    name = "GoPay",
                    accentKey = "COBALT",
                    accountType = "E_WALLET",
                    balance = 950_000L,
                    initialBalance = 500_000L,
                    sortIndex = 0,
                    updatedAt = 1760000000000L
                )
            ),
            transactions = emptyList()
        )

        val encoded = json.encodeToString(payload)
        val decoded = json.decodeFromString<BackupPayload>(encoded)

        assertEquals("E_WALLET", decoded.accounts[0].accountType)
    }
}
