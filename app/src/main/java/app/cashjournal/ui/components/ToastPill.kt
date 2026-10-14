package app.cashjournal.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.SpaceGroteskFamily
import kotlinx.coroutines.delay

@Composable
fun ToastPill(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    durationMs: Long = 2200L // 2.2 seconds per brief
) {
    LaunchedEffect(visible, message) {
        if (visible) {
            delay(durationMs)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Charcoal.copy(alpha = 0.2f),
                    spotColor = Charcoal.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(20.dp),
            color = Charcoal,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) // 1dp white border at 10%
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emerald Dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Emerald, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // White Space Grotesk Bold 12sp
                Text(
                    text = message,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}
