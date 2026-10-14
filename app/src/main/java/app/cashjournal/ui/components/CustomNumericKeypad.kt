package app.cashjournal.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.ui.theme.Berry
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Backspace

import app.cashjournal.domain.model.Currency

@Composable
fun CustomNumericKeypad(
    currencyCode: String,
    onKeyPressed: (String) -> Unit,
    modifier: Modifier = Modifier,
    hapticsEnabled: Boolean = true
) {
    val currency = Currency.findByCode(currencyCode)
    CustomNumericKeypad(
        onDigitPress = onKeyPressed,
        onBackspacePress = { onKeyPressed("⌫") },
        modifier = modifier,
        isZeroDecimal = currency.isZeroDecimal,
        hapticsEnabled = hapticsEnabled
    )
}

@Composable
fun CustomNumericKeypad(
    onDigitPress: (String) -> Unit,
    onBackspacePress: () -> Unit,
    modifier: Modifier = Modifier,
    isZeroDecimal: Boolean = true, // true: "000" for IDR/JPY; false: "00" for USD/EUR
    hapticsEnabled: Boolean = true
) {
    val view = LocalView.current
    val multiZeroKey = if (isZeroDecimal) "000" else "00"

    fun handlePress(key: String) {
        if (hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        onDigitPress(key)
    }

    fun handleBackspace() {
        if (hapticsEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        onBackspacePress()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: 1, 2, 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(text = "1", modifier = Modifier.weight(1f), onClick = { handlePress("1") })
            KeypadButton(text = "2", modifier = Modifier.weight(1f), onClick = { handlePress("2") })
            KeypadButton(text = "3", modifier = Modifier.weight(1f), onClick = { handlePress("3") })
        }

        // Row 2: 4, 5, 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(text = "4", modifier = Modifier.weight(1f), onClick = { handlePress("4") })
            KeypadButton(text = "5", modifier = Modifier.weight(1f), onClick = { handlePress("5") })
            KeypadButton(text = "6", modifier = Modifier.weight(1f), onClick = { handlePress("6") })
        }

        // Row 3: 7, 8, 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(text = "7", modifier = Modifier.weight(1f), onClick = { handlePress("7") })
            KeypadButton(text = "8", modifier = Modifier.weight(1f), onClick = { handlePress("8") })
            KeypadButton(text = "9", modifier = Modifier.weight(1f), onClick = { handlePress("9") })
        }

        // Row 4: 000/00, 0, Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KeypadButton(text = multiZeroKey, modifier = Modifier.weight(1f), onClick = { handlePress(multiZeroKey) })
            KeypadButton(text = "0", modifier = Modifier.weight(1f), onClick = { handlePress("0") })
            KeypadIconButton(
                icon = {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Backspace,
                        contentDescription = "Backspace",
                        tint = Berry,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.weight(1f),
                onClick = { handleBackspace() }
            )
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Slate100.copy(alpha = 0.7f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (text.length > 2) 18.sp else 22.sp,
                color = Charcoal
            )
        }
    }
}

@Composable
private fun KeypadIconButton(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Slate100.copy(alpha = 0.7f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}
