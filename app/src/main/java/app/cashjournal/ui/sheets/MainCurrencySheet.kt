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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import app.cashjournal.domain.model.Country
import app.cashjournal.domain.model.Currency
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.Slate600
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.MagnifyingGlass
import com.adamglin.phosphoricons.regular.X

@Composable
fun MainCurrencySheet(
    currentCurrencyCode: String,
    onSelectCurrency: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var pendingCountry by remember { mutableStateOf<Country?>(null) }

    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            Country.COUNTRIES
        } else {
            Country.COUNTRIES.filter {
                it.nameEn.contains(searchQuery, ignoreCase = true) ||
                        it.currencyCode.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    if (pendingCountry != null) {
        val target = pendingCountry!!
        AlertDialog(
            onDismissRequest = { pendingCountry = null },
            title = {
                Text(
                    text = "Change main currency?",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
            },
            text = {
                Text(
                    text = "Change main currency to ${target.currencyNameEn} (${target.currencyCode})? Your accounts, transactions, and budget will be converted at the latest rate. Amounts may change by a few cents.",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 14.sp,
                    color = Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val code = target.currencyCode
                        pendingCountry = null
                        onSelectCurrency(code)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Convert",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCountry = null }) {
                    Text(
                        text = "Cancel",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate600
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
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
            Text(
                text = "Main Currency",
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
                                text = "Search country or currency",
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

        // Country & Currency List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filteredCountries.forEach { country ->
                val isSelected = country.currencyCode.equals(currentCurrencyCode, ignoreCase = true)

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isSelected) {
                                pendingCountry = country
                            }
                        },
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
                            Text(
                                text = country.nameEn,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )
                            Text(
                                text = country.currencyNameEn,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Slate100)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = country.currencyCode,
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Cobalt
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Cobalt, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = PhosphorIcons.Regular.Check,
                                        contentDescription = "Active",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
