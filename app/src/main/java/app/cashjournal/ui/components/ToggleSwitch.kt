package app.cashjournal.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Slate300

@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hapticsEnabled: Boolean = true
) {
    val view = LocalView.current
    val trackColor by animateColorAsState(
        targetValue = if (checked) Cobalt else Slate300,
        label = "trackColor"
    )

    // Track is 52dp wide, knob is 24dp, inset is 3dp
    // When unchecked: offset is 3dp
    // When checked: offset is 52 - 24 - 3 = 25dp (travel distance = 22dp)
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 25.dp else 3.dp,
        animationSpec = spring(
            dampingRatio = 0.65f, // slight overshoot per brief section 13
            stiffness = Spring.StiffnessMedium
        ),
        label = "knobOffset"
    )

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (enabled) trackColor else trackColor.copy(alpha = 0.4f))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (hapticsEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(24.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(8.dp),
                    ambientColor = Charcoal.copy(alpha = 0.15f),
                    spotColor = Charcoal.copy(alpha = 0.2f)
                )
                .background(Color.White, RoundedCornerShape(8.dp))
        )
    }
}
