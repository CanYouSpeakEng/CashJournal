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
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.PlusJakartaSansFamily
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

data class WorldCurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val rateToIdr: Double
)

@Composable
fun WorldCurrenciesSheet(
    totalNetWorthIdr: Long,
    selectedCode: String,
    onSelectCode: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val worldCurrencies = listOf(
        WorldCurrencyInfo("USD", "US Dollar", "US$", 15700.0),
        WorldCurrencyInfo("EUR", "Euro", "€", 17100.0),
        WorldCurrencyInfo("SGD", "Singapore Dollar", "S$", 11900.0),
        WorldCurrencyInfo("JPY", "Japanese Yen", "¥", 105.0),
        WorldCurrencyInfo("GBP", "British Pound", "£", 19800.0),
        WorldCurrencyInfo("AUD", "Australian Dollar", "A$", 10400.0),
        WorldCurrencyInfo("MYR", "Malaysian Ringgit", "RM", 3550.0),
        WorldCurrencyInfo("SAR", "Saudi Riyal", "SR", 4180.0)
    )

    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            worldCurrencies
        } else {
            worldCurrencies.filter {
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
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Emerald))
                    Text(
                        text = "Updated daily · Oct 14, 09:30",
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
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.MagnifyingGlass,
                    contentDescription = "Search",
                    tint = Slate500,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        color = Charcoal
                    ),
                    cursorBrush = SolidColor(Cobalt),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search currency (USD, JPY, EUR, SGD...)",
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

        // List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filteredList.forEach { item ->
                val isSelected = item.code.equals(selectedCode, ignoreCase = true)
                val converted = (totalNetWorthIdr / item.rateToIdr).toLong()

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCode(item.code) },
                    borderWidth = if (isSelected) 2.dp else 1.dp,
                    borderColor = if (isSelected) Cobalt else Slate200
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Slate100)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.code,
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Cobalt
                                    )
                                }

                                Text(
                                    text = item.name,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Charcoal
                                )
                            }

                            Text(
                                text = "1 ${item.code} = ${Currency.formatMinor(item.rateToIdr.toLong(), "IDR")}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${item.symbol} ${Currency.formatMinor(converted, item.code, includeSymbol = false)}",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )

                            Text(
                                text = if (isSelected) "Active" else "Select",
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isSelected) Cobalt else Slate500
                            )
                        }
                    }
                }
            }
        }
    }
}
