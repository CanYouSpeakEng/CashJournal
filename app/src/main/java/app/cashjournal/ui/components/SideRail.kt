package app.cashjournal.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.GlassBorder
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretLeft
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.EyeSlash
import com.adamglin.phosphoricons.regular.GearSix
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Receipt
import com.adamglin.phosphoricons.regular.SquaresFour
import com.adamglin.phosphoricons.regular.Wallet
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

enum class NavTab {
    DASHBOARD,
    LEDGER,
    ACCOUNTS,
    ANALYTICS,
    SETTINGS
}

/**
 * Side Rail with liquid Tab Drip and Clock Arc animations per Build Brief v2 Section 15:
 * - "Rail drip: on tab change, a blue blob leaves the old button, stretches along the rail,
 *   and lands on the new button over 420 ms. The old button loses its fill at the start,
 *   the new one gains it at the end. An approximation is allowed, but it must read as a drip."
 * - "Clock arc: when a nested screen opens, the five characters move from the top arc to the
 *   bottom arc, rotating 180°, with an overshoot ease over 900 ms each. Start delays 270, 180,
 *   150, 90, and 0 ms. The back button scales in from 40% at the top. Reverse on leaving."
 */
@Composable
fun SideRail(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onQuickAddClick: () -> Unit,
    balancesVisible: Boolean,
    onToggleBalances: () -> Unit,
    modifier: Modifier = Modifier,
    isNestedScreen: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    hapticsEnabled: Boolean = true
) {
    val view = LocalView.current

    // Clock state updated periodically
    var timeString by remember { mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            timeString = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }

    // Tab drip animation setup
    val dripAnim = remember { Animatable(currentTab.ordinal.toFloat()) }
    var previousTabOrdinal by remember { mutableIntStateOf(currentTab.ordinal) }
    var stretchFraction by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentTab) {
        val target = currentTab.ordinal.toFloat()
        if (dripAnim.value != target) {
            val start = dripAnim.value
            previousTabOrdinal = start.toInt()
            val diff = abs(target - start)

            // Animate over 420 ms with (0.16, 1, 0.3, 1) curve
            dripAnim.animateTo(
                targetValue = target,
                animationSpec = tween(
                    durationMillis = 420,
                    easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                )
            ) {
                // Liquid stretch calculation: peaks mid-transit
                val totalDist = abs(target - start)
                val currentDist = abs(value - start)
                val normalized = if (totalDist > 0f) (currentDist / totalDist).coerceIn(0f, 1f) else 0f
                stretchFraction = (sin(normalized * PI).toFloat() * diff.coerceAtMost(2.5f))
            }
            stretchFraction = 0f
        }
    }

    // Back button scale animation from 40% per Section 15
    val backButtonScale by animateFloatAsState(
        targetValue = if (isNestedScreen) 1f else 0.4f,
        animationSpec = tween(durationMillis = 350, easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)),
        label = "backButtonScale"
    )

    // Side rail container: 42dp pill, translucent white at 88%
    Surface(
        modifier = modifier
            .width(42.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(21.dp),
                ambientColor = Charcoal.copy(alpha = 0.08f),
                spotColor = Charcoal.copy(alpha = 0.12f)
            ),
        shape = RoundedCornerShape(21.dp),
        color = Color(0xE0FFFFFF), // 88% white per brief section 12
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Zone: Clock Arc or Back Button
            if (isNestedScreen) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(backButtonScale)
                        .clip(CircleShape)
                        .clickable {
                            if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onBackClick?.invoke()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretLeft,
                        contentDescription = "Back",
                        tint = Charcoal,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // Clock Zone with animated characters
                ClockArcDisplay(
                    timeString = timeString,
                    isNested = false
                )

                // Privacy Vault Toggle (Lock when visible, EyeSlash when hidden)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onToggleBalances()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (balancesVisible) PhosphorIcons.Regular.Lock else PhosphorIcons.Regular.EyeSlash,
                        contentDescription = if (balancesVisible) "Hide Balances" else "Show Balances",
                        tint = Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Navigation Area with Liquid Drip Canvas
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(212.dp) // 5 buttons (36dp) + 4 gaps (8dp) = 212dp
            ) {
                // Liquid Tab Drip Canvas
                Canvas(modifier = Modifier.matchParentSize()) {
                    val stepPx = 44.dp.toPx() // 36dp + 8dp spacing
                    val buttonRadius = 18.dp.toPx()
                    val buttonDiameter = 36.dp.toPx()

                    val currentCenterY = buttonRadius + dripAnim.value * stepPx
                    val stretchExtraPx = stretchFraction * 14.dp.toPx()
                    val blobHeight = buttonDiameter + stretchExtraPx
                    val blobWidth = (buttonDiameter - stretchFraction * 6.dp.toPx()).coerceAtLeast(24.dp.toPx())

                    val top = currentCenterY - blobHeight / 2f
                    val left = (size.width - blobWidth) / 2f

                    drawRoundRect(
                        color = Cobalt,
                        topLeft = Offset(left, top),
                        size = Size(blobWidth, blobHeight),
                        cornerRadius = CornerRadius(blobWidth / 2f, blobWidth / 2f)
                    )
                }

                // The 5 Nav Buttons rendered on top
                Column(
                    modifier = Modifier.matchParentSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RailNavButton(
                        icon = PhosphorIcons.Regular.SquaresFour,
                        contentDescription = "Dashboard",
                        isSelected = currentTab == NavTab.DASHBOARD,
                        onClick = {
                            if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onTabSelected(NavTab.DASHBOARD)
                        }
                    )

                    RailNavButton(
                        icon = PhosphorIcons.Regular.Receipt,
                        contentDescription = "Ledger",
                        isSelected = currentTab == NavTab.LEDGER,
                        onClick = {
                            if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onTabSelected(NavTab.LEDGER)
                        }
                    )

                    RailNavButton(
                        icon = PhosphorIcons.Regular.Wallet,
                        contentDescription = "Accounts",
                        isSelected = currentTab == NavTab.ACCOUNTS,
                        onClick = {
                            if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onTabSelected(NavTab.ACCOUNTS)
                        }
                    )

                    RailNavButton(
                        icon = PhosphorIcons.Regular.ChartBar,
                        contentDescription = "Analytics",
                        isSelected = currentTab == NavTab.ANALYTICS,
                        onClick = {
                            if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onTabSelected(NavTab.ANALYTICS)
                        }
                    )

                    RailNavButton(
                        icon = PhosphorIcons.Regular.GearSix,
                        contentDescription = "Settings",
                        isSelected = currentTab == NavTab.SETTINGS,
                        onClick = {
                            if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onTabSelected(NavTab.SETTINGS)
                        }
                    )
                }
            }

            // Divider before Quick Add
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(1.dp)
                    .background(Slate200)
            )

            // "+" Button at bottom: 40dp button, opens Log transaction
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable {
                        if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onQuickAddClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Plus,
                    contentDescription = "Log Transaction",
                    tint = Cobalt,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Clock Arc Display with staggered character rotations per Build Brief v2 Section 15:
 * "The five characters move from the top arc to the bottom arc, rotating 180°,
 * with an overshoot ease over 900 ms each. Start delays 270, 180, 150, 90, and 0 ms."
 */
@Composable
private fun ClockArcDisplay(
    timeString: String,
    isNested: Boolean,
    modifier: Modifier = Modifier
) {
    val delays = listOf(270, 180, 150, 90, 0)
    val overshootEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

    Box(
        modifier = modifier
            .width(42.dp)
            .height(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            timeString.forEachIndexed { index, char ->
                val delayMs = delays.getOrElse(index) { 0 }
                val rotation by animateFloatAsState(
                    targetValue = if (isNested) 180f else 0f,
                    animationSpec = tween(
                        durationMillis = 900,
                        delayMillis = delayMs,
                        easing = overshootEasing
                    ),
                    label = "char_rotation_$index"
                )

                Text(
                    text = char.toString(),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Slate500,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun RailNavButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor = if (isSelected) Color.White else Slate500

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
    }
}
