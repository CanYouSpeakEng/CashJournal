package app.cashjournal.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.GlassBorder
import app.cashjournal.ui.theme.GlassCardBackground
import app.cashjournal.ui.theme.GlassCardShape
import app.cashjournal.ui.theme.SubtleCardBackground
import app.cashjournal.ui.theme.SubtleCardShape

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = GlassCardShape,
    backgroundColor: Color = GlassCardBackground,
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Charcoal.copy(alpha = 0.05f),
                spotColor = Charcoal.copy(alpha = 0.08f)
            ),
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        val clickModifier = if (onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        }
        Box(modifier = clickModifier) {
            content()
        }
    }
}

@Composable
fun SubtleGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = SubtleCardShape,
    backgroundColor: Color = SubtleCardBackground,
    borderColor: Color = GlassBorder.copy(alpha = 0.6f),
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    GlassCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        elevation = 0.dp,
        onClick = onClick,
        content = content
    )
}
