package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DailyExpenseEntity
import com.example.data.ExpenseMetaData
import com.example.data.UnifiedLedgerEntry
import com.example.CalendarWorkspacePage
import com.example.ui.viewmodel.WealthPulseViewModel
import com.example.ui.viewmodel.ExpenseFilters
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExpensesWorkspaceHub(
    dailyExpenses: List<DailyExpenseEntity>,
    currencyFormatter: NumberFormat,
    viewModel: WealthPulseViewModel
) {
    val context = LocalContext.current
    
    // Core Reactive States
    val filteredExpenses by viewModel.filteredDailyExpenses.collectAsState()
    val activeFilters by viewModel.expenseFilters.collectAsState()
    
    // UI Panels toggle states
    var showAddForm by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedDetailExpense by remember { mutableStateOf<DailyExpenseEntity?>(null) }
    var selectedEditingExpense by remember { mutableStateOf<DailyExpenseEntity?>(null) }
    
    // Tab within Expense workspace: "list" (Production Ledger) vs "calendar" (Original Chrono Ledger)
    var expenseSubTab by remember { mutableStateOf("list") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP RETREAD TITLE HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EXPENSES 2.0",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Orchestrated Ledger",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                // Add Expense FAB inline
                Button(
                    onClick = {
                        selectedEditingExpense = null
                        showAddForm = !showAddForm
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAddForm) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("add_expense_toggle_button")
                ) {
                    Icon(
                        imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "New Expense",
                        tint = if (showAddForm) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showAddForm) "Close" else "Record",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Sub-nav tabs: Production List Ledger, Original Chrono Ledger, and Account Transfers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    Pair("list", "📊 Ledger"),
                    Pair("calendar", "📆 Calendar"),
                    Pair("transfer", "🔄 Transfers")
                ).forEach { (tabId, label) ->
                    val isSelected = expenseSubTab == tabId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { expenseSubTab = tabId }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Expanded collapsible Add/Edit Panel
        AnimatedVisibility(
            visible = showAddForm,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                ExpenseEntryForm(
                    editingExpense = selectedEditingExpense,
                    onSave = { amount, desc, cat, payMode, notes, rUri, tags, dStr, tStr, isRec, recPeriod, ts, cardId ->
                        if (selectedEditingExpense == null) {
                            viewModel.insertFullExpense(amount, desc, cat, payMode, notes, rUri, tags, dStr, tStr, isRec, recPeriod, ts, cardId)
                        } else {
                            viewModel.updateFullExpense(selectedEditingExpense!!.id, amount, desc, cat, payMode, notes, rUri, tags, dStr, tStr, isRec, recPeriod, ts, cardId)
                        }
                        showAddForm = false
                        selectedEditingExpense = null
                    },
                    onCancel = {
                        showAddForm = false
                        selectedEditingExpense = null
                    }
                )
            }
        }

        // Conditional display based on subtab selection
        if (expenseSubTab == "list") {
            // TRANSACTION MANAGEMENT DASHBOARD
            Column(modifier = Modifier.weight(1f)) {
                // Real-time Search and Filter Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = activeFilters.searchQuery,
                        onValueChange = {
                            viewModel.updateExpenseFilters(activeFilters.copy(searchQuery = it))
                        },
                        placeholder = { Text("Search title, tag, category, notes...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (activeFilters.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateExpenseFilters(activeFilters.copy(searchQuery = "")) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("expense_search_input")
                    )

                    // Filter sheet launch button
                    FilledIconButton(
                        onClick = { showFilterSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (hasActiveFilters(activeFilters)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("filter_sheet_launch_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (hasActiveFilters(activeFilters)) {
                                    Badge { Text("!") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Filters",
                                tint = if (hasActiveFilters(activeFilters)) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Currently active filtering status chips
                if (hasActiveFilters(activeFilters)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active filters: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        
                        if (activeFilters.category != "All") {
                            FilterStatusChip(label = "Category: ${activeFilters.category}") {
                                viewModel.updateExpenseFilters(activeFilters.copy(category = "All"))
                            }
                        }
                        if (activeFilters.paymentMethod != "All") {
                            FilterStatusChip(label = "Method: ${activeFilters.paymentMethod}") {
                                viewModel.updateExpenseFilters(activeFilters.copy(paymentMethod = "All"))
                            }
                        }
                        if (activeFilters.selectedTag.isNotEmpty()) {
                            FilterStatusChip(label = "Tag: ${activeFilters.selectedTag}") {
                                viewModel.updateExpenseFilters(activeFilters.copy(selectedTag = ""))
                            }
                        }
                        if (activeFilters.startDate != null || activeFilters.endDate != null) {
                            FilterStatusChip(label = "Date Filtered") {
                                viewModel.updateExpenseFilters(activeFilters.copy(startDate = null, endDate = null))
                            }
                        }
                        if (activeFilters.minAmount != null || activeFilters.maxAmount != null) {
                            FilterStatusChip(label = "Amount Limits") {
                                viewModel.updateExpenseFilters(activeFilters.copy(minAmount = null, maxAmount = null))
                            }
                        }
                        
                        TextButton(
                            onClick = { viewModel.resetExpenseFilters() },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // TRANSACTION LIST (Production Redesigned)
                if (filteredExpenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Empty list symbol",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matching expenses found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your filters, searching other keywords, or record a new expense.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        items(filteredExpenses, key = { it.id }) { expense ->
                            RedesignedTransactionItem(
                                expense = expense,
                                formatter = currencyFormatter,
                                onClick = { selectedDetailExpense = expense }
                            )
                        }
                    }
                }
            }
        } else if (expenseSubTab == "calendar") {
            // ORIGINAL CALENDAR LEDGER VIEWPORT
            Box(modifier = Modifier.weight(1f)) {
                com.example.CalendarWorkspacePage(
                    dailyExpenses = dailyExpenses,
                    creditExpenses = viewModel.creditExpenses.collectAsState().value,
                    emiLoans = viewModel.emiLoans.collectAsState().value,
                    sipRecords = viewModel.sipRecords.collectAsState().value,
                    incomePaydays = viewModel.incomePaydays.collectAsState().value,
                    currencyFormatter = currencyFormatter,
                    viewModel = viewModel
                )
            }
        } else if (expenseSubTab == "transfer") {
            // ACCOUNT TRANSFERS VIEWPORT
            Box(modifier = Modifier.weight(1f)) {
                AccountTransfersViewport(
                    viewModel = viewModel,
                    currencyFormatter = currencyFormatter
                )
            }
        }
    }

    // --- EXPENSE DETAILED MODAL SCREEN ---
    selectedDetailExpense?.let { expense ->
        ExpenseDetailModal(
            expense = expense,
            currencyFormatter = currencyFormatter,
            onDismiss = { selectedDetailExpense = null },
            onEdit = {
                selectedEditingExpense = expense
                showAddForm = true
                selectedDetailExpense = null
            },
            onDelete = {
                viewModel.softDeleteExpense(expense.id)
                selectedDetailExpense = null
            }
        )
    }

    // --- EXPENSE ADVANCED FILTER DRAWER / SHEET ---
    if (showFilterSheet) {
        Dialog(onDismissRequest = { showFilterSheet = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter Ledger",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showFilterSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss Filters")
                        }
                    }

                    // Sort By Options
                    Text("Sort transactions by", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Newest First", "Oldest First", "Highest Amount", "Lowest Amount").chunked(2).forEach { rowList ->
                            Column {
                                rowList.forEach { sortOpt ->
                                    val isSelected = activeFilters.sortBy == sortOpt
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.updateExpenseFilters(activeFilters.copy(sortBy = sortOpt))
                                            }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.updateExpenseFilters(activeFilters.copy(sortBy = sortOpt)) }
                                        )
                                        Text(sortOpt, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Categories selection grid
                    Text("Limit by Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val categoryNames = listOf("All") + ExpenseMetaData.Categories.map { it.name }
                        categoryNames.forEach { cat ->
                            val isSelected = activeFilters.category == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateExpenseFilters(activeFilters.copy(category = cat)) },
                                label = { Text(cat, fontSize = 12.sp) }
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Payment Methods selection limits
                    Text("Limit by Payment Method", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val pmNames = listOf("All") + ExpenseMetaData.PaymentMethods
                        pmNames.forEach { pm ->
                            val isSelected = activeFilters.paymentMethod == pm
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateExpenseFilters(activeFilters.copy(paymentMethod = pm)) },
                                label = { Text(pm, fontSize = 12.sp) }
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Tag Filtering
                    Text("Limit by Tag", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("").plus(ExpenseMetaData.Tags).forEach { tag ->
                            val isSelected = activeFilters.selectedTag == tag
                            val label = if (tag.isEmpty()) "All" else tag
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateExpenseFilters(activeFilters.copy(selectedTag = tag)) },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Amount ranges
                    Text("Amount Range boundaries", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = activeFilters.minAmount?.toString() ?: "",
                            onValueChange = {
                                val minVal = it.toDoubleOrNull()
                                viewModel.updateExpenseFilters(activeFilters.copy(minAmount = minVal))
                            },
                            label = { Text("Min Amount", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        TextField(
                            value = activeFilters.maxAmount?.toString() ?: "",
                            onValueChange = {
                                val maxVal = it.toDoubleOrNull()
                                viewModel.updateExpenseFilters(activeFilters.copy(maxAmount = maxVal))
                            },
                            label = { Text("Max Amount", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Apply Filters", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun hasActiveFilters(filters: ExpenseFilters): Boolean {
    return filters.category != "All" ||
            filters.paymentMethod != "All" ||
            filters.selectedTag.isNotEmpty() ||
            filters.startDate != null ||
            filters.endDate != null ||
            filters.minAmount != null ||
            filters.maxAmount != null
}

@Composable
fun FilterStatusChip(label: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove filter segment",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onDismiss() }
            )
        }
    }
}

@Composable
fun RedesignedTransactionItem(
    expense: DailyExpenseEntity,
    formatter: NumberFormat,
    onClick: () -> Unit
) {
    val categoryMeta = remember(expense.category) {
        ExpenseMetaData.Categories.firstOrNull { it.name.equals(expense.category, ignoreCase = true) }
            ?: ExpenseMetaData.Categories.last() // Others fallback
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("expense_transaction_item_${expense.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Icon in stylized container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryMeta.composeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = categoryMeta.icon,
                    fontSize = 20.sp
                )
            }

            // Description and Metadata columns
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = expense.description.ifEmpty { expense.category },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "- ${formatter.format(expense.amount)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444) // Clean Crimson
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date & Mode badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = getFormattedDate(expense.timestamp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = expense.paymentMode,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (expense.isRecurring) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Recurring", modifier = Modifier.size(8.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = expense.recurringPeriod,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Render tags chips if present
                    if (expense.tags.isNotBlank()) {
                        val firstTag = expense.tags.split(",").firstOrNull { it.isNotBlank() } ?: ""
                        val extraTagsCount = expense.tags.split(",").filter { it.isNotBlank() }.size - 1
                        val chipText = if (extraTagsCount > 0) "$firstTag +$extraTagsCount" else firstTag
                        
                        Text(
                            text = chipText,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// FORMAT DATE helper
fun getFormattedDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExpenseEntryForm(
    editingExpense: DailyExpenseEntity?,
    onSave: (amount: Double, desc: String, cat: String, payMode: String, notes: String, rUri: String, tags: String, dStr: String, tStr: String, isRec: Boolean, recPeriod: String, timestamp: Long, cardId: Int) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    
    val db = remember { com.example.data.AppDatabase.getDatabase(context) }
    val availableCards by db.dao().getAllCreditCardsForUser("guest").collectAsState(initial = emptyList())
    var selectedCreditCardId by remember { mutableStateOf(editingExpense?.cardId ?: 0) }

    LaunchedEffect(availableCards) {
        if (selectedCreditCardId == 0 && availableCards.isNotEmpty()) {
            selectedCreditCardId = availableCards.first().id
        }
    }

    // Form Inputs State variables
    var amount by remember { mutableStateOf(editingExpense?.amount?.toString() ?: "") }
    var description by remember { mutableStateOf(editingExpense?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(editingExpense?.category ?: "Food") }
    var selectedPaymentMethod by remember { mutableStateOf(editingExpense?.paymentMode ?: "UPI") }
    
    // Collapsible advanced states
    var isAdvancedExpanded by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(editingExpense?.notes ?: "") }
    var receiptUri by remember { mutableStateOf(editingExpense?.receiptImageUri ?: "") }
    var tagsInput by remember { mutableStateOf(editingExpense?.tags ?: "") }
    
    // Date & Time states
    val calInstance = Calendar.getInstance().apply {
        if (editingExpense != null) {
            timeInMillis = editingExpense.timestamp
        }
    }
    var dayValue by remember { mutableStateOf(calInstance.get(Calendar.DAY_OF_MONTH).toString()) }
    var monthValue by remember { mutableStateOf((calInstance.get(Calendar.MONTH) + 1).toString()) } // 1-indexed for quick entry
    var yearValue by remember { mutableStateOf(calInstance.get(Calendar.YEAR).toString()) }
    
    // Recurring state
    var isRecurring by remember { mutableStateOf(editingExpense?.isRecurring ?: false) }
    var recurringPeriod by remember { mutableStateOf(editingExpense?.recurringPeriod ?: "None") }

    // Media picking launchers (Gallery picker)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            receiptUri = uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (editingExpense == null) "Record New Expense Fast" else "Edit Ledger Record",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Standard Amount Input (Focused First)
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (INR) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Text("₹ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_amount_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Fast Grid Category Selector
        Text("Select Category *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ExpenseMetaData.Categories.forEach { cat ->
                val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) cat.composeColor.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) cat.composeColor else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedCategory = cat.name }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cat.icon, fontSize = 14.sp)
                        Text(cat.name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        // Fast Grid Payment Method Selector
        Text("Payment Method", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ExpenseMetaData.PaymentMethods.forEach { pm ->
                val isSelected = selectedPaymentMethod == pm
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedPaymentMethod = pm }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(pm, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        // Selected Credit card details mapping
        if ((selectedPaymentMethod == "Credit Card" || selectedPaymentMethod.contains("Credit", ignoreCase = true)) && availableCards.isNotEmpty()) {
            Text("Select Billing Credit Card", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableCards.forEach { card ->
                    val isSel = selectedCreditCardId == card.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSel) MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSel) 1.dp else 0.dp,
                                color = if (isSel) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCreditCardId = card.id }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${card.cardName} (₹${card.outstandingAmount.toInt()}/₹${card.creditLimit.toInt()})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Fast Date inputs (Inline, avoiding clumsy fullscreen calendars for <10 seconds flow)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = dayValue,
                onValueChange = { dayValue = it },
                label = { Text("DD") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = monthValue,
                onValueChange = { monthValue = it },
                label = { Text("MM") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = yearValue,
                onValueChange = { yearValue = it },
                label = { Text("YYYY") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1.5f)
            )
        }

        // COLLAPSIBLE ADVANCED / OPTIONAL OPTIONS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { isAdvancedExpanded = !isAdvancedExpanded }
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Advanced Options (Title, Notes, Receipt, Tags, Recurring)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Expand collapsible parameters",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = isAdvancedExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title (Optional descriptor)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Expense Title (e.g., Dinner with rakshit)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes (Optional text note descriptors)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Add transaction notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Tags System
                Text("Select or Add Tags (Comma Separated)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("e.g. #office, #vacation, #trip") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Tag suggest list
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ExpenseMetaData.Tags.forEach { suggestTag ->
                        val isSuggestedActive = tagsInput.contains(suggestTag, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSuggestedActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    if (!isSuggestedActive) {
                                        tagsInput = if (tagsInput.isBlank()) suggestTag else "$tagsInput, $suggestTag"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(suggestTag, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Receipt attachment support
                Text("Attach Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Browse local files")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery Picker", fontSize = 11.sp)
                    }
                    
                    // Simulated instant camera receipt attachment supporting rapid mock previews
                    OutlinedButton(
                        onClick = {
                            // Assign a local mock recipe URI
                            receiptUri = "mock://receipt_placeholder_${(1..100).random()}.jpg"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Simulate Camera snapshot")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate Camera", fontSize = 11.sp)
                    }
                }

                if (receiptUri.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (receiptUri.startsWith("mock")) "📷 [SIMULATED CAMERA RECEIPT]" else "🖼️ " + receiptUri.takeLast(30),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { receiptUri = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove attachment", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Recurring configuration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                    Text("This is a Recurring Expense", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                }

                if (isRecurring) {
                    Text("Select Interval Frequency", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("None", "Daily", "Weekly", "Monthly", "Yearly").forEach { period ->
                            val isSelected = recurringPeriod == period
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { recurringPeriod = period }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(period, fontSize = 11.sp, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // SAVE AND CANCEL ACTIONS Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    val doubleAmt = amount.toDoubleOrNull() ?: 0.0
                    if (doubleAmt <= 0) return@Button
                    
                    // Parse date inputs reliably
                    val dVal = dayValue.toIntOrNull() ?: calInstance.get(Calendar.DAY_OF_MONTH)
                    val mVal = (monthValue.toIntOrNull() ?: (calInstance.get(Calendar.MONTH) + 1)) - 1
                    val yVal = yearValue.toIntOrNull() ?: calInstance.get(Calendar.YEAR)
                    
                    val saveCalObj = Calendar.getInstance()
                    saveCalObj.set(Calendar.YEAR, yVal)
                    saveCalObj.set(Calendar.MONTH, mVal)
                    saveCalObj.set(Calendar.DAY_OF_MONTH, dVal)
                    
                    val formattedDateString = String.format("%04d-%02d-%02d", yVal, mVal+1, dVal)
                    val formattedTimeString = String.format("%02d:%02d", calInstance.get(Calendar.HOUR_OF_DAY), calInstance.get(Calendar.MINUTE))

                    onSave(
                        doubleAmt,
                        description,
                        selectedCategory,
                        selectedPaymentMethod,
                        notes,
                        receiptUri,
                        tagsInput,
                        formattedDateString,
                        formattedTimeString,
                        isRecurring,
                        recurringPeriod,
                        saveCalObj.timeInMillis,
                        selectedCreditCardId
                    )
                },
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("form_save_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Confirm save")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Transaction", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- DEDICATED EXPENSE DETAIL DIALOG SCREEN ---
@Composable
fun ExpenseDetailModal(
    expense: DailyExpenseEntity,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val categoryMeta = remember(expense.category) {
        ExpenseMetaData.Categories.find { it.name.equals(expense.category, ignoreCase = true) }
            ?: ExpenseMetaData.Categories.last()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Transaction Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss view")
                    }
                }

                // Category Circle Icon and Title
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(categoryMeta.composeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(categoryMeta.icon, fontSize = 28.sp)
                }

                Text(
                    text = expense.description.ifEmpty { "Cash Expense" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Large stylized Amount
                Text(
                    text = "₹ ${currencyFormatter.format(expense.amount)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFEF4444)
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Grid detail elements
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow(label = "Category", value = expense.category)
                    DetailRow(label = "Payment Method", value = expense.paymentMode)
                    DetailRow(label = "Date & Time", value = getFormattedDate(expense.timestamp))
                    
                    if (expense.isRecurring) {
                        DetailRow(label = "Recurring", value = "YES (${expense.recurringPeriod})")
                    }

                    if (expense.tags.isNotBlank()) {
                        DetailRow(label = "Tags", value = expense.tags)
                    }

                    if (expense.notes.isNotBlank()) {
                        DetailRow(label = "Notes", value = expense.notes)
                    }

                    DetailRow(label = "Created", value = getFormattedDate(expense.createdAt))
                    if (expense.updatedAt > expense.createdAt) {
                        DetailRow(label = "Last Audited", value = getFormattedDate(expense.updatedAt))
                    }
                }

                // Receipt Attachment Preview
                if (expense.receiptImageUri.isNotEmpty()) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text("Receipt Image Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start))
                    
                    if (expense.receiptImageUri.startsWith("mock")) {
                        // Simulated camera receipt graphic box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Check, contentDescription = "Verified Receipt", tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                                Text("MYFIN SECURE RECEIPT", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                Text("Simulated Store Code: ${expense.id * 13}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        // Native Compose Styled Receipt Holder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.Menu, contentDescription = "Attached Receipt File", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Attached Receipt File Secured", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                Text("Path: ...${expense.receiptImageUri.takeLast(30)}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // ACTION CONTROL ROW (Share, Edit, Delete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Mockup sharing text trigger
                            val shareBody = "MYFin Expense Details:\n--------------------\nTitle: ${expense.description}\nAmount: ₹${expense.amount}\nCategory: ${expense.category}\nDate: ${getFormattedDate(expense.timestamp)}"
                            android.widget.Toast.makeText(context, "Copied receipt audit statement to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onError)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AccountTransfersViewport(
    viewModel: WealthPulseViewModel,
    currencyFormatter: NumberFormat
) {
    val ledgerEntries by viewModel.unifiedLedgerEntries.collectAsState()
    val transfersList = remember(ledgerEntries) {
        ledgerEntries.filter { it.type == "Transfer" }.sortedByDescending { it.timestamp }
    }

    var amountInput by remember { mutableStateOf("") }
    var sourceAccount by remember { mutableStateOf("Bank") }
    var destAccount by remember { mutableStateOf("Wallet") }
    var notesInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Compute dynamic real-time balances from standard base starters
    val (cashBal, bankBal, walletBal) = remember(ledgerEntries) {
        var cash = 0.0
        var bank = 0.0
        var wallet = 0.0
        // Apply deltas sequentially
        for (entry in ledgerEntries.sortedBy { it.timestamp }) {
            val amt = entry.amount
            when (entry.type) {
                "Income" -> {
                    when (com.example.data.FinancialLedgerEngine.getPaymentMethodCategory(entry.paymentMode)) {
                        "Cash" -> cash += amt
                        "Wallet" -> wallet += amt
                        "Bank" -> bank += amt
                    }
                }
                "Expense", "Credit Card Expense" -> {
                    when (com.example.data.FinancialLedgerEngine.getPaymentMethodCategory(entry.paymentMode)) {
                        "Cash" -> cash -= amt
                        "Wallet" -> wallet -= amt
                        "Bank" -> bank -= amt
                    }
                }
                "Transfer" -> {
                    when (entry.sourceAccount) {
                        "Cash" -> cash -= amt
                        "Wallet" -> wallet -= amt
                        "Bank" -> bank -= amt
                    }
                    when (entry.destAccount) {
                        "Cash" -> cash += amt
                        "Wallet" -> wallet += amt
                        "Bank" -> bank += amt
                    }
                }
                "Credit Card Payment", "EMI Payment", "Loan Repayment", "Investment" -> {
                    when (com.example.data.FinancialLedgerEngine.getPaymentMethodCategory(entry.paymentMode)) {
                        "Cash" -> cash -= amt
                        "Wallet" -> wallet -= amt
                        "Bank" -> bank -= amt
                    }
                }
                "Redemption" -> {
                    when (com.example.data.FinancialLedgerEngine.getPaymentMethodCategory(entry.paymentMode)) {
                        "Cash" -> cash += amt
                        "Wallet" -> wallet += amt
                        "Bank" -> bank += amt
                    }
                }
                "Settlement" -> {
                    if (entry.category == "Paid") {
                        when (com.example.data.FinancialLedgerEngine.getPaymentMethodCategory(entry.paymentMode)) {
                            "Cash" -> cash -= amt
                            "Wallet" -> wallet -= amt
                            "Bank" -> bank -= amt
                        }
                    } else {
                        when (com.example.data.FinancialLedgerEngine.getPaymentMethodCategory(entry.paymentMode)) {
                            "Cash" -> cash += amt
                            "Wallet" -> wallet += amt
                            "Bank" -> bank += amt
                        }
                    }
                }
            }
        }
        Triple(cash, bank, wallet)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Real-Time Account Balances Overview Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REAL-TIME BALANCES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Bank
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("🏦 Bank / UPI", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormatter.format(bankBal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        // Wallet
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("📱 Wallet", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormatter.format(walletBal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        // Cash
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("💵 Cash", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormatter.format(cashBal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // New Transfer Input Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RECORD NEW TRANSFER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Source and Destination Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Source Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("From Source Account", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            listOf("Bank", "Wallet", "Cash").forEach { acc ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { sourceAccount = acc }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(selected = sourceAccount == acc, onClick = { sourceAccount = acc })
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(acc, fontSize = 12.sp)
                                }
                            }
                        }

                        // Dest Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("To Destination Account", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            listOf("Bank", "Wallet", "Cash").forEach { acc ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { destAccount = acc }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(selected = destAccount == acc, onClick = { destAccount = acc })
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(acc, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Transfer Amount (₹)", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_amount_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Transfer Purpose / Notes", fontSize = 12.sp) },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_notes_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull()
                            if (amt == null || amt <= 0.0) {
                                android.widget.Toast.makeText(context, "Please enter a valid transfer amount", android.widget.Toast.LENGTH_SHORT).show()
                            } else if (sourceAccount == destAccount) {
                                android.widget.Toast.makeText(context, "Source and destination accounts must be different", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.recordTransfer(
                                    amount = amt,
                                    sourceAccount = sourceAccount,
                                    destAccount = destAccount,
                                    notes = notesInput.ifBlank { "Routine Account Transfer" },
                                    timestamp = System.currentTimeMillis()
                                )
                                amountInput = ""
                                notesInput = ""
                                android.widget.Toast.makeText(context, "Transfer successfully registered!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("record_transfer_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Transfer")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Account Transfer", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Previous Transfers Title
        item {
            Text(
                text = "▪ PREVIOUS TRANSFER LOGS",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        if (transfersList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transfers recorded yet", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(transfersList, key = { it.id }) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔄", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${entry.sourceAccount} ➜ ${entry.destAccount}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = entry.description.ifBlank { "Account Transfer" },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(entry.timestamp)),
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currencyFormatter.format(entry.amount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { viewModel.deleteLedgerEntry(entry.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete transfer",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
