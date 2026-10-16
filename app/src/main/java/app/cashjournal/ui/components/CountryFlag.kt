package app.cashjournal.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pixel-perfect, zero-dependency, open-source vector country flag renderer
 * for the 16 supported world currencies in CashJournal.
 * Formatted within squircle containers with subtle border.
 */
@Composable
fun CountryFlag(
    currencyCode: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    cornerRadius: Dp = 6.dp
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .border(0.75.dp, Color(0x1F000000), shape)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            when (currencyCode.uppercase()) {
                "IDR" -> drawIndonesiaFlag()
                "USD" -> drawUsFlag()
                "EUR" -> drawEuFlag()
                "GBP" -> drawGbpFlag()
                "SGD" -> drawSingaporeFlag()
                "JPY" -> drawJapanFlag()
                "AUD" -> drawAustraliaFlag()
                "MYR" -> drawMalaysiaFlag()
                "SAR" -> drawSaudiFlag()
                "CNY" -> drawChinaFlag()
                "CAD" -> drawCanadaFlag()
                "CHF" -> drawSwissFlag()
                "HKD" -> drawHongKongFlag()
                "NZD" -> drawNewZealandFlag()
                "KRW" -> drawKoreaFlag()
                "THB" -> drawThailandFlag()
                else -> drawFallbackFlag(currencyCode)
            }
        }
    }
}

private fun DrawScope.drawIndonesiaFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color(0xFFE11D48), topLeft = Offset.Zero, size = Size(w, h / 2f))
    drawRect(Color.White, topLeft = Offset(0f, h / 2f), size = Size(w, h / 2f))
}

private fun DrawScope.drawJapanFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color.White, topLeft = Offset.Zero, size = size)
    drawCircle(
        color = Color(0xFFBC002D),
        radius = h * 0.30f,
        center = Offset(w / 2f, h / 2f)
    )
}

private fun DrawScope.drawThailandFlag() {
    val h = size.height
    val w = size.width
    val stripe = h / 6f
    drawRect(Color(0xFFA51931), topLeft = Offset.Zero, size = Size(w, stripe))
    drawRect(Color.White, topLeft = Offset(0f, stripe), size = Size(w, stripe))
    drawRect(Color(0xFF2D2A4A), topLeft = Offset(0f, stripe * 2f), size = Size(w, stripe * 2f))
    drawRect(Color.White, topLeft = Offset(0f, stripe * 4f), size = Size(w, stripe))
    drawRect(Color(0xFFA51931), topLeft = Offset(0f, stripe * 5f), size = Size(w, stripe))
}

private fun DrawScope.drawSwissFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color(0xFFD52B1E), topLeft = Offset.Zero, size = size)
    val armW = w * 0.22f
    val armL = h * 0.62f
    // Vertical arm
    drawRect(
        Color.White,
        topLeft = Offset((w - armW) / 2f, (h - armL) / 2f),
        size = Size(armW, armL)
    )
    // Horizontal arm
    drawRect(
        Color.White,
        topLeft = Offset((w - armL) / 2f, (h - armW) / 2f),
        size = Size(armL, armW)
    )
}

private fun DrawScope.drawEuFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color(0xFF003399), topLeft = Offset.Zero, size = size)
    val center = Offset(w / 2f, h / 2f)
    val ringR = h * 0.32f
    val starR = h * 0.045f
    val gold = Color(0xFFFFCC00)
    for (i in 0 until 12) {
        val angle = (i * 30.0 - 90.0) * PI / 180.0
        val cx = center.x + (ringR * cos(angle)).toFloat()
        val cy = center.y + (ringR * sin(angle)).toFloat()
        drawCircle(gold, radius = starR, center = Offset(cx, cy))
    }
}

private fun DrawScope.drawUsFlag() {
    val h = size.height
    val w = size.width
    val stripeH = h / 7f
    for (i in 0 until 7) {
        val color = if (i % 2 == 0) Color(0xFFB22234) else Color.White
        drawRect(color, topLeft = Offset(0f, i * stripeH), size = Size(w, stripeH))
    }
    // Canton
    val cantonW = w * 0.46f
    val cantonH = stripeH * 4f
    drawRect(Color(0xFF3C3B6E), topLeft = Offset.Zero, size = Size(cantonW, cantonH))
    // Small star dot
    drawCircle(Color.White, radius = cantonH * 0.16f, center = Offset(cantonW / 2f, cantonH / 2f))
}

private fun DrawScope.drawGbpFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color(0xFF012169), topLeft = Offset.Zero, size = size)
    // Diagonal white cross
    drawLine(Color.White, Offset(0f, 0f), Offset(w, h), strokeWidth = w * 0.18f)
    drawLine(Color.White, Offset(w, 0f), Offset(0f, h), strokeWidth = w * 0.18f)
    // Diagonal red cross
    drawLine(Color(0xFFC8102E), Offset(0f, 0f), Offset(w, h), strokeWidth = w * 0.08f)
    drawLine(Color(0xFFC8102E), Offset(w, 0f), Offset(0f, h), strokeWidth = w * 0.08f)
    // St George white cross
    drawRect(Color.White, topLeft = Offset((w - w * 0.28f) / 2f, 0f), size = Size(w * 0.28f, h))
    drawRect(Color.White, topLeft = Offset(0f, (h - h * 0.28f) / 2f), size = Size(w, h * 0.28f))
    // St George red cross
    drawRect(Color(0xFFC8102E), topLeft = Offset((w - w * 0.16f) / 2f, 0f), size = Size(w * 0.16f, h))
    drawRect(Color(0xFFC8102E), topLeft = Offset(0f, (h - h * 0.16f) / 2f), size = Size(w, h * 0.16f))
}

private fun DrawScope.drawSingaporeFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color(0xFFED2939), topLeft = Offset.Zero, size = Size(w, h / 2f))
    drawRect(Color.White, topLeft = Offset(0f, h / 2f), size = Size(w, h / 2f))
    // Crescent & stars
    drawCircle(Color.White, radius = h * 0.15f, center = Offset(w * 0.22f, h * 0.25f))
    drawCircle(Color(0xFFED2939), radius = h * 0.12f, center = Offset(w * 0.26f, h * 0.25f))
    drawCircle(Color.White, radius = h * 0.035f, center = Offset(w * 0.32f, h * 0.25f))
}

private fun DrawScope.drawMalaysiaFlag() {
    val h = size.height
    val w = size.width
    val stripeH = h / 8f
    for (i in 0 until 8) {
        val color = if (i % 2 == 0) Color(0xFFCC0000) else Color.White
        drawRect(color, topLeft = Offset(0f, i * stripeH), size = Size(w, stripeH))
    }
    // Blue canton
    val cantonW = w * 0.48f
    val cantonH = stripeH * 5f
    drawRect(Color(0xFF000066), topLeft = Offset.Zero, size = Size(cantonW, cantonH))
    // Yellow crescent & star
    val gold = Color(0xFFFFCC00)
    drawCircle(gold, radius = cantonH * 0.32f, center = Offset(cantonW * 0.42f, cantonH * 0.5f))
    drawCircle(Color(0xFF000066), radius = cantonH * 0.26f, center = Offset(cantonW * 0.49f, cantonH * 0.5f))
    drawCircle(gold, radius = cantonH * 0.14f, center = Offset(cantonW * 0.70f, cantonH * 0.5f))
}

private fun DrawScope.drawSaudiFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color(0xFF006C35), topLeft = Offset.Zero, size = size)
    // White emblem sword line
    drawLine(
        Color.White,
        start = Offset(w * 0.2f, h * 0.65f),
        end = Offset(w * 0.8f, h * 0.65f),
        strokeWidth = h * 0.08f
    )
    // Central inscription bar
    drawRoundRect(
        Color.White,
        topLeft = Offset(w * 0.25f, h * 0.30f),
        size = Size(w * 0.50f, h * 0.18f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.05f, h * 0.05f)
    )
}

private fun DrawScope.drawChinaFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color(0xFFDE2910), topLeft = Offset.Zero, size = size)
    val gold = Color(0xFFFFDE00)
    // Big star
    drawCircle(gold, radius = h * 0.16f, center = Offset(w * 0.24f, h * 0.32f))
    // Small stars
    drawCircle(gold, radius = h * 0.045f, center = Offset(w * 0.42f, h * 0.18f))
    drawCircle(gold, radius = h * 0.045f, center = Offset(w * 0.48f, h * 0.28f))
    drawCircle(gold, radius = h * 0.045f, center = Offset(w * 0.48f, h * 0.42f))
    drawCircle(gold, radius = h * 0.045f, center = Offset(w * 0.42f, h * 0.52f))
}

private fun DrawScope.drawCanadaFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color(0xFFFF0000), topLeft = Offset.Zero, size = Size(w * 0.25f, h))
    drawRect(Color.White, topLeft = Offset(w * 0.25f, 0f), size = Size(w * 0.50f, h))
    drawRect(Color(0xFFFF0000), topLeft = Offset(w * 0.75f, 0f), size = Size(w * 0.25f, h))
    // Center red stylized maple emblem
    drawCircle(Color(0xFFFF0000), radius = h * 0.22f, center = Offset(w * 0.5f, h * 0.5f))
    val stem = Path().apply {
        moveTo(w * 0.5f, h * 0.35f)
        lineTo(w * 0.44f, h * 0.65f)
        lineTo(w * 0.56f, h * 0.65f)
        close()
    }
    drawPath(stem, Color.White)
}

private fun DrawScope.drawHongKongFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color(0xFFDE2910), topLeft = Offset.Zero, size = size)
    // Bauhinia white flower circle
    drawCircle(Color.White, radius = h * 0.24f, center = Offset(w / 2f, h / 2f))
    drawCircle(Color(0xFFDE2910), radius = h * 0.12f, center = Offset(w / 2f, h / 2f))
}

private fun DrawScope.drawNewZealandFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color(0xFF00247D), topLeft = Offset.Zero, size = size)
    // Small canton
    drawRect(Color(0xFF012169), topLeft = Offset.Zero, size = Size(w * 0.45f, h * 0.45f))
    drawLine(Color.White, Offset(0f, 0f), Offset(w * 0.45f, h * 0.45f), strokeWidth = w * 0.06f)
    drawLine(Color(0xFFCC0000), Offset(0f, 0f), Offset(w * 0.45f, h * 0.45f), strokeWidth = w * 0.03f)
    // 4 red stars with white borders
    val red = Color(0xFFCC0000)
    drawCircle(Color.White, radius = h * 0.07f, center = Offset(w * 0.75f, h * 0.25f))
    drawCircle(red, radius = h * 0.05f, center = Offset(w * 0.75f, h * 0.25f))
    drawCircle(Color.White, radius = h * 0.07f, center = Offset(w * 0.85f, h * 0.50f))
    drawCircle(red, radius = h * 0.05f, center = Offset(w * 0.85f, h * 0.50f))
    drawCircle(Color.White, radius = h * 0.07f, center = Offset(w * 0.75f, h * 0.75f))
    drawCircle(red, radius = h * 0.05f, center = Offset(w * 0.75f, h * 0.75f))
    drawCircle(Color.White, radius = h * 0.06f, center = Offset(w * 0.65f, h * 0.55f))
    drawCircle(red, radius = h * 0.04f, center = Offset(w * 0.65f, h * 0.55f))
}

private fun DrawScope.drawAustraliaFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color(0xFF00008B), topLeft = Offset.Zero, size = size)
    // Small canton
    drawRect(Color(0xFF012169), topLeft = Offset.Zero, size = Size(w * 0.45f, h * 0.45f))
    drawLine(Color.White, Offset(0f, 0f), Offset(w * 0.45f, h * 0.45f), strokeWidth = w * 0.06f)
    drawLine(Color(0xFFCC0000), Offset(0f, 0f), Offset(w * 0.45f, h * 0.45f), strokeWidth = w * 0.03f)
    // Commonwealth star + Southern Cross
    drawCircle(Color.White, radius = h * 0.09f, center = Offset(w * 0.24f, h * 0.72f))
    drawCircle(Color.White, radius = h * 0.05f, center = Offset(w * 0.75f, h * 0.25f))
    drawCircle(Color.White, radius = h * 0.05f, center = Offset(w * 0.85f, h * 0.45f))
    drawCircle(Color.White, radius = h * 0.05f, center = Offset(w * 0.75f, h * 0.75f))
    drawCircle(Color.White, radius = h * 0.05f, center = Offset(w * 0.65f, h * 0.55f))
}

private fun DrawScope.drawKoreaFlag() {
    val h = size.height
    val w = size.width
    drawRect(Color.White, topLeft = Offset.Zero, size = size)
    // Taegeuk circle (red top, blue bottom)
    val center = Offset(w / 2f, h / 2f)
    val r = h * 0.25f
    drawCircle(Color(0xFFCD2E3A), radius = r, center = center)
    val path = Path().apply {
        moveTo(center.x - r, center.y)
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(center.x - r, center.y - r, center.x + r, center.y + r),
            startAngleDegrees = 0f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        close()
    }
    drawPath(path, Color(0xFF0047A0))
}

private fun DrawScope.drawFallbackFlag(code: String) {
    drawRect(Color(0xFF0F172A), topLeft = Offset.Zero, size = size)
}
