package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.viewmodel.*
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Calendar

@Composable
fun DashboardScreen(
    onNavigateToTab: (String) -> Unit,
    onTriggerQuickAction: (String) -> Unit,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Color tokens
    val bgNavy = NavyBg
    val greenGlow = NeonGreen
    val surfaceB = SurfaceBlue
    val borderC = BorderColor

    // Dashboard Personalization Settings State
    var showPersonalizationDialog by remember { mutableStateOf(false) }
    var pinSnapshotAtTop by remember { mutableStateOf(true) }
    var showHealthScoreSection by remember { mutableStateOf(true) }
    var showUpcomingActionsSection by remember { mutableStateOf(true) }
    var showRecentActivitySection by remember { mutableStateOf(true) }
    var showSpendingInsightsSection by remember { mutableStateOf(true) }
    var showSplitSummarySection by remember { mutableStateOf(true) }
    var showAiInsightsSection by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgNavy)
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = greenGlow)
                }
            }
            is DashboardUiState.Error -> {
                val warningRed = DangerRed
                val textW = TextWhite
                val textG = TextGray
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
                        tint = warningRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Dashboard failed to load",
                        color = textW,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = textG,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is DashboardUiState.Success -> {
                val data = state.data
                val mainViewModel: WealthPulseViewModel = viewModel()
                val currentUser by mainViewModel.currentUser.collectAsState()
                
                val displayName = remember(currentUser) {
                    val user = currentUser
                    if (user == null || user.isAnonymous) {
                        "Rakshit"
                    } else {
                        val email = user.email
                        val localPart = email.substringBefore("@")
                        localPart.split(".", "_", "-").joinToString(" ") { part ->
                            part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(start = 16.dp, top = 20.dp, end = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // HERO HEADER ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DashboardGreetingHeader(
                            displayName = displayName,
                            expenseTrend = data.expenseTrend
                        )
                        IconButton(
                            onClick = { showPersonalizationDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(surfaceB)
                                .border(1.dp, borderC, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Personalize Dashboard",
                                tint = greenGlow,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // --- ABOVE THE FOLD PERSISTENT QUICK ACTIONS AREA ---
                    QuickActionsSectionGrid(onActionTriggered = onTriggerQuickAction)

                    // Dynamically render dashboard widgets based on user configuration
                    val widgetOrder = remember(pinSnapshotAtTop) {
                        if (pinSnapshotAtTop) {
                            listOf(
                                "snapshot", "health_score", "upcoming_actions", 
                                "recent_activity", "spending_insights", "split_summary", "ai_insights"
                            )
                        } else {
                            listOf(
                                "health_score", "snapshot", "upcoming_actions", 
                                "recent_activity", "spending_insights", "split_summary", "ai_insights"
                            )
                        }
                    }

                    widgetOrder.forEach { widget ->
                        when (widget) {
                            "snapshot" -> {
                                if (pinSnapshotAtTop) {
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        FinancialSnapshotHero(
                                            data = data,
                                            currencyFormatter = currencyFormatter
                                        )
                                    }
                                }
                            }
                            "health_score" -> {
                                if (showHealthScoreSection) {
                                    Column {
                                        DashboardSectionLabel(title = "Health Score Meter")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FinancialHealthScoreGauge(data = data)
                                    }
                                }
                            }
                            "upcoming_actions" -> {
                                if (showUpcomingActionsSection) {
                                    Column {
                                        DashboardSectionLabel(title = "Task Center & Obligations")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        UpcomingActionsWidget(
                                            data = data,
                                            currencyFormatter = currencyFormatter,
                                            onNavigateToTab = onNavigateToTab,
                                            onTriggerQuickAction = onTriggerQuickAction
                                        )
                                    }
                                }
                            }
                            "recent_activity" -> {
                                if (showRecentActivitySection) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            DashboardSectionLabel(title = "Real Time Activity Ledger")
                                            Text(
                                                text = "View All →",
                                                fontSize = 11.sp,
                                                color = greenGlow,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clickable { onNavigateToTab("expenses") }
                                                    .padding(vertical = 4.dp, horizontal = 12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        RecentActivityWidget(
                                            data = data,
                                            currencyFormatter = currencyFormatter,
                                            onNavigateToExpenses = { onNavigateToTab("expenses") }
                                        )
                                    }
                                }
                            }
                            "spending_insights" -> {
                                if (showSpendingInsightsSection) {
                                    Column {
                                        DashboardSectionLabel(title = "Spending Insights & Efficiency")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        SpendingInsightsWidget(
                                            data = data,
                                            currencyFormatter = currencyFormatter,
                                            onNavigateToBudgets = { onNavigateToTab("budgets") }
                                        )
                                    }
                                }
                            }
                            "split_summary" -> {
                                if (showSplitSummarySection) {
                                    Column {
                                        DashboardSectionLabel(title = "Splits, Groups & Shared Ledger")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        SplitGroupSummaryWidget(
                                            data = data,
                                            currencyFormatter = currencyFormatter,
                                            onNavigateToTabs = onNavigateToTab
                                        )
                                    }
                                }
                            }
                            "ai_insights" -> {
                                if (showAiInsightsSection) {
                                    Column {
                                        DashboardSectionLabel(title = "AI Model Observations")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        AiFinancialAdvisorInsightsWidget(data = data)
                                    }
                                }
                            }
                        }
                    }

                    // Render snapshot at bottom if not pinned at top
                    if (!pinSnapshotAtTop) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            FinancialSnapshotHero(
                                data = data,
                                currencyFormatter = currencyFormatter
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp)) // Safe padding for bottom bar info
                }
            }
        }
    }

    // PERSONALIZATION DIALOG
    if (showPersonalizationDialog) {
        val textW = TextWhite
        val textG = TextGray
        Dialog(onDismissRequest = { showPersonalizationDialog = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceB),
                border = BorderStroke(1.2.dp, borderC),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Customize Home Center",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textW,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = "Toggle, reorder, and configure your daily layout blocks to minimize noise.",
                        fontSize = 12.sp,
                        color = textG,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(color = borderC, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Config list
                    PersonalizationToggleItem(
                        label = "Pin Financial Snapshot at Top",
                        checked = pinSnapshotAtTop,
                        onCheckedChange = { pinSnapshotAtTop = it }
                    )
                    PersonalizationToggleItem(
                        label = "Show Health Score Meter",
                        checked = showHealthScoreSection,
                        onCheckedChange = { showHealthScoreSection = it }
                    )
                    PersonalizationToggleItem(
                        label = "Show Tasks & Obligations",
                        checked = showUpcomingActionsSection,
                        onCheckedChange = { showUpcomingActionsSection = it }
                    )
                    PersonalizationToggleItem(
                        label = "Show Activity ledger",
                        checked = showRecentActivitySection,
                        onCheckedChange = { showRecentActivitySection = it }
                    )
                    PersonalizationToggleItem(
                        label = "Show Spending Insights",
                        checked = showSpendingInsightsSection,
                        onCheckedChange = { showSpendingInsightsSection = it }
                    )
                    PersonalizationToggleItem(
                        label = "Show Splits & Groups",
                        checked = showSplitSummarySection,
                        onCheckedChange = { showSplitSummarySection = it }
                    )
                    PersonalizationToggleItem(
                        label = "Show AI Observations",
                        checked = showAiInsightsSection,
                        onCheckedChange = { showAiInsightsSection = it }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showPersonalizationDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = greenGlow),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Apply Configuration", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = bgNavy)
                    }
                }
            }
        }
    }
}

// SUBCOMPONENT: Section General Label Style
@Composable
fun DashboardSectionLabel(
    title: String,
    modifier: Modifier = Modifier
) {
    val textG = TextGray
    val greenGlow = NeonGreen
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.testTag("sec_header_${title.lowercase().replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(greenGlow)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textG,
            letterSpacing = 1.5.sp
        )
    }
}

// SUBCOMPONENT: Personalization list item
@Composable
fun PersonalizationToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val textW = TextWhite
    val textG = TextGray
    val greenGlow = NeonGreen
    val bgNavy = NavyBg
    val borderC = BorderColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = textW)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = bgNavy,
                checkedTrackColor = greenGlow,
                uncheckedThumbColor = textG,
                uncheckedTrackColor = borderC
            )
        )
    }
}

// SUBCOMPONENT: Dashboard greeting header
@Composable
fun DashboardGreetingHeader(
    displayName: String,
    expenseTrend: Double?,
    modifier: Modifier = Modifier
) {
    val textW = TextWhite
    val textG = TextGray
    val greenGlow = NeonGreen
    val errorRed = DangerRed
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingText = when (hour) {
        in 0..11 -> "Good Morning, $displayName"
        in 12..16 -> "Good Afternoon, $displayName"
        else -> "Good Evening, $displayName"
    }

    Column(modifier = modifier) {
        Text(
            text = greetingText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textW
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val months = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            val currentMonthLabel = months[Calendar.getInstance().get(Calendar.MONTH)]
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            Text(
                text = "$currentMonthLabel $currentYear • Control Center",
                fontSize = 12.sp,
                color = textG,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(if (expenseTrend != null && expenseTrend > 0.0) errorRed else greenGlow)
            )
            Text(
                text = when {
                    expenseTrend == null -> "Starting cycle"
                    expenseTrend < 0 -> "${String.format("%.1f", -expenseTrend)}% under last month"
                    expenseTrend > 0 -> "${String.format("%.1f", expenseTrend)}% over last month"
                    else -> "Stable trend line"
                },
                fontSize = 11.sp,
                color = when {
                    expenseTrend == null -> textG
                    expenseTrend < 0 -> greenGlow
                    expenseTrend > 0 -> errorRed
                    else -> textG
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// SUBCOMPONENT: Quick Actions Grid Section (thumb reach friendly, horizontal chips)
@Composable
fun QuickActionsSectionGrid(
    onActionTriggered: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val greenGlow = NeonGreen
    val purpleSwipe = CreditPurple
    val blueCap = SipBlue
    val orangeEmi = AccentOrange
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ModernActionChip(
            label = "Add Expense",
            icon = Icons.Default.Add,
            color = greenGlow,
            onClick = { onActionTriggered("EXPENSE") }
        )
        ModernActionChip(
            label = "Add Income",
            icon = Icons.Default.Add,
            color = greenGlow,
            onClick = { onActionTriggered("INCOME") }
        )
        ModernActionChip(
            label = "Swipe Card",
            icon = Icons.Default.Send,
            color = purpleSwipe,
            onClick = { onActionTriggered("CREDIT") }
        )
        ModernActionChip(
            label = "Add Settlement",
            icon = Icons.Default.CheckCircle,
            color = blueCap,
            onClick = { onActionTriggered("DEBT") }
        )
        ModernActionChip(
            label = "Add EMI Loan",
            icon = Icons.Default.DateRange,
            color = orangeEmi,
            onClick = { onActionTriggered("EMI") }
        )
        ModernActionChip(
            label = "Auto SIP",
            icon = Icons.Default.Star,
            color = blueCap,
            onClick = { onActionTriggered("SIP") }
        )
    }
}

@Composable
fun ModernActionChip(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceB = SurfaceBlue
    val borderC = BorderColor
    val textW = TextWhite
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = surfaceB,
        border = BorderStroke(1.dp, borderC),
        modifier = modifier
            .height(40.dp)
            .testTag("home_action_${label.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = textW,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==========================================
// SECTION 1: FINANCIAL SNAPSHOT (HERO VIEW)
// ==========================================
@Composable
fun FinancialSnapshotHero(
    data: DashboardData,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    val surfaceB = SurfaceBlue
    val borderC = BorderColor
    val textG = TextGray
    val textW = TextWhite
    val greenGlow = NeonGreen
    val errorRed = DangerRed
    val orangeAccent = AccentOrange

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceB),
        border = BorderStroke(1.dp, borderC),
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_snapshot_widget")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NET AVAILABLE WEALTH",
                        fontSize = 11.sp,
                        color = textG,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
                IconButton(
                    onClick = { /* Detail info modal */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Analysis information",
                        tint = textG,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Central Net Balance (Typography dominates visual weight)
            Text(
                text = currencyFormatter.format(data.netAvailableMoney),
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = textW,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            HorizontalDivider(color = borderC.copy(alpha = 0.4f), thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))

            // Sub ledger overview: Income, Expenses, Savings Ratio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // INCOME
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "INCOME",
                        fontSize = 9.sp,
                        color = textG,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormatter.format(data.currentMonthIncome),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textW
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (data.incomeTrend != null && data.currentMonthIncome > 0) {
                        Text(
                            text = "+${data.incomeTrend.toInt()}% m/m",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = greenGlow
                        )
                    } else {
                        Text(text = "Stable", fontSize = 10.sp, color = textG)
                    }
                }

                // Divider line vertical
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(borderC.copy(alpha = 0.4f)).align(Alignment.CenterVertically))

                // EXPENSES
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(
                        text = "SPENDING",
                        fontSize = 9.sp,
                        color = textG,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormatter.format(data.currentMonthExpenses),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textW
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (data.expenseTrend != null && data.currentMonthExpenses > 0) {
                        Text(
                            text = if (data.expenseTrend < 0) "${data.expenseTrend.toInt()}% standard" else "+${data.expenseTrend.toInt()}% spike",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (data.expenseTrend < 0) greenGlow else errorRed
                        )
                    } else {
                        Text(text = "Stable", fontSize = 10.sp, color = textG)
                    }
                }

                // Divider line vertical
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(borderC.copy(alpha = 0.4f)).align(Alignment.CenterVertically))

                // NET CASH & SAVINGS STATE
                Column(modifier = Modifier.weight(1.1f).padding(start = 16.dp)) {
                    val savingsRate = if (data.currentMonthIncome > 0) {
                        ((data.currentSavings / data.currentMonthIncome) * 100).coerceIn(0.0, 100.0)
                    } else if (data.currentSavings > 0) {
                        100.0
                    } else {
                        0.0
                    }
                    Text(
                        text = "SAVED (${savingsRate.toInt()}%)",
                        fontSize = 9.sp,
                        color = textG,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormatter.format(data.currentSavings),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = textW
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (data.currentSavings >= 0) "Growth trend" else "Deficit cycle",
                        fontSize = 10.sp,
                        color = if (data.currentSavings >= 0) greenGlow else errorRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ==========================================
// SECTION 2: FINANCIAL HEALTH SCORE (GAUGE)
// ==========================================
@Composable
fun FinancialHealthScoreGauge(
    data: DashboardData,
    modifier: Modifier = Modifier
) {
    val surfaceB = SurfaceBlue
    val borderC = BorderColor
    val textG = TextGray
    val textW = TextWhite
    val orangeAccent = AccentOrange
    val greenGlow = NeonGreen

    // Elegant calculation based on actual system parameters safely
    val score = remember(data) {
        var baseScore = 78 // Core anchor
        
        // 1. Budget Adherence: deduction if any category exceeds 90% limit
        data.budgetSnapshot.forEach { b ->
            if (b.percentage > 0.9f) baseScore -= 5
            else if (b.percentage > 0.7f) baseScore -= 2
        }
        
        // 2. Debt ratio
        if (data.youOwe > data.netAvailableMoney * 0.5) baseScore -= 10
        else if (data.youOwe > data.netAvailableMoney * 0.25) baseScore -= 4
        
        // 3. Savings trend factor
        if (data.currentSavings > 0) baseScore += 5
        else if (data.currentSavings < 0) baseScore -= 8
        
        // Ensure score stays bounded
        baseScore.coerceIn(30, 99)
    }

    val activeRingColor = if (score >= 75) greenGlow else orangeAccent

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceB),
        border = BorderStroke(1.dp, borderC),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Canvas Circular Gauge
            Box(
                modifier = Modifier.size(70.dp),
                contentAlignment = Alignment.Center
            ) {
                val animatedScore by animateFloatAsState(
                    targetValue = score / 100f,
                    animationSpec = tween(900, easing = FastOutSlowInEasing),
                    label = "gauge_score_anim"
                )
                Canvas(modifier = Modifier.size(64.dp)) {
                    // Back arc
                    drawCircle(
                        color = borderC.copy(alpha = 0.5f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
                    )
                    // Solid foreground Arc (Clean like Apple watch active rings)
                    drawArc(
                        color = activeRingColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedScore,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 6.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
                Text(
                    text = "$score",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textW
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "FINANCIAL INDEX",
                    fontSize = 9.sp,
                    color = textG,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        score >= 85 -> "Exceptional Financial Control"
                        score >= 70 -> "Optimal Wallet Adherence"
                        else -> "Attention Advised"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textW
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Scores map your savings index, SIP regularity, and split-debt liquidation cycles.",
                    color = textG,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

// ==========================================
// SECTION 3: UPCOMING ACTIONS (TASK CENTER)
// ==========================================
// SECTION 3: UPCOMING ACTIONS (TASK CENTER)
// ==========================================
@Composable
fun UpcomingActionsWidget(
    data: DashboardData,
    currencyFormatter: NumberFormat,
    onNavigateToTab: (String) -> Unit,
    onTriggerQuickAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceB = SurfaceBlue
    val borderC = BorderColor
    val textG = TextGray
    val textW = TextWhite
    val orangeAccent = AccentOrange
    val purpleSwipe = CreditPurple
    val blueCap = SipBlue
    val errorRed = DangerRed

    val nonMockObligations = data.upcomingObligations.filter { ob ->
        (ob.type != "Credit Card" || data.hasCreditCards) &&
        (ob.type != "EMI" || data.hasLoans)
    }

    if (nonMockObligations.isEmpty()) {
        CabinetEmptyState(
            desc = "Perfect clear! No upcoming bill liability, EMI, or credit card outstanding payments found.",
            btnLabel = "Track New Liability",
            onClick = { onTriggerQuickAction("EMI") },
            testTag = "upcoming_actions_empty"
        )
    } else {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceB),
            border = BorderStroke(1.dp, borderC),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                nonMockObligations.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = borderC.copy(alpha = 0.4f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (item.type == "Credit Card") onNavigateToTab("credit_cards")
                                else if (item.type == "EMI") onNavigateToTab("emi_loans")
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (item.type) {
                                            "EMI" -> orangeAccent.copy(alpha = 0.08f)
                                            "Credit Card" -> purpleSwipe.copy(alpha = 0.08f)
                                            else -> blueCap.copy(alpha = 0.08f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (item.type) {
                                        "EMI" -> Icons.Default.DateRange
                                        "Credit Card" -> Icons.Default.Send
                                        else -> Icons.Default.Star
                                    },
                                    contentDescription = item.type,
                                    tint = when (item.type) {
                                        "EMI" -> orangeAccent
                                        "Credit Card" -> purpleSwipe
                                        else -> blueCap
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = item.description,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textW,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.type,
                                    fontSize = 10.sp,
                                    color = textG
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                    text = currencyFormatter.format(item.amount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textW
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when {
                                    item.daysRemaining == 0 -> "Due Today"
                                    item.daysRemaining == 1 -> "Due Tomorrow"
                                    else -> "In ${item.daysRemaining} days"
                                },
                                fontSize = 10.sp,
                                color = if (item.daysRemaining <= 1) errorRed else textG,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SECTION 4: RECENT ACTIVITY
// ==========================================
@Composable
fun RecentActivityWidget(
    data: DashboardData,
    currencyFormatter: NumberFormat,
    onNavigateToExpenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceB = SurfaceBlue
    val borderC = BorderColor
    val textG = TextGray
    val textW = TextWhite
    val greenGlow = NeonGreen

    if (data.recentTransactions.isEmpty()) {
        CabinetEmptyState(
            desc = "No recorded transactions for the ongoing calendar period. Record an expense now.",
            btnLabel = "Record Cash Spend",
            onClick = onNavigateToExpenses,
            testTag = "recent_activity_empty"
        )
    } else {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceB),
            border = BorderStroke(1.dp, borderC),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                val compactList = data.recentTransactions.take(4)
                compactList.forEachIndexed { index, tx ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = borderC.copy(alpha = 0.4f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToExpenses() }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(borderC.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = tx.emoji, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = tx.title.ifEmpty { "Transaction" },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textW,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tx.category,
                                    fontSize = 10.sp,
                                    color = textG
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            val isPlus = tx.type == "Income"
                            Text(
                                text = "${if (isPlus) "+" else "-"}${currencyFormatter.format(tx.amount)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPlus) greenGlow else textW
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                            val dateLabel = String.format("%02d/%02d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1)
                            Text(
                                text = dateLabel,
                                fontSize = 10.sp,
                                color = textG
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SECTION 5: SPENDING INSIGHTS
// ==========================================
@Composable
fun SpendingInsightsWidget(
    data: DashboardData,
    currencyFormatter: NumberFormat,
    onNavigateToBudgets: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceB = SurfaceBlue
    val borderC = BorderColor
    val textG = TextGray
    val textW = TextWhite
    val errorRed = DangerRed
    val greenGlow = NeonGreen
    val bgNavy = NavyBg
    val orangeAccent = AccentOrange

    if (!data.hasBudgets || data.budgetSnapshot.isEmpty()) {
        CabinetEmptyState(
            desc = "No spending guidelines established. Formulate strict budget limits to manage outflows.",
            btnLabel = "Configure Fresh Budget",
            onClick = onNavigateToBudgets,
            testTag = "spending_insights_empty"
        )
    } else {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceB),
            border = BorderStroke(1.dp, borderC),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                val highestVariance = data.budgetSnapshot.maxByOrNull { it.percentage }
                
                if (highestVariance != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CRITICAL OUTFLOW FOCUS",
                                fontSize = 9.sp,
                                color = textG,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Highest stress: ${highestVariance.category}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textW
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (highestVariance.percentage >= 0.85f) errorRed.copy(alpha = 0.08f)
                                    else greenGlow.copy(alpha = 0.08f)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${(highestVariance.percentage * 100).toInt()}% Limit Utilized",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (highestVariance.percentage >= 0.85f) errorRed else greenGlow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    data.budgetSnapshot.take(3).forEach { budget ->
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
                                    color = textW
                                )
                                Text(
                                    text = "${currencyFormatter.format(budget.spent)} of ${currencyFormatter.format(budget.limit)}",
                                    fontSize = 11.sp,
                                    color = textG
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(bgNavy)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(budget.percentage.coerceIn(0f, 1f))
                                        .clip(CircleShape)
                                        .background(
                                            if (budget.percentage >= 0.85f) errorRed
                                            else if (budget.percentage >= 0.60f) orangeAccent
                                            else greenGlow
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SECTION 6: SPLIT & GROUP SUMMARY
// ==========================================
@Composable
fun SplitGroupSummaryWidget(
    data: DashboardData,
    currencyFormatter: NumberFormat,
    onNavigateToTabs: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceB = SurfaceBlue
    val borderC = BorderColor
    val textG = TextGray
    val textW = TextWhite
    val errorRed = DangerRed
    val greenGlow = NeonGreen
    val bgNavy = NavyBg

    if (!data.hasSplits && data.youOwe == 0.0 && data.youAreOwed == 0.0 && !data.hasTrips) {
        CabinetEmptyState(
            desc = "No shared splitting information, active vacations, group settlements, or travel logs.",
            btnLabel = "Start Split Group",
            onClick = { onNavigateToTabs("split") },
            testTag = "splits_empty"
        )
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceB),
                border = BorderStroke(1.dp, borderC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "YOU OWE FRIENDS",
                                fontSize = 9.sp,
                                color = textG,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currencyFormatter.format(data.youOwe),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (data.youOwe > 0) errorRed else textW
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(borderC.copy(alpha = 0.4f))
                                .align(Alignment.CenterVertically)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        ) {
                            Text(
                                text = "YOU ARE OWED",
                                fontSize = 9.sp,
                                color = textG,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currencyFormatter.format(data.youAreOwed),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (data.youAreOwed > 0) greenGlow else textW
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { onNavigateToTabs("split") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, borderC),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Text("Settle Up or Open Groups", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (data.hasTrips) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceB),
                    border = BorderStroke(1.dp, borderC),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTabs("trips") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌎", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Active Shared Travel Ledger",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textW
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Manage ongoing split travel bills dynamically",
                                    fontSize = 10.sp,
                                    color = textG
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Travel details",
                            tint = greenGlow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SECTION 7: AI FINANCIAL INSIGHTS (RESERVED)
// ==========================================
@Composable
fun AiFinancialAdvisorInsightsWidget(
    data: DashboardData,
    modifier: Modifier = Modifier
) {
    val surfaceB = SurfaceBlue
    val borderC = BorderColor
    val textG = TextGray
    val textW = TextWhite
    val greenGlow = NeonGreen
    val bgNavy = NavyBg

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceB),
        border = BorderStroke(1.dp, borderC),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = greenGlow.copy(alpha = 0.12f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "AI Icon",
                            tint = greenGlow,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AI ADVISORY INSIGHTS",
                    fontSize = 9.sp,
                    color = textG,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgNavy.copy(alpha = 0.4f))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "• Savings pacing is 7.2% stronger than last month due to reduced dining frequency.",
                        fontSize = 11.sp,
                        color = textW,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Tip: Settle pending credit balances before tomorrow's interest statement accrual.",
                        fontSize = 11.sp,
                        color = textG,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==========================================
// CENTRAL REUSABLE EMPTY STATE COMPONENT
// ==========================================
@Composable
fun CabinetEmptyState(
    desc: String,
    btnLabel: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val surfaceB = SurfaceBlue
    val borderC = BorderColor
    val textG = TextGray
    val bgNavy = NavyBg
    val greenGlow = NeonGreen

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceB),
        border = BorderStroke(1.dp, borderC),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📁",
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = textG,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = bgNavy, contentColor = greenGlow),
                border = BorderStroke(1.dp, borderC),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = "+ $btnLabel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
