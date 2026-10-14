package app.cashjournal.domain.model

data class Country(
    val code: String,
    val nameEn: String,
    val nameIn: String,
    val currencyCode: String,
    val currencyNameEn: String,
    val currencyNameIn: String
) {
    fun localizedName(isIndonesian: Boolean): String = if (isIndonesian) nameIn else nameEn
    fun localizedCurrencyName(isIndonesian: Boolean): String = if (isIndonesian) currencyNameIn else currencyNameEn

    companion object {
        val COUNTRIES = listOf(
            Country("ID", "Indonesia", "Indonesia", "IDR", "Indonesian Rupiah", "Rupiah Indonesia"),
            Country("US", "United States", "Amerika Serikat", "USD", "US Dollar", "Dolar AS"),
            Country("EU", "European Union", "Uni Eropa", "EUR", "Euro", "Euro"),
            Country("GB", "United Kingdom", "Inggris Raya", "GBP", "British Pound", "Pound Sterling"),
            Country("SG", "Singapore", "Singapura", "SGD", "Singapore Dollar", "Dolar Singapura"),
            Country("JP", "Japan", "Jepang", "JPY", "Japanese Yen", "Yen Jepang"),
            Country("AU", "Australia", "Australia", "AUD", "Australian Dollar", "Dolar Australia"),
            Country("MY", "Malaysia", "Malaysia", "MYR", "Malaysian Ringgit", "Ringgit Malaysia")
        )

        fun findByCode(code: String): Country {
            return COUNTRIES.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: COUNTRIES[0]
        }
    }
}
