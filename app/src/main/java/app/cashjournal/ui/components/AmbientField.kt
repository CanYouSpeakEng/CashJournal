package app.cashjournal.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import app.cashjournal.domain.model.Account
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Ambient background field per Build Brief v2 Section 15:
 * "One soft blurred blob per account, in its accent color, ordered by balance.
 * Size = 28% + 42% * (balance / largest)^0.8 of the screen width.
 * Positions: (30%, 24%), (78%, 62%), (24%, 80%), (76%, 14%), (58%, 94%), (12%, 48%).
 * Opacity 32%, blur about 56dp. Each blob slowly morphs and drifts on a 30 s loop.
 * Shifts with 5% of rail movement. Colors cross-fade over 1 s when balances change.
 * Use blurred radial gradients on a Canvas; full blur needs Android 12 or newer,
 * so use soft radial gradients on older versions."
 *
 * Performance note: Each blob is isolated to its own hardware RenderNode via graphicsLayer.
 * Radial gradient brushes are rendered once into the layer and translated via GPU matrix,
 * eliminating full-screen redraws and canvas brush allocations on the 30-second loop.
 */
@Composable
fun AmbientField(
    accounts: List<Account>,
    railSideRight: Boolean,
    modifier: Modifier = Modifier
) {
    if (accounts.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "ambient_drift")
    val driftProgress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_angle"
    )

    // Rail shift: 5% of rail offset (positive if right, negative if left)
    val railShiftX = if (railSideRight) 16f else -16f

    val basePositions = remember {
        listOf(
            Offset(0.30f, 0.24f),
            Offset(0.78f, 0.62f),
            Offset(0.24f, 0.80f),
            Offset(0.76f, 0.14f),
            Offset(0.58f, 0.94f),
            Offset(0.12f, 0.48f)
        )
    }

    val sortedAccounts = remember(accounts) {
        accounts.sortedByDescending { it.balance }.take(6)
    }
    val largestBalance = remember(sortedAccounts) {
        sortedAccounts.firstOrNull()?.balance?.coerceAtLeast(1L) ?: 1L
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        sortedAccounts.forEachIndexed { index, account ->
            val posRatio = basePositions[index % basePositions.size]
            val phase = index * (PI.toFloat() / 3f)

            // Size formula = 28% + 42% * (balance / largest)^0.8 of screen width
            val balanceRatio = (account.balance.toDouble() / largestBalance).coerceIn(0.0, 1.0)
            val sizeFraction = 0.28f + 0.42f * balanceRatio.pow(0.8).toFloat()
            val diameterPx = screenWidthPx * sizeFraction
            val radiusPx = diameterPx / 2f
            val diameterDp = with(density) { diameterPx.toDp() }

            val animatedColor by animateColorAsState(
                targetValue = account.accentKey.color,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "blob_color_$index"
            )

            val baseX = posRatio.x * screenWidthPx - radiusPx + railShiftX
            val baseY = posRatio.y * screenHeightPx - radiusPx
            val driftAmplitudeX = screenWidthPx * 0.04f
            val driftAmplitudeY = screenHeightPx * 0.04f

            val gradientBrush = remember(animatedColor, radiusPx) {
                Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to animatedColor.copy(alpha = 0.32f),
                        0.45f to animatedColor.copy(alpha = 0.20f),
                        0.75f to animatedColor.copy(alpha = 0.08f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(radiusPx, radiusPx),
                    radius = radiusPx.coerceAtLeast(10f)
                )
            }

            Box(
                modifier = Modifier
                    .size(diameterDp)
                    .graphicsLayer {
                        val progress = driftProgress.value
                        translationX = baseX + cos(progress + phase) * driftAmplitudeX
                        translationY = baseY + sin(progress + phase) * driftAmplitudeY
                    }
                    .drawBehind {
                        drawCircle(
                            brush = gradientBrush,
                            radius = radiusPx,
                            center = Offset(radiusPx, radiusPx)
                        )
                    }
            )
        }
    }
}
