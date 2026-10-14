package app.cashjournal.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.MainUiState
import app.cashjournal.ui.SafeToSpendStatus
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.MaskedAmount
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.components.TransactionRow
import app.cashjournal.ui.theme.CashColors
import app.cashjournal.ui.theme.CashTypography
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Saffron
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Wallet
import androidx.compose.material3.Icon

@Composable
fun DashboardScreen(
    state: MainUiState,
    onSeeAllAccounts: () -> Unit,
    onAccountClick: (Account) -> Unit,
    onViewTransactions: () -> Unit,
    onSetBudgetClick: () -> Unit,
    onAddAccountClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = Currency.findByCode(state.userPreferences.currencyCode)
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Total Net Worth Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "TOTAL NET WORTH",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = Slate500
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currency.symbol,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Cobalt
                )
                Spacer(modifier = Modifier.width(6.dp))
                MaskedAmount(
                    amountMinor = state.totalNetWorth,
                    currencyCode = state.userPreferences.currencyCode,
                    isMasked = state.hideBalances,
                    textStyle = CashTypography.HeroAmount
                )
            }

            Text(
                text = "${state.accounts.size} accounts · all in ${currency.code}",
                fontFamily = PlusJakartaSansFamily,
                fontSize = 12.sp,
                color = Slate500
            )
        }

        // 2. Safe-to-Spend Card (28dp glass)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 28.dp
        ) {
            val safe = state.safeToSpend
            val statusColor = when (safe.status) {
                SafeToSpendStatus.SAFE -> Emerald
                SafeToSpendStatus.NEAR_LIMIT -> Saffron
                SafeToSpendStatus.RECALIBRATE -> Color(0xFFF87171)
                SafeToSpendStatus.SET_BUDGET -> Slate500
            }

            val statusText = when (safe.status) {
                SafeToSpendStatus.SAFE -> "Safe"
                SafeToSpendStatus.NEAR_LIMIT -> "Near Limit"
                SafeToSpendStatus.RECALIBRATE -> "Recalibrate"
                SafeToSpendStatus.SET_BUDGET -> "Set a monthly budget"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SAFE-TO-SPEND TODAY",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = Slate500
                    )

                    if (safe.status == SafeToSpendStatus.SET_BUDGET) {
                        Text(
                            text = "Set a monthly budget",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Charcoal,
                            modifier = Modifier.clickable { onSetBudgetClick() }
                        )
                    } else {
                        Row(verticalAlignment = Alignment.Bottom) {
                            MaskedAmount(
                                amountMinor = safe.safeToSpendToday,
                                currencyCode = state.userPreferences.currencyCode,
                                isMasked = state.hideBalances,
                                textStyle = CashTypography.HeroAmount.copy(fontSize = 26.sp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "/ day",
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = Slate500,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    // Status pill and subline
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = statusColor
                            )
                        }

                        if (safe.budget > 0) {
                            val daysLabel = if (safe.isLastDay) "Last day" else "${safe.daysLeft} days left"
                            Text(
                                text = "$daysLabel · ${Currency.formatMinor(safe.remaining, state.userPreferences.currencyCode)} remaining",
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                    }
                }

                // 64dp Circular Gauge Ring
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(64.dp)) {
                        val strokeWidth = 6.dp.toPx()
                        drawArc(
                            color = Slate200,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = statusColor,
                            startAngle = -90f,
                            sweepAngle = (safe.percentSpent.coerceIn(0f, 1f) * 360f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${safe.displayPercent}%",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Charcoal
                        )
                        Text(
                            text = "spent",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 9.sp,
                            color = Slate500
                        )
                    }
                }
            }
        }

        // 3. Active Accounts Header + See All
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Accounts",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Charcoal
            )

            Text(
                text = "See All",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Cobalt,
                modifier = Modifier.clickable { onSeeAllAccounts() }
            )
        }

        // 4. Horizontal Accounts Carousel
        if (state.accounts.isEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddAccountClick() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Wallet,
                        contentDescription = "Wallet",
                        tint = Cobalt,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Add your first account",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Charcoal
                        )
                        Text(
                            text = "Track bank, cash, or e-wallet balances",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                state.accounts.forEach { account ->
                    GlassCard(
                        modifier = Modifier
                            .width(130.dp)
                            .height(96.dp)
                            .clickable { onAccountClick(account) },
                        cornerRadius = 22.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(account.accentKey.color)
                                )
                                Text(
                                    text = account.name,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Charcoal,
                                    maxLines = 1
                                )
                            }

                            MaskedAmount(
                                amountMinor = account.balance,
                                currencyCode = state.userPreferences.currencyCode,
                                isMasked = state.hideBalances,
                                textStyle = CashTypography.Mono12.copy(fontSize = 13.sp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Recent Activity Header + View transactions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Activity",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Charcoal
            )

            Text(
                text = "View transactions",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Cobalt,
                modifier = Modifier.clickable { onViewTransactions() }
            )
        }

        // 6. Recent Transactions List (GlassCard wrapper)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.recentTransactions.isEmpty()) {
                    Text(
                        text = "No transactions yet",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 13.sp,
                        color = Slate500,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    state.recentTransactions.forEach { tx ->
                        TransactionRow(
                            transaction = tx,
                            isMasked = state.hideBalances,
                            currencyCode = state.userPreferences.currencyCode
                        )
                    }
                }
            }
        }

        // 7. Monthly Budget Runway Card (16dp subtle glass)
        SubtleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            val safe = state.safeToSpend
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Budget Runway",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Charcoal
                    )

                    Text(
                        text = "Budget ${Currency.formatMinor(safe.budget, state.userPreferences.currencyCode)}",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Cobalt
                    )
                }

                // Progress Bar
                val statusColor = when (safe.status) {
                    SafeToSpendStatus.SAFE -> Emerald
                    SafeToSpendStatus.NEAR_LIMIT -> Saffron
                    SafeToSpendStatus.RECALIBRATE -> Color(0xFFF87171)
                    SafeToSpendStatus.SET_BUDGET -> Slate500
                }

                LinearProgressIndicator(
                    progress = { safe.percentSpent.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = statusColor,
                    trackColor = Slate200,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spent ${Currency.formatMinor(safe.spentThisMonth, state.userPreferences.currencyCode)}",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = Slate600
                    )

                    Text(
                        text = if (safe.isLastDay) "Last day" else "Days left: ${safe.daysLeft} days",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }
        }
    }
}
