package app.cashjournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import app.cashjournal.ui.i18n.AppStrings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.Transaction
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.ui.MainUiState
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.MaskedAmount
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.components.TransactionRow
import app.cashjournal.ui.theme.CashTypography
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import kotlin.math.roundToInt
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.PencilSimple

@Composable
fun AccountDetailScreen(
    account: Account,
    state: MainUiState,
    onTransactionClick: ((Transaction) -> Unit)? = null,
    onReconcileClick: (() -> Unit)? = null,
    onEditAccountClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = AppStrings.get(state.userPreferences.language)
    val accountTransactions = remember(state.transactions, account.id) {
        state.transactions.filter { tx ->
            tx.sourceAccountId == account.id || tx.destinationAccountId == account.id
        }
    }

    // Inflow: income into this account + transfers in
    val totalInflow = remember(accountTransactions, account.id) {
        accountTransactions.filter { tx ->
            (tx.type == TransactionType.INCOME && tx.sourceAccountId == account.id) ||
                    (tx.type == TransactionType.TRANSFER && tx.destinationAccountId == account.id)
        }.sumOf { it.amount }
    }

    // Outflow: expenses from this account + transfers out
    val totalOutflow = remember(accountTransactions, account.id) {
        accountTransactions.filter { tx ->
            (tx.type == TransactionType.EXPENSE && tx.sourceAccountId == account.id) ||
                    (tx.type == TransactionType.TRANSFER && tx.sourceAccountId == account.id)
        }.sumOf { it.amount }
    }

    val pctOfTotal = if (state.totalNetWorth > 0) {
        ((account.balance.toFloat() / state.totalNetWorth.toFloat()) * 100).roundToInt()
    } else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 68.dp)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                val mistHeightPx = 64.dp.toPx()
                val mistStartRatio = ((size.height - mistHeightPx) / size.height).coerceIn(0f, 1f)
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black,
                            mistStartRatio to Color.Black,
                            mistStartRatio + (1f - mistStartRatio) * 0.4f to Color.Black.copy(alpha = 0.6f),
                            mistStartRatio + (1f - mistStartRatio) * 0.75f to Color.Black.copy(alpha = 0.15f),
                            1.0f to Color.Transparent
                        )
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Row: Account Category + Name + Percent of Total Pill + Edit Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = account.accountType.icon,
                    contentDescription = account.accountType.localizedTitle(state.userPreferences.language),
                    tint = account.accentKey.color,
                    modifier = Modifier.size(22.dp)
                )

                Text(
                    text = account.name,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = account.accentKey.color
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(account.accentKey.color.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = strings.accountDetailPctOfTotal.format(pctOfTotal),
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = account.accentKey.color
                    )
                }

                if (onEditAccountClick != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(account.accentKey.color.copy(alpha = 0.12f))
                            .clickable { onEditAccountClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.PencilSimple,
                            contentDescription = "Edit Account",
                            tint = account.accentKey.color,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // Balance Card (24dp glass) with 4dp left border
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(account.accentKey.color)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.accountDetailBalance,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.8.sp,
                            color = Slate500
                        )

                        if (onReconcileClick != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(account.accentKey.color.copy(alpha = 0.12f))
                                    .clickable { onReconcileClick() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = strings.reconcileBalanceButton,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = account.accentKey.color
                                )
                            }
                        }
                    }

                    MaskedAmount(
                        amountMinor = account.balance,
                        currencyCode = state.userPreferences.currencyCode,
                        isMasked = state.hideBalances,
                        showSymbol = true,
                        textStyle = CashTypography.HeroAmount.copy(fontSize = 28.sp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Two Tiles: Inflow (emerald) & Outflow (charcoal)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SubtleGlassCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = strings.accountDetailInflow,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Emerald
                    )

                    MaskedAmount(
                        amountMinor = totalInflow,
                        currencyCode = state.userPreferences.currencyCode,
                        isMasked = state.hideBalances,
                        textStyle = CashTypography.Mono12.copy(fontSize = 15.sp, color = Emerald)
                    )
                }
            }

            SubtleGlassCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = strings.accountDetailOutflow,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Charcoal
                    )

                    MaskedAmount(
                        amountMinor = totalOutflow,
                        currencyCode = state.userPreferences.currencyCode,
                        isMasked = state.hideBalances,
                        textStyle = CashTypography.Mono12.copy(fontSize = 15.sp, color = Charcoal)
                    )
                }
            }
        }

        // Account Activity Header
        Text(
            text = strings.accountDetailActivity,
            fontFamily = PlusJakartaSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Charcoal,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Activity Transactions List
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (accountTransactions.isEmpty()) {
                    Text(
                        text = strings.accountDetailNoTransactions,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 13.sp,
                        color = Slate500,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    accountTransactions.forEach { tx ->
                        TransactionRow(
                            transaction = tx,
                            isMasked = state.hideBalances,
                            currencyCode = state.userPreferences.currencyCode,
                            accounts = state.accounts,
                            onClick = if (onTransactionClick != null) { { onTransactionClick(tx) } } else null
                        )
                    }
                }
            }
        }
    }
}
