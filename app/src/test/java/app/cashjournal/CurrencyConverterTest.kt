package app.cashjournal

import app.cashjournal.data.currency.CurrencyConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyConverterTest {

    private val mockRates = mapOf(
        "USD" to 1.0,
        "IDR" to 16000.0,
        "EUR" to 0.92,
        "JPY" to 155.0,
        "BHD" to 0.376
    )

    @Test
    fun testSameCurrencyReturnsIdenticalAmount() {
        val result = CurrencyConverter.convertMinorUnits(
            amountMinor = 500000L,
            sourceCode = "IDR",
            targetCode = "IDR",
            baseCode = "USD",
            rates = mockRates,
            sourceDecimals = 0,
            targetDecimals = 0
        )
        assertEquals(500000L, result)
    }

    @Test
    fun testBaseCurrencyToTargetWithDecimalScaling() {
        // $10.00 USD (1000 minor units, 2 decimals) -> IDR (0 decimals)
        // 10.00 * 16000 = 160,000 IDR
        val result = CurrencyConverter.convertMinorUnits(
            amountMinor = 1000L,
            sourceCode = "USD",
            targetCode = "IDR",
            baseCode = "USD",
            rates = mockRates,
            sourceDecimals = 2,
            targetDecimals = 0
        )
        assertEquals(160000L, result)
    }

    @Test
    fun testTargetToBaseWithDecimalScaling() {
        // Rp 160.000 IDR (160000 minor units, 0 decimals) -> USD (2 decimals)
        // 160000 / 16000 = $10.00 -> 1000 minor units
        val result = CurrencyConverter.convertMinorUnits(
            amountMinor = 160000L,
            sourceCode = "IDR",
            targetCode = "USD",
            baseCode = "USD",
            rates = mockRates,
            sourceDecimals = 0,
            targetDecimals = 2
        )
        assertEquals(1000L, result)
    }

    @Test
    fun testCrossCurrencyConversion() {
        // EUR (2 decimals) -> JPY (0 decimals)
        // 100 EUR = 10000 minor units
        // USD per EUR = 1 / 0.92, JPY per EUR = 155 / 0.92 = 168.478
        // 100 EUR = 16848 JPY
        val result = CurrencyConverter.convertMinorUnits(
            amountMinor = 10000L,
            sourceCode = "EUR",
            targetCode = "JPY",
            baseCode = "USD",
            rates = mockRates,
            sourceDecimals = 2,
            targetDecimals = 0
        )
        assertEquals(16848L, result)
    }

    @Test
    fun testZeroAmountReturnsZero() {
        val result = CurrencyConverter.convertMinorUnits(
            amountMinor = 0L,
            sourceCode = "USD",
            targetCode = "EUR",
            baseCode = "USD",
            rates = mockRates,
            sourceDecimals = 2,
            targetDecimals = 2
        )
        assertEquals(0L, result)
    }

    @Test
    fun testMissingRateReturnsOriginalAmount() {
        val result = CurrencyConverter.convertMinorUnits(
            amountMinor = 2500L,
            sourceCode = "UNKNOWN",
            targetCode = "USD",
            baseCode = "USD",
            rates = mockRates,
            sourceDecimals = 2,
            targetDecimals = 2
        )
        assertEquals(2500L, result)
    }
}
