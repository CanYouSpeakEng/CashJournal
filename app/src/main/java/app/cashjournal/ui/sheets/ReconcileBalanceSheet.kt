package app.cashjournal.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.components.CustomNumericKeypad
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.X
import kotlin.math.abs

@Composable
fun ReconcileBalanceSheet(
    account: Account,
    currencyCode: String,
    onConfirmReconciliation: (actualBalance: Long, note: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    languageCode: String = "en",
    hapticsEnabled: Boolean = true
) {
    val strings = AppStrings.get(languageCode)
    val currency = Currency.findByCode(currencyCode)
    var note by remember { mutableStateOf("") }
    var rawDigits by remember { mutableStateOf(account.balance.toString()) }

    val actualBalance: Long = remember(rawDigits, currency.isZeroDecimal) {
        rawDigits.toLongOrNull() ?: 0L
    }

    val displayAmountText = remember(actualBalance, currencyCode) {
        Currency.formatMinor(actualBalance, currencyCode, includeSymbol = false)
    }

    val delta = actualBalance - account.balance

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Grab Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Slate200)
        )

        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(account.accentKey.color)
                )
                Text(
                    text = "${strings.reconcileBalanceTitle} · ${account.name}",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Charcoal
                )
            }

            Icon(
                imageVector = PhosphorIcons.Regular.X,
                contentDescription = "Close",
                tint = Slate500,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onDismiss() }
            )
        }

        // Current App Balance vs Delta Tile
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = strings.currentBalanceLabel.uppercase(),
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp,
                        color = Slate500
                    )
                    Text(
                        text = Currency.formatMinor(account.balance, currencyCode, includeSymbol = true),
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Charcoal
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = strings.differenceLabel.uppercase(),
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp,
                        color = Slate500
                    )
                    val deltaColor = when {
                        delta > 0L -> Emerald
                        delta < 0L -> Persimmon
                        else -> Slate500
                    }
                    val deltaPrefix = when {
                        delta > 0L -> "+"
                        delta < 0L -> "−"
                        else -> ""
                    }
                    val formattedDelta = Currency.formatMinor(abs(delta), currencyCode, includeSymbol = true)
                    Text(
                        text = "$deltaPrefix$formattedDelta",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = deltaColor
                    )
                }
            }
        }

        // Actual Real Balance Heading
        Text(
            text = strings.actualBalanceLabel.uppercase(),
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
            color = Slate500,
            modifier = Modifier.align(Alignment.Start)
        )

        // Amount Display (Centered)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Text(
                text = currency.symbol,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Cobalt,
                modifier = Modifier.padding(end = 6.dp)
            )

            Text(
                text = displayAmountText,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 34.sp,
                color = Charcoal
            )
        }

        // Note Input Field
        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = note,
                    onValueChange = { note = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 13.sp,
                        color = Charcoal
                    ),
                    cursorBrush = SolidColor(Cobalt),
                    decorationBox = { innerTextField ->
                        if (note.isEmpty()) {
                            Text(
                                text = strings.reconcileNotePlaceholder,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 13.sp,
                                color = Slate400
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Numeric Keypad
        CustomNumericKeypad(
            currencyCode = currencyCode,
            hapticsEnabled = hapticsEnabled,
            onKeyPressed = { key ->
                if (key == "BACKSPACE") {
                    rawDigits = if (rawDigits.length > 1) {
                        rawDigits.dropLast(1)
                    } else {
                        "0"
                    }
                } else {
                    if (rawDigits.length < 11) {
                        rawDigits = if (rawDigits == "0") {
                            key
                        } else {
                            rawDigits + key
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Confirm Reconciliation Button
        Button(
            onClick = {
                onConfirmReconciliation(actualBalance, note)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = strings.confirmReconciliationAction.uppercase(),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 0.5.sp,
                color = Color.White
            )
        }
    }
}
