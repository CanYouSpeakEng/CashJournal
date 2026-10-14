package app.cashjournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.domain.model.Transaction
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowDownLeft

@Composable
fun TransactionRow(
    transaction: Transaction,
    isMasked: Boolean,
    currencyCode: String = "IDR",
    dateText: String = "",
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val icon = when (transaction.type) {
        TransactionType.TRANSFER -> Category.transferIcon
        TransactionType.INCOME -> PhosphorIcons.Regular.ArrowDownLeft
        TransactionType.EXPENSE -> transaction.category.icon
    }

    TransactionRow(
        title = transaction.title,
        categoryName = if (transaction.type == TransactionType.TRANSFER) "Transfer" else transaction.category.defaultName,
        categoryIcon = icon,
        accent = transaction.accentKey,
        type = transaction.type,
        amountMinor = transaction.amount,
        currencyCode = currencyCode,
        dateText = dateText,
        balancesVisible = !isMasked,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable

fun TransactionRow(
    title: String,
    categoryName: String,
    categoryIcon: ImageVector,
    accent: AccentKey,
    type: TransactionType,
    amountMinor: Long,
    currencyCode: String = "IDR",
    dateText: String = "",
    balancesVisible: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(clickModifier)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 36dp Icon Tile with tinted background
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.tint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = categoryName,
                tint = accent.color,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Category/Date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Charcoal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val subtitle = if (dateText.isNotEmpty()) "$categoryName · $dateText" else categoryName
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = Slate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Amount:
        // Income in emerald with "+"
        // Expenses in charcoal with "−" (U+2212)
        // Transfers in charcoal, no sign
        val amountColor: Color
        val signPrefix: String
        when (type) {
            TransactionType.INCOME -> {
                amountColor = Emerald
                signPrefix = "+"
            }
            TransactionType.EXPENSE -> {
                amountColor = Charcoal
                signPrefix = "−"
            }
            TransactionType.TRANSFER -> {
                amountColor = Charcoal
                signPrefix = ""
            }
        }

        val formattedAmount = Currency.formatMinor(
            minorUnits = amountMinor,
            currencyCode = currencyCode,
            includeSymbol = true
        )

        val fullDisplay = if (signPrefix.isNotEmpty()) "$signPrefix$formattedAmount" else formattedAmount

        MaskedAmountText(
            amountText = fullDisplay,
            currencySymbol = Currency.findByCode(currencyCode).symbol,
            balancesVisible = balancesVisible,
            keepSymbolVisible = false,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}
