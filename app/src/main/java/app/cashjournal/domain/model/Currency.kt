package app.cashjournal.domain.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val decimalDigits: Int,
    val flag: String = "" // Pure code identifier, NO emoji
) {
    val isZeroDecimal: Boolean get() = decimalDigits == 0
    val keypadZeroKey: String get() = if (isZeroDecimal) "000" else "00"

    companion object {
        val IDR = Currency("IDR", "Indonesian Rupiah", "Rp", 0)
        val USD = Currency("USD", "US Dollar", "US$", 2)
        val EUR = Currency("EUR", "Euro", "€", 2)
        val GBP = Currency("GBP", "British Pound", "£", 2)
        val SGD = Currency("SGD", "Singapore Dollar", "S$", 2)
        val JPY = Currency("JPY", "Japanese Yen", "¥", 0)
        val AUD = Currency("AUD", "Australian Dollar", "A$", 2)
        val MYR = Currency("MYR", "Malaysian Ringgit", "RM", 2)

        val ALL_CURRENCIES = listOf(IDR, USD, EUR, GBP, SGD, JPY, AUD, MYR)

        fun findByCode(code: String): Currency {
            return ALL_CURRENCIES.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: IDR
        }

        /**
         * Formats minor units into localized string.
         * For IDR, uses dots for thousands and no decimal: Rp 12.500.000
         * For USD, uses dots for thousands and comma for decimal in Indonesian locale,
         * or commas and dots in English locale.
         */
        fun formatMinor(
            minorUnits: Long,
            currencyCode: String = "IDR",
            locale: Locale = Locale("id", "ID"),
            includeSymbol: Boolean = true,
            isNegativeExplicit: Boolean = false
        ): String {
            val currency = findByCode(currencyCode)
            val isNegative = minorUnits < 0 || isNegativeExplicit
            val absMinor = abs(minorUnits)

            val symbols = DecimalFormatSymbols(locale)
            val pattern = if (currency.isZeroDecimal) {
                "#,##0"
            } else {
                "#,##0." + "0".repeat(currency.decimalDigits)
            }
            val formatter = DecimalFormat(pattern, symbols)

            val amountValue = if (currency.isZeroDecimal) {
                absMinor.toDouble()
            } else {
                absMinor.toDouble() / Math.pow(10.0, currency.decimalDigits.toDouble())
            }

            val formattedNumber = formatter.format(amountValue)
            val minusPrefix = if (isNegative) "−" else "" // U+2212 minus sign

            return if (includeSymbol) {
                "$minusPrefix${currency.symbol} $formattedNumber"
            } else {
                "$minusPrefix$formattedNumber"
            }
        }
    }
}
