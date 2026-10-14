package app.cashjournal.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.theme.CashColors
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.X

@Composable
fun NewAccountSheet(
    currencyCode: String,
    onCreateAccount: (name: String, startingBalance: Long, accentKey: AccentKey) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var rawBalance by remember { mutableStateOf("") }
    var selectedAccent by remember { mutableStateOf(AccentKey.COBALT) }

    val currency = Currency.findByCode(currencyCode)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row with Title and X
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "New Account",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Charcoal
            )

            Icon(
                imageVector = PhosphorIcons.Regular.X,
                contentDescription = "Close",
                tint = Slate500,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { onDismiss() }
            )
        }

        // Account Name Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "ACCOUNT NAME",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = Slate500
            )

            SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(14.dp)) {
                    BasicTextField(
                        value = name,
                        onValueChange = { if (it.length <= 28) name = it },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Charcoal
                        ),
                        cursorBrush = SolidColor(Cobalt),
                        decorationBox = { innerTextField ->
                            if (name.isEmpty()) {
                                Text(
                                    text = "e.g. Checking Account, Cash Wallet",
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 14.sp,
                                    color = Slate400
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Starting Balance Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "STARTING BALANCE (${currency.symbol})",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = Slate500
            )

            SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(14.dp)) {
                    BasicTextField(
                        value = rawBalance,
                        onValueChange = { digits ->
                            rawBalance = digits.filter { it.isDigit() }
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Charcoal
                        ),
                        cursorBrush = SolidColor(Cobalt),
                        decorationBox = { innerTextField ->
                            if (rawBalance.isEmpty()) {
                                Text(
                                    text = "1000000",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 16.sp,
                                    color = Slate400
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Accent Color Picker: 6 circles (Cobalt, Emerald, Persimmon, Saffron, Iris, Berry)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "ACCENT COLOR",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = Slate500
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AccentKey.entries.forEach { accent ->
                    val isSelected = accent == selectedAccent
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(if (isSelected) 1.1f else 1.0f)
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, Cobalt, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .padding(if (isSelected) 3.dp else 0.dp)
                            .clip(CircleShape)
                            .background(accent.color)
                            .clickable { selectedAccent = accent },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Create Account Button
        Button(
            onClick = {
                val balance = rawBalance.toLongOrNull() ?: 0L
                onCreateAccount(name, balance, selectedAccent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Create Account",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}
