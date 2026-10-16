package app.cashjournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
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
import app.cashjournal.ui.MainUiState
import app.cashjournal.ui.components.CountryFlag
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.MaskedAmount
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.theme.CashTypography
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Saffron
import app.cashjournal.ui.theme.Slate100
import kotlin.math.pow
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowDown
import com.adamglin.phosphoricons.regular.ArrowUp
import com.adamglin.phosphoricons.regular.ArrowsClockwise
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Wallet
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun AccountsScreen(
    state: MainUiState,
    onAccountClick: (Account) -> Unit,
    onAddAccountClick: () -> Unit,
    onMoveAccount: (index: Int, moveUp: Boolean) -> Unit,
    onToggleReorderMode: (Boolean) -> Unit,
    onOpenWorldCurrencies: () -> Unit,
    modifier: Modifier = Modifier,
    onRefreshRates: () -> Unit = {}
) {
    var selectedWorldCurrency by remember { mutableStateOf("USD") }
    val mainCurrency = Currency.findByCode(state.userPreferences.currencyCode)
    val strings = AppStrings.get(state.userPreferences.language)

    val cachedRates = state.cachedRates
    val ratesToIdr = mapOf(
        "USD" to 15700.0,
        "EUR" to 17100.0,
        "SGD" to 11900.0,
        "JPY" to 105.0,
        "MYR" to 3550.0,
        "SAR" to 4180.0
    )

    val sourceDecimals = Currency.getDecimals(mainCurrency.code)
    val targetDecimals = Currency.getDecimals(selectedWorldCurrency)

    val convertedTotal = if (cachedRates != null && cachedRates.rates.isNotEmpty()) {
        app.cashjournal.data.currency.CurrencyConverter.convertMinorUnits(
            amountMinor = state.totalNetWorth,
            sourceCode = mainCurrency.code,
            targetCode = selectedWorldCurrency,
            baseCode = cachedRates.baseCode,
            rates = cachedRates.rates,
            sourceDecimals = sourceDecimals,
            targetDecimals = targetDecimals
        )
    } else {
        val currentRate = ratesToIdr[selectedWorldCurrency] ?: 15700.0
        val totalMajor = state.totalNetWorth.toDouble()
        ((totalMajor / currentRate) * 100).toLong()
    }

    val maxBalance = state.accounts.maxOfOrNull { it.balance }?.coerceAtLeast(1L) ?: 1L

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
        // Top Header Row with Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = strings.accountsTitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Charcoal
                )
                Text(
                    text = "${String.format(strings.accountsCountFormat, state.accounts.size)}${if (state.isReorderMode) strings.reorderModeSuffix else ""}",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 13.sp,
                    color = Slate500
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit / Done Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (state.isReorderMode) Cobalt else Slate100)
                        .clickable { onToggleReorderMode(!state.isReorderMode) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.isReorderMode) strings.doneButton else strings.editButton,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (state.isReorderMode) Color.White else Charcoal
                    )
                }

                // 32dp Square "+" Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Cobalt)
                        .clickable { onAddAccountClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Plus,
                        contentDescription = "New Account",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // World Currency Card (Subtle Glass)
        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.worldCurrenciesCardTitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate600
                    )

                    Icon(
                        imageVector = PhosphorIcons.Regular.ArrowsClockwise,
                        contentDescription = "Refresh",
                        tint = Slate500,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable { onRefreshRates() }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${strings.baseNetWorthLabel}:",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                    MaskedAmount(
                        amountMinor = state.totalNetWorth,
                        currencyCode = mainCurrency.code,
                        isMasked = state.hideBalances,
                        textStyle = CashTypography.Mono12.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate600)
                    )
                }

                val rateForOneUnit = if (cachedRates != null && cachedRates.rates.isNotEmpty()) {
                    val oneUnitMinor = 10.0.pow(targetDecimals.toDouble()).toLong()
                    app.cashjournal.data.currency.CurrencyConverter.convertMinorUnits(
                        amountMinor = oneUnitMinor,
                        sourceCode = selectedWorldCurrency,
                        targetCode = mainCurrency.code,
                        baseCode = cachedRates.baseCode,
                        rates = cachedRates.rates,
                        sourceDecimals = targetDecimals,
                        targetDecimals = sourceDecimals
                    )
                } else {
                    ratesToIdr[selectedWorldCurrency]?.toLong() ?: 15700L
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = Currency.findByCode(selectedWorldCurrency).symbol,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Cobalt
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        MaskedAmount(
                            amountMinor = convertedTotal,
                            currencyCode = selectedWorldCurrency,
                            isMasked = state.hideBalances,
                            textStyle = CashTypography.HeroAmount.copy(fontSize = 24.sp)
                        )
                    }

                    Text(
                        text = "1 $selectedWorldCurrency = ${Currency.formatMinor(rateForOneUnit, mainCurrency.code)}",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (cachedRates != null) {
                        val dotColor = if (state.isRatesOffline) Saffron else Emerald
                        val diffMs = System.currentTimeMillis() - cachedRates.fetchedAt
                        val diffMin = (diffMs / 60000L).coerceAtLeast(0L)
                        val syncStatus = if (state.isRatesOffline) {
                            "${strings.ratesOfflineLabel} · (Daily rates: 00:00 UTC)"
                        } else if (diffMin < 1) {
                            "${strings.ratesSyncedJustNow} · (Daily rates: 00:00 UTC)"
                        } else {
                            "${String.format(strings.ratesSyncedMinsAgo, diffMin)} · (Daily rates: 00:00 UTC)"
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(dotColor, CircleShape)
                        )
                        Text(
                            text = syncStatus,
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    } else {
                        Text(
                            text = "No rates yet",
                            fontFamily = PlusJakartaSansFamily,
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                // Quick Currency Chips with Country Flags
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val chips = listOf("USD", "EUR", "SGD", "JPY", "GBP", "MYR", "SAR", "THB").filter { it != mainCurrency.code }
                    chips.forEach { code ->
                        val isSelected = code == selectedWorldCurrency
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Cobalt else Slate100)
                                .clickable { selectedWorldCurrency = code }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            CountryFlag(currencyCode = code, size = 16.dp, cornerRadius = 4.dp)
                            Text(
                                text = code,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else Charcoal
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate100)
                            .clickable { onOpenWorldCurrencies() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "+ World",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Cobalt
                        )
                    }
                }
            }
        }

        // Account Cards List
        if (state.accounts.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Wallet,
                        contentDescription = "Wallet",
                        tint = Slate400,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "No accounts yet",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Charcoal
                    )
                    Button(
                        onClick = onAddAccountClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Add account",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            state.accounts.forEachIndexed { index, account ->
                val pctOfTotal = if (state.totalNetWorth > 0) {
                    ((account.balance.toFloat() / state.totalNetWorth.toFloat()) * 100).roundToInt()
                } else 0

                val barRatio = max(0.02f, account.balance.toFloat() / maxBalance.toFloat()).coerceIn(0.02f, 1f)

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !state.isReorderMode) { onAccountClick(account) },
                    cornerRadius = 24.dp
                ) {
                    // Frosted Account Color Haze
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        account.accentKey.color.copy(alpha = 0.14f),
                                        account.accentKey.color.copy(alpha = 0.03f),
                                        Color.Transparent
                                    ),
                                    center = Offset(0f, 0f),
                                    radius = 450f
                                )
                            )
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // 4dp left border in account color
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(90.dp)
                                .background(account.accentKey.color)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Top Row: Name and Percent Pill + Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = account.name.uppercase(),
                                        fontFamily = PlusJakartaSansFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = account.accentKey.color
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(account.accentKey.color.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$pctOfTotal%",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = account.accentKey.color
                                        )
                                    }
                                }

                                if (state.isReorderMode) {
                                    // Up / Down reorder buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val canUp = index > 0
                                        val canDown = index < state.accounts.size - 1

                                        Icon(
                                            imageVector = PhosphorIcons.Regular.ArrowUp,
                                            contentDescription = "Move Up",
                                            tint = if (canUp) Charcoal else Slate400.copy(alpha = 0.3f),
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable(enabled = canUp) { onMoveAccount(index, true) }
                                        )

                                        Icon(
                                            imageVector = PhosphorIcons.Regular.ArrowDown,
                                            contentDescription = "Move Down",
                                            tint = if (canDown) Charcoal else Slate400.copy(alpha = 0.3f),
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clickable(enabled = canDown) { onMoveAccount(index, false) }
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.CaretRight,
                                        contentDescription = "Detail",
                                        tint = Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Balance
                            MaskedAmount(
                                amountMinor = account.balance,
                                currencyCode = state.userPreferences.currencyCode,
                                isMasked = state.hideBalances,
                                textStyle = CashTypography.Mono12.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            )

                            // Thin bar against largest balance
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(Slate200)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(barRatio)
                                        .fillMaxHeight()
                                        .background(account.accentKey.color)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
