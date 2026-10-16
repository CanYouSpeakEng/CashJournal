package app.cashjournal.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate600

data class SegmentItem<T>(
    val value: T,
    val title: String,
    val activeColor: Color
)

@Composable
fun TransactionTypeSegmentedControl(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
    expenseLabel: String = "Expense",
    incomeLabel: String = "Income",
    transferLabel: String = "Transfer",
    hapticsEnabled: Boolean = true
) {
    val items = listOf(
        SegmentItem(TransactionType.EXPENSE, expenseLabel, Persimmon),
        SegmentItem(TransactionType.INCOME, incomeLabel, Emerald),
        SegmentItem(TransactionType.TRANSFER, transferLabel, Cobalt)
    )

    SegmentedControl(
        items = items,
        selectedValue = selectedType,
        onValueSelected = onTypeSelected,
        modifier = modifier,
        hapticsEnabled = hapticsEnabled
    )
}

@Composable
fun <T> SegmentedControl(
    items: List<SegmentItem<T>>,
    selectedValue: T,
    onValueSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    hapticsEnabled: Boolean = true
) {
    val view = LocalView.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Slate100)
            .padding(4.dp)
    ) {
        val count = items.size
        val itemWidth = if (count > 0) maxWidth / count else maxWidth
        val selectedIndex = items.indexOfFirst { it.value == selectedValue }.let { if (it >= 0) it else 0 }

        val animatedIndex by animateFloatAsState(
            targetValue = selectedIndex.toFloat(),
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 450f),
            label = "slidingSegmentPill"
        )

        val activeColor = items.getOrNull(selectedIndex)?.activeColor ?: Cobalt

        // Physical Sliding Pill Highlight
        Box(
            modifier = Modifier
                .offset(x = itemWidth * animatedIndex)
                .width(itemWidth)
                .height(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(activeColor)
        )

        // Segment Items Row with tactile touch response
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.value == selectedValue
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Slate600,
                    label = "segmentText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (hapticsEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                            onValueSelected(item.value)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}
