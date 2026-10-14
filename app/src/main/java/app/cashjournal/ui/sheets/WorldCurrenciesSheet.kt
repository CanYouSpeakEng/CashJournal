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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import app.cashjournal.data.currency.CachedExchangeRates
import app.cashjournal.data.currency.CurrencyConverter
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
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
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.X
import kotlin.math.pow

data class WorldCurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String
)

@Composable
fun WorldCurrenciesSheet(
    totalNetWorth: Long,
    mainCurrencyCode: String,
    cachedRates: CachedExchangeRates?,
    isRatesOffline: Boolean,
    selectedCode: String,
    onSelectCode: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val majorCurrencies = remember {
        listOf(
            WorldCurrencyInfo("USD", "US Dollar", "US$"),
            WorldCurrencyInfo("EUR", "Euro", "€"),
            WorldCurrencyInfo("SGD", "Singapore Dollar", "S$"),
            WorldCurrencyInfo("JPY", "Japanese Yen", "¥"),
            WorldCurrencyInfo("GBP", "British Pound", "£"),
            WorldCurrencyInfo("AUD", "Australian Dollar", "A$"),
            WorldCurrencyInfo("MYR", "Malaysian Ringgit", "RM"),
            WorldCurrencyInfo("SAR", "Saudi Riyal", "SR"),
            WorldCurrencyInfo("CNY", "Chinese Yuan", "¥"),
            WorldCurrencyInfo("CAD", "Canadian Dollar", "C$"),
            WorldCurrencyInfo("CHF", "Swiss Franc", "CHF"),
            WorldCurrencyInfo("HKD", "Hong Kong Dollar", "HK$"),
            WorldCurrencyInfo("NZD", "New Zealand Dollar", "NZ$"),
            WorldCurrencyInfo("KRW", "South Korean Won", "₩"),
            WorldCurrencyInfo("THB", "Thai Baht", "฿"),
            WorldCurrencyInfo("IDR", "Indonesian Rupiah", "Rp")
        )
    }

    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            majorCurrencies
        } else {
            majorCurrencies.filter {
                it.code.contains(searchQuery, ignoreCase = true) ||
                        it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "World Currencies",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val dotColor = if (isRatesOffline) Saffron else Emerald
                    val statusText = if (cachedRates != null) {
                        if (isRatesOffline) "Offline · last update ${cachedRates.timeLastUpdateUtc}"
                        else "Updated daily · ${cachedRates.timeLastUpdateUtc}"
                    } else {
                        "No rates yet"
                    }

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Text(
                        text = statusText,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
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

        // Search Field
        SubtleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                    contentDescription = "Search",
                    tint = Slate400,
                    modifier = Modifier.size(18.dp)
                )

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        color = Charcoal
                    ),
                    cursorBrush = SolidColor(Cobalt),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search currency (USD, JPY, EUR, SGD, GBP...)",
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 14.sp,
                                color = Slate400
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        // Currency Rows List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sourceDecimals = Currency.getDecimals(mainCurrencyCode)

            filteredList.forEach { currency ->
                val isSelected = currency.code.equals(selectedCode, ignoreCase = true)
                val targetDecimals = Currency.getDecimals(currency.code)

                val convertedTotal = if (cachedRates != null && cachedRates.rates.isNotEmpty()) {
                    CurrencyConverter.convertMinorUnits(
                        amountMinor = totalNetWorth,
                        sourceCode = mainCurrencyCode,
                        targetCode = currency.code,
                        baseCode = cachedRates.baseCode,
                        rates = cachedRates.rates,
                        sourceDecimals = sourceDecimals,
                        targetDecimals = targetDecimals
                    )
                } else {
                    totalNetWorth
                }

                val rateForOneUnit = if (cachedRates != null && cachedRates.rates.isNotEmpty()) {
                    val oneUnitMinor = 10.0.pow(targetDecimals.toDouble()).toLong()
                    CurrencyConverter.convertMinorUnits(
                        amountMinor = oneUnitMinor,
                        sourceCode = currency.code,
                        targetCode = mainCurrencyCode,
                        baseCode = cachedRates.baseCode,
                        rates = cachedRates.rates,
                        sourceDecimals = targetDecimals,
                        targetDecimals = sourceDecimals
                    )
                } else {
                    1L
                }

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCode(currency.code) },
                    borderWidth = if (isSelected) 2.dp else 1.dp,
                    borderColor = if (isSelected) Cobalt else Slate200
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Badge
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Slate100),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currency.code,
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Cobalt
                                )
                            }

                            Column {
                                Text(
                                    text = currency.name,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Charcoal
                                )
                                Text(
                                    text = "1 ${currency.code} = ${Currency.formatMinor(rateForOneUnit, mainCurrencyCode)}",
                                    fontFamily = SpaceGroteskFamily,
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Currency.formatMinor(convertedTotal, currency.code),
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Charcoal
                            )

                            Text(
                                text = if (isSelected) "Active" else "Select",
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) Cobalt else Slate400
                            )
                        }
                    }
                }
            }
        }
    }
}
