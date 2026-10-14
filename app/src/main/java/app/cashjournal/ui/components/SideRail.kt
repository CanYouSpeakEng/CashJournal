package app.cashjournal.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
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

enum class NavTab {
    DASHBOARD,
    LEDGER,
    ACCOUNTS,
    ANALYTICS,
    SETTINGS
}

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

    // Clock state updated every 30 seconds
    var timeString by remember { mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L)
            timeString = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        }
    }

    // Side rail: 42dp pill, translucent white at 88%
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
            // Top Zone: Clock (or Back button when nested)
            if (isNestedScreen) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                // Clock Zone: 5 characters HH:MM
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeString,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Slate500
                    )
                }

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

            // Navigation Buttons (Dashboard, Ledger, Accounts, Analytics, Settings)
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

@Composable
private fun RailNavButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Cobalt else Color.Transparent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "navButtonBg"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Slate500,
        label = "navButtonIcon"
    )

    val shadowElevation = if (isSelected) 4.dp else 0.dp

    Box(
        modifier = Modifier
            .size(36.dp)
            .shadow(
                elevation = shadowElevation,
                shape = CircleShape,
                ambientColor = Cobalt.copy(alpha = 0.3f),
                spotColor = Cobalt.copy(alpha = 0.4f)
            )
            .clip(CircleShape)
            .background(backgroundColor)
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
