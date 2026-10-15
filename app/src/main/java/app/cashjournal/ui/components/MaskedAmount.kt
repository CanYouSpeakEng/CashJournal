package app.cashjournal.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.SpaceGroteskFamily

@Composable
fun MaskedAmount(
    amountMinor: Long,
    currencyCode: String = "IDR",
    isMasked: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    modifier: Modifier = Modifier
) {
    val formatted = Currency.formatMinor(amountMinor, currencyCode, includeSymbol = false)
    MaskedAmountText(
        amountText = formatted,
        balancesVisible = !isMasked,
        fontSize = textStyle.fontSize,
        fontWeight = textStyle.fontWeight ?: FontWeight.Normal,
        color = textStyle.color,
        modifier = modifier
    )
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

        AnimatedContent(
            targetState = balancesVisible,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
            },
            label = "masked_balance_flip"
        ) { visible ->
            if (visible) {
                // Reveal digits with left-to-right staggered animation
                Row(verticalAlignment = Alignment.CenterVertically) {
                    amountText.forEachIndexed { index, char ->
                        val delay = (index * 32).coerceAtMost(320)
                        val charScale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 180, delayMillis = delay, easing = easing),
                            label = "char_scale"
                        )
                        val charAlpha by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 140, delayMillis = delay, easing = FastOutSlowInEasing),
                            label = "char_alpha"
                        )

                        Text(
                            text = char.toString(),
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = fontWeight,
                            fontSize = fontSize,
                            color = color,
                            modifier = Modifier
                                .scale(charScale)
                                .alpha(charAlpha)
                        )
                    }
                }
            } else {
                // Hidden: Exactly 7 "•" dots with 0.12em letter spacing
                val maskedDots = "•••••••"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    maskedDots.forEachIndexed { index, dot ->
                        val delay = ((maskedDots.length - 1 - index) * 32).coerceAtMost(320)
                        val dotScale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 180, delayMillis = delay, easing = easing),
                            label = "dot_scale"
                        )
                        val dotAlpha by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 140, delayMillis = delay, easing = FastOutSlowInEasing),
                            label = "dot_alpha"
                        )

                        Text(
                            text = dot.toString(),
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = fontWeight,
                            fontSize = fontSize,
                            letterSpacing = 0.12.sp,
                            color = color,
                            modifier = Modifier
                                .scale(dotScale)
                                .alpha(dotAlpha)
                        )
                    }
                }
            }
        }
    }
}
