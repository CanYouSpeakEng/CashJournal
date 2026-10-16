package app.cashjournal

import app.cashjournal.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryDetectorTest {

    @Test
    fun testFoodAndDiningKeywords() {
        assertEquals(Category.FOOD, Category.detectFromNote("Morning coffee at cafe"))
        assertEquals(Category.FOOD, Category.detectFromNote("Beli kopi kenangan"))
        assertEquals(Category.FOOD, Category.detectFromNote("Makan siang Padang"))
        assertEquals(Category.FOOD, Category.detectFromNote("Delicious dinner with team"))
        assertEquals(Category.FOOD, Category.detectFromNote("Mie ayam baso"))
    }

    @Test
    fun testTransportKeywords() {
        assertEquals(Category.TRANSPORT, Category.detectFromNote("Gojek to office"))
        assertEquals(Category.TRANSPORT, Category.detectFromNote("Grabcar airport"))
        assertEquals(Category.TRANSPORT, Category.detectFromNote("Isi bensin pertamax"))
        assertEquals(Category.TRANSPORT, Category.detectFromNote("Subway train ticket"))
        assertEquals(Category.TRANSPORT, Category.detectFromNote("Bayar tol cikampek"))
        assertEquals(Category.TRANSPORT, Category.detectFromNote("Mall parking fee"))
    }

    @Test
    fun testShoppingKeywords() {
        assertEquals(Category.SHOPPING, Category.detectFromNote("Tokopedia checkout"))
        assertEquals(Category.SHOPPING, Category.detectFromNote("Shopee order"))
        assertEquals(Category.SHOPPING, Category.detectFromNote("Beli baju baru"))
        assertEquals(Category.SHOPPING, Category.detectFromNote("Running shoes"))
    }

    @Test
    fun testUtilitiesKeywords() {
        assertEquals(Category.UTILITIES, Category.detectFromNote("Token listrik PLN"))
        assertEquals(Category.UTILITIES, Category.detectFromNote("Tagihan air PDAM"))
        assertEquals(Category.UTILITIES, Category.detectFromNote("Home wifi internet"))
        assertEquals(Category.UTILITIES, Category.detectFromNote("Beli pulsa telkomsel"))
        assertEquals(Category.UTILITIES, Category.detectFromNote("Monthly subscription"))
    }

    @Test
    fun testHousingKeywords() {
        assertEquals(Category.HOUSING, Category.detectFromNote("Monthly rent payment"))
        assertEquals(Category.HOUSING, Category.detectFromNote("Bayar kosan"))
        assertEquals(Category.HOUSING, Category.detectFromNote("Cicilan kontrakan"))
        assertEquals(Category.HOUSING, Category.detectFromNote("Apartment maintenance"))
    }

    @Test
    fun testInvestmentKeywords() {
        assertEquals(Category.INVESTMENT, Category.detectFromNote("Beli reksadana Bibit"))
        assertEquals(Category.INVESTMENT, Category.detectFromNote("Top up tabungan emas"))
        assertEquals(Category.INVESTMENT, Category.detectFromNote("Tech stock portfolio"))
    }

    @Test
    fun testSalaryKeywords() {
        assertEquals(Category.SALARY, Category.detectFromNote("Monthly tech salary"))
        assertEquals(Category.SALARY, Category.detectFromNote("Transfer gaji pokok"))
        assertEquals(Category.SALARY, Category.detectFromNote("Year-end performance bonus"))
        assertEquals(Category.SALARY, Category.detectFromNote("Freelance design project"))
    }

    @Test
    fun testUnmatchedFallbackToOther() {
        assertEquals(Category.OTHER, Category.detectFromNote("Random miscellaneous note"))
        assertEquals(Category.OTHER, Category.detectFromNote(""))
        assertEquals(Category.OTHER, Category.detectFromNote("   "))
    }

    @Test
    fun testWordBoundaryEliminatesFalseSubstrings() {
        // "Movie theater" must detect as ENTERTAINMENT via "movie", NOT FOOD via substring "eat" in "theater"
        assertEquals(Category.ENTERTAINMENT, Category.detectFromNote("Movie theater"))

        // "New sweater" must NOT detect as FOOD via substring "eat" in "sweater"
        assertEquals(Category.OTHER, Category.detectFromNote("New sweater"))

        // "Office chair" must NOT detect as UTILITIES via substring "air" in "chair"
        assertEquals(Category.OTHER, Category.detectFromNote("Office chair"))

        // "Car repair" must NOT detect as UTILITIES via substring "air" in "repair"
        assertEquals(Category.OTHER, Category.detectFromNote("Car repair"))

        // "Cost of shoes" must detect as SHOPPING via "shoes", NOT HOUSING via substring "kos" in "cost"
        assertEquals(Category.SHOPPING, Category.detectFromNote("Cost of shoes"))
    }
}
