package app.cashjournal.ui.components

import android.content.Context
import android.os.BatteryManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.ui.theme.CashColors
import app.cashjournal.ui.theme.PlusJakartaSans
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretLeft
import com.adamglin.phosphoricons.regular.Eye
import com.adamglin.phosphoricons.regular.EyeSlash
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Unified Status & Privacy Cluster for the top end of the SideRail.
 *
 * Implements the circular status button design:
 * - Outer 270° arc: Dynamic battery level indicator (Green > 30%, Amber <= 30%, Red <= 10%).
 * - Remaining battery stroke in dark charcoal.
 * - Bottom 90° gap: 5 signal strength dots (polar angles 55°, 70°, 90°, 110°, 125°).
 * - Center: Privacy eye icon (Eye when visible, EyeSlash when masked). Tapping toggles privacy.
 * - Below circle: Clean bold time indicator in Plus Jakarta Sans ExtraBold (e.g. "6:30").
 * - Below time: Subtle horizontal divider rule (36dp).
 * - In nested screens (e.g. Account Detail): Transforms into the Back caret.
 */
@Composable
fun RailStatusCluster(
    balancesVisible: Boolean,
    onToggleBalances: () -> Unit,
    modifier: Modifier = Modifier,
    isNestedScreen: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    hapticsEnabled: Boolean = true
) {
    val context = LocalContext.current
    val view = LocalView.current

    // Real device battery reading (offline & private)
    var batteryLevel by remember {
        mutableIntStateOf(
            try {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                val capacity = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
                if (capacity in 0..100) capacity else 75
            } catch (_: Exception) {
                75
            }
        )
    }

    val formatTime = remember(context) {
        {
            val is24h = android.text.format.DateFormat.is24HourFormat(context)
            val pattern = if (is24h) "HH:mm" else "h:mm"
            LocalTime.now().format(DateTimeFormatter.ofPattern(pattern))
        }
    }

    // Refresh time & battery every 30 seconds
    var timeString by remember {
        mutableStateOf(formatTime())
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            timeString = formatTime()
            try {
                val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                val capacity = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
                if (capacity in 0..100) batteryLevel = capacity
            } catch (_: Exception) {
                // keep current
            }
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Circular Status Button / Back Button
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(elevation = 3.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .clickable {
                    if (hapticsEnabled) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                    if (isNestedScreen) {
                        onBackClick?.invoke()
                    } else {
                        onToggleBalances()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isNestedScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "status_back_transition"
            ) { nested ->
                if (nested) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretLeft,
                        contentDescription = "Back",
                        tint = CashColors.Charcoal,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier.size(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Status Ring: Battery Arc & Signal Dots
                        StatusRingCanvas(
                            batteryPercent = batteryLevel,
                            activeSignalBars = 4, // 4 out of 5 bars
                            modifier = Modifier.size(38.dp)
                        )

                        // Center Privacy Eye Icon
                        Icon(
                            imageVector = if (balancesVisible) PhosphorIcons.Regular.Eye else PhosphorIcons.Regular.EyeSlash,
                            contentDescription = if (balancesVisible) "Mask Balances" else "Reveal Balances",
                            tint = CashColors.Charcoal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Time indicator below the circle
        Text(
            text = timeString,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            color = CashColors.Charcoal
        )

        // Subtle horizontal divider line
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(1.5.dp)
                .background(Color(0xFFCBD5E1), RoundedCornerShape(1.dp))
        )

        Spacer(modifier = Modifier.height(2.dp))
    }
}

/**
 * Draws the battery arc and 5 signal dots on a circular perimeter
 * with exact polar angles matching the design specification.
 */
@Composable
private fun StatusRingCanvas(
    batteryPercent: Int,
    activeSignalBars: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidthPx = 2.6.dp.toPx()
        val diameter = size.minDimension - strokeWidthPx - 1.dp.toPx()
        val radius = diameter / 2f
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        val arcTopLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)
        val arcSize = Size(diameter, diameter)

        // Battery Arc geometry:
        // Starts at 135° (bottom-left) and sweeps clockwise across 270° to 45° (bottom-right).
        // The drain runs from left to right:
        // - Black (used space) expands from 135° clockwise as battery drains.
        // - Battery color (remaining charge) covers the remaining arc to 45°.
        val arcStartAngle = 135f
        val arcTotalSpan = 270f
        val fraction = (batteryPercent.coerceIn(0, 100)) / 100f
        val usedFraction = 1f - fraction
        val usedSpan = arcTotalSpan * usedFraction
        val remainingSpan = arcTotalSpan * fraction

        // Dynamic battery color for remaining charge
        val batteryColor = when {
            batteryPercent <= 10 -> Color(0xFFE53935) // Red Alert
            batteryPercent <= 30 -> Color(0xFFF5B301) // Saffron / Amber
            else -> Color(0xFF00B26B)                 // Emerald Green
        }
        val usedColor = Color(0xFF1E293B)             // Deep Charcoal Black (used space)

        // 1. Used Battery Space (Black - runs left to right)
        if (usedSpan > 0f) {
            drawArc(
                color = usedColor,
                startAngle = arcStartAngle,
                sweepAngle = usedSpan,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // 2. Remaining Battery Arc (Green/Amber/Red)
        if (remainingSpan > 0f) {
            drawArc(
                color = batteryColor,
                startAngle = arcStartAngle + usedSpan,
                sweepAngle = remainingSpan,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        // 3. Signal Strength Dots in the bottom 90° gap:
        // Angles: 55°, 70°, 90°, 110°, 125°
        val dotAngles = floatArrayOf(55f, 70f, 90f, 110f, 125f)
        val dotRadiusPx = 1.5.dp.toPx()

        for (i in dotAngles.indices) {
            val angleDeg = dotAngles[i]
            val angleRad = angleDeg * (PI.toFloat() / 180f)
            val dotX = centerOffset.x + radius * cos(angleRad)
            val dotY = centerOffset.y + radius * sin(angleRad)

            val dotColor = if (i < activeSignalBars) {
                Color(0xFF000000) // Active dot
            } else {
                Color(0xFFCBD5E1) // Inactive dot
            }

            drawCircle(
                color = dotColor,
                radius = dotRadiusPx,
                center = Offset(dotX, dotY)
            )
        }
    }
}
