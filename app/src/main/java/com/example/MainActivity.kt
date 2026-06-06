package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.viewmodel.AiCoreState
import com.example.ui.viewmodel.WealthPulseViewModel
import java.text.NumberFormat
import java.util.Locale

// "Sophisticated Dark" Design System Color Definitions via CSS Variables mapping
data class CssThemeVariables(
    val bg: Color = Color(0xFF0C0A09),           // --bg: #0c0a09 (Deep elegant charcoal stone)
    val surface: Color = Color(0xFF161616),      // --surface: #161616 (Textured dark stone surface)
    val primary: Color = Color(0xFF10B981),      // --primary: #10b981 (Signature Emerald Green highlight)
    val secondary: Color = Color(0xFF78716C),    // --secondary: #78716c (Secondary muted grey)
    val accent: Color = Color(0xFFF59E0B),       // --accent: #f59e0b (Amber Gold for alerts)
    val danger: Color = Color(0xFFEF4444),       // --danger: #ef4444 (Crimson warning color)
    val textGray: Color = Color(0xFFA8A29E),     // --text-gray: #a8a29e (Info secondary text Stone 400)
    val textWhite: Color = Color(0xFFF5F5F4),    // --text-white: #f5f5f4 (Title primary text Stone 100)
    val border: Color = Color(0xFF292524)        // --border: #292524 (Subtle border)
) {
    fun get(name: String): Color {
        return when (name) {
            "--bg" -> bg
            "--surface" -> surface
            "--primary" -> primary
            "--secondary" -> secondary
            "--accent" -> accent
            "--danger" -> danger
            "--text-gray" -> textGray
            "--text-white" -> textWhite
            "--border" -> border
            else -> Color.Unspecified
        }
    }
}

val LocalCssThemeVariables = staticCompositionLocalOf { CssThemeVariables() }

@Composable
fun cssVar(name: String): Color {
    return LocalCssThemeVariables.current.get(name)
}

val NavyBg: Color @Composable get() = cssVar("--bg")
val SurfaceBlue: Color @Composable get() = cssVar("--surface")
val NeonGreen: Color @Composable get() = cssVar("--primary")
val DeepPurple: Color @Composable get() = cssVar("--secondary")
val AccentOrange: Color @Composable get() = cssVar("--accent")
val DangerRed: Color @Composable get() = cssVar("--danger")
val TextGray: Color @Composable get() = cssVar("--text-gray")
val TextWhite: Color @Composable get() = cssVar("--text-white")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WealthPulseTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    FinancialWorkspaceScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun WealthPulseTheme(
    variables: CssThemeVariables = CssThemeVariables(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalCssThemeVariables provides variables) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                background = cssVar("--bg"),
                surface = cssVar("--surface"),
                primary = cssVar("--primary"),
                secondary = cssVar("--secondary"),
                tertiary = cssVar("--accent"),
                onBackground = cssVar("--text-white"),
                onSurface = cssVar("--text-white")
            ),
            content = content
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FinancialWorkspaceScreen(
    modifier: Modifier = Modifier,
    viewModel: WealthPulseViewModel = viewModel()
) {
    val dailyExpenses by viewModel.dailyExpenses.collectAsState()
    val creditExpenses by viewModel.creditExpenses.collectAsState()
    val emiLoans by viewModel.emiLoans.collectAsState()
    val debtSplits by viewModel.debtSplits.collectAsState()
    val incomePaydays by viewModel.incomePaydays.collectAsState()
    val sipRecords by viewModel.sipRecords.collectAsState()
    val investmentRecords by viewModel.investmentRecords.collectAsState()
    val aiState by viewModel.aiState.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    var activeNavigationMenuTab by remember { mutableStateOf("home") }
    var userName by remember { mutableStateOf("Marcus Aurelius") }
    var userInputText by remember { mutableStateOf("") }
    var showQuickSimMicSheet by remember { mutableStateOf(false) }

    // Dialogue State for Manual Controls
    var activeManualDialog by remember { mutableStateOf<String?>(null) }
    var currentCategoryPage by remember { mutableStateOf<String?>(null) }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    // Aggregate values
    val totalExpense = dailyExpenses.sumOf { it.amount }
    val totalCredit = creditExpenses.sumOf { it.amount }
    val totalEmi = emiLoans.sumOf { it.amount }
    val totalDebt = debtSplits.sumOf { it.amount }
    val totalIncome = incomePaydays.sumOf { it.amount }
    val totalSip = sipRecords.sumOf { it.amount }
    val totalInvestmentInvested = investmentRecords.sumOf { it.amount }
    val totalInvestmentCurrent = investmentRecords.sumOf { it.currentValue }
    val trueDisposable = totalIncome - totalExpense - totalCredit - totalEmi - totalSip

    // Category Specific sums for Hub display
    val catExps = dailyExpenses.filter { it.category.contains("food", true) || it.category.contains("dining", true) }.sumOf { it.amount } +
                  creditExpenses.filter { it.category.contains("food", true) || it.category.contains("dining", true) || it.description.contains("zomato", true) || it.description.contains("swiggy", true) }.sumOf { it.amount }

    val catTrans = dailyExpenses.filter { it.category.contains("transport", true) || it.category.contains("ride", true) || it.category.contains("uber", true) || it.category.contains("ola", true) }.sumOf { it.amount }

    val catShop = dailyExpenses.filter { it.category.contains("shopping", true) || it.category.contains("clothes", true) }.sumOf { it.amount } +
                  creditExpenses.filter { it.category.contains("shopping", true) || it.category.contains("clothes", true) || it.category.contains("electronics", true) }.sumOf { it.amount }

    val catUtil = dailyExpenses.filter { it.category.contains("utility", true) || it.category.contains("utilities", true) || it.category.contains("rent", true) || it.category.contains("bill", true) }.sumOf { it.amount } +
                  creditExpenses.filter { it.category.contains("utility", true) || it.category.contains("utilities", true) || it.category.contains("bill", true) }.sumOf { it.amount } +
                  emiLoans.sumOf { it.amount }

    val catDebt = debtSplits.sumOf { it.amount }
    val catSipSum = sipRecords.sumOf { it.amount }
    val catEquitySum = investmentRecords.sumOf { it.currentValue }
    val catIncomeSum = incomePaydays.sumOf { it.amount }

    Box(
        modifier = modifier
            .background(NavyBg)
            .fillMaxSize()
    ) {
        if (currentCategoryPage != null) {
            CategoryWorkspacePage(
                categoryName = currentCategoryPage!!,
                dailyExpenses = dailyExpenses,
                creditExpenses = creditExpenses,
                emiLoans = emiLoans,
                debtSplits = debtSplits,
                incomePaydays = incomePaydays,
                sipRecords = sipRecords,
                investmentRecords = investmentRecords,
                currencyFormatter = currencyFormatter,
                viewModel = viewModel,
                onBack = { currentCategoryPage = null }
            )
        } else {
            Scaffold(
                containerColor = NavyBg
            ) { innerScaffoldPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerScaffoldPadding.calculateBottomPadding())
                ) {
                    if (activeNavigationMenuTab == "home") {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
            // BRAND HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "WEALTHPULSE CORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF78716C), // Stone 500
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Good Evening, $userName",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFE7E5E4), // Stone 200 elegant serif
                        fontFamily = FontFamily.Serif,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick simulation live indicator dot inside elegant border wrapping
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1C1917))
                            .border(1.dp, Color(0xFF292524), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF10B981).copy(alpha = 0.2f), Color(0xFF1C1917))
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                    }
                }
            }

            // FINANCIAL METRICS ORCHESTRATOR DASHBOARD (Sophisticated Dark Runway Card)
            Card(
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color(0xFF292524)), // subtle stone-800 border
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)), // stone card base
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "DAILY RUN RATE",
                        fontSize = 11.sp,
                        color = Color(0xFF78716C), // Stone 500
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = currencyFormatter.format(trueDisposable),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Light,
                            color = if (trueDisposable >= 0) NeonGreen else DangerRed,
                            fontFamily = FontFamily.Serif
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    if (trueDisposable >= 0) Color(0xFF047857).copy(alpha = 0.15f) else Color(0xFFB91C1C).copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (trueDisposable >= 0) NeonGreen else DangerRed)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (trueDisposable >= 0) "+4.2%" else "OVERFLOW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (trueDisposable >= 0) NeonGreen else DangerRed,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Gorgeous integrated emerald safe-spend tip strip
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF064E3B).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF064E3B).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (trueDisposable >= 0) "Runway stable. Safe to spend ₹8.5k this weekend." else "Runway warning! Re-balance active card debts.",
                            fontSize = 11.sp,
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF292524), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Row showing values of individual modules
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FinancialMetricItem(
                            label = "Expense",
                            value = currencyFormatter.format(totalExpense),
                            color = TextWhite,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricItem(
                            label = "Cards",
                            value = currencyFormatter.format(totalCredit),
                            color = Color(0xFFD97706),
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricItem(
                            label = "EMI due",
                            value = currencyFormatter.format(totalEmi),
                            color = AccentOrange,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricItem(
                            label = "Payday",
                            value = currencyFormatter.format(totalIncome),
                            color = NeonGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FinancialMetricItem(
                            label = "SIP Commit",
                            value = currencyFormatter.format(totalSip),
                            color = Color(0xFF3B82F6),
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricItem(
                            label = "Port. Cost",
                            value = currencyFormatter.format(totalInvestmentInvested),
                            color = Color(0xFFA8A29E),
                            modifier = Modifier.weight(1f)
                        )
                        FinancialMetricItem(
                            label = "Port. Value",
                            value = currencyFormatter.format(totalInvestmentCurrent),
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        val portProfit = totalInvestmentCurrent - totalInvestmentInvested
                        val portPct = if (totalInvestmentInvested > 0) (portProfit / totalInvestmentInvested) * 100 else 0.0
                        FinancialMetricItem(
                            label = "Port. G/L",
                            value = "${if (portProfit >= 0) "+" else ""}${currencyFormatter.format(portProfit)} (${String.format("%.1f", portPct)}%)",
                            color = if (portProfit >= 0) NeonGreen else DangerRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- CATEGORIES HUB & PAGES MENU ---
            Text(
                text = "📂 COGNITIVE CATEGORIES PAGES",
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF292524)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EXPLORE CATEGORIES",
                        fontSize = 12.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Access dedicated category-specific spaces to track focused records and commit details instantly.",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val categories = listOf(
                        Triple("Food & Dining", "🍔", NeonGreen),
                        Triple("Transport", "🚗", Color(0xFF3B82F6)),
                        Triple("Shopping", "🛍️", Color(0xFFA855F7)),
                        Triple("Rent & Utilities", "💡", AccentOrange),
                        Triple("Debts & Splits", "👥", Color(0xFFF43F5E)),
                        Triple("SIP Mutual Funds", "📈", Color(0xFF10B981)),
                        Triple("Equity Portfolio", "💼", Color(0xFFEAB308)),
                        Triple("Total Inflow", "💰", NeonGreen)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in categories.indices step 2) {
                            if (i + 1 < categories.size) {
                                val cat1 = categories[i]
                                val cat2 = categories[i + 1]
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CategoryHubGridCard(
                                        title = cat1.first,
                                        emoji = cat1.second,
                                        color = cat1.third,
                                        total = when (cat1.first) {
                                            "Food & Dining" -> currencyFormatter.format(catExps)
                                            "Transport" -> currencyFormatter.format(catTrans)
                                            "Shopping" -> currencyFormatter.format(catShop)
                                            "Rent & Utilities" -> currencyFormatter.format(catUtil)
                                            "Debts & Splits" -> currencyFormatter.format(catDebt)
                                            "SIP Mutual Funds" -> currencyFormatter.format(catSipSum)
                                            "Equity Portfolio" -> currencyFormatter.format(catEquitySum)
                                            else -> currencyFormatter.format(catIncomeSum)
                                        },
                                        modifier = Modifier.weight(1f),
                                        onClick = { currentCategoryPage = cat1.first }
                                    )
                                    CategoryHubGridCard(
                                        title = cat2.first,
                                        emoji = cat2.second,
                                        color = cat2.third,
                                        total = when (cat2.first) {
                                            "Food & Dining" -> currencyFormatter.format(catExps)
                                            "Transport" -> currencyFormatter.format(catTrans)
                                            "Shopping" -> currencyFormatter.format(catShop)
                                            "Rent & Utilities" -> currencyFormatter.format(catUtil)
                                            "Debts & Splits" -> currencyFormatter.format(catDebt)
                                            "SIP Mutual Funds" -> currencyFormatter.format(catSipSum)
                                            "Equity Portfolio" -> currencyFormatter.format(catEquitySum)
                                            else -> currencyFormatter.format(catIncomeSum)
                                        },
                                        modifier = Modifier.weight(1f),
                                        onClick = { currentCategoryPage = cat2.first }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- AI CONSOLE (NATURAL LANGUAGE OR VOICE EXTRACTOR) ---
            Text(
                text = "LAST AI ORCHESTRATION",
                fontSize = 10.sp,
                color = Color(0xFF57534E), // Stone 600
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), // subtle border border-white/5
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = userInputText,
                            onValueChange = { userInputText = it },
                            placeholder = { Text("Log daily, credits, EMIs, splits, incomes...", color = TextGray, fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite, fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonGreen,
                                unfocusedBorderColor = Color(0xFF292524),
                                focusedLabelColor = NeonGreen
                            ),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Mic Button (Simulates Voice-to-Text inputs)
                        IconButton(
                            onClick = { showQuickSimMicSheet = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF292524))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                                .testTag("mic_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBox, // Using AccountBox or suitable standard icon for voice simulation
                                contentDescription = "Simulate voice input",
                                tint = NeonGreen
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        val context = LocalContext.current
                        IconButton(
                            onClick = {
                                if (userInputText.isBlank()) {
                                    Toast.makeText(context, "Please enter a transaction prompt first", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.processVoiceOrTextInput(userInputText)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(NeonGreen)
                                .testTag("send_prompt_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Process financial command",
                                tint = Color(0xFF0C0A09)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated voice cue helpers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💡 Tap the profile mic button to test native transactions instantly",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                    }

                    // REST API RESPONSE / PARSED STATE INDICATION
                    AnimatedVisibility(
                        visible = aiState !is AiCoreState.Idle,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            HorizontalDivider(color = Color(0xFF262A44), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            when (val state = aiState) {
                                is AiCoreState.Idle -> { /* Idle placeholder */ }
                                is AiCoreState.Loading -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(color = DeepPurple, strokeWidth = 3.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            "WealthPulse AI Parsing Payload...",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextWhite
                                        )
                                    }
                                }
                                is AiCoreState.Error -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(DangerRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = DangerRed)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("ANALYSIS ENGINES EXCEPTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DangerRed)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(state.message, fontSize = 12.sp, color = TextWhite)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.clearAiState() },
                                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Acknowledge", fontSize = 11.sp)
                                        }
                                    }
                                }
                                is AiCoreState.Success -> {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .clip(CircleShape)
                                                        .background(NeonGreen)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    "DECISION : ${state.intent}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NeonGreen
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.clearAiState() },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Dismiss status", tint = TextGray, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // If it is a financial query conversational reply
                                        if (state.intent == "FINANCIAL_QUERY_INSIGHT" || state.intent == "CLARIFICATION_REQUIRED") {
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F38)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Menu, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("AI RESPONSE (${state.targetModule})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
                                                    }
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = state.queryResponseText ?: "Awaiting response values...",
                                                        fontSize = 13.sp,
                                                        color = TextWhite,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        } else {
                                            // LOGGING successful mapping notification
                                            Text(
                                                text = "Parsed mapping auto-saved to database!",
                                                fontSize = 13.sp,
                                                color = TextWhite,
                                                fontWeight = FontWeight.Normal
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "• Mapped: ${state.description} | Value: ${currencyFormatter.format(state.amount)} | Mode: ${state.paymentMode}",
                                                fontSize = 12.sp,
                                                color = TextGray
                                            )
                                        }

                                        // SMART INSIGHT ALERT (If tripped)
                                        if (state.alertFlagged) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Card(
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C151D)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Warning, contentDescription = "Threat Alert", tint = DangerRed, modifier = Modifier.size(20.dp))
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text("SMART AI DETECTED ALERT", fontSize = 11.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                                                        Text(state.alertReason ?: "Threat anomaly flags matching spend boundaries.", fontSize = 12.sp, color = TextWhite)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // RAW EXTRACTED JSON PREVIEW (Collapsable)
                                        var showRawJson by remember { mutableStateOf(false) }
                                        TextButton(
                                            onClick = { showRawJson = !showRawJson },
                                            colors = ButtonDefaults.textButtonColors(contentColor = TextGray),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (showRawJson) Icons.Default.PlayArrow else Icons.Default.PlayArrow, // Rotate manually or simple toggles
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (showRawJson) "Hide JSON extraction Payload" else "Show RAW extracted JSON schema", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        if (showRawJson) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFF07090E), RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFF1D2636), RoundedCornerShape(8.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = state.rawJson,
                                                    color = NeonGreen,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    modifier = Modifier.horizontalScroll(rememberScrollState())
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

            // QUICK OPTION LOG TEMPLATES CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF292524)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡ ONE-TAP QUICK LOG TEMPLATES",
                        fontSize = 11.sp,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Draft typical financial entries instantly with a single tap. Records can be removed one-by-one inside their respective workspace tabs.",
                        fontSize = 11.sp,
                        color = TextGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val context = LocalContext.current

                    // Scrollable list of typical predefined transactions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            FinancialPresetCardTemp("☕ Chai & Coffee", "₹30", "Cash Expense") {
                                viewModel.addManualExpense(30.0, "Tea & Cookies with colleagues", "Food & Dining", "Cash")
                            },
                            FinancialPresetCardTemp("🍕 Zomato Dinner", "₹420", "UPI Expense") {
                                viewModel.addManualExpense(420.0, "Dinner Order Zomato Pro", "Food & Dining", "UPI")
                            },
                            FinancialPresetCardTemp("🚗 Auto Rickshaw", "₹120", "UPI Expense") {
                                viewModel.addManualExpense(120.0, "Auto Commute to office", "Transport", "UPI")
                            },
                            FinancialPresetCardTemp("🛍️ Amazon Clothes", "₹1,800", "Credit Swipes") {
                                viewModel.addManualCredit(1800.0, "Apparel Order Amazon Fashion", "HDFC Millennia", "Shopping")
                            },
                            FinancialPresetCardTemp("🏠 Home Rent", "₹15,000", "Bank Transfer") {
                                viewModel.addManualExpense(15000.0, "Flat Rental Payout", "Rent", "Bank Transfer")
                            },
                            FinancialPresetCardTemp("💰 Salary Credited", "₹85,000", "Income Direct") {
                                viewModel.addManualIncome(85000.0, "Corporate Fulltime Salary", "Monthly")
                            },
                            FinancialPresetCardTemp("📈 Mutual Fund SIP", "₹5,000", "SIP Committed") {
                                viewModel.addManualSip(5000.0, "Parag Parikh Flexi Fund Mutual SIP", "Mutual Funds", 5)
                            },
                            FinancialPresetCardTemp("🪙 Digital Gold Asset", "₹10,000", "Portfolio Item") {
                                viewModel.addManualInvestment(10000.0, "Digital sovereign Gold Bond asset", "Gold", 11200.0)
                            }
                        )

                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF262524).copy(alpha = 0.5f))
                                    .border(1.dp, Color(0xFF292524), RoundedCornerShape(14.dp))
                                    .clickable {
                                        preset.action()
                                        Toast.makeText(context, "Logged ${preset.name} (${preset.amt})", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Text(preset.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(preset.amt, fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(TextGray)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(preset.type, fontSize = 10.sp, color = TextGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TOOL MODULE QUICK SPREADSHEEET FOR MANUAL ADDS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOCAL DATABASES & ACTIONS",
                    fontSize = 12.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Button(
                    onClick = {
                        activeManualDialog = when (activeTab) {
                            0 -> "EXPENSE"
                            1 -> "CREDIT"
                            2 -> "EMI"
                            3 -> "DEBT"
                            4 -> "INCOME"
                            5 -> "SIP"
                            6 -> "INVESTMENT"
                            else -> "EXPENSE"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceBlue),
                    border = BorderStroke(1.dp, Color(0xFF1D2636)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Manual Append", tint = NeonGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manual Record", fontSize = 11.sp, color = TextWhite)
                }
            }

            // TAB SCENARDS
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.Transparent,
                contentColor = NeonGreen,
                edgePadding = 0.dp,
                divider = { HorizontalDivider(color = Color(0xFF1D2636), thickness = 1.dp) },
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("EXPENSE 💸", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("CARDS 💳", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("EMIS 📊", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("SPLIT 👥", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    text = { Text("PAYDAY 💰", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 5,
                    onClick = { activeTab = 5 },
                    text = { Text("SIP 📈", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 6,
                    onClick = { activeTab = 6 },
                    text = { Text("PORTFOLIO 💼", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            // LIST VIEWS OF ACTIVE MODULARY TAB
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp)
            ) {
                when (activeTab) {
                    0 -> {
                        // Expense Column Card List
                        if (dailyExpenses.isEmpty()) {
                            EmptyStatePlaceholder("No Expense logged. Try entering '₹150 for lunch via UPI'")
                        } else {
                            Column {
                                dailyExpenses.forEach { exp ->
                                    TransactionItemCard(
                                        title = exp.description,
                                        subtitle = "${exp.category} • ${exp.paymentMode}",
                                        amount = currencyFormatter.format(exp.amount),
                                        onDelete = { viewModel.deleteExpense(exp.id) },
                                        colorAccent = TextWhite
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Card Columns
                        if (creditExpenses.isEmpty()) {
                            EmptyStatePlaceholder("No Credit spending logged. Try: 'Swiped ₹1,200 on my ICICI Amazon Card'")
                        } else {
                            Column {
                                creditExpenses.forEach { cred ->
                                    TransactionItemCard(
                                        title = cred.description,
                                        subtitle = "${cred.cardName} • ${cred.category} ${if (cred.isEmiConversion) "• EMI convert" else ""}",
                                        amount = currencyFormatter.format(cred.amount),
                                        onDelete = { viewModel.deleteCredit(cred.id) },
                                        colorAccent = DeepPurple
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // EMI Tracker Column
                        if (emiLoans.isEmpty()) {
                            EmptyStatePlaceholder("No long-term structural liabilities logged. Try: 'EMIs of ₹8,000 for car tenure left 12 months'")
                        } else {
                            Column {
                                emiLoans.forEach { emi ->
                                    TransactionItemCard(
                                        title = emi.description,
                                        subtitle = "Liability Category • Remaining Tenure: ${emi.remainingMonths}/${emi.totalTenureMonths} months",
                                        amount = "${currencyFormatter.format(emi.amount)} /mo",
                                        onDelete = { viewModel.deleteEmi(emi.id) },
                                        colorAccent = AccentOrange,
                                        progressFraction = emi.remainingMonths.toFloat() / emi.totalTenureMonths.toFloat()
                                    )
                                }
                            }
                        }
                    }
                    3 -> {
                        // Peer Debt Splitting Tracker
                        if (debtSplits.isEmpty()) {
                            EmptyStatePlaceholder("No lend/borrow ratios logged. Try: 'Amit owes ₹450 for lunch on Flatmates split'")
                        } else {
                            Column {
                                debtSplits.forEach { debt ->
                                    TransactionItemCard(
                                        title = debt.description,
                                        subtitle = "Debtor: ${debt.debtPersonInvolved} ${if (debt.isGroupSplit) "• Room: ${debt.groupName}" else ""}",
                                        amount = currencyFormatter.format(debt.amount),
                                        onDelete = { viewModel.deleteDebt(debt.id) },
                                        colorAccent = AccentOrange
                                    )
                                }
                            }
                        }
                    }
                    4 -> {
                        // Income Payday Countdown Tracker
                        if (incomePaydays.isEmpty()) {
                            EmptyStatePlaceholder("No salary or wage records saved. Try: 'Received salary payout of ₹50,000'")
                        } else {
                            Column {
                                incomePaydays.forEach { inc ->
                                    TransactionItemCard(
                                        title = inc.description,
                                        subtitle = "Wage Class: ${inc.incomeFrequency} • Mode: ${inc.paymentMode}",
                                        amount = currencyFormatter.format(inc.amount),
                                        onDelete = { viewModel.deleteIncome(inc.id) },
                                        colorAccent = NeonGreen
                                    )
                                }
                            }
                        }
                    }
                    5 -> {
                        // SIP Records Tracker Column
                        if (sipRecords.isEmpty()) {
                            EmptyStatePlaceholder("No active SIPs logged. Try: 'Auto-debit ₹5,000 for Nippon Small Cap monthly SIP on 10th'")
                        } else {
                            Column {
                                sipRecords.forEach { sip ->
                                    TransactionItemCard(
                                        title = sip.description,
                                        subtitle = "SIP Schedule: ${sip.frequency} on ${sip.dayOfMonth}th • Category: ${sip.investmentCategory}",
                                        amount = "${currencyFormatter.format(sip.amount)}/mo",
                                        onDelete = { viewModel.deleteSip(sip.id) },
                                        colorAccent = Color(0xFF3B82F6) // blue
                                    )
                                }
                            }
                        }
                    }
                    6 -> {
                        // Investment Records Tracker Column
                        if (investmentRecords.isEmpty()) {
                            EmptyStatePlaceholder("No portfolio assets logged. Try: 'Invested ₹20,000 in INFY Stock category equity value ₹23,000'")
                        } else {
                            Column {
                                investmentRecords.forEach { inv ->
                                    val profit = inv.currentValue - inv.amount
                                    val percentage = if (inv.amount > 0) (profit / inv.amount) * 100 else 0.0
                                    val returnString = String.format("%.2f%%", percentage)
                                    val returnIndicator = if (profit >= 0) "+$returnString" else returnString
                                    TransactionItemCard(
                                        title = inv.description,
                                        subtitle = "Invested: ${currencyFormatter.format(inv.amount)} • Category: ${inv.category} • Return: $returnIndicator",
                                        amount = currencyFormatter.format(inv.currentValue),
                                        onDelete = { viewModel.deleteInvestment(inv.id) },
                                        colorAccent = if (profit >= 0) NeonGreen else DangerRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
                            Spacer(modifier = Modifier.height(100.dp))
                        } // CLOSES home page column
                    } else if (activeNavigationMenuTab == "commitments") {
                        CommitmentsWorkspacePage(
                            emiLoans = emiLoans,
                            sipRecords = sipRecords,
                            debtSplits = debtSplits,
                            totalEmi = totalEmi,
                            totalSip = totalSip,
                            totalDebt = totalDebt,
                            currencyFormatter = currencyFormatter,
                            viewModel = viewModel,
                            onTriggerManual = { type -> activeManualDialog = type }
                        )
                    } else if (activeNavigationMenuTab == "user") {
                        UserProfileWorkspacePage(
                            currencyFormatter = currencyFormatter,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            totalCredit = totalCredit,
                            totalSip = totalSip,
                            trueDisposable = trueDisposable,
                            catExps = catExps
                        )
                    } else if (activeNavigationMenuTab == "settings") {
                        SettingsWorkspacePage(
                            viewModel = viewModel
                        )
                    }

                    // BEAUTIFUL FLOATING GLASS ISLAND NAVIGATION TAB
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .widthIn(max = 500.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xEE161616),
                                        Color(0xFB0F0F0F)
                                    )
                                )
                            )
                            .border(
                                BorderStroke(
                                    width = 1.dp,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x33FFFFFF),
                                            Color(0x0EFFFFFF)
                                        )
                                    )
                                ),
                                shape = RoundedCornerShape(32.dp)
                            )
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val items = listOf(
                                Triple("home", Icons.Default.Home, "Dashboard"),
                                Triple("commitments", Icons.Default.DateRange, "Commitments"),
                                Triple("user", Icons.Default.Person, "Profile"),
                                Triple("settings", Icons.Default.Settings, "Settings")
                            )

                            items.forEach { (route, icon, label) ->
                                val isSelected = activeNavigationMenuTab == route
                                val tintColor = if (isSelected) NeonGreen else TextGray
                                val bgAlpha = if (isSelected) 0.15f else 0.0f

                                Box(
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) NeonGreen.copy(alpha = bgAlpha) else Color.Transparent)
                                        .clickable { activeNavigationMenuTab = route }
                                        .padding(vertical = 8.dp)
                                        .testTag("nav_item_$route"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = tintColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = label,
                                            color = tintColor,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            letterSpacing = 0.4.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } // CLOSES THE conditional "else" block

        // VOICE SIMULATOR PROFILE MODAL
        if (showQuickSimMicSheet) {
            Dialog(onDismissRequest = { showQuickSimMicSheet = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151926)),
                    border = BorderStroke(1.dp, Color(0xFF2B334D)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DeepPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBox, contentDescription = null, tint = TextWhite, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("SIMULATE AI COMMUNICATOR", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Select a voice payload to auto-parse", fontSize = 11.sp, color = TextGray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFF232A40))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Sandbox few-shot examples inside list items
                        val templates = listOf(
                            "₹120 spent on Auto via UPI" to "LOG_DAILY_EXPENSE",
                            "Swiped ₹45,000 on my HDFC Millennia for a new laptop" to "LOG_CREDIT_EXPENSE",
                            "Paid ₹3,000 total for dinner on Goa Trip 2026. Amit and Neha owe their equal shares." to "LOG_DEBT_SPLIT",
                            "Logged monthly housing EMI of ₹12,000 with 8 months remaining" to "LOG_EMI_LOAN",
                            "Freelance payouts received ₹60,000 from tech giant" to "LOG_INCOME_PAYDAY",
                            "Auto-debit ₹5,000 Nippon Large Cap Mutual Fund SIP recurring monthly on 5th" to "LOG_SIP",
                            "Invested ₹25,000 in INFY Stock holding value ₹28,500 under equity" to "LOG_INVESTMENT",
                            "How is my portfolio health and what are my active SIP commitments?" to "FINANCIAL_QUERY_INSIGHT",
                            "I bought some groceries" to "CLARIFICATION_REQUIRED (Missing Price!)"
                        )

                        templates.forEach { (prompt, label) ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        userInputText = prompt
                                        showQuickSimMicSheet = false
                                        viewModel.processVoiceOrTextInput(prompt)
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2436))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(prompt, fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Simulates: $label", fontSize = 10.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showQuickSimMicSheet = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282F48)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel", color = TextWhite)
                        }
                    }
                }
            }
        }

        // --- MANUAL CONSTRUCT RECORD DIALOGUES (1 PER MODULE) ---
        activeManualDialog?.let { type ->
            var fieldAmt by remember { mutableStateOf("") }
            var fieldDesc by remember { mutableStateOf("") }

            // Module specific fields
            var fieldCat by remember {
                mutableStateOf(
                    when (type) {
                        "SIP" -> "Mutual Funds"
                        "INVESTMENT" -> "Equity"
                        "CREDIT" -> "Shopping"
                        else -> "Food & Dining"
                    }
                )
            }
            var fieldPayMode by remember { mutableStateOf("UPI") }
            var fieldCardName by remember { mutableStateOf("HDFC Millennia") }
            var fieldTenure by remember { mutableStateOf("12") }
            var fieldRemMonths by remember { mutableStateOf("8") }
            var fieldPerson by remember { mutableStateOf("Amit") }
            var fieldGroupName by remember { mutableStateOf("Flatmates") }
            var fieldFrequency by remember { mutableStateOf("Monthly") }
            var fieldSipDay by remember { mutableStateOf("5") }
            var fieldCurrentVal by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { activeManualDialog = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                    border = BorderStroke(1.dp, Color(0xFF1E2B3E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Log $type manually",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = fieldAmt,
                            onValueChange = { fieldAmt = it },
                            label = { Text("Amount (INR)", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Amount Additions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(100, 500, 1000, 5000).forEach { inc ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF292524))
                                        .clickable {
                                            val current = fieldAmt.toDoubleOrNull() ?: 0.0
                                            fieldAmt = (current + inc).toInt().toString()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "+₹$inc", fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF442D2D))
                                    .clickable { fieldAmt = "" }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "Clear", fontSize = 10.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                             }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = fieldDesc,
                            onValueChange = { fieldDesc = it },
                            label = { Text("Description (e.g., Zomato, Rent, INFY)", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom configurations per type
                        when (type) {
                            "EXPENSE" -> {
                                OutlinedTextField(
                                    value = fieldCat,
                                    onValueChange = { fieldCat = it },
                                    label = { Text("Category (Spent On)", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Category:",
                                    options = listOf(
                                        "🍔 Food" to "Food & Dining",
                                        "🚗 Transport" to "Transport",
                                        "💡 Utilities" to "Utilities",
                                        "🛒 Groceries" to "Groceries",
                                        "🛍️ Shopping" to "Shopping",
                                        "🎬 Entertainment" to "Entertainment",
                                        "✈️ Travel" to "Travel",
                                        "📦 Others" to "General"
                                    ),
                                    selected = fieldCat,
                                    onSelect = { fieldCat = it }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = fieldPayMode,
                                    onValueChange = { fieldPayMode = it },
                                    label = { Text("Payment Mode", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Payment Mode:",
                                    options = listOf(
                                        "📱 UPI" to "UPI",
                                        "💵 Cash" to "Cash",
                                        "💳 Credit Card" to "Credit Card",
                                        "💳 Debit Card" to "Debit Card",
                                        "🏦 Bank Transfer" to "Bank Transfer"
                                    ),
                                    selected = fieldPayMode,
                                    onSelect = { fieldPayMode = it }
                                )
                            }
                            "CREDIT" -> {
                                OutlinedTextField(
                                    value = fieldCardName,
                                    onValueChange = { fieldCardName = it },
                                    label = { Text("Card Name", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Card Name:",
                                    options = listOf(
                                        "💳 HDFC Millennia" to "HDFC Millennia",
                                        "💳 ICICI Amazon" to "ICICI Amazon",
                                        "💳 SBI Card" to "SBI Card",
                                        "💳 AXIS Magnus" to "Axis Magnus",
                                        "💳 Custom" to "Custom"
                                    ),
                                    selected = fieldCardName,
                                    onSelect = { fieldCardName = it },
                                    colorAccent = DeepPurple
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = fieldCat,
                                    onValueChange = { fieldCat = it },
                                    label = { Text("Category (Spent On)", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Category:",
                                    options = listOf(
                                        "🛍️ Shopping" to "Shopping",
                                        "🍔 Dining" to "Dining",
                                        "💻 Electronics" to "Electronics",
                                        "💡 Utilities" to "Utilities",
                                        "🛒 Groceries" to "Groceries",
                                        "📦 Others" to "General"
                                    ),
                                    selected = fieldCat,
                                    onSelect = { fieldCat = it },
                                    colorAccent = DeepPurple
                                )
                            }
                            "EMI" -> {
                                OutlinedTextField(
                                    value = fieldTenure,
                                    onValueChange = { fieldTenure = it },
                                    label = { Text("Total Tenure (Months)", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Tenure:",
                                    options = listOf(
                                        "3 Months" to "3",
                                        "6 Months" to "6",
                                        "9 Months" to "9",
                                        "12 Months" to "12",
                                        "24 Months" to "24",
                                        "36 Months" to "36"
                                    ),
                                    selected = fieldTenure,
                                    onSelect = { fieldTenure = it },
                                    colorAccent = AccentOrange
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = fieldRemMonths,
                                    onValueChange = { fieldRemMonths = it },
                                    label = { Text("Remaining Months", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Remaining:",
                                    options = listOf(
                                        "1 Month" to "1",
                                        "3 Months" to "3",
                                        "6 Months" to "6",
                                        "8 Months" to "8",
                                        "12 Months" to "12",
                                        "18 Months" to "18",
                                        "24 Months" to "24"
                                    ),
                                    selected = fieldRemMonths,
                                    onSelect = { fieldRemMonths = it },
                                    colorAccent = AccentOrange
                                )
                            }
                            "DEBT" -> {
                                OutlinedTextField(
                                    value = fieldPerson,
                                    onValueChange = { fieldPerson = it },
                                    label = { Text("Involved Person Name", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Person:",
                                    options = listOf(
                                        "👤 Amit" to "Amit",
                                        "👤 Neha" to "Neha",
                                        "👤 Rahul" to "Rahul",
                                        "👤 Priya" to "Priya",
                                        "👤 Custom" to "Custom"
                                    ),
                                    selected = fieldPerson,
                                    onSelect = { fieldPerson = it },
                                    colorAccent = AccentOrange
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = fieldGroupName,
                                    onValueChange = { fieldGroupName = it },
                                    label = { Text("Group Split Session name", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Group/Room:",
                                    options = listOf(
                                        "✈️ Goa Trip 2026" to "Goa Trip 2026",
                                        "🏠 Flatmates" to "Flatmates",
                                        "🍱 Office Lunch" to "Office Lunch"
                                    ),
                                    selected = fieldGroupName,
                                    onSelect = { fieldGroupName = it },
                                    colorAccent = AccentOrange
                                )
                            }
                            "INCOME" -> {
                                OutlinedTextField(
                                    value = fieldFrequency,
                                    onValueChange = { fieldFrequency = it },
                                    label = { Text("Frequency", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Income Frequency:",
                                    options = listOf(
                                        "💰 Monthly" to "Monthly",
                                        "💼 Freelance" to "Freelance",
                                        "🎁 One-off" to "One-off"
                                    ),
                                    selected = fieldFrequency,
                                    onSelect = { fieldFrequency = it },
                                    colorAccent = NeonGreen
                                )
                            }
                            "SIP" -> {
                                OutlinedTextField(
                                    value = fieldCat,
                                    onValueChange = { fieldCat = it },
                                    label = { Text("SIP category", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Investment Class:",
                                    options = listOf(
                                        "📈 Mutual Funds" to "Mutual Funds",
                                        "📊 Index Funds" to "Index Funds",
                                        "💼 Equity" to "Equity",
                                        "🧑‍💻 Crypto" to "Crypto",
                                        "🟡 Gold" to "Gold"
                                    ),
                                    selected = fieldCat,
                                    onSelect = { fieldCat = it },
                                    colorAccent = Color(0xFF3B82F6)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = fieldSipDay,
                                    onValueChange = { fieldSipDay = it },
                                    label = { Text("Day of month auto-debits (e.g. 5)", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Day of Month:",
                                    options = listOf(
                                        "1st" to "1",
                                        "5th" to "5",
                                        "10th" to "10",
                                        "15th" to "15",
                                        "20th" to "20",
                                        "25th" to "25"
                                    ),
                                    selected = fieldSipDay,
                                    onSelect = { fieldSipDay = it },
                                    colorAccent = Color(0xFF3B82F6)
                                )
                            }
                            "INVESTMENT" -> {
                                OutlinedTextField(
                                    value = fieldCat,
                                    onValueChange = { fieldCat = it },
                                    label = { Text("Investment Asset category (Stocks, Gold, FD, Crypto)", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                ManualOptionSelector(
                                    label = "Or Speed Select Asset category:",
                                    options = listOf(
                                        "💼 Stocks/Equity" to "Equity",
                                        "📈 Mutual Funds" to "Mutual Funds",
                                        "🪙 Crypto" to "Crypto",
                                        "🟡 Gold" to "Gold",
                                        "🏦 Fixed Deposits" to "Fixed Deposits"
                                    ),
                                    selected = fieldCat,
                                    onSelect = { fieldCat = it },
                                    colorAccent = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = fieldCurrentVal,
                                    onValueChange = { fieldCurrentVal = it },
                                    label = { Text("Current valuation", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { activeManualDialog = null }) {
                                Text("Cancel", color = TextGray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val amtD = fieldAmt.toDoubleOrNull() ?: 0.0
                                    if (amtD > 0.0 && fieldDesc.isNotBlank()) {
                                        when (type) {
                                            "EXPENSE" -> viewModel.addManualExpense(amtD, fieldDesc, fieldCat, fieldPayMode)
                                            "CREDIT" -> viewModel.addManualCredit(amtD, fieldDesc, fieldCardName, fieldCat)
                                            "EMI" -> viewModel.addManualEmi(amtD, fieldDesc, fieldTenure.toIntOrNull() ?: 12, fieldRemMonths.toIntOrNull() ?: 8)
                                            "DEBT" -> viewModel.addManualDebt(amtD, fieldDesc, fieldPerson, fieldGroupName.isNotBlank(), fieldGroupName)
                                            "INCOME" -> viewModel.addManualIncome(amtD, fieldDesc, fieldFrequency)
                                            "SIP" -> viewModel.addManualSip(amtD, fieldDesc, fieldCat, fieldSipDay.toIntOrNull() ?: 5)
                                            "INVESTMENT" -> {
                                                val currVal = fieldCurrentVal.toDoubleOrNull() ?: amtD
                                                viewModel.addManualInvestment(amtD, fieldDesc, fieldCat, currVal)
                                            }
                                        }
                                        activeManualDialog = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                            ) {
                                Text("Save Record", color = NavyBg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManualOptionSelector(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    colorAccent: Color = NeonGreen
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            options.forEach { (display, rawValue) ->
                val isSelected = selected.lowercase() == rawValue.lowercase() || selected == rawValue
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) colorAccent else Color(0xFF292524),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(if (isSelected) colorAccent.copy(alpha = 0.15f) else Color(0xFF161616))
                        .clickable { onSelect(rawValue) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = display,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) colorAccent else TextWhite
                    )
                }
            }
        }
    }
}

// Subordinate helper layouts
@Composable
fun FinancialMetricItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = label, fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TransactionItemCard(
    title: String,
    subtitle: String,
    amount: String,
    onDelete: () -> Unit,
    colorAccent: Color,
    progressFraction: Float? = null
) {
    Card(
        shape = RoundedCornerShape(24.dp), // round-3xl style
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), // subtle border-white/5
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colorAccent)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            color = TextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = amount,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = colorAccent
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete record",
                            tint = DangerRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (progressFraction != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AccentOrange,
                    trackColor = Color(0xFF1E2638)
                )
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(msg: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = null,
            tint = TextGray.copy(alpha = 0.5f),
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = msg,
            fontSize = 12.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

data class FinancialPresetCardTemp(
    val name: String,
    val amt: String,
    val type: String,
    val action: () -> Unit
)

@Composable
fun CategoryHubGridCard(
    title: String,
    emoji: String,
    color: Color,
    total: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1C1A))
            .border(1.dp, Color(0xFF292524), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 14.sp)
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = total,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun CategoryWorkspacePage(
    categoryName: String,
    dailyExpenses: List<DailyExpenseEntity>,
    creditExpenses: List<CreditExpenseEntity>,
    emiLoans: List<EmiLoanEntity>,
    debtSplits: List<DebtSplitEntity>,
    incomePaydays: List<IncomePaydayEntity>,
    sipRecords: List<SipEntity>,
    investmentRecords: List<InvestmentEntity>,
    currencyFormatter: NumberFormat,
    viewModel: WealthPulseViewModel,
    onBack: () -> Unit
) {
    val emoji = when (categoryName) {
        "Food & Dining" -> "🍔"
        "Transport" -> "🚗"
        "Shopping" -> "🛍️"
        "Rent & Utilities" -> "💡"
        "Debts & Splits" -> "👥"
        "SIP Mutual Funds" -> "📈"
        "Equity Portfolio" -> "💼"
        else -> "💰"
    }

    val themeColor = when (categoryName) {
        "Food & Dining" -> NeonGreen
        "Transport" -> Color(0xFF3B82F6)
        "Shopping" -> Color(0xFFA855F7)
        "Rent & Utilities" -> AccentOrange
        "Debts & Splits" -> Color(0xFFF43F5E)
        "SIP Mutual Funds" -> Color(0xFF10B981)
        "Equity Portfolio" -> Color(0xFFEAB308)
        else -> NeonGreen
    }

    var localAmount by remember { mutableStateOf("") }
    var localDesc by remember { mutableStateOf("") }
    var localPayMode by remember { mutableStateOf("UPI") }
    var localCardName by remember { mutableStateOf("HDFC Millennia") }
    var localTenure by remember { mutableStateOf("12") }
    var localPerson by remember { mutableStateOf("Amit") }
    var localGroup by remember { mutableStateOf("Office Lunch") }
    var localSipDay by remember { mutableStateOf("5") }
    var localCurVal by remember { mutableStateOf("") }

    val context = LocalContext.current

    val matchingExpenses = dailyExpenses.filter {
        when (categoryName) {
            "Food & Dining" -> it.category.lowercase().contains("food") || it.category.lowercase().contains("dining")
            "Transport" -> it.category.lowercase().contains("transport") || it.category.lowercase().contains("ride") || it.category.lowercase().contains("car") || it.category.lowercase().contains("transit")
            "Shopping" -> it.category.lowercase().contains("shopping") || it.category.lowercase().contains("clothing") || it.category.lowercase().contains("electronics") || it.category.lowercase().contains("clothes")
            "Rent & Utilities" -> it.category.lowercase().contains("utility") || it.category.lowercase().contains("utilities") || it.category.lowercase().contains("rent") || it.category.lowercase().contains("bill")
            else -> false
        }
    }

    val matchingCredits = creditExpenses.filter {
        when (categoryName) {
            "Food & Dining" -> it.category.lowercase().contains("food") || it.category.lowercase().contains("dining")
            "Transport" -> it.category.lowercase().contains("transport") || it.category.lowercase().contains("fuel")
            "Shopping" -> it.category.lowercase().contains("shopping") || it.category.lowercase().contains("clothing") || it.category.lowercase().contains("electronics") || it.category.lowercase().contains("clothes")
            "Rent & Utilities" -> it.category.lowercase().contains("utility") || it.category.lowercase().contains("utilities") || it.category.lowercase().contains("bill")
            else -> false
        }
    }

    val matchingEmis = if (categoryName == "Rent & Utilities") emiLoans else emptyList()
    val matchingDebts = if (categoryName == "Debts & Splits") debtSplits else emptyList()
    val matchingSips = if (categoryName == "SIP Mutual Funds") sipRecords else emptyList()
    val matchingInvestments = if (categoryName == "Equity Portfolio") investmentRecords else emptyList()
    val matchingIncomes = if (categoryName == "Total Inflow") incomePaydays else emptyList()

    val totalSum = when (categoryName) {
        "Food & Dining" -> matchingExpenses.sumOf { it.amount } + matchingCredits.sumOf { it.amount }
        "Transport" -> matchingExpenses.sumOf { it.amount } + matchingCredits.sumOf { it.amount }
        "Shopping" -> matchingExpenses.sumOf { it.amount } + matchingCredits.sumOf { it.amount }
        "Rent & Utilities" -> matchingExpenses.sumOf { it.amount } + matchingCredits.sumOf { it.amount } + matchingEmis.sumOf { it.amount }
        "Debts & Splits" -> matchingDebts.sumOf { it.amount }
        "SIP Mutual Funds" -> matchingSips.sumOf { it.amount }
        "Equity Portfolio" -> matchingInvestments.sumOf { it.currentValue }
        else -> matchingIncomes.sumOf { it.amount }
    }

    val totalCount = matchingExpenses.size + matchingCredits.size + matchingEmis.size + matchingDebts.size + matchingSips.size + matchingInvestments.size + matchingIncomes.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                colors = ButtonDefaults.textButtonColors(contentColor = TextGray),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Back to Dashboard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(themeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = categoryName.uppercase(),
                    fontSize = 11.sp,
                    color = themeColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Dedicated Category Page",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    color = TextWhite,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF292524)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (categoryName == "Total Inflow") "TOTAL INCOME RECEIVED" else "TOTAL MONITORED VALUE",
                        fontSize = 10.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormatter.format(totalSum),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Light,
                        color = themeColor,
                        fontFamily = FontFamily.Serif
                    )
                }
                Box(
                    modifier = Modifier
                        .background(themeColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$totalCount Records",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF292524)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚡ QUICK DIRECT SECURE LOGGER",
                    fontSize = 11.sp,
                    color = themeColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = localAmount,
                    onValueChange = { localAmount = it },
                    label = { Text("Amount (₹)", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(100, 500, 1000).forEach { inc ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF292524))
                                .clickable {
                                    val current = localAmount.toDoubleOrNull() ?: 0.0
                                    localAmount = (current + inc).toInt().toString()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("+₹$inc", fontSize = 10.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF442D2D))
                            .clickable { localAmount = "" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Clear", fontSize = 10.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = localDesc,
                    onValueChange = { localDesc = it },
                    label = { Text("Description", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor)
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (categoryName) {
                    "Food & Dining", "Transport" -> {
                        ManualOptionSelector(
                            label = "Select payment medium:",
                            options = listOf(
                                "📱 UPI" to "UPI",
                                "💵 Cash" to "Cash",
                                "💳 Credit Card" to "Credit Card",
                                "💳 Debit Card" to "Debit Card"
                            ),
                            selected = localPayMode,
                            onSelect = { localPayMode = it },
                            colorAccent = themeColor
                        )
                    }
                    "Shopping" -> {
                        OutlinedTextField(
                            value = localCardName,
                            onValueChange = { localCardName = it },
                            label = { Text("Swiped Card Name", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        ManualOptionSelector(
                            label = "Or Speed Select Card:",
                            options = listOf(
                                "💳 HDFC Millennia" to "HDFC Millennia",
                                "💳 ICICI Amazon" to "ICICI Amazon",
                                "💳 SBI Card" to "SBI Card"
                            ),
                            selected = localCardName,
                            onSelect = { localCardName = it },
                            colorAccent = themeColor
                        )
                    }
                    "Rent & Utilities" -> {
                        ManualOptionSelector(
                            label = "Select category type:",
                            options = listOf(
                                "💡 Utilities" to "Utilities",
                                "🏠 Rent" to "Rent",
                                "🚗 Loan EMI" to "EMI"
                            ),
                            selected = localPayMode,
                            onSelect = { localPayMode = it },
                            colorAccent = themeColor
                        )
                    }
                    "Debts & Splits" -> {
                        OutlinedTextField(
                            value = localPerson,
                            onValueChange = { localPerson = it },
                            label = { Text("Person Involved", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = localGroup,
                            onValueChange = { localGroup = it },
                            label = { Text("Group Session Name", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor)
                        )
                    }
                    "SIP Mutual Funds" -> {
                        ManualOptionSelector(
                            label = "Select SIP Day of Month:",
                            options = listOf(
                                "1st" to "1",
                                "5th" to "5",
                                "10th" to "10",
                                "15th" to "15",
                                "25th" to "25"
                            ),
                            selected = localSipDay,
                            onSelect = { localSipDay = it },
                            colorAccent = themeColor
                        )
                    }
                    "Equity Portfolio" -> {
                        OutlinedTextField(
                            value = localCurVal,
                            onValueChange = { localCurVal = it },
                            label = { Text("Current Valuation Estimator (₹)", color = TextGray) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = themeColor)
                        )
                    }
                    "Total Inflow" -> {
                        ManualOptionSelector(
                            label = "Income Inflow Frequency:",
                            options = listOf(
                                "💰 Monthly Salary" to "Monthly",
                                "💼 Freelance Fee" to "Freelance",
                                "🎁 One-off Gift" to "One-off"
                            ),
                            selected = localPayMode,
                            onSelect = { localPayMode = it },
                            colorAccent = themeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val amt = localAmount.toDoubleOrNull()
                        if (amt == null || amt <= 0.0) {
                            Toast.makeText(context, "Please enter a valid numeric transaction amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val desc = if (localDesc.isBlank()) "Quick Direct $categoryName Log" else localDesc

                        when (categoryName) {
                            "Food & Dining" -> viewModel.addManualExpense(amt, desc, "Food & Dining", localPayMode)
                            "Transport" -> viewModel.addManualExpense(amt, desc, "Transport", localPayMode)
                            "Shopping" -> viewModel.addManualCredit(amt, desc, localCardName, "Shopping")
                            "Rent & Utilities" -> {
                                if (localPayMode == "EMI") {
                                    viewModel.addManualEmi(amt, desc, 12, 12)
                                } else {
                                    viewModel.addManualExpense(amt, desc, localPayMode, "UPI")
                                }
                            }
                            "Debts & Splits" -> viewModel.addManualDebt(amt, desc, localPerson, true, localGroup)
                            "SIP Mutual Funds" -> viewModel.addManualSip(amt, desc, "Mutual Funds", localSipDay.toIntOrNull() ?: 5)
                            "Equity Portfolio" -> viewModel.addManualInvestment(amt, desc, "Equity", localCurVal.toDoubleOrNull() ?: amt)
                            "Total Inflow" -> viewModel.addManualIncome(amt, desc, localPayMode)
                        }

                        Toast.makeText(context, "Successfully saved record instantly to database!", Toast.LENGTH_SHORT).show()
                        localAmount = ""
                        localDesc = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Commit New Record to $categoryName",
                        color = NavyBg,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Text(
            text = "📋 HISTORICAL $categoryName DIRECTORY",
            fontSize = 11.sp,
            color = TextGray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        if (totalCount == 0) {
            EmptyStatePlaceholder("No matching transactions registered yet. Click pre-defined records, try AI phrasing, or log directly above!")
        } else {
            Column {
                if (matchingExpenses.isNotEmpty()) {
                    matchingExpenses.forEach { exp ->
                        TransactionItemCard(
                            title = exp.description,
                            subtitle = "[Expense Table] ${exp.category} ${if (exp.paymentMode.isNotEmpty()) "• " + exp.paymentMode else ""}",
                            amount = currencyFormatter.format(exp.amount),
                            onDelete = { viewModel.deleteExpense(exp.id) },
                            colorAccent = TextWhite
                        )
                    }
                }
                if (matchingCredits.isNotEmpty()) {
                    matchingCredits.forEach { cred ->
                        TransactionItemCard(
                            title = cred.description,
                            subtitle = "[Credits Table] card: ${cred.cardName} • ${cred.category}",
                            amount = currencyFormatter.format(cred.amount),
                            onDelete = { viewModel.deleteCredit(cred.id) },
                            colorAccent = Color(0xFFD97706)
                        )
                    }
                }
                if (matchingEmis.isNotEmpty()) {
                    matchingEmis.forEach { emi ->
                        TransactionItemCard(
                            title = emi.description,
                            subtitle = "[EMIs Table] ${emi.category} • months left: ${emi.remainingMonths}/${emi.totalTenureMonths}",
                            amount = currencyFormatter.format(emi.amount),
                            onDelete = { viewModel.deleteEmi(emi.id) },
                            colorAccent = AccentOrange
                        )
                    }
                }
                if (matchingDebts.isNotEmpty()) {
                     matchingDebts.forEach { debt ->
                        TransactionItemCard(
                            title = debt.description,
                            subtitle = "[Debt Split] person: ${debt.debtPersonInvolved} • group: ${debt.groupName}",
                            amount = currencyFormatter.format(debt.amount),
                            onDelete = { viewModel.deleteDebt(debt.id) },
                            colorAccent = Color(0xFFF43F5E)
                        )
                     }
                }
                if (matchingSips.isNotEmpty()) {
                    matchingSips.forEach { sip ->
                        TransactionItemCard(
                            title = sip.description,
                            subtitle = "[SIP Table] ${sip.investmentCategory} • auto-pay date: ${sip.dayOfMonth}th",
                            amount = currencyFormatter.format(sip.amount),
                            onDelete = { viewModel.deleteSip(sip.id) },
                            colorAccent = Color(0xFF10B981)
                        )
                    }
                }
                if (matchingInvestments.isNotEmpty()) {
                    matchingInvestments.forEach { inv ->
                        TransactionItemCard(
                            title = inv.description,
                            subtitle = "[Portfolio Table] category: ${inv.category} | current: ${currencyFormatter.format(inv.currentValue)}",
                            amount = currencyFormatter.format(inv.amount),
                            onDelete = { viewModel.deleteInvestment(inv.id) },
                            colorAccent = Color(0xFFEAB308)
                        )
                    }
                }
                if (matchingIncomes.isNotEmpty()) {
                    matchingIncomes.forEach { inc ->
                        TransactionItemCard(
                            title = inc.description,
                            subtitle = "[Inflow Table] ${inc.category} • ${inc.incomeFrequency}",
                            amount = currencyFormatter.format(inc.amount),
                            onDelete = { viewModel.deleteIncome(inc.id) },
                            colorAccent = NeonGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommitmentsWorkspacePage(
    emiLoans: List<EmiLoanEntity>,
    sipRecords: List<SipEntity>,
    debtSplits: List<DebtSplitEntity>,
    totalEmi: Double,
    totalSip: Double,
    totalDebt: Double,
    currencyFormatter: NumberFormat,
    viewModel: WealthPulseViewModel,
    onTriggerManual: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Core Header
        Text(
            text = "RECURRING SYSTEM COMMITMENTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF78716C), // Stone 500
            letterSpacing = 2.sp
        )
        Text(
            text = "Structured Liabilities & Assets",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            color = TextWhite,
            fontWeight = FontWeight.Light,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Aggregated Dashboard Summary
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF292524)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "RECURRING OUTFLOW RUNRATE /mo",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = currencyFormatter.format(totalEmi + totalSip),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = AccentOrange,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("EMI RECURRING", fontSize = 10.sp, color = TextGray)
                        Text(currencyFormatter.format(totalEmi), fontSize = 14.sp, color = AccentOrange, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("SIP SCHEDULING", fontSize = 10.sp, color = TextGray)
                        Text(currencyFormatter.format(totalSip), fontSize = 14.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("ACTIVE LEDGER", fontSize = 10.sp, color = TextGray)
                        Text(currencyFormatter.format(totalDebt), fontSize = 14.sp, color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 1: Active Loan EMIs
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 ACTIVE EMIs & LOANS LIABILITIES",
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            TextButton(
                onClick = { onTriggerManual("EMI") },
                modifier = Modifier.testTag("emi_tab_manual_button")
            ) {
                Text("+ New EMI", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
            }
        }

        if (emiLoans.isEmpty()) {
            EmptyStatePlaceholder("No live structural liabilities or EMI plans registered.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                emiLoans.forEach { emi ->
                    TransactionItemCard(
                        title = emi.description,
                        subtitle = "Remaining: ${emi.remainingMonths}/${emi.totalTenureMonths} months left",
                        amount = "${currencyFormatter.format(emi.amount)}/mo",
                        onDelete = { viewModel.deleteEmi(emi.id) },
                        colorAccent = AccentOrange,
                        progressFraction = emi.remainingMonths.toFloat() / emi.totalTenureMonths.toFloat()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: SIP Mutual Funds
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📈 SIP MUTUAL FUNDS CALENDAR",
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            TextButton(
                onClick = { onTriggerManual("SIP") },
                modifier = Modifier.testTag("sip_tab_manual_button")
            ) {
                Text("+ New SIP", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
            }
        }

        if (sipRecords.isEmpty()) {
            EmptyStatePlaceholder("No active automated SIP portfolio schedules configured.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sipRecords.forEach { sip ->
                    TransactionItemCard(
                        title = sip.description,
                        subtitle = "Auto-pay day: ${sip.dayOfMonth}th monthly | Type: ${sip.investmentCategory}",
                        amount = "${currencyFormatter.format(sip.amount)}/mo",
                        onDelete = { viewModel.deleteSip(sip.id) },
                        colorAccent = Color(0xFF3B82F6)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Peer Lending Debt Splits
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "👥 PEER SPLITS & LENT BALANCES (LENT/BORROW)",
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            TextButton(
                onClick = { onTriggerManual("DEBT") },
                modifier = Modifier.testTag("split_tab_manual_button")
            ) {
                Text("+ Record Split", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
            }
        }

        if (debtSplits.isEmpty()) {
            EmptyStatePlaceholder("No lend/borrow peer ratios or Flatmates balances active.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                debtSplits.forEach { debt ->
                    TransactionItemCard(
                        title = debt.description,
                        subtitle = "Subject: ${debt.debtPersonInvolved} | Group: ${debt.groupName} | Type: ${if (debt.isGroupSplit) "Flat Group" else "Personal Peer"}",
                        amount = currencyFormatter.format(debt.amount),
                        onDelete = { viewModel.deleteDebt(debt.id) },
                        colorAccent = Color(0xFFF43F5E)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun UserProfileWorkspacePage(
    currencyFormatter: NumberFormat,
    totalIncome: Double,
    totalExpense: Double,
    totalCredit: Double,
    totalSip: Double,
    trueDisposable: Double,
    catExps: Double
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Core Header
        Text(
            text = "Marcus Aurelius",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            color = TextWhite,
            fontWeight = FontWeight.Light,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
        )
        Text(
            text = "👤 INTEGRATED FINANCIAL IDENTITY",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF78716C), // Stone 500
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Profile Card
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF292524)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(NeonGreen.copy(alpha = 0.15f))
                        .border(1.dp, NeonGreen.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("MA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Marcus Aurelius", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("Analyst Level: Pro Wealth Planner", fontSize = 11.sp, color = TextGray)
                    Text("ID: WP-99-MARCUS", fontSize = 10.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section: Live Targets & Goals
        Text(
            text = "🏁 STRATEGIC FINANCIAL GOALS",
            fontSize = 11.sp,
            color = TextGray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF292524)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Goal 1: Savings Goal
                val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense - totalCredit) / totalIncome) * 100 else 0.0
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Savings Ratio", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        Text("${String.format("%.1f", savingsRate)}% / 40.0%", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (savingsRate.toFloat() / 40f).coerceIn(0f, 1f) },
                        color = NeonGreen,
                        trackColor = Color(0xFF292524),
                        modifier = Modifier.fillMaxWidth().clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Goal 2: Food limits alert
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Monthly Dining Budget", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        Text("${currencyFormatter.format(catExps)} / ₹15,000", fontSize = 12.sp, color = AccentOrange, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (catExps.toFloat() / 15000f).coerceIn(0f, 1f) },
                        color = AccentOrange,
                        trackColor = Color(0xFF292524),
                        modifier = Modifier.fillMaxWidth().clip(CircleShape)
                    )
                }
            }
        }

        // Section: Risk appetite and smart metrics
        Text(
            text = "⚡ ADVISORY METRICS",
            fontSize = 11.sp,
            color = TextGray,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161616))
                    .border(1.dp, Color(0xFF292524), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("RISK INDEX", fontSize = 10.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("GROWTH-MOD", fontSize = 14.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                    Text("Moderate equity allocation", fontSize = 10.sp, color = TextGray)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161616))
                    .border(1.dp, Color(0xFF292524), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text("LIQUIDITY RUNWAY", fontSize = 10.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("STABLE STATS", fontSize = 14.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                    Text("Disposable run rate healthy", fontSize = 10.sp, color = TextGray)
                }
            }
        }

        // Interactive profile configuration info section
        Card(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF292524)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🏆 LEADERBOARD ACHIEVEMENTS",
                    fontSize = 11.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                BulletTextItem("🌟 Wealth Pillar: Saving streak successfully sustained for 6 weeks.")
                BulletTextItem("🛡️ Debt Sentinel: Active monthly utility autopay and no unpaid credit arrears.")
                BulletTextItem("💡 AI Co-pilot active: AI Voice Analyzer logs compiled over 3 sessions.")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SettingsWorkspacePage(
    viewModel: WealthPulseViewModel
) {
    var currencyMode by remember { mutableStateOf("INR") }
    var overspendAlerts by remember { mutableStateOf(true) }
    var highPrecisionParser by remember { mutableStateOf(true) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Core Header
        Text(
            text = "SYSTEM CONFIGURATOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF78716C), // Stone 500
            letterSpacing = 2.sp
        )
        Text(
            text = "Settings, Database & Tools",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            color = TextWhite,
            fontWeight = FontWeight.Light,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Preferences Card
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF292524)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "APP USER PREFERENCES",
                    fontSize = 11.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Option 1: Currency Preference Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Regional Base Currency", fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        Text("Active formatting in Indian Rupee (₹)", fontSize = 11.sp, color = TextGray)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = { currencyMode = "INR"; Toast.makeText(context, "Base Currency: INR (₹)", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (currencyMode == "INR") NeonGreen else Color(0xFF292524)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("currency_inr_button")
                        ) {
                            Text("INR (₹)", fontSize = 11.sp, color = if (currencyMode == "INR") NavyBg else TextWhite, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { currencyMode = "USD"; Toast.makeText(context, "Base Currency: USD ($)", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (currencyMode == "USD") NeonGreen else Color(0xFF292524)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("currency_usd_button")
                        ) {
                            Text("USD ($)", fontSize = 11.sp, color = if (currencyMode == "USD") NavyBg else TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF292524))
                Spacer(modifier = Modifier.height(16.dp))

                // Option 2: Overspend alerts Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart Overspend Guard", fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        Text("Flag transaction limits with red indicators", fontSize = 11.sp, color = TextGray)
                    }
                    Switch(
                        checked = overspendAlerts,
                        onCheckedChange = { overspendAlerts = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen),
                        modifier = Modifier.testTag("overspend_switch")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFF292524))
                Spacer(modifier = Modifier.height(16.dp))

                // Option 3: Precision Toggle Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AI Smart Extraction Strictness", fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        Text("Apply rigorous multi-field context evaluation", fontSize = 11.sp, color = TextGray)
                    }
                    Switch(
                        checked = highPrecisionParser,
                        onCheckedChange = { highPrecisionParser = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen),
                        modifier = Modifier.testTag("precision_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Technical System Diagnostics
        Card(
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFF292524)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161616)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🛠️ SYSTEM DIAGNOSTICS",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("SQLite Engines: Active (Room Persistence Library)", fontSize = 12.sp, color = TextWhite)
                Text("AI Parser: Google Gemini-Flash Web Orchestration (Model 3.5)", fontSize = 12.sp, color = TextWhite)
                Text("Telemetry Client: Operational on DeX / Foldable Adaptive layout", fontSize = 12.sp, color = TextWhite)
                Text("Application Version: WealthPulse Core v2.4.2", fontSize = 12.sp, color = NeonGreen)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BulletTextItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp, end = 8.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(NeonGreen)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextWhite
        )
    }
}

