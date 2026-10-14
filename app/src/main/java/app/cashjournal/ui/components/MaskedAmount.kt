package app.cashjournal.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.SpaceGroteskFamily
import app.cashjournal.domain.model.Currency

@Composable
fun MaskedAmount(
    amountMinor: Long,
    currencyCode: String = "IDR",
    isMasked: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    modifier: Modifier = Modifier
) {
    val formatted = Currency.formatMinor(amountMinor, currencyCode, includeSymbol = false)
    val maskedDots = "•••••••" // 7 dots per brief

    if (!isMasked) {
        Text(
            text = formatted,
            style = textStyle,
            modifier = modifier
        )
    } else {
        Text(
            text = maskedDots,
            style = textStyle.copy(letterSpacing = 0.12.sp),
            modifier = modifier
        )
    }
}

@Composable
fun MaskedAmountText(
    amountText: String,
    currencySymbol: String = "",
    balancesVisible: Boolean = true,
    keepSymbolVisible: Boolean = true,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = Charcoal,
    symbolColor: Color = color,
    modifier: Modifier = Modifier
) {
    val maskedDots = "•••••••" // Exactly 7 dots per brief section 13

    if (balancesVisible) {
        Text(
            text = amountText,
            fontFamily = SpaceGroteskFamily,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = color,
            modifier = modifier
        )
    } else {
        val displayText = if (keepSymbolVisible && currencySymbol.isNotEmpty()) {
            "$currencySymbol $maskedDots"
        } else {
            maskedDots
        }

        Text(
            text = displayText,
            fontFamily = SpaceGroteskFamily,
            fontWeight = fontWeight,
            fontSize = fontSize,
            letterSpacing = 0.12.sp, // 0.12em letter spacing per brief
            color = color,
            modifier = modifier
        )
    }
}
