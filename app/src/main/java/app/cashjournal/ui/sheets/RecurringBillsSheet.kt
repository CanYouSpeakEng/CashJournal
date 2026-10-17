package app.cashjournal.ui.sheets

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cashjournal.domain.model.Account
import app.cashjournal.domain.model.Category
import app.cashjournal.domain.model.Currency
import app.cashjournal.domain.model.RecurrenceFrequency
import app.cashjournal.domain.model.RecurringTransaction
import app.cashjournal.domain.model.SafeToSpendCalculator
import app.cashjournal.ui.components.SegmentItem
import app.cashjournal.ui.components.SegmentedControl
import app.cashjournal.ui.components.SubtleGlassCard
import app.cashjournal.ui.components.ThousandsSeparatorVisualTransformation
import app.cashjournal.ui.components.ToggleSwitch
import app.cashjournal.ui.i18n.AppStrings
import app.cashjournal.ui.theme.Saffron
import app.cashjournal.ui.theme.Charcoal
import app.cashjournal.ui.theme.Cobalt
import app.cashjournal.ui.theme.Emerald
import app.cashjournal.ui.theme.Persimmon
import app.cashjournal.ui.theme.PlusJakartaSansFamily
import app.cashjournal.ui.theme.Slate100
import app.cashjournal.ui.theme.Slate200
import app.cashjournal.ui.theme.Slate400
import app.cashjournal.ui.theme.Slate500
import app.cashjournal.ui.theme.SpaceGroteskFamily
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CalendarCheck
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Minus
import com.adamglin.phosphoricons.regular.PencilSimple
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Receipt
import com.adamglin.phosphoricons.regular.Trash
import com.adamglin.phosphoricons.regular.X
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale

private enum class SheetMode {
    LIST,
    FORM
}

@Composable
fun RecurringBillsSheet(
    recurringBills: List<RecurringTransaction>,
    accounts: List<Account>,
    currencyCode: String,
    cycleStartDay: Int,
    onAddBill: (
        title: String,
        amount: Long,
        category: Category,
        dueDay: Int,
        sourceAccountId: String?,
        frequency: RecurrenceFrequency
    ) -> Unit,
    onUpdateBill: (
        id: String,
        title: String,
        amount: Long,
        category: Category,
        dueDay: Int,
        sourceAccountId: String?,
        frequency: RecurrenceFrequency,
        isActive: Boolean
    ) -> Unit,
    onDeleteBill: (id: String) -> Unit,
    onToggleActive: (id: String, isActive: Boolean) -> Unit,
    onMarkAsPaid: (id: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    languageCode: String = "en",
    hapticsEnabled: Boolean = true
) {
    val strings = AppStrings.get(languageCode)
    var mode by remember { mutableStateOf(SheetMode.LIST) }
    var editingBill by remember { mutableStateOf<RecurringTransaction?>(null) }
    var billToMarkAsPaid by remember { mutableStateOf<RecurringTransaction?>(null) }
    var billToDelete by remember { mutableStateOf<RecurringTransaction?>(null) }

    val currentCycle = remember(cycleStartDay) {
        SafeToSpendCalculator.getCycleRange(cycleStartDay)
    }

    if (billToMarkAsPaid != null) {
        val targetBill = billToMarkAsPaid!!
        val targetAcc = accounts.find { it.id == targetBill.sourceAccountId } ?: accounts.firstOrNull()
        val accName = targetAcc?.name ?: "Account"
        val formattedAmount = Currency.formatMinor(targetBill.amount, currencyCode)

        AlertDialog(
            onDismissRequest = { billToMarkAsPaid = null },
            title = {
                Text(
                    text = strings.markAsPaidConfirmTitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            },
            text = {
                Text(
                    text = String.format(strings.markAsPaidConfirmMessage, formattedAmount, targetBill.title, accName),
                    fontFamily = PlusJakartaSansFamily,
                    color = Slate500,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = targetBill.id
                        billToMarkAsPaid = null
                        onMarkAsPaid(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = strings.markAsPaidAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { billToMarkAsPaid = null }) {
                    Text(
                        text = strings.cancelAction,
                        fontFamily = PlusJakartaSansFamily,
                        color = Slate500
                    )
                }
            }
        )
    }

    if (billToDelete != null) {
        val targetBill = billToDelete!!
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            title = {
                Text(
                    text = strings.deleteBillConfirmTitle,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal
                )
            },
            text = {
                Text(
                    text = String.format(strings.deleteBillConfirmMessage, targetBill.title),
                    fontFamily = PlusJakartaSansFamily,
                    color = Slate500,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = targetBill.id
                        billToDelete = null
                        onDeleteBill(id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Persimmon),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = strings.confirmDeleteAction,
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { billToDelete = null }) {
                    Text(
                        text = strings.cancelAction,
                        fontFamily = PlusJakartaSansFamily,
                        color = Slate500
                    )
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Grab Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Slate200)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (mode == SheetMode.FORM) {
                        if (editingBill != null) strings.editRecurringBillTitle else strings.addRecurringBillAction
                    } else {
                        strings.recurringBillsTitle
                    },
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Charcoal
                )
                if (mode == SheetMode.LIST) {
                    Text(
                        text = strings.recurringBillsSubtitle,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        color = Slate500
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mode == SheetMode.LIST) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Slate100)
                            .clickable {
                                editingBill = null
                                mode = SheetMode.FORM
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Plus,
                            contentDescription = strings.addRecurringBillAction,
                            tint = Cobalt,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Slate100)
                        .clickable {
                            if (mode == SheetMode.FORM) {
                                mode = SheetMode.LIST
                                editingBill = null
                            } else {
                                onDismiss()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.X,
                        contentDescription = "Close",
                        tint = Slate500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        AnimatedContent(
            targetState = mode,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "SheetModeCrossfade"
        ) { currentMode ->
            when (currentMode) {
                SheetMode.LIST -> {
                    BillsListView(
                        recurringBills = recurringBills,
                        accounts = accounts,
                        currencyCode = currencyCode,
                        currentCycle = currentCycle,
                        strings = strings,
                        onAddNew = {
                            editingBill = null
                            mode = SheetMode.FORM
                        },
                        onEdit = { bill ->
                            editingBill = bill
                            mode = SheetMode.FORM
                        },
                        onDelete = { bill -> billToDelete = bill },
                        onToggleActive = onToggleActive,
                        onMarkAsPaid = { bill -> billToMarkAsPaid = bill }
                    )
                }

                SheetMode.FORM -> {
                    BillFormView(
                        editingBill = editingBill,
                        accounts = accounts,
                        currencyCode = currencyCode,
                        strings = strings,
                        hapticsEnabled = hapticsEnabled,
                        onSave = { title, amount, category, dueDay, sourceAccountId, frequency, isActive ->
                            if (editingBill != null) {
                                onUpdateBill(
                                    editingBill!!.id,
                                    title,
                                    amount,
                                    category,
                                    dueDay,
                                    sourceAccountId,
                                    frequency,
                                    isActive
                                )
                            } else {
                                onAddBill(title, amount, category, dueDay, sourceAccountId, frequency)
                            }
                            mode = SheetMode.LIST
                            editingBill = null
                        },
                        onCancel = {
                            mode = SheetMode.LIST
                            editingBill = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BillsListView(
    recurringBills: List<RecurringTransaction>,
    accounts: List<Account>,
    currencyCode: String,
    currentCycle: app.cashjournal.domain.model.CycleRange,
    strings: app.cashjournal.ui.i18n.ScreenStrings,
    onAddNew: () -> Unit,
    onEdit: (RecurringTransaction) -> Unit,
    onDelete: (RecurringTransaction) -> Unit,
    onToggleActive: (String, Boolean) -> Unit,
    onMarkAsPaid: (RecurringTransaction) -> Unit
) {
    if (recurringBills.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Cobalt.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Receipt,
                    contentDescription = null,
                    tint = Cobalt,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = strings.emptyRecurringBillsTitle,
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Charcoal
            )

            Text(
                text = strings.emptyRecurringBillsDesc,
                fontFamily = PlusJakartaSansFamily,
                fontSize = 13.sp,
                color = Slate500,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAddNew,
                colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.Plus,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.addRecurringBillAction,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    } else {
        val totalCommitted = remember(recurringBills) {
            recurringBills.filter { it.isActive }.sumOf { it.amount }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SubtleGlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL COMMITTED BILLS",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Slate500,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = String.format(strings.committedBillsSubline, Currency.formatMinor(totalCommitted, currencyCode)),
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 12.sp,
                                color = Slate400
                            )
                        }
                        Text(
                            text = Currency.formatMinor(totalCommitted, currencyCode),
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Charcoal
                        )
                    }
                }
            }

            items(recurringBills, key = { it.id }) { bill ->
                BillItemCard(
                    bill = bill,
                    accounts = accounts,
                    currencyCode = currencyCode,
                    currentCycle = currentCycle,
                    strings = strings,
                    onEdit = { onEdit(bill) },
                    onDelete = { onDelete(bill) },
                    onToggleActive = { active -> onToggleActive(bill.id, active) },
                    onMarkAsPaid = { onMarkAsPaid(bill) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun BillItemCard(
    bill: RecurringTransaction,
    accounts: List<Account>,
    currencyCode: String,
    currentCycle: app.cashjournal.domain.model.CycleRange,
    strings: app.cashjournal.ui.i18n.ScreenStrings,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onMarkAsPaid: () -> Unit
) {
    val account = accounts.find { it.id == bill.sourceAccountId }
    val isPaidInCycle = !bill.isCommittedInCycle(currentCycle.startMillis, currentCycle.endMillis)
    val todayCal = Calendar.getInstance()
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    val (badgeText, badgeColor) = when {
        !bill.isActive -> strings.billPausedBadge to Slate400
        isPaidInCycle -> "Paid this cycle" to Emerald
        bill.dueDay == todayDay -> strings.billDueTodayBadge to Saffron
        bill.dueDay > todayDay -> String.format(strings.billDueInDaysBadge, bill.dueDay - todayDay) to Cobalt
        else -> String.format(strings.billDueOnDayBadge, bill.dueDay.toString()) to Slate500
    }

    SubtleGlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Category icon, Title, Status badge, and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bill.category.defaultAccent.color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = bill.category.icon,
                            contentDescription = null,
                            tint = bill.category.defaultAccent.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bill.title,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Charcoal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(badgeColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp,
                                    color = badgeColor
                                )
                            }

                            if (account != null) {
                                Text(
                                    text = "· ${account.name}",
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Text(
                    text = Currency.formatMinor(bill.amount, currencyCode),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Charcoal
                )
            }

            // Bottom Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (bill.isActive && !isPaidInCycle) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Emerald.copy(alpha = 0.12f))
                                .clickable { onMarkAsPaid() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.Check,
                                    contentDescription = null,
                                    tint = Emerald,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = strings.markAsPaidAction,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Emerald
                                )
                            }
                        }
                    }

                    // Edit
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Slate100)
                            .clickable { onEdit() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.PencilSimple,
                            contentDescription = "Edit",
                            tint = Slate500,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Delete
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Slate100)
                            .clickable { onDelete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Trash,
                            contentDescription = "Delete",
                            tint = Persimmon,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Active Switch
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (bill.isActive) "Active" else "Paused",
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 11.sp,
                        color = if (bill.isActive) Cobalt else Slate400
                    )
                    ToggleSwitch(
                        checked = bill.isActive,
                        onCheckedChange = { onToggleActive(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BillFormView(
    editingBill: RecurringTransaction?,
    accounts: List<Account>,
    currencyCode: String,
    strings: app.cashjournal.ui.i18n.ScreenStrings,
    hapticsEnabled: Boolean,
    onSave: (
        title: String,
        amount: Long,
        category: Category,
        dueDay: Int,
        sourceAccountId: String?,
        frequency: RecurrenceFrequency,
        isActive: Boolean
    ) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf(editingBill?.title ?: "") }
    var rawDigits by remember { mutableStateOf(editingBill?.amount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(editingBill?.category ?: Category.UTILITIES) }
    var selectedFrequency by remember { mutableStateOf(editingBill?.frequency ?: RecurrenceFrequency.MONTHLY) }
    var dueDay by remember { mutableIntStateOf(editingBill?.dueDay ?: 1) }
    var selectedAccountId by remember {
        mutableStateOf(editingBill?.sourceAccountId ?: accounts.firstOrNull()?.id)
    }
    var isActive by remember { mutableStateOf(editingBill?.isActive ?: true) }

    val decimalSymbols = remember { DecimalFormatSymbols(Locale.getDefault()) }
    val thousandsTransformation = remember(decimalSymbols) {
        ThousandsSeparatorVisualTransformation(decimalSymbols.groupingSeparator)
    }

    val parsedAmount = rawDigits.toLongOrNull() ?: 0L

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Frequency Segmented Control
        val freqSegments = listOf(
            SegmentItem(RecurrenceFrequency.MONTHLY, strings.frequencyMonthly, Cobalt),
            SegmentItem(RecurrenceFrequency.WEEKLY, strings.frequencyWeekly, Cobalt)
        )
        SegmentedControl(
            items = freqSegments,
            selectedValue = selectedFrequency,
            hapticsEnabled = hapticsEnabled,
            onValueSelected = { selectedFrequency = it }
        )

        // Title Input
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = strings.billNameLabel.uppercase(),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Slate500,
                letterSpacing = 0.5.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate100)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                if (title.isEmpty()) {
                    Text(
                        text = strings.billNamePlaceholder,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        color = Slate400
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Charcoal
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Cobalt),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Amount Input
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "AMOUNT (${currencyCode})",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Slate500,
                letterSpacing = 0.5.sp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate100)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                if (rawDigits.isEmpty()) {
                    Text(
                        text = "0",
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 18.sp,
                        color = Slate400
                    )
                }
                BasicTextField(
                    value = rawDigits,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }.take(12)
                        rawDigits = digits
                    },
                    visualTransformation = thousandsTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(Cobalt),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Due Day Stepper (1 - 31)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = strings.dueDayLabel.uppercase(),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Slate500,
                letterSpacing = 0.5.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate100)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Day $dueDay of month",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Charcoal
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { if (dueDay > 1) dueDay-- },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Minus,
                            contentDescription = "Decrease",
                            tint = if (dueDay > 1) Cobalt else Slate400,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { if (dueDay < 31) dueDay++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Plus,
                            contentDescription = "Increase",
                            tint = if (dueDay < 31) Cobalt else Slate400,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Category Picker (Horizontal Chips)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "CATEGORY",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = Slate500,
                letterSpacing = 0.5.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Category.entries.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) cat.defaultAccent.color else Slate100)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Slate500,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = cat.defaultName,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Charcoal
                            )
                        }
                    }
                }
            }
        }

        // Account Selector
        if (accounts.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = strings.selectPaymentAccountPrompt.uppercase(),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Slate500,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = acc.id == selectedAccountId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Cobalt else Slate100)
                                .clickable { selectedAccountId = acc.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = acc.name,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else Charcoal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save & Cancel Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = strings.cancelAction,
                    fontFamily = PlusJakartaSansFamily,
                    color = Slate500
                )
            }

            Button(
                onClick = {
                    onSave(
                        title,
                        parsedAmount,
                        selectedCategory,
                        dueDay,
                        selectedAccountId,
                        selectedFrequency,
                        isActive
                    )
                },
                enabled = title.isNotBlank() && parsedAmount > 0,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Cobalt),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = strings.saveBillAction,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
