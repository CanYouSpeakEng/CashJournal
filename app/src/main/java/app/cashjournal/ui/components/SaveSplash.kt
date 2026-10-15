package app.cashjournal.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import app.cashjournal.ui.theme.Cobalt
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Droplet(
    val vx: Float,
    val vy: Float,
    val radius: Float
)

/**
 * Save splash animation per Build Brief v2 Section 15:
 * "14 small cobalt droplets burst from the Save button in random directions,
 * pulled down by gravity, fading over about one second."
 */
@Composable
fun SaveSplash(
    trigger: Boolean,
    onAnimationEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!trigger) return

    val progress = remember { Animatable(0f) }
    val droplets = remember {
        val rand = Random(System.currentTimeMillis())
        List(14) { index ->
            val angle = (2 * PI * (index + rand.nextDouble(-0.2, 0.2)) / 14).toFloat()
            val speed = rand.nextDouble(320.0, 680.0).toFloat()
            Droplet(
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 150f, // Upward initial bias
                radius = rand.nextDouble(9.0, 15.0).toFloat()
            )
        }
    }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
        onAnimationEnd()
    }

    val t = progress.value // 0.0 to 1.0 (seconds)
    val alpha = (1f - t).coerceIn(0f, 1f)
    val gravity = 480f // dp/s² pull

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        droplets.forEach { droplet ->
            val x = centerX + droplet.vx * t
            val y = centerY + droplet.vy * t + 0.5f * gravity * t * t
            val currentRadius = droplet.radius * (1f - 0.3f * t)

            drawCircle(
                color = Cobalt.copy(alpha = alpha),
                radius = currentRadius,
                center = Offset(x, y)
            )
        }
    }
}
