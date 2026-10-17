package app.cashjournal.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.GlassBorder
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretLeft
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.Eye
import com.adamglin.phosphoricons.regular.EyeSlash
import com.adamglin.phosphoricons.regular.GearSix
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
 * Side Rail with liquid Tab Drip, expanding entry reveal animation, and shockwave tooltip physics.
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
    hapticsEnabled: Boolean = true,
    railOnRight: Boolean = true,
    languageCode: String = "en",
    isUpdateReady: Boolean = false
) {
    val view = LocalView.current
    val strings = AppStrings.get(languageCode)
    val deviceStatus = rememberDeviceStatus()

    // Liquid Status Ring Island Progress: Detaches upward when entering nested screen
    val islandProgress by animateFloatAsState(
        targetValue = if (isNestedScreen) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 320f
        ),
        label = "liquid_island_progress"
    )

    // Entry reveal animation: vertical expansion from center status circle over 450ms
    val revealProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 450,
                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
            )
        )
    }

    // Active tooltip pill state: text and vertical position (dp offset from top of rail)
    var activeTooltip by remember { mutableStateOf<String?>(null) }
    var activeTooltipY by remember { mutableFloatStateOf(0f) }

    // Shockwave Physics: When rail swaps sides (knock event) while a tooltip is held,
    // the pill collapses in 120ms (scaleOut + fadeOut) and blooms back out on landing.
    val pillAnim = remember { Animatable(1f) }
    LaunchedEffect(railOnRight) {
        if (activeTooltip != null) {
            pillAnim.animateTo(0f, tween(durationMillis = 120, easing = FastOutSlowInEasing))
            delay(160L)
            pillAnim.animateTo(1f, spring(dampingRatio = 0.75f, stiffness = 500f))
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
                val totalDist = abs(target - start)
                val currentDist = abs(value - start)
                val normalized = if (totalDist > 0f) (currentDist / totalDist).coerceIn(0f, 1f) else 0f
                stretchFraction = (sin(normalized * PI).toFloat() * diff.coerceAtMost(2.5f))
            }
            stretchFraction = 0f
        }
    }

    Box(
        modifier = modifier.wrapContentSize(),
        contentAlignment = if (railOnRight) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        // Floating Tooltip Capsule Pill (Emerges inward toward the screen center)
        if (activeTooltip != null) {
            val pillOffsetModifier = if (railOnRight) {
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-66).dp, y = activeTooltipY.dp)
            } else {
                Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 66.dp, y = activeTooltipY.dp)
            }

            Surface(
                modifier = pillOffsetModifier
                    .scale(pillAnim.value)
                    .alpha(pillAnim.value)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xF5FFFFFF), // Frosted glass 96% white
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Text(
                    text = activeTooltip ?: "",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Charcoal,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        // Side rail container: 54dp pill, translucent white at 88%
        Surface(
            modifier = Modifier
                .width(54.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(27.dp),
                    ambientColor = Charcoal.copy(alpha = 0.08f),
                    spotColor = Charcoal.copy(alpha = 0.12f)
                ),
            shape = RoundedCornerShape(27.dp),
            color = Color(0xE0FFFFFF), // 88% white per brief section 12
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Zone: Back Caret Slot (emerges when nested), Time Indicator, and Divider
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // 42dp Slot for Back Caret Button
                    Box(
                        modifier = Modifier.size(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val caretAlpha = (islandProgress - 0.2f).coerceIn(0f, 1f) / 0.8f
                        val caretScale = 0.5f + 0.5f * caretAlpha
                        if (caretAlpha > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .graphicsLayer {
                                        alpha = caretAlpha
                                        scaleX = caretScale
                                        scaleY = caretScale
                                    }
                                    .shadow(elevation = 2.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .pointerInput(isNestedScreen) {
                                        detectTapGestures(
                                            onPress = {
                                                val released = tryAwaitRelease()
                                                activeTooltip = null
                                            },
                                            onLongPress = {
                                                if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                                activeTooltip = strings.tooltipBack
                                                activeTooltipY = 16f
                                            },
                                            onTap = {
                                                if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                                onBackClick?.invoke()
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.CaretLeft,
                                    contentDescription = strings.tooltipBack,
                                    tint = Charcoal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Time indicator below top slot
                    Text(
                        text = deviceStatus.timeString,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Charcoal
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

                // Expanding container: buttons bloom vertically on entry
                val progress = revealProgress.value
                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = progress
                            scaleY = progress
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Navigation Area with Liquid Drip Canvas
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(242.dp)
                    ) {
                        // Liquid Tab Drip Canvas
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val stepPx = 50.dp.toPx()
                            val buttonRadius = 21.dp.toPx()
                            val buttonDiameter = 42.dp.toPx()

                            val currentCenterY = buttonRadius + dripAnim.value * stepPx
                            val stretchExtraPx = stretchFraction * 14.dp.toPx()
                            val blobHeight = buttonDiameter + stretchExtraPx
                            val blobWidth = (buttonDiameter - stretchFraction * 6.dp.toPx()).coerceAtLeast(28.dp.toPx())

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
                                contentDescription = strings.tooltipDashboard,
                                isSelected = currentTab == NavTab.DASHBOARD,
                                onClick = {
                                    if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTabSelected(NavTab.DASHBOARD)
                                },
                                onLongPress = {
                                    activeTooltip = strings.tooltipDashboard
                                    activeTooltipY = 82f
                                },
                                onRelease = {
                                    activeTooltip = null
                                },
                                hapticsEnabled = hapticsEnabled
                            )

                            RailNavButton(
                                icon = PhosphorIcons.Regular.Receipt,
                                contentDescription = strings.tooltipLedger,
                                isSelected = currentTab == NavTab.LEDGER,
                                onClick = {
                                    if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTabSelected(NavTab.LEDGER)
                                },
                                onLongPress = {
                                    activeTooltip = strings.tooltipLedger
                                    activeTooltipY = 132f
                                },
                                onRelease = {
                                    activeTooltip = null
                                },
                                hapticsEnabled = hapticsEnabled
                            )

                            RailNavButton(
                                icon = PhosphorIcons.Regular.Wallet,
                                contentDescription = strings.tooltipAccounts,
                                isSelected = currentTab == NavTab.ACCOUNTS,
                                onClick = {
                                    if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTabSelected(NavTab.ACCOUNTS)
                                },
                                onLongPress = {
                                    activeTooltip = strings.tooltipAccounts
                                    activeTooltipY = 182f
                                },
                                onRelease = {
                                    activeTooltip = null
                                },
                                hapticsEnabled = hapticsEnabled
                            )

                            RailNavButton(
                                icon = PhosphorIcons.Regular.ChartBar,
                                contentDescription = strings.tooltipAnalytics,
                                isSelected = currentTab == NavTab.ANALYTICS,
                                onClick = {
                                    if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTabSelected(NavTab.ANALYTICS)
                                },
                                onLongPress = {
                                    activeTooltip = strings.tooltipAnalytics
                                    activeTooltipY = 232f
                                },
                                onRelease = {
                                    activeTooltip = null
                                },
                                hapticsEnabled = hapticsEnabled
                            )

                            RailNavButton(
                                icon = PhosphorIcons.Regular.GearSix,
                                contentDescription = strings.tooltipSettings,
                                isSelected = currentTab == NavTab.SETTINGS,
                                onClick = {
                                    if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onTabSelected(NavTab.SETTINGS)
                                },
                                onLongPress = {
                                    activeTooltip = strings.tooltipSettings
                                    activeTooltipY = 282f
                                },
                                onRelease = {
                                    activeTooltip = null
                                },
                                hapticsEnabled = hapticsEnabled,
                                glowBlue = isUpdateReady
                            )
                        }
                    }

                    // Divider before Quick Add
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(1.dp)
                            .background(Slate200)
                    )

                    val currentQuickAddStrings by rememberUpdatedState(strings)
                    val currentOnQuickAddClick by rememberUpdatedState(onQuickAddClick)
                    val currentHapticsEnabled by rememberUpdatedState(hapticsEnabled)
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .shadow(elevation = 3.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        val released = tryAwaitRelease()
                                        activeTooltip = null
                                    },
                                    onLongPress = {
                                        if (currentHapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                        activeTooltip = currentQuickAddStrings.tooltipLogTransaction
                                        activeTooltipY = 338f
                                    },
                                    onTap = {
                                        if (currentHapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                        currentOnQuickAddClick()
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Plus,
                            contentDescription = strings.tooltipLogTransaction,
                            tint = Cobalt,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Floating Status Ring Island (Liquid upward morphing satellite island matching sidebar width)
        val islandSize = 42.dp + (12.dp * islandProgress)
        val targetOffsetX = (if (railOnRight) (-6).dp else 6.dp) * (1f - islandProgress)
        val targetOffsetY = 12.dp + ((-76).dp * islandProgress) // Rests at -64dp when progress = 1f (10dp above rail)
        val stretch = sin(islandProgress * PI.toFloat()) * 0.18f
        val scaleY = 1f + stretch
        val scaleX = 1f - (stretch * 0.35f)
        val islandElevation = (2.dp + 8.dp * islandProgress)

        Box(
            modifier = Modifier
                .align(if (railOnRight) Alignment.TopEnd else Alignment.TopStart)
                .offset(
                    x = targetOffsetX,
                    y = targetOffsetY
                )
                .size(islandSize)
                .graphicsLayer {
                    this.scaleX = scaleX
                    this.scaleY = scaleY
                }
                .shadow(
                    elevation = islandElevation,
                    shape = CircleShape,
                    ambientColor = Charcoal.copy(alpha = 0.08f),
                    spotColor = Charcoal.copy(alpha = 0.14f)
                )
                .clip(CircleShape)
                .background(if (islandProgress > 0.05f) Color(0xF5FFFFFF) else Color.White)
                .border(
                    BorderStroke(1.dp, GlassBorder.copy(alpha = islandProgress.coerceIn(0f, 1f))),
                    CircleShape
                )
                .pointerInput(isNestedScreen) {
                    detectTapGestures(
                        onPress = {
                            val released = tryAwaitRelease()
                            activeTooltip = null
                        },
                        onLongPress = {
                            if (hapticsEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                            }
                            activeTooltip = strings.tooltipPrivacyEye
                            activeTooltipY = if (isNestedScreen) -56f else 16f
                        },
                        onTap = {
                            if (hapticsEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                            onToggleBalances()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            StatusRingCanvas(
                batteryPercent = deviceStatus.batteryLevel,
                activeSignalBars = deviceStatus.signalLevel,
                modifier = Modifier.size(38.dp)
            )

            Icon(
                imageVector = if (balancesVisible) PhosphorIcons.Regular.Eye else PhosphorIcons.Regular.EyeSlash,
                contentDescription = if (balancesVisible) "Mask Balances" else "Reveal Balances",
                tint = Charcoal,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun RailNavButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: (() -> Unit)? = null,
    onRelease: (() -> Unit)? = null,
    hapticsEnabled: Boolean = true,
    glowBlue: Boolean = false
) {
    val iconColor = if (isSelected) Color.White else Slate500
    val view = LocalView.current
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongPress by rememberUpdatedState(onLongPress)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val currentHapticsEnabled by rememberUpdatedState(hapticsEnabled)

    val breatheTransition = rememberInfiniteTransition(label = "breatheTransition")
    val breatheScale by if (glowBlue && !isSelected) {
        breatheTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breatheScale"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }
    val breatheAlpha by if (glowBlue && !isSelected) {
        breatheTransition.animateFloat(
            initialValue = 0.40f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breatheAlpha"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val rippleScale by if (glowBlue && !isSelected) {
        breatheTransition.animateFloat(
            initialValue = 1.05f,
            targetValue = 1.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rippleScale"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }
    val rippleAlpha by if (glowBlue && !isSelected) {
        breatheTransition.animateFloat(
            initialValue = 0.45f,
            targetValue = 0.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rippleAlpha"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val released = tryAwaitRelease()
                        currentOnRelease?.invoke()
                    },
                    onLongPress = {
                        if (currentHapticsEnabled) {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        }
                        currentOnLongPress?.invoke()
                    },
                    onTap = {
                        currentOnClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (glowBlue && !isSelected) {
            // Ambient outer ripple pulse
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(rippleScale)
                    .clip(CircleShape)
                    .border(
                        BorderStroke(1.dp, Cobalt.copy(alpha = rippleAlpha * 0.35f)),
                        CircleShape
                    )
            )

            // Rich cobalt breathing halo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(breatheScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Cobalt.copy(alpha = breatheAlpha * 0.38f),
                                Cobalt.copy(alpha = breatheAlpha * 0.12f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        BorderStroke(1.2.dp, Cobalt.copy(alpha = breatheAlpha * 0.45f)),
                        CircleShape
                    )
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
