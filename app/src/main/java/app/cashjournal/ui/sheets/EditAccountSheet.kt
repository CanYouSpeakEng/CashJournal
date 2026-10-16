package app.cashjournal.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.AccountType
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.theme.Berry
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.X

@Composable
fun EditAccountSheet(
    account: Account,
    onSave: (name: String, accountType: AccountType, accentKey: AccentKey) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    canDelete: Boolean = true,
    onCannotDeleteToast: () -> Unit = {},
    modifier: Modifier = Modifier,
    languageCode: String = "en"
) {
    val strings = AppStrings.get(languageCode)
    var selectedType by remember { mutableStateOf(account.accountType) }
    var name by remember { mutableStateOf(account.name) }
    var selectedAccent by remember { mutableStateOf(account.accentKey) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
                text = strings.editAccountTitle,
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

        // Account Category / Type Selector
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = strings.accountTypeLabel,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = Slate500
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccountType.entries.forEach { type ->
                    val isSelected = type == selectedType
                    val chipBg = if (isSelected) Cobalt else Slate100
                    val contentColor = if (isSelected) Color.White else Charcoal

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .clickable { selectedType = type }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = type.icon,
                            contentDescription = type.localizedTitle(languageCode),
                            tint = contentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = type.localizedTitle(languageCode),
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = contentColor
                        )
                    }
                }
            }
        }

        // Account Name Input
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = strings.accountNameLabel,
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
                                    text = selectedType.localizedPlaceholder(languageCode),
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

        // Accent Color Picker: 6 circles (Cobalt, Emerald, Persimmon, Saffron, Iris, Berry)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = strings.accentColorLabel,
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

        Spacer(modifier = Modifier.height(4.dp))

        // Save Changes Button
        Button(
            onClick = {
                if (name.isNotBlank()) {
                    onSave(name.trim(), selectedType, selectedAccent)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = strings.saveChangesAction,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }

        // Delete Account Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (canDelete) {
                        showDeleteConfirmDialog = true
                    } else {
                        onCannotDeleteToast()
                    }
                }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Trash,
                contentDescription = "Delete",
                tint = Berry,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "  ${strings.deleteAccountAction}",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Berry
            )
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = strings.deleteAccountConfirmTitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = strings.deleteAccountConfirmMessage,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 13.sp,
                    color = Slate500
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Berry)
                ) {
                    Text(
                        text = strings.deleteAccountAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(
                        text = strings.cancelAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate500
                    )
                }
            }
        )
    }
}
