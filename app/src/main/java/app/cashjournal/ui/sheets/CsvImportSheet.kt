package app.cashjournal.ui.sheets

import android.view.HapticFeedbackConstants
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.data.csv.ParsedCsvTransaction
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.ui.CsvImportState
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.FileCsv
import com.adamglin.phosphoricons.regular.X
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CsvImportSheet(
    importState: CsvImportState,
    accounts: List<Account>,
    currencyCode: String,
    languageCode: String,
    hapticsEnabled: Boolean,
    onTargetAccountChange: (String) -> Unit,
    onToggleItem: (String) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onConfirmImport: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val strings = AppStrings.get(languageCode)
    val currency = Currency.findByCode(currencyCode)
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", if (languageCode == "in") Locale("id", "ID") else Locale.US)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sheet Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Cobalt.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.FileCsv,
                        contentDescription = null,
                        tint = Cobalt,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = strings.csvImportTitle,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Charcoal
                    )
                    Text(
                        text = importState.fileName.ifBlank { "Statement.csv" },
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Slate100)
                    .clickable {
                        if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onDismiss()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.X,
                    contentDescription = "Close",
                    tint = Slate600,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Summary Statistics Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.csvImportSummaryFormat.format(
                            importState.totalFound - importState.duplicatesCount,
                            importState.duplicatesCount,
                            if (importState.duplicatesCount == 1) "" else "s"
                        ),
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Charcoal
                    )
                    Text(
                        text = "${importState.selectedCount} / ${importState.totalFound} selected",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (importState.selectedCount == importState.totalFound) strings.csvImportDeselectAll else strings.csvImportSelectAll,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Cobalt,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Cobalt.copy(alpha = 0.08f))
                            .clickable {
                                if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                val shouldSelectAll = importState.selectedCount < importState.totalFound
                                onSelectAll(shouldSelectAll)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Target Account Selector Row
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = strings.csvImportTargetAccount,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Slate500
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accounts.forEach { acc ->
                    val isSelected = acc.id == importState.targetAccountId
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Cobalt.copy(alpha = 0.12f) else Slate100)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Cobalt else Slate200,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                onTargetAccountChange(acc.id)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = acc.accountType.icon,
                            contentDescription = acc.name,
                            tint = if (isSelected) Cobalt else acc.accentKey.color,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = acc.name,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Cobalt else Charcoal
                        )
                    }
                }
            }
        }

        // Transactions Preview List
        Text(
            text = strings.csvImportPreviewTitle,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = Slate500
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 340.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(importState.items, key = { it.id }) { item ->
                CsvItemRow(
                    item = item,
                    currency = currency,
                    dateFormatter = dateFormatter,
                    duplicateBadgeText = strings.csvImportDuplicateBadge,
                    onToggle = {
                        if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onToggleItem(item.id)
                    }
                )
            }
        }

        // Bottom Action Button
        Button(
            onClick = {
                if (hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onConfirmImport()
            },
            enabled = importState.selectedCount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Cobalt,
                disabledContainerColor = Slate200
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = strings.csvImportActionFormat.format(importState.selectedCount),
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (importState.selectedCount > 0) Color.White else Slate400
            )
        }
    }
}

@Composable
private fun CsvItemRow(
    item: ParsedCsvTransaction,
    currency: Currency,
    dateFormatter: SimpleDateFormat,
    duplicateBadgeText: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = item.type == TransactionType.EXPENSE
    val formattedDate = dateFormatter.format(Date(item.parsedDateMillis))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (item.isSelected) Slate100.copy(alpha = 0.7f) else Color.White)
            .border(
                width = 1.dp,
                color = if (item.isSelected) Cobalt.copy(alpha = 0.3f) else Slate200.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(
            checked = item.isSelected,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = Cobalt,
                uncheckedColor = Slate400
            ),
            modifier = Modifier.size(20.dp)
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(item.category.defaultAccent.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.category.icon,
                contentDescription = item.category.defaultName,
                tint = item.category.defaultAccent.color,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Charcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formattedDate,
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 11.sp,
                    color = Slate500
                )

                if (item.isDuplicate) {
                    Text(
                        text = duplicateBadgeText,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Persimmon,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Persimmon.copy(alpha = 0.12f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Amount
        val sign = if (isExpense) "-" else "+"
        val amountFormatted = Currency.formatMinor(item.amount, currency.code)
        Text(
            text = "$sign $amountFormatted",
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isExpense) Charcoal else Emerald
        )
    }
}
