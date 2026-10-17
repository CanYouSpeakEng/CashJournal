package app.cashjournal.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Bank
import com.adamglin.phosphoricons.regular.ChartLineUp
import com.adamglin.phosphoricons.regular.CreditCard
import com.adamglin.phosphoricons.regular.DeviceMobile
import com.adamglin.phosphoricons.regular.Vault
import com.adamglin.phosphoricons.regular.Wallet

enum class AccountType(
    val titleEn: String,
    val titleIn: String,
    val placeholderEn: String,
    val placeholderIn: String
) {
    CASH(
        titleEn = "Cash / Wallet",
        titleIn = "Tunai / Dompet",
        placeholderEn = "e.g. Physical Wallet, Pocket Cash",
        placeholderIn = "cth. Dompet Fisik, Uang Saku"
    ),
    BANK_DEBIT(
        titleEn = "Bank / Debit",
        titleIn = "Bank / Debit",
        placeholderEn = "e.g. BCA Checking, Main Bank",
        placeholderIn = "cth. Rekening BCA, Bank Mandiri"
    ),
    E_WALLET(
        titleEn = "E-Wallet",
        titleIn = "E-Wallet",
        placeholderEn = "e.g. GoPay, OVO, DANA",
        placeholderIn = "cth. GoPay, OVO, DANA, ShopeePay"
    ),
    CREDIT_CARD(
        titleEn = "Credit Card",
        titleIn = "Kartu Kredit",
        placeholderEn = "e.g. Visa Platinum, Everyday Card",
        placeholderIn = "cth. Kartu Kredit BCA, Mandiri Visa"
    ),
    SAVINGS(
        titleEn = "Savings / Emergency",
        titleIn = "Tabungan / Dana Darurat",
        placeholderEn = "e.g. Emergency Fund, High-Yield Vault",
        placeholderIn = "cth. Dana Darurat, Kantong Tabungan"
    ),
    INVESTMENT(
        titleEn = "Investment Holdings",
        titleIn = "Investasi / Portofolio",
        placeholderEn = "e.g. Gold Antam, Bibit Reksadana",
        placeholderIn = "cth. Emas Antam, Bibit Reksadana"
    );

    val icon: ImageVector
        get() = when (this) {
            CASH -> PhosphorIcons.Regular.Wallet
            BANK_DEBIT -> PhosphorIcons.Regular.Bank
            E_WALLET -> PhosphorIcons.Regular.DeviceMobile
            CREDIT_CARD -> PhosphorIcons.Regular.CreditCard
            SAVINGS -> PhosphorIcons.Regular.Vault
            INVESTMENT -> PhosphorIcons.Regular.ChartLineUp
        }

    fun localizedTitle(languageCode: String): String =
        if (languageCode.equals("in", ignoreCase = true)) titleIn else titleEn

    fun localizedPlaceholder(languageCode: String): String =
        if (languageCode.equals("in", ignoreCase = true)) placeholderIn else placeholderEn

    companion object {
        fun fromKey(key: String?): AccountType = when (key?.uppercase()) {
            "CASH", "WALLET" -> CASH
            "BANK_DEBIT", "DEBIT", "BANK" -> BANK_DEBIT
            "E_WALLET", "EWALLET", "DIGITAL_WALLET" -> E_WALLET
            "CREDIT_CARD", "CREDIT" -> CREDIT_CARD
            "SAVINGS", "INVESTMENT", "INVEST" -> SAVINGS
            else -> BANK_DEBIT
        }

        fun inferFromName(name: String): AccountType {
            val lower = name.lowercase()
            val tokens = lower.split(Regex("[\\s-_/]+"))
            return when {
                lower.contains("cash") || lower.contains("tunai") || lower.contains("dompet") ||
                    lower.contains("pocket") || lower.contains("saku") || lower.contains("wallet") -> CASH
                lower.contains("gopay") || lower.contains("ovo") || lower.contains("dana") ||
                    lower.contains("shopeepay") || lower.contains("linkaja") || lower.contains("ewallet") ||
                    lower.contains("e-wallet") || lower.contains("paypal") -> E_WALLET
                lower.contains("credit") || lower.contains("kredit") || tokens.contains("cc") -> CREDIT_CARD
                lower.contains("saving") || lower.contains("tabungan") || lower.contains("invest") ||
                    lower.contains("bibit") || lower.contains("deposito") || lower.contains("darurat") ||
                    lower.contains("vault") || lower.contains("emergency") || lower.contains("fund") -> SAVINGS
                else -> BANK_DEBIT
            }
        }
    }
}
