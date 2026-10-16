package app.cashjournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import app.cashjournal.domain.model.AccountColor
import app.cashjournal.ui.i18n.ScreenStrings
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

@Composable
fun CustomHexColorDialog(
    currentHex: String,
    strings: ScreenStrings,
    onDismiss: () -> Unit,
    onColorSelected: (AccountColor) -> Unit
) {
    var rawInput by remember {
        mutableStateOf(currentHex.removePrefix("#").uppercase())
    }

    val cleanHex = rawInput.trim().removePrefix("#")
    val isValid = cleanHex.length == 6 && cleanHex.all { it in "0123456789ABCDEFabcdef" }

    val previewColor = remember(cleanHex, isValid) {
        if (isValid) {
            try {
                Color((0xFF000000L or cleanHex.toLong(16)).toInt())
            } catch (e: Exception) {
                Cobalt
            }
        } else {
            Cobalt
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.customColorTitle,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Charcoal
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Live Color Preview & Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Preview Swatch
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isValid) previewColor else Slate200)
                            .border(1.dp, Slate200, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isValid) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Hex Input Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, if (isValid) Cobalt else Slate200, RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Slate500
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            BasicTextField(
                                value = rawInput,
                                onValueChange = { input ->
                                    val filtered = input.filter { it in "0123456789ABCDEFabcdef" }
                                    if (filtered.length <= 6) {
                                        rawInput = filtered.uppercase()
                                    }
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Charcoal
                                ),
                                cursorBrush = SolidColor(Cobalt),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    if (rawInput.isEmpty()) {
                                        Text(
                                            text = "0060AF",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Normal,
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

                if (!isValid && cleanHex.isNotEmpty()) {
                    Text(
                        text = strings.invalidHexError,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        color = Color(0xFFDC2626)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        val parsed = AccountColor.fromHex("#$cleanHex")
                        onColorSelected(parsed)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Cobalt,
                    disabledContainerColor = Slate200
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = strings.applyAction,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isValid) Color.White else Slate500
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Slate600
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}
