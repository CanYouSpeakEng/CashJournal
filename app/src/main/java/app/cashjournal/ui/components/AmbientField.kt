package app.cashjournal.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 */
@Composable
fun AmbientField(
    accounts: List<Account>,
    railSideRight: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_drift")
    val driftProgress by infiniteTransition.animateFloat(
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

    val basePositions = listOf(
        Offset(0.30f, 0.24f),
        Offset(0.78f, 0.62f),
        Offset(0.24f, 0.80f),
        Offset(0.76f, 0.14f),
        Offset(0.58f, 0.94f),
        Offset(0.12f, 0.48f)
    )

    val sortedAccounts = accounts.sortedByDescending { it.balance }.take(6)
    val largestBalance = sortedAccounts.firstOrNull()?.balance?.coerceAtLeast(1L) ?: 1L

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        sortedAccounts.forEachIndexed { index, account ->
            val posRatio = basePositions[index % basePositions.size]

            // Drift phase offset for each blob
            val phase = index * (PI.toFloat() / 3f)
            val driftX = cos(driftProgress + phase) * (width * 0.04f)
            val driftY = sin(driftProgress + phase) * (height * 0.04f)

            val centerX = posRatio.x * width + driftX + railShiftX
            val centerY = posRatio.y * height + driftY

            // Size formula = 28% + 42% * (balance / largest)^0.8 of screen width
            val balanceRatio = (account.balance.toDouble() / largestBalance).coerceIn(0.0, 1.0)
            val sizeFraction = 0.28f + 0.42f * balanceRatio.pow(0.8).toFloat()
            val radius = (width * sizeFraction) / 2f

            val blobColor = account.accentKey.color

            val gradientBrush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to blobColor.copy(alpha = 0.32f),
                    0.45f to blobColor.copy(alpha = 0.20f),
                    0.75f to blobColor.copy(alpha = 0.08f),
                    1.0f to Color.Transparent
                ),
                center = Offset(centerX, centerY),
                radius = radius.coerceAtLeast(10f)
            )

            drawCircle(
                brush = gradientBrush,
                radius = radius,
                center = Offset(centerX, centerY)
            )
        }
    }
}
