package app.cashjournal.domain.model

import androidx.compose.ui.graphics.Color
import app.cashjournal.ui.theme.Berry
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Iris
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.Saffron

enum class AccentKey(val color: Color, val tint: Color) {
    COBALT(Cobalt, Color(0x1A1B4DFF)),
    EMERALD(Emerald, Color(0x1A00875A)),
    PERSIMMON(Persimmon, Color(0x1AD34508)),
    SAFFRON(Saffron, Color(0x1AB86200)),
    IRIS(Iris, Color(0x1A5B5BD6)),
    BERRY(Berry, Color(0x1ABA1A1A));

    companion object {
        fun fromKey(key: String?): AccentKey {
            return when (key?.lowercase()) {
                "cobalt" -> COBALT
                "emerald" -> EMERALD
                "persimmon" -> PERSIMMON
                "saffron" -> SAFFRON
                "iris" -> IRIS
                "berry" -> BERRY
                else -> COBALT
            }
        }
    }
}
