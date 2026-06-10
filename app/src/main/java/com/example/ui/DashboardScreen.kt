package com.example

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.viewmodel.*
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToTab: (String) -> Unit,
    onTriggerQuickAction: (String) -> Unit,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val isLight = LocalCssThemeVariables.current.isLight

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBg)
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonGreen)
                }
            }
            is DashboardUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = DangerRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Dashboard failed to load",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = TextGray,
                        fontSize = 14.sp,
                        textAlign = Alignment.CenterHorizontally as? TextAlign ?: TextAlign.Center
                    )
                }
            }
            is DashboardUiState.Success -> {
                val data = state.data
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // SECTION 1: Greeting & Month Overview
                    DashboardGreetingSection(isLight = isLight)

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 1.5: Net Available Money Card (True Financial Position)
                    NetAvailableMoneyCard(
                        netMoney = data.netAvailableMoney,
                        currencyFormatter = currencyFormatter
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 2: Financial Summary Card (Hero Section)
                    FinancialSummaryCard(
                        income = data.currentMonthIncome,
                        expenses = data.currentMonthExpenses,
                        savings = data.currentSavings,
                        incomeTrend = data.incomeTrend,
                        expenseTrend = data.expenseTrend,
                        currencyFormatter = currencyFormatter
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 3: Financial Health Card (Upcoming Obligations)
                    val filteredObligations = data.upcomingObligations.filter { ob ->
                        (ob.type != "Credit Card" || data.hasCreditCards) &&
                        (ob.type != "EMI" || data.hasLoans)
                    }
                    if (filteredObligations.isNotEmpty()) {
                        DashboardSectionHeader(title = "Upcoming Payments")
                        Spacer(modifier = Modifier.height(8.dp))
                        FinancialHealthCard(
                            obligations = filteredObligations,
                            currencyFormatter = currencyFormatter
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // SECTION 4: Budget Snapshot Placeholder
                    if (data.hasBudgets) {
                        DashboardSectionHeader(title = "Budget Target Space")
                        Spacer(modifier = Modifier.height(8.dp))
                        BudgetSnapshotCard(
                            budgets = data.budgetSnapshot,
                            currencyFormatter = currencyFormatter,
                            modifier = Modifier.clickable { onNavigateToTab("budgets") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // SECTION 5: Split Expense Summary
                    if (data.hasSplits) {
                        DashboardSectionHeader(title = "Shared Splitting Info")
                        Spacer(modifier = Modifier.height(8.dp))
                        SplitSummaryCard(
                            youOwe = data.youOwe,
                            youAreOwed = data.youAreOwed,
                            currencyFormatter = currencyFormatter,
                            onViewDetails = { onNavigateToTab("split") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // SECTION 5.5: Trip & Event Summary
                    if (data.hasTrips) {
                        DashboardSectionHeader(title = "Active Trips Summary")
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BorderColor),
                            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToTab("trips") }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✈️", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Manage Trips & Vacations",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = "Keep travel finance in absolute perfect sync",
                                            fontSize = 11.sp,
                                            color = TextGray
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Navigate to trips",
                                    tint = NeonGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // SECTION 6: Recent Transactions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DashboardSectionHeader(title = "Recent Transactions")
                        Text(
                            text = "View All",
                            fontSize = 12.sp,
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onNavigateToTab("expenses") }
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    RecentTransactionsContainer(
                        transactions = data.recentTransactions,
                        currencyFormatter = currencyFormatter
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SECTION 7: Quick Actions
                    DashboardSectionHeader(title = "Quick Manual Actions")
                    Spacer(modifier = Modifier.height(8.dp))
                    QuickActionsRow(onActionTriggered = onTriggerQuickAction)

                    Spacer(modifier = Modifier.height(100.dp)) // Safe padding for bottom bar
                }
            }
        }
    }
}

@Composable
fun DashboardSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "▪ $title",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextGray,
        letterSpacing = 1.sp,
        modifier = modifier.testTag("dashboard_section_header_${title.lowercase().replace(" ", "_")}")
    )
}

@Composable
fun DashboardGreetingSection(
    isLight: Boolean,
    modifier: Modifier = Modifier
) {
    // Current time greeting logic
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingText = when (hour) {
        in 0..11 -> "Good Morning, Rakshit"
        in 12..16 -> "Good Afternoon, Rakshit"
        else -> "Good Evening, Rakshit"
    }
    
    // Obtain Month label
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentMonthLabel = months[Calendar.getInstance().get(Calendar.MONTH)]
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Column(modifier = modifier) {
        Text(
            text = greetingText,
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = TextWhite
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "$currentMonthLabel $currentYear Summary",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(NeonGreen)
            )
            Text(
                text = "Spent 12% less than last month",
                fontSize = 11.sp,
                color = NeonGreen,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun NetAvailableMoneyCard(
    netMoney: Double,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BorderColor),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
        modifier = modifier
            .fillMaxWidth()
            .testTag("net_available_money_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TRUE FINANCIAL POSITION",
                    fontSize = 10.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Net Available Money",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Asset Accounts + Recoverables - Liabilities",
                    fontSize = 9.sp,
                    color = TextGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = currencyFormatter.format(netMoney),
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = if (netMoney >= 0) NeonGreen else DangerRed
            )
        }
    }
}

@Composable
fun FinancialSummaryCard(
    income: Double,
    expenses: Double,
    savings: Double,
    incomeTrend: Double?,
    expenseTrend: Double?,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BorderColor),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_financial_summary_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "SAVINGS OVERVIEW",
                fontSize = 10.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            
            Text(
                text = currencyFormatter.format(savings),
                fontSize = 32.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Light,
                color = if (savings >= 0) NeonGreen else DangerRed,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            HorizontalDivider(color = BorderColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income detail element
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Income", fontSize = 11.sp, color = TextGray)
                    }
                    Text(
                        text = currencyFormatter.format(income),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    if (incomeTrend != null && income > 0) {
                        Text(
                            text = "↑ +${incomeTrend.toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(BorderColor)
                        .align(Alignment.CenterVertically)
                )

                // Expense detail element
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(DangerRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Expenses", fontSize = 11.sp, color = TextGray)
                    }
                    Text(
                        text = currencyFormatter.format(expenses),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    if (expenseTrend != null && expenses > 0) {
                        Text(
                            text = "↓ ${expenseTrend.toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialHealthCard(
    obligations: List<UpcomingObligation>,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
        modifier = modifier
            .fillMaxWidth()
            .testTag("financial_health_card")
    ) {
        if (obligations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No upcoming payments found",
                    fontSize = 12.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                obligations.forEach { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(NavyBg.copy(alpha = 0.5f))
                            .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (order.type) {
                                            "EMI" -> AccentOrange.copy(alpha = 0.15f)
                                            "Credit Card" -> CreditPurple.copy(alpha = 0.15f)
                                            else -> SipBlue.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (order.type) {
                                        "EMI" -> Icons.Default.DateRange
                                        "Credit Card" -> Icons.Default.Send
                                        else -> Icons.Default.PlayArrow
                                    },
                                    contentDescription = order.type,
                                    tint = when (order.type) {
                                        "EMI" -> AccentOrange
                                        "Credit Card" -> CreditPurple
                                        else -> SipBlue
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Column {
                                Text(
                                    text = order.description,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = order.type,
                                    fontSize = 10.sp,
                                    color = TextGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = currencyFormatter.format(order.amount),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        if (order.daysRemaining <= 1) DangerRed.copy(alpha = 0.15f)
                                        else BorderColor
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when {
                                        order.daysRemaining == 0 -> "Due Today"
                                        order.daysRemaining == 1 -> "Due Tomorrow"
                                        else -> "In ${order.daysRemaining} Days"
                                    },
                                    fontSize = 8.sp,
                                    color = if (order.daysRemaining <= 1) DangerRed else TextGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetSnapshotCard(
    budgets: List<BudgetCategoryLimit>,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_snapshot_placeholder")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            budgets.forEach { budget ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = budget.category,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            text = "${currencyFormatter.format(budget.spent)} / ${currencyFormatter.format(budget.limit)}",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(NavyBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(budget.percentage)
                                .clip(CircleShape)
                                .background(
                                    if (budget.percentage >= 0.85f) DangerRed
                                    else if (budget.percentage >= 0.60f) AccentOrange
                                    else NeonGreen
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplitSummaryCard(
    youOwe: Double,
    youAreOwed: Double,
    currencyFormatter: NumberFormat,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
        modifier = modifier
            .fillMaxWidth()
            .testTag("split_summary_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (youOwe == 0.0 && youAreOwed == 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pending settlements",
                        fontSize = 12.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // You owe column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "YOU OWE",
                            fontSize = 9.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currencyFormatter.format(youOwe),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (youOwe > 0) DangerRed else TextWhite
                        )
                    }

                    // Divider segment
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp)
                            .background(BorderColor)
                            .align(Alignment.CenterVertically)
                    )

                    // You are owed column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = "YOU ARE OWED",
                            fontSize = 9.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currencyFormatter.format(youAreOwed),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (youAreOwed > 0) NeonGreen else TextWhite
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
            }

            Button(
                onClick = onViewDetails,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyBg,
                    contentColor = NeonGreen
                ),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("split_view_details_button")
            ) {
                Text("View Details 👥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RecentTransactionsContainer(
    transactions: List<RecentTransaction>,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
        modifier = modifier
            .fillMaxWidth()
            .testTag("recent_transactions_container")
    ) {
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Transactions Found",
                    fontSize = 12.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                transactions.forEach { tx ->
                    TransactionTile(transaction = tx, currencyFormatter = currencyFormatter)
                }
            }
        }
    }
}

@Composable
fun TransactionTile(
    transaction: RecentTransaction,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(NavyBg.copy(alpha = 0.3f))
            .padding(10.dp)
            .testTag("transaction_tile_${transaction.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            // Circle icon containing emoji
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(BorderColor),
                contentAlignment = Alignment.Center
            ) {
                Text(text = transaction.emoji, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = transaction.title.ifEmpty { "Cash Transfer" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.category,
                    fontSize = 10.sp,
                    color = TextGray
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            val amountText = currencyFormatter.format(transaction.amount)
            val isPlus = transaction.type == "Income"
            Text(
                text = "${if (isPlus) "+" else "-"}$amountText",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPlus) NeonGreen else TextWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            
            // Format time nicely
            val timeCal = Calendar.getInstance().apply { timeInMillis = transaction.timestamp }
            val timeLabel = String.format("%02d/%02d", timeCal.get(Calendar.DAY_OF_MONTH), timeCal.get(Calendar.MONTH) + 1)
            Text(
                text = timeLabel,
                fontSize = 9.sp,
                color = TextGray
            )
        }
    }
}

@Composable
fun QuickActionsRow(
    onActionTriggered: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            label = "Add Expense",
            icon = Icons.Default.Add,
            color = NeonGreen,
            onClick = { onActionTriggered("EXPENSE") }
        )
        QuickActionButton(
            label = "Add EMI",
            icon = Icons.Default.DateRange,
            color = AccentOrange,
            onClick = { onActionTriggered("EMI") }
        )
        QuickActionButton(
            label = "CC Expense",
            icon = Icons.Default.Send,
            color = CreditPurple,
            onClick = { onActionTriggered("CREDIT") }
        )
        QuickActionButton(
            label = "Add Split",
            icon = Icons.Default.Share,
            color = NeonGreen,
            onClick = { onActionTriggered("DEBT") }
        )
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = SurfaceBlue,
            contentColor = color
        ),
        border = BorderStroke(1.dp, BorderColor),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = modifier
            .height(44.dp)
            .testTag("quick_action_${label.lowercase().replace(" ", "_")}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
    }
}
