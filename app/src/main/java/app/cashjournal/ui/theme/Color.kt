package app.cashjournal.ui.theme

import androidx.compose.ui.graphics.Color

object CashColors {
    // Primary Cobalt
    val Cobalt = Color(0xFF1D4ED8)
    val CobaltDark = Color(0xFF1E40AF)
    val CobaltLight = Color(0xFFEFF6FF)

    // Emerald
    val Emerald = Color(0xFF059669)
    val EmeraldDark = Color(0xFF065F46)
    val EmeraldLight = Color(0xFFECFDF5)

    // Persimmon
    val Persimmon = Color(0xFFEA580C)
    val PersimmonDark = Color(0xFFC2410C)
    val PersimmonLight = Color(0xFFFFF7ED)

    // Saffron
    val Saffron = Color(0xFFD97706)
    val SaffronDark = Color(0xFFB45309)
    val SaffronLight = Color(0xFFFFFBEB)

    // Iris
    val Iris = Color(0xFF6366F1)
    val IrisDark = Color(0xFF4338CA)
    val IrisLight = Color(0xFFEEF2FF)

    // Berry
    val Berry = Color(0xFFE11D48)
    val BerryDark = Color(0xFFBE123C)
    val BerryLight = Color(0xFFFFF1F2)

    // Charcoal & Text
    val Charcoal = Color(0xFF0F172A)
    val TextMuted = Color(0xFF475569)
    val TextSubtle = Color(0xFF94A3B8)

    // Slate Scale
    val Slate50 = Color(0xFFF8FAFC)
    val Slate100 = Color(0xFFF1F5F9)
    val Slate200 = Color(0xFFE2E8F0)
    val Slate300 = Color(0xFFCBD5E1)
    val Slate400 = Color(0xFF94A3B8)
    val Slate500 = Color(0xFF64748B)
    val Slate600 = Color(0xFF475569)
    val Slate800 = Color(0xFF1E293B)

    // Status
    val Recalibrate = Color(0xFFF87171)

    // Glass & Surfaces
    val GlassCardBg = Color(0xC7FFFFFF) // 78% White
    val GlassCardSubtleBg = Color(0x94FFFFFF) // 58% White
    val SideRailBg = Color(0xE0FFFFFF) // 88% White
    val GlassBorder = Color(0xF2FFFFFF) // 95% White
    val SheetBg = Color(0xF2FFFFFF) // 95% White
    val Scrim = Color(0x4D0F172A) // 30% Charcoal

    // Pure White & Black
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    // Accent mapping by key
    fun getAccent(key: String): Color = when (key.lowercase()) {
        "cobalt" -> Cobalt
        "emerald" -> Emerald
        "persimmon" -> Persimmon
        "saffron" -> Saffron
        "iris" -> Iris
        "berry" -> Berry
        else -> Cobalt
    }

    fun getAccentLight(key: String): Color = when (key.lowercase()) {
        "cobalt" -> CobaltLight
        "emerald" -> EmeraldLight
        "persimmon" -> PersimmonLight
        "saffron" -> SaffronLight
        "iris" -> IrisLight
        "berry" -> BerryLight
        else -> CobaltLight
    }

    fun getAccentDark(key: String): Color = when (key.lowercase()) {
        "cobalt" -> CobaltDark
        "emerald" -> EmeraldDark
        "persimmon" -> PersimmonDark
        "saffron" -> SaffronDark
        "iris" -> IrisDark
        "berry" -> BerryDark
        else -> CobaltDark
    }
}

val Cobalt = CashColors.Cobalt
val CobaltDark = CashColors.CobaltDark
val CobaltLight = CashColors.CobaltLight
val Emerald = CashColors.Emerald
val EmeraldDark = CashColors.EmeraldDark
val EmeraldLight = CashColors.EmeraldLight
val Persimmon = CashColors.Persimmon
val PersimmonDark = CashColors.PersimmonDark
val PersimmonLight = CashColors.PersimmonLight
val Saffron = CashColors.Saffron
val SaffronDark = CashColors.SaffronDark
val SaffronLight = CashColors.SaffronLight
val Iris = CashColors.Iris
val IrisDark = CashColors.IrisDark
val IrisLight = CashColors.IrisLight
val Berry = CashColors.Berry
val BerryDark = CashColors.BerryDark
val BerryLight = CashColors.BerryLight
val Charcoal = CashColors.Charcoal
val Slate50 = CashColors.Slate50
val Slate100 = CashColors.Slate100
val Slate200 = CashColors.Slate200
val Slate300 = CashColors.Slate300
val Slate400 = CashColors.Slate400
val Slate500 = CashColors.Slate500
val Slate600 = CashColors.Slate600
val Slate800 = CashColors.Slate800
val Recalibrate = CashColors.Recalibrate
val GlassCardBackground = CashColors.GlassCardBg
val SubtleCardBackground = CashColors.GlassCardSubtleBg
val SideRailBackground = CashColors.SideRailBg
val GlassBorder = CashColors.GlassBorder
val SheetBackground = CashColors.SheetBg
val ScrimColor = CashColors.Scrim

