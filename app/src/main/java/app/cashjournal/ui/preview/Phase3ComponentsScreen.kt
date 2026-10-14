package app.cashjournal.ui.preview

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.AccentKey
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.TransactionType
import app.cashjournal.ui.components.CustomNumericKeypad
import app.cashjournal.ui.components.GlassCard
import app.cashjournal.ui.components.GreetingBar
import app.cashjournal.ui.components.MaskedAmountText
import app.cashjournal.ui.components.NavTab
import app.cashjournal.ui.components.SegmentedControl
import app.cashjournal.ui.components.SideRail
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.components.ToastPill
import app.cashjournal.ui.components.ToggleSwitch
import app.cashjournal.ui.components.TransactionRow
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.PillShape
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Sun

@Composable
fun Phase3ComponentsScreen() {
    var currentTab by remember { mutableStateOf(NavTab.DASHBOARD) }
    var balancesVisible by remember { mutableStateOf(true) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var toggleState by remember { mutableStateOf(true) }
    var toastMessage by remember { mutableStateOf("") }
    var toastVisible by remember { mutableStateOf(false) }

    // Live Keypad input state (minor units in IDR)
    var keypadInputString by remember { mutableStateOf("1250000") }

    fun showToast(msg: String) {
        toastMessage = msg
        toastVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
    ) {
        // Main Screen Scaffold: Content Column (20dp left, 78dp right) + SideRail (12dp right)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 16.dp)
        ) {
            // Content Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp, end = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Greeting Bar
                GreetingBar(
                    userName = "Alex",
                    weatherIcon = PhosphorIcons.Regular.Sun,
                    temperatureText = "24°C",
                    weatherEnabled = true,
                    onWeatherClick = { showToast("Weather: 24°C Sunny in Jakarta") }
                )

                // 2. Safe-to-Spend & Masked Amount Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SAFE-TO-SPEND TODAY",
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.1.sp,
                                color = Slate500
                            )

                            // Status Pill
                            Box(
                                modifier = Modifier
                                    .background(Emerald.copy(alpha = 0.15f), app.cashjournal.ui.theme.PillShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Safe",
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = Emerald
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Hero Amount with Masking
                        val rawNumber = keypadInputString.toLongOrNull() ?: 0L
                        val formattedHero = Currency.formatMinor(
                            minorUnits = rawNumber,
                            currencyCode = "IDR",
                            includeSymbol = false
                        )

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "Rp ",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Cobalt
                            )
                            MaskedAmountText(
                                amountText = formattedHero,
                                currencySymbol = "Rp",
                                balancesVisible = balancesVisible,
                                keepSymbolVisible = false,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Charcoal
                            )
                        }

                        Text(
                            text = "17 days left · Rp 2.555.000 remaining",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                // 3. Segmented Control (Expense / Income / Transfer)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "TRANSACTION TYPE",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.1.sp,
                        color = Slate500
                    )

                    app.cashjournal.ui.components.TransactionTypeSegmentedControl(
                        selectedType = selectedType,
                        onTypeSelected = { selectedType = it }
                    )
                }

                // 4. Custom Numeric Keypad
                SubtleGlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "CASH-ENTRY NUMERIC KEYPAD",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 0.1.sp,
                            color = Slate500
                        )

                        CustomNumericKeypad(
                            onDigitPress = { digit ->
                                if (keypadInputString == "0") {
                                    keypadInputString = digit
                                } else if (keypadInputString.length < 9) {
                                    keypadInputString += digit
                                }
                            },
                            onBackspacePress = {
                                keypadInputString = if (keypadInputString.length > 1) {
                                    keypadInputString.dropLast(1)
                                } else {
                                    "0"
                                }
                            },
                            isZeroDecimal = true
                        )
                    }
                }

                // 5. Transaction Rows
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "RECENT TRANSACTIONS",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.1.sp,
                        color = Slate500
                    )

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            TransactionRow(
                                title = "Starbucks Reserve",
                                categoryName = "Food & Dining",
                                categoryIcon = Category.FOOD.icon,
                                accent = AccentKey.PERSIMMON,
                                type = TransactionType.EXPENSE,
                                amountMinor = 65000L,
                                dateText = "Today",
                                balancesVisible = balancesVisible,
                                onClick = { showToast("Clicked Starbucks") }
                            )

                            TransactionRow(
                                title = "Client Project Milestone",
                                categoryName = "Salary",
                                categoryIcon = Category.SALARY.icon,
                                accent = AccentKey.EMERALD,
                                type = TransactionType.INCOME,
                                amountMinor = 7500000L,
                                dateText = "Yesterday",
                                balancesVisible = balancesVisible,
                                onClick = { showToast("Clicked Milestone") }
                            )

                            TransactionRow(
                                title = "Transfer to Savings",
                                categoryName = "Transfer",
                                categoryIcon = Category.transferIcon,
                                accent = AccentKey.COBALT,
                                type = TransactionType.TRANSFER,
                                amountMinor = 2000000L,
                                dateText = "12 Oct",
                                balancesVisible = balancesVisible,
                                onClick = { showToast("Clicked Transfer") }
                            )
                        }
                    }
                }

                // 6. Toggle Switch Row
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tactile Haptics",
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Charcoal
                            )
                            Text(
                                text = if (toggleState) "Enabled" else "Disabled",
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }

                        ToggleSwitch(
                            checked = toggleState,
                            onCheckedChange = { toggleState = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }

            // Side Rail on the right edge (12dp from right margin)
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .align(Alignment.CenterVertically)
            ) {
                SideRail(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        currentTab = tab
                        showToast("Switched to ${tab.name.lowercase().replaceFirstChar { it.uppercase() }}")
                    },
                    onQuickAddClick = { showToast("Opened Log Transaction Sheet") },
                    balancesVisible = balancesVisible,
                    onToggleBalances = {
                        balancesVisible = !balancesVisible
                        if (balancesVisible) {
                            showToast("Balances revealed")
                        } else {
                            showToast("Local vault · Balances hidden")
                        }
                    }
                )
            }
        }

        // Floating Toast Pill (positioned 6% from top per brief section 13)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 54.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            ToastPill(
                message = toastMessage,
                visible = toastVisible,
                onDismiss = { toastVisible = false }
            )
        }
    }
}
