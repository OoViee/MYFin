package com.example.ui

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.*
import com.example.data.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BudgetUiState
import com.example.ui.viewmodel.BudgetViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BudgetWorkspaceHub(
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier,
    budgetViewModel: BudgetViewModel = viewModel()
) {
    val uiState by budgetViewModel.uiState.collectAsState()
    
    var showCreateBudgetModal by remember { mutableStateOf(false) }
    var selectedBudgetProgressForDetail by remember { mutableStateOf<BudgetProgress?>(null) }
    
    // Auto sync state when detail changes in database
    LaunchedEffect(uiState, selectedBudgetProgressForDetail) {
        val currentDetail = selectedBudgetProgressForDetail
        if (currentDetail != null && uiState is BudgetUiState.Success) {
            val matching = (uiState as BudgetUiState.Success).budgets.find { it.budget.id == currentDetail.budget.id }
            selectedBudgetProgressForDetail = matching
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBg)
    ) {
        when (val state = uiState) {
            is BudgetUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonGreen)
                }
            }
            is BudgetUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Error Logo", tint = DangerRed, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Error loading Budgets", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(state.message, color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
            is BudgetUiState.Success -> {
                BudgetListScreen(
                    budgets = state.budgets,
                    summary = state.summary,
                    highestSpendingCategory = state.highestSpendingCategory,
                    mostExceededBudget = state.mostExceededBudget,
                    remainingBudgetTotals = state.remainingBudgetTotals,
                    currencyFormatter = currencyFormatter,
                    onBudgetClick = { selectedBudgetProgressForDetail = it },
                    onCreateBudgetClick = { showCreateBudgetModal = true }
                )
            }
        }

        // Create Budget Modal Popup
        if (showCreateBudgetModal) {
            CreateBudgetModal(
                budgetViewModel = budgetViewModel,
                onDismiss = { showCreateBudgetModal = false }
            )
        }

        // Budget Detail Screen Popup
        val detailBudget = selectedBudgetProgressForDetail
        if (detailBudget != null) {
            BudgetDetailModal(
                progress = detailBudget,
                currencyFormatter = currencyFormatter,
                budgetViewModel = budgetViewModel,
                onDismiss = { selectedBudgetProgressForDetail = null }
            )
        }
    }
}

@Composable
fun BudgetListScreen(
    budgets: List<BudgetProgress>,
    summary: MonthlyBudgetSummary,
    highestSpendingCategory: String?,
    mostExceededBudget: Pair<String, Double>?,
    remainingBudgetTotals: Double,
    currencyFormatter: NumberFormat,
    onBudgetClick: (BudgetProgress) -> Unit,
    onCreateBudgetClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Upper section title
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TARGET BUDGET CONTROL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF78716C),
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "Budget Goals",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontFamily = FontFamily.Serif
                )
            }
            Button(
                onClick = onCreateBudgetClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("create_budget_cta_header")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create", tint = NavyBg, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Budget", color = NavyBg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (budgets.isEmpty()) {
            // Pristine Empty State Layout
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(SurfaceBlue.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Empty Budgets",
                        tint = NeonGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No budgets created yet",
                    fontSize = 18.sp,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Create your first budget to start tracking spending goals.",
                    fontSize = 13.sp,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onCreateBudgetClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("empty_state_create_budget_cta")
                ) {
                    Text("Setup Standard Budget Limit", color = NavyBg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Aggregated Summary Panel Row
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MONTHLY BUDGET SUMMARY",
                        fontSize = 10.sp,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Budget", fontSize = 11.sp, color = TextGray)
                            Text(
                                currencyFormatter.format(summary.totalBudgetLimit),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                        Column {
                            Text("Total Spent", fontSize = 11.sp, color = TextGray)
                            Text(
                                currencyFormatter.format(summary.totalSpent),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (summary.totalSpent > summary.totalBudgetLimit) DangerRed else TextWhite
                            )
                        }
                        Column {
                            Text("Remaining", fontSize = 11.sp, color = TextGray)
                            Text(
                                currencyFormatter.format(remainingBudgetTotals),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                        }
                    }

                    // Insights row underneath if applicable
                    if (summary.exceededAmount > 0 || mostExceededBudget != null || highestSpendingCategory != null) {
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderColor)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (mostExceededBudget != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = "Breakage Warning", tint = DangerRed, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Exceeded ${mostExceededBudget.first} limit by ${currencyFormatter.format(mostExceededBudget.second)}!",
                                        fontSize = 11.sp,
                                        color = DangerRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (highestSpendingCategory != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = "Trend Meta", tint = AccentOrange, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Highest overall category limit: $highestSpendingCategory",
                                        fontSize = 11.sp,
                                        color = TextGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Target Budgets Scrollable List view
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(budgets) { item ->
                    BudgetListItemCard(
                        progress = item,
                        currencyFormatter = currencyFormatter,
                        onClick = { onBudgetClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetListItemCard(
    progress: BudgetProgress,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit
) {
    val budget = progress.budget
    val categoryMeta = remember(budget.category) {
        ExpenseMetaData.Categories.find { it.name.equals(budget.category, ignoreCase = true) }
            ?: ExpenseMetaData.Categories.last()
    }
    
    val pctFloat = (progress.usagePercentage / 100.0).toFloat()
    
    // Progress States matching rules:
    // 0-79%: NeonGreen (Normal)
    // 80-89%: AccentOrange (Warning)
    // 90-99%: DangerRed/DeepPurple (Critical)
    // 100%+: Bold DangerRed (Exceeded)
    val indicatorColor = when {
        pctFloat >= 1.0f -> DangerRed
        pctFloat >= 0.90f -> DangerRed.copy(alpha = 0.8f)
        pctFloat >= 0.80f -> AccentOrange
        else -> NeonGreen
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("budget_item_card_${budget.category.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(categoryMeta.composeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(categoryMeta.icon, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = budget.category,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "Monthly Budget Limit",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${currencyFormatter.format(progress.spentAmount)} spent",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pctFloat >= 1.0f) DangerRed else TextWhite
                    )
                    Text(
                        text = "Limit: ${currencyFormatter.format(budget.budgetAmount)}",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom styled progress slider indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(NavyBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(pctFloat.coerceIn(0.0f, 1.0f))
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${progress.usagePercentage.toInt()}% Capacity Used",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = indicatorColor
                )
                
                val remainText = if (progress.remainingAmount >= 0) {
                    "Left: ${currencyFormatter.format(progress.remainingAmount)}"
                } else {
                    "Over limit: ${currencyFormatter.format(Math.abs(progress.remainingAmount))}"
                }
                Text(
                    text = remainText,
                    fontSize = 11.sp,
                    color = if (progress.remainingAmount < 0) DangerRed else TextGray,
                    fontWeight = if (progress.remainingAmount < 0) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CreateBudgetModal(
    budgetViewModel: BudgetViewModel,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ExpenseMetaData.Categories.first().name) }
    var budgetAmountString by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("create_budget_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "DEFINE OUTFLOW LIMIT",
                    fontSize = 10.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Set Category Budget",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Validation alerts
                if (errorMsg != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Error icon", tint = DangerRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMsg ?: "", color = DangerRed, fontSize = 11.sp)
                        }
                    }
                }

                if (successMsg != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NeonGreen.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "Check icon", tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(successMsg ?: "", color = NeonGreen, fontSize = 11.sp)
                        }
                    }
                }

                // Category selection block
                Text("Select Category", fontSize = 11.sp, color = TextGray, modifier = Modifier.padding(bottom = 6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyBg)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .clickable { isDropdownExpanded = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val activeIcon = remember(selectedCategory) {
                            ExpenseMetaData.Categories.find { it.name == selectedCategory }?.icon ?: "💸"
                        }
                        Text("$activeIcon   $selectedCategory", color = TextWhite, fontSize = 14.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown expand", tint = NeonGreen)
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(SurfaceBlue).border(1.dp, BorderColor)
                    ) {
                        ExpenseMetaData.Categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.icon}   ${cat.name}", color = TextWhite) },
                                onClick = {
                                    selectedCategory = cat.name
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Budget limit input
                Text("Budget Amount (₹)", fontSize = 11.sp, color = TextGray, modifier = Modifier.padding(bottom = 6.dp))
                TextField(
                    value = budgetAmountString,
                    onValueChange = { budgetAmountString = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = NavyBg,
                        unfocusedContainerColor = NavyBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .testTag("budget_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Notice: Budget period defaults to Current Month as standard.", fontSize = 10.sp, color = TextGray)

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, BorderColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextWhite)
                    }

                    Button(
                        onClick = {
                            val amount = budgetAmountString.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                errorMsg = "Please enter a valid amount greater than 0."
                                return@Button
                            }
                            errorMsg = null
                            budgetViewModel.createBudget(
                                category = selectedCategory,
                                amount = amount,
                                startDate = null,
                                endDate = null,
                                onResult = { success, msg ->
                                    if (success) {
                                        successMsg = msg
                                        errorMsg = null
                                        // Wait a tiny bit then dismiss
                                        onDismiss()
                                    } else {
                                        errorMsg = msg
                                        successMsg = null
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_budget_button")
                    ) {
                        Text("Save Budget", color = NavyBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetDetailModal(
    progress: BudgetProgress,
    currencyFormatter: NumberFormat,
    budgetViewModel: BudgetViewModel,
    onDismiss: () -> Unit
) {
    val budget = progress.budget
    val categoryMeta = remember(budget.category) {
        ExpenseMetaData.Categories.find { it.name.equals(budget.category, ignoreCase = true) }
            ?: ExpenseMetaData.Categories.last()
    }

    var editAmountString by remember { mutableStateOf(budget.budgetAmount.toString()) }
    var isEditing by remember { mutableStateOf(false) }
    var detailErrorMsg by remember { mutableStateOf<String?>(null) }
    
    val pctFloat = (progress.usagePercentage / 100).toFloat()
    val indicatorColor = when {
        pctFloat >= 1.0f -> DangerRed
        pctFloat >= 0.80f -> AccentOrange
        else -> NeonGreen
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("budget_detail")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(categoryMeta.composeColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(categoryMeta.icon, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = budget.category,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextGray)
                    }
                }

                if (detailErrorMsg != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(detailErrorMsg ?: "", color = DangerRed, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }
                }

                // General Stats Panel
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Spending", fontSize = 11.sp, color = TextGray)
                            Text("Category Limit", fontSize = 11.sp, color = TextGray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currencyFormatter.format(progress.spentAmount),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pctFloat >= 1.0f) DangerRed else TextWhite
                            )

                            if (!isEditing) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currencyFormatter.format(budget.budgetAmount),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Amount",
                                        tint = NeonGreen,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { isEditing = true }
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(100.dp)) {
                                    TextField(
                                        value = editAmountString,
                                        onValueChange = { editAmountString = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = SurfaceBlue,
                                            unfocusedContainerColor = SurfaceBlue,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            val nextAmount = editAmountString.toDoubleOrNull()
                                            if (nextAmount == null || nextAmount <= 0) {
                                                detailErrorMsg = "Limit must be a positive number"
                                                return@IconButton
                                            }
                                            detailErrorMsg = null
                                            budgetViewModel.updateBudgetAmount(budget.id, nextAmount) { success, msg ->
                                                if (success) {
                                                    isEditing = false
                                                } else {
                                                    detailErrorMsg = msg
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Save edit", tint = NeonGreen)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // Progress Slider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(SurfaceBlue)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(pctFloat.coerceIn(0.0f, 1.0f))
                                    .clip(CircleShape)
                                    .background(indicatorColor)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${progress.usagePercentage.toInt()}% Used", fontSize = 11.sp, color = indicatorColor, fontWeight = FontWeight.Bold)
                            
                            val remain = if (progress.remainingAmount >= 0) {
                                "Remaining: ${currencyFormatter.format(progress.remainingAmount)}"
                            } else {
                                "Over Limit: ${currencyFormatter.format(Math.abs(progress.remainingAmount))}"
                            }
                            Text(remain, fontSize = 11.sp, color = if (progress.remainingAmount < 0) DangerRed else TextGray)
                        }
                    }
                }

                // Recent Contributing Transactions list (Max 10)
                Text(
                    text = "CONTRIBUTING TRANSACTIONS (${progress.recentExpenses.size})",
                    fontSize = 10.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (progress.recentExpenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .border(1.dp, BorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active expenses in this category.", color = TextGray, fontSize = 11.sp)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyBg),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            progress.recentExpenses.forEachIndexed { idx, exp ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exp.description.ifEmpty { "Expense" },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        
                                        val df = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                                        Text(df.format(Date(exp.timestamp)), fontSize = 10.sp, color = TextGray)
                                    }
                                    
                                    Text(
                                        text = currencyFormatter.format(exp.amount),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                }
                                if (idx < progress.recentExpenses.size - 1) {
                                    Divider(color = BorderColor.copy(alpha = 0.4f), modifier = Modifier.padding(horizontal = 8.dp))
                                }
                            }
                        }
                    }
                }

                // Direct State Modifier actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val activeLabel = if (budget.isActive) "Deactivate" else "Activate"
                    
                    OutlinedButton(
                        onClick = {
                            budgetViewModel.toggleBudgetActive(budget.id, !budget.isActive)
                        },
                        border = BorderStroke(1.dp, BorderColor),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (budget.isActive) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = activeLabel,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(activeLabel, fontSize = 11.sp, maxLines = 1)
                    }

                    Button(
                        onClick = {
                            budgetViewModel.deleteBudget(budget.id)
                            onDismiss() // Dismiss panel
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("delete_budget_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextWhite, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = TextWhite, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
