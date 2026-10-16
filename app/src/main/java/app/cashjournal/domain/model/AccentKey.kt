package app.cashjournal.domain.model

import androidx.compose.ui.graphics.Color
import app.cashjournal.ui.theme.Berry
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Iris
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.Saffron

data class AccountColor(
    val key: String,
    val color: Color,
    val tint: Color = color.copy(alpha = 0.12f),
    val displayName: String = key
) {
    val name: String get() = key

    companion object {
        // Original 6 foundational accents for 100% backward compatibility
        val COBALT = AccountColor("COBALT", Cobalt, Color(0x1A1B4DFF), "Cobalt")
        val EMERALD = AccountColor("EMERALD", Emerald, Color(0x1A00875A), "Emerald")
        val PERSIMMON = AccountColor("PERSIMMON", Persimmon, Color(0x1AD34508), "Persimmon")
        val SAFFRON = AccountColor("SAFFRON", Saffron, Color(0x1AB86200), "Saffron")
        val IRIS = AccountColor("IRIS", Iris, Color(0x1A5B5BD6), "Iris")
        val BERRY = AccountColor("BERRY", Berry, Color(0x1ABA1A1A), "Berry")

        // Curated modern fintech palette (16 colors)
        val CHARCOAL_TITANIUM = AccountColor("CHARCOAL_TITANIUM", Color(0xFF1E293B), displayName = "Charcoal")
        val SLATE_STEEL = AccountColor("SLATE_STEEL", Color(0xFF475569), displayName = "Slate")
        val DEEP_NAVY = AccountColor("DEEP_NAVY", Color(0xFF0F172A), displayName = "Deep Navy")
        val COBALT_SAPPHIRE = AccountColor("COBALT_SAPPHIRE", Color(0xFF1D4ED8), displayName = "Sapphire")
        val AZURE_SKY = AccountColor("AZURE_SKY", Color(0xFF0284C7), displayName = "Azure")
        val CYAN_TEAL = AccountColor("CYAN_TEAL", Color(0xFF0D9488), displayName = "Teal")
        val MINT_FOREST = AccountColor("MINT_FOREST", Color(0xFF059669), displayName = "Mint")
        val EMERALD_CURRENCY = AccountColor("EMERALD_CURRENCY", Color(0xFF10B981), displayName = "Emerald")
        val LIME_ENERGY = AccountColor("LIME_ENERGY", Color(0xFF65A30D), displayName = "Lime")
        val AMBER_GOLD = AccountColor("AMBER_GOLD", Color(0xFFD97706), displayName = "Amber")
        val TANGERINE_ORANGE = AccountColor("TANGERINE_ORANGE", Color(0xFFEA580C), displayName = "Tangerine")
        val PERSIMMON_CORAL = AccountColor("PERSIMMON_CORAL", Color(0xFFDC2626), displayName = "Coral")
        val ROSE_RUBY = AccountColor("ROSE_RUBY", Color(0xFFE11D48), displayName = "Ruby")
        val BERRY_FUCHSIA = AccountColor("BERRY_FUCHSIA", Color(0xFFC026D3), displayName = "Fuchsia")
        val VIOLET_PURPLE = AccountColor("VIOLET_PURPLE", Color(0xFF7C3AED), displayName = "Violet")
        val INDIGO_NIGHT = AccountColor("INDIGO_NIGHT", Color(0xFF4338CA), displayName = "Indigo")

        val curatedPalette: List<AccountColor> = listOf(
            CHARCOAL_TITANIUM,
            SLATE_STEEL,
            DEEP_NAVY,
            COBALT_SAPPHIRE,
            AZURE_SKY,
            CYAN_TEAL,
            MINT_FOREST,
            EMERALD_CURRENCY,
            LIME_ENERGY,
            AMBER_GOLD,
            TANGERINE_ORANGE,
            PERSIMMON_CORAL,
            ROSE_RUBY,
            BERRY_FUCHSIA,
            VIOLET_PURPLE,
            INDIGO_NIGHT
        )

        val entries: List<AccountColor> = listOf(
            COBALT,
            EMERALD,
            PERSIMMON,
            SAFFRON,
            IRIS,
            BERRY
        )

        fun fromHex(hexString: String, name: String? = null): AccountColor {
            val cleanHex = hexString.trim().removePrefix("#")
            if (cleanHex.length != 6 && cleanHex.length != 8) {
                return COBALT
            }
            return try {
                val colorLong = cleanHex.toLong(16)
                val colorInt = if (cleanHex.length == 6) {
                    (0xFF000000L or colorLong).toInt()
                } else {
                    colorLong.toInt()
                }
                val color = Color(colorInt)
                val formattedHex = "#${cleanHex.uppercase()}"
                AccountColor(
                    key = formattedHex,
                    color = color,
                    tint = color.copy(alpha = 0.12f),
                    displayName = name ?: formattedHex
                )
            } catch (e: Exception) {
                COBALT
            }
        }

        fun fromKey(key: String?): AccountColor {
            if (key == null) return COBALT
            val trimmed = key.trim()
            if (trimmed.startsWith("#")) {
                return fromHex(trimmed)
            }
            return when (trimmed.uppercase()) {
                "COBALT" -> COBALT
                "EMERALD" -> EMERALD
                "PERSIMMON" -> PERSIMMON
                "SAFFRON" -> SAFFRON
                "IRIS" -> IRIS
                "BERRY" -> BERRY
                "CHARCOAL_TITANIUM" -> CHARCOAL_TITANIUM
                "SLATE_STEEL" -> SLATE_STEEL
                "DEEP_NAVY" -> DEEP_NAVY
                "COBALT_SAPPHIRE" -> COBALT_SAPPHIRE
                "AZURE_SKY" -> AZURE_SKY
                "CYAN_TEAL" -> CYAN_TEAL
                "MINT_FOREST" -> MINT_FOREST
                "EMERALD_CURRENCY" -> EMERALD_CURRENCY
                "LIME_ENERGY" -> LIME_ENERGY
                "AMBER_GOLD" -> AMBER_GOLD
                "TANGERINE_ORANGE" -> TANGERINE_ORANGE
                "PERSIMMON_CORAL" -> PERSIMMON_CORAL
                "ROSE_RUBY" -> ROSE_RUBY
                "BERRY_FUCHSIA" -> BERRY_FUCHSIA
                "VIOLET_PURPLE" -> VIOLET_PURPLE
                "INDIGO_NIGHT" -> INDIGO_NIGHT
                else -> {
                    val brand = BrandRegistry.findById(trimmed)
                    brand?.accountColor ?: fromHex(trimmed)
                }
            }
        }
    }
}

typealias AccentKey = AccountColor
