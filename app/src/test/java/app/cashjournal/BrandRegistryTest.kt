package app.cashjournal

import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.AccountColor
import app.cashjournal.domain.model.AccountType
import app.cashjournal.domain.model.BrandRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrandRegistryTest {

    @Test
    fun matchBrand_exactAndMultiWordKeywords() {
        val bcaPrioritas = BrandRegistry.matchBrand("BCA Prioritas Executive")
        assertNotNull(bcaPrioritas)
        assertEquals("bca_prioritas", bcaPrioritas?.id)
        assertEquals("#B38E44", bcaPrioritas?.hexColor)
        assertEquals(AccountType.SAVINGS, bcaPrioritas?.defaultAccountType)

        val bcaRegular = BrandRegistry.matchBrand("Tabungan BCA Utama")
        assertNotNull(bcaRegular)
        assertEquals("bca", bcaRegular?.id)
        assertEquals("#0060AF", bcaRegular?.hexColor)
        assertEquals(AccountType.BANK_DEBIT, bcaRegular?.defaultAccountType)
    }

    @Test
    fun matchBrand_indonesianDigitalBanksAndWallets() {
        val jago = BrandRegistry.matchBrand("Kantong Utama Bank Jago")
        assertNotNull(jago)
        assertEquals("jago", jago?.id)
        assertEquals("#FF8A00", jago?.hexColor)

        val gopay = BrandRegistry.matchBrand("Saldo GoPay Aktif")
        assertNotNull(gopay)
        assertEquals("gopay", gopay?.id)
        assertEquals("#00AED6", gopay?.hexColor)
        assertEquals(AccountType.E_WALLET, gopay?.defaultAccountType)

        val ovo = BrandRegistry.matchBrand("OVO Cash")
        assertNotNull(ovo)
        assertEquals("ovo", ovo?.id)
        assertEquals("#4C3494", ovo?.hexColor)

        val dana = BrandRegistry.matchBrand("DANA Dompet")
        assertNotNull(dana)
        assertEquals("dana", dana?.id)
        assertEquals("#118EEA", dana?.hexColor)

        val shopee = BrandRegistry.matchBrand("ShopeePay Tabungan")
        assertNotNull(shopee)
        assertEquals("shopeepay", shopee?.id)
        assertEquals("#EE4D2D", shopee?.hexColor)
    }

    @Test
    fun matchBrand_globalInstitutions() {
        val amex = BrandRegistry.matchBrand("Amex Platinum Card")
        assertNotNull(amex)
        assertEquals("amex", amex?.id)
        assertEquals("#006FCF", amex?.hexColor)
        assertEquals(AccountType.CREDIT_CARD, amex?.defaultAccountType)

        val paypal = BrandRegistry.matchBrand("PayPal Business Account")
        assertNotNull(paypal)
        assertEquals("paypal", paypal?.id)
        assertEquals("#003087", paypal?.hexColor)
        assertEquals(AccountType.E_WALLET, paypal?.defaultAccountType)

        val apple = BrandRegistry.matchBrand("Apple Card")
        assertNotNull(apple)
        assertEquals("apple", apple?.id)
        assertEquals(AccountType.CREDIT_CARD, apple?.defaultAccountType)

        val chase = BrandRegistry.matchBrand("Chase Sapphire Checking")
        assertNotNull(chase)
        assertEquals("chase", chase?.id)
        assertEquals("#117AC9", chase?.hexColor)
    }

    @Test
    fun matchBrand_cashAndPhysicalWallets() {
        val cash = BrandRegistry.matchBrand("Dompet Fisik Harian")
        assertNotNull(cash)
        assertEquals("cash", cash?.id)
        assertEquals(AccountType.CASH, cash?.defaultAccountType)

        val pettyCash = BrandRegistry.matchBrand("Petty Cash Kantor")
        assertNotNull(pettyCash)
        assertEquals("cash", pettyCash?.id)
    }

    @Test
    fun matchBrand_strictWordBoundaryPreventsFalsePositives() {
        // "perdana" contains "dana" but should not trigger DANA e-wallet
        val perdana = BrandRegistry.matchBrand("Kartu Perdana Sim")
        assertNull(perdana)

        // "pemberian" contains "bri" but should not trigger BRI bank
        val pemberian = BrandRegistry.matchBrand("Pemberian Orang Tua")
        assertNull(pemberian)

        // "mandarin" contains "mandari" but should not trigger Mandiri
        val mandarin = BrandRegistry.matchBrand("Kursus Mandarin")
        assertNull(mandarin)
    }

    @Test
    fun matchBrand_handlesPunctuationAndShortStrings() {
        assertNull(BrandRegistry.matchBrand(""))
        assertNull(BrandRegistry.matchBrand("a"))
        assertNull(BrandRegistry.matchBrand("   "))

        val bcaPunctuation = BrandRegistry.matchBrand("(BCA) - Tabungan")
        assertNotNull(bcaPunctuation)
        assertEquals("bca", bcaPunctuation?.id)

        val gopayPunctuation = BrandRegistry.matchBrand("GoPay!")
        assertNotNull(gopayPunctuation)
        assertEquals("gopay", gopayPunctuation?.id)
    }

    @Test
    fun accountColor_fromKey_legacyAccentsBackwardCompatibility() {
        val cobalt = AccentKey.fromKey("COBALT")
        assertEquals("COBALT", cobalt.name)
        assertEquals("Cobalt", cobalt.displayName)

        val emerald = AccentKey.fromKey("EMERALD")
        assertEquals("EMERALD", emerald.name)

        val berry = AccentKey.fromKey("BERRY")
        assertEquals("BERRY", berry.name)
    }

    @Test
    fun accountColor_fromKey_brandIdLookup() {
        val bcaColor = AccentKey.fromKey("bca")
        assertEquals("#0060AF", bcaColor.key)
        assertEquals("BCA", bcaColor.displayName)

        val gopayColor = AccentKey.fromKey("gopay")
        assertEquals("#00AED6", gopayColor.key)
        assertEquals("GoPay", gopayColor.displayName)
    }

    @Test
    fun accountColor_fromKey_customHexStrings() {
        val hex1 = AccentKey.fromKey("#FF00AA")
        assertEquals("#FF00AA", hex1.key)
        assertEquals("#FF00AA", hex1.name)

        val hexWithoutHash = AccentKey.fromKey("FF00AA")
        assertEquals("#FF00AA", hexWithoutHash.key)

        val fallback = AccentKey.fromKey("not_a_valid_hex_or_brand")
        // Falls back to Cobalt
        assertEquals("COBALT", fallback.name)
    }

    @Test
    fun accountColor_curatedPaletteIntegrity() {
        assertEquals(16, AccountColor.curatedPalette.size)
        assertTrue(AccountColor.curatedPalette.any { it.displayName == "Charcoal" })
        assertTrue(AccountColor.curatedPalette.any { it.displayName == "Sapphire" })
        assertTrue(AccountColor.curatedPalette.any { it.displayName == "Emerald" })
    }
}
