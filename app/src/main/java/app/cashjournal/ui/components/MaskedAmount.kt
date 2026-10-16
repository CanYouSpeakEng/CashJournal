package app.cashjournal.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.SpaceGroteskFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MaskedAmount(
    amountMinor: Long,
    currencyCode: String = "IDR",
    isMasked: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    modifier: Modifier = Modifier
) {
    val formatted = Currency.formatMinor(amountMinor, currencyCode, includeSymbol = false)
    val baseFontSize = textStyle.fontSize
    val effectiveFontSize = if (baseFontSize != TextUnit.Unspecified && baseFontSize.value >= 24f) {
        when {
            formatted.length >= 14 -> (baseFontSize.value * 0.70f).sp
            formatted.length >= 11 -> (baseFontSize.value * 0.82f).sp
            else -> baseFontSize
        }
    } else {
        baseFontSize
    }

    MaskedAmountText(
        amountText = formatted,
        balancesVisible = !isMasked,
        fontSize = effectiveFontSize,
        fontWeight = textStyle.fontWeight ?: FontWeight.Normal,
        color = textStyle.color,
        modifier = modifier
    )
}

private enum class MaskVisualState {
    MASKED_IDLE,
    REVEALED_IDLE,
    WAVE_HIDING,
    WAVE_REVEALING
}

/**
 * Animated Masked Amount Text per Build Brief v2 Section 15:
 * "Balance hide: each digit flips right to left, 32 ms apart. Each digit shrinks,
 * fades, and blurs for 90 ms, then becomes a dot. Separators disappear. Width animates
 * over 480 ms. Reveal runs left to right and separators fade back in. With reduce-motion
 * on, switch instantly."
 */
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
    val easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    var visualState by remember {
        mutableStateOf(if (balancesVisible) MaskVisualState.REVEALED_IDLE else MaskVisualState.MASKED_IDLE)
    }

    var isInitial by remember { mutableStateOf(true) }

    LaunchedEffect(balancesVisible) {
        if (isInitial) {
            isInitial = false
            visualState = if (balancesVisible) MaskVisualState.REVEALED_IDLE else MaskVisualState.MASKED_IDLE
            return@LaunchedEffect
        }
        val totalDigits = amountText.count { it.isDigit() }
        if (balancesVisible) {
            visualState = MaskVisualState.WAVE_REVEALING
            delay(totalDigits * 32L + 200L)
            visualState = MaskVisualState.REVEALED_IDLE
        } else {
            visualState = MaskVisualState.WAVE_HIDING
            delay(totalDigits * 32L + 110L)
            visualState = MaskVisualState.MASKED_IDLE
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .wrapContentWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 480, easing = easing))
    ) {
        if (keepSymbolVisible && currencySymbol.isNotEmpty()) {
            Text(
                text = "$currencySymbol ",
                fontFamily = SpaceGroteskFamily,
                fontWeight = fontWeight,
                fontSize = fontSize,
                color = symbolColor
            )
        }

        when (visualState) {
            MaskVisualState.MASKED_IDLE -> {
                Text(
                    text = "•••••••",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                    letterSpacing = 0.12.sp,
                    color = color,
                    maxLines = 1,
                    softWrap = false
                )
            }
            MaskVisualState.REVEALED_IDLE -> {
                Text(
                    text = amountText,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = fontWeight,
                    fontSize = fontSize,
                    color = color,
                    maxLines = 1,
                    softWrap = false
                )
            }
            MaskVisualState.WAVE_HIDING -> {
                val totalDigits = amountText.count { it.isDigit() }
                var digitIndex = 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    amountText.forEach { ch ->
                        if (ch.isDigit()) {
                            val dIdx = digitIndex++
                            val delayMs = ((totalDigits - 1 - dIdx) * 32L).coerceAtLeast(0L)
                            FlipChar(
                                initialChar = ch,
                                targetChar = '•',
                                staggerDelayMs = delayMs,
                                fontWeight = fontWeight,
                                fontSize = fontSize,
                                color = color
                            )
                        } else {
                            FlipSeparator(
                                char = ch,
                                hiding = true,
                                revealDelayMs = 0L,
                                fontWeight = fontWeight,
                                fontSize = fontSize,
                                color = color
                            )
                        }
                    }
                }
            }
            MaskVisualState.WAVE_REVEALING -> {
                val totalDigits = amountText.count { it.isDigit() }
                var digitIndex = 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    amountText.forEach { ch ->
                        if (ch.isDigit()) {
                            val dIdx = digitIndex++
                            val delayMs = (dIdx * 32L).coerceAtLeast(0L)
                            FlipChar(
                                initialChar = '•',
                                targetChar = ch,
                                staggerDelayMs = delayMs,
                                fontWeight = fontWeight,
                                fontSize = fontSize,
                                color = color
                            )
                        } else {
                            FlipSeparator(
                                char = ch,
                                hiding = false,
                                revealDelayMs = totalDigits * 32L,
                                fontWeight = fontWeight,
                                fontSize = fontSize,
                                color = color
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlipChar(
    initialChar: Char,
    targetChar: Char,
    staggerDelayMs: Long,
    fontWeight: FontWeight,
    fontSize: TextUnit,
    color: Color
) {
    var displayChar by remember { mutableStateOf(initialChar) }
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }
    val offsetY = remember { Animatable(0f) }

    LaunchedEffect(staggerDelayMs) {
        delay(staggerDelayMs)
        launch { scale.animateTo(0.45f, tween(45, easing = FastOutSlowInEasing)) }
        launch { alpha.animateTo(0f, tween(45, easing = FastOutSlowInEasing)) }
        offsetY.animateTo(-4f, tween(45, easing = FastOutSlowInEasing))

        displayChar = targetChar

        launch { scale.animateTo(1f, tween(45, easing = FastOutSlowInEasing)) }
        launch { alpha.animateTo(1f, tween(45, easing = FastOutSlowInEasing)) }
        offsetY.animateTo(0f, tween(45, easing = FastOutSlowInEasing))
    }

    Text(
        text = displayChar.toString(),
        fontFamily = SpaceGroteskFamily,
        fontWeight = fontWeight,
        fontSize = fontSize,
        color = color,
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
            translationY = offsetY.value * density
        }
    )
}

@Composable
private fun FlipSeparator(
    char: Char,
    hiding: Boolean,
    revealDelayMs: Long = 0L,
    fontWeight: FontWeight,
    fontSize: TextUnit,
    color: Color
) {
    val alpha = remember { Animatable(if (hiding) 1f else 0f) }
    val scaleX = remember { Animatable(if (hiding) 1f else 0f) }

    LaunchedEffect(hiding) {
        if (hiding) {
            launch { alpha.animateTo(0f, tween(60)) }
            scaleX.animateTo(0f, tween(90))
        } else {
            delay(revealDelayMs)
            launch { alpha.animateTo(1f, tween(180)) }
            scaleX.animateTo(1f, tween(180))
        }
    }

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            this.scaleX = scaleX.value
        }
    ) {
        Text(
            text = char.toString(),
            fontFamily = SpaceGroteskFamily,
            fontWeight = fontWeight,
            fontSize = fontSize,
            color = color
        )
    }
}
