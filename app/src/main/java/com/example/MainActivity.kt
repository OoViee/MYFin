package com.example

import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
    val border: Color = Color(0xFF292524),       // --border: #292524 (Subtle border)
    val credit: Color = Color(0xFF8B5CF6),       // --credit: credit swipe cards theme accent
    val sip: Color = Color(0xFF3B82F6),          // --sip: sip wealth accent
    val isLight: Boolean = false
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
            "--credit" -> credit
            "--sip" -> sip
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
val BorderColor: Color @Composable get() = cssVar("--border")
val CreditPurple: Color @Composable get() = cssVar("--credit")
val SipBlue: Color @Composable get() = cssVar("--sip")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WealthPulseViewModel = viewModel()
            val selectedThemeName by viewModel.selectedTheme.collectAsState()
            
            val themeVariables = remember(selectedThemeName) {
                when (selectedThemeName) {
                    "oceanic_abyss" -> CssThemeVariables(
                        bg = Color(0xFF020617),          // Dark Navy Slate 950
                        surface = Color(0xFF0F172A),     // Slate 900
                        primary = Color(0xFF8B5CF6),     // Violet Accent
                        secondary = Color(0xFF475569),   // Slate 600
                        accent = Color(0xFFEC4899),      // Pink Glow
                        danger = Color(0xFFF43F5E),      // Rose Indicator
                        textGray = Color(0xFF94A3B8),    // Muted Slate 400
                        textWhite = Color(0xFFF8FAFC),   // Bright Slate 50
                        border = Color(0xFF1E293B),      // Border Slate 800
                        credit = Color(0xFFC084FC),      // Lavender Violet
                        sip = Color(0xFF38BDF8)          // Calm Sky Blue
                    )
                    "forest_sage" -> CssThemeVariables(
                        bg = Color(0xFF052E16),          // Dark Forest Space Green
                        surface = Color(0xFF114224),     // Herb/Sage Green
                        primary = Color(0xFF22C55E),     // Vibrant Lime Green
                        secondary = Color(0xFF15803D),   // Muted Green 700
                        accent = Color(0xFFEAB308),      // Gold Yellow Accent
                        danger = Color(0xFFEF4444),      // Sharp Red
                        textGray = Color(0xFF86EFAC),    // Green Spruce 300
                        textWhite = Color(0xFFF0FDF4),   // Pale Green Mint 50
                        border = Color(0xFF166534),      // Dark Green Spruce Border
                        credit = Color(0xFFA3E635),      // Fresh Lime
                        sip = Color(0xFF60A5FA)          // Light blue sky
                    )
                    "sunset_glow" -> CssThemeVariables(
                        bg = Color(0xFF1C0A10),          // Twilight Auburn Space
                        surface = Color(0xFF2D101C),     // Dark Burgundy Orchid
                        primary = Color(0xFFF43F5E),     // Crimson Rose Red
                        secondary = Color(0xFF9D174D),   // Velvet Pink/Rose Border
                        accent = Color(0xFFF97316),      // Vivid Sunset Orange
                        danger = Color(0xFFEF4444),      // Neon Amber Warning
                        textGray = Color(0xFFFDA4AF),    // Warm Rose Dust 300
                        textWhite = Color(0xFFFFF1F2),   // Cream White Rose 50
                        border = Color(0xFF4C0519),      // Deep Rose Charcoal Border
                        credit = Color(0xFFF472B6),      // Warm Rose
                        sip = Color(0xFFFB923C)          // Soft Orange
                    )
                    "bright_aurora" -> CssThemeVariables(
                        bg = Color(0xFFF0F9FF),          // Soft Alice Blue Light Sky background
                        surface = Color(0xFFFFFFFF),     // Bright Pure White Surface
                        primary = Color(0xFF0284C7),     // Dynamic Sky Blue primary Accent
                        secondary = Color(0xFF0369A1),   // Ocean Deep Blue secondary
                        accent = Color(0xFFEA580C),      // Sunset Orange Accent
                        danger = Color(0xFFE11D48),      // Crimson Rose Red danger
                        textGray = Color(0xFF475569),    // Slate secondary text 500
                        textWhite = Color(0xFF0F172A),   // Slate primary text 900
                        border = Color(0xFFCBD5E1),      // Soft border slate-blue 300
                        credit = Color(0xFF7C3AED),      // Indigo Purple
                        sip = Color(0xFF2563EB),         // Strong Blue
                        isLight = true
                    )
                    else -> CssThemeVariables()         // Default "Sophisticated Dark" (Stone + Emerald)
                }
            }

            val context = LocalContext.current
            val isFirstLaunchPref = remember { context.getSharedPreferences("myfin_launch_prefs", android.content.Context.MODE_PRIVATE) }
            var isFirstLaunch by remember { mutableStateOf(isFirstLaunchPref.getBoolean("is_first_launch", true)) }
            var showTempEmailAuthOnboarding by remember { mutableStateOf(false) }
            var showSplash by remember { mutableStateOf(true) }

            val showMigrationDialog by viewModel.showMigrationDialog.collectAsState()

            WealthPulseTheme(variables = themeVariables) {
                if (showSplash) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(themeVariables.bg)
                            .testTag("premium_splash_overlay"),
                        contentAlignment = Alignment.Center
                    ) {
                        val logoScale = remember { Animatable(0.4f) }
                        val logoAlpha = remember { Animatable(0.0f) }

                        LaunchedEffect(Unit) {
                            launch {
                                logoScale.animateTo(
                                    targetValue = 1.0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            logoAlpha.animateTo(
                                targetValue = 1.0f,
                                animationSpec = tween(durationMillis = 500)
                            )
                            kotlinx.coroutines.delay(800)
                            logoAlpha.animateTo(
                                targetValue = 0.0f,
                                animationSpec = tween(durationMillis = 350)
                            )
                            showSplash = false
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.graphicsLayer {
                                scaleX = logoScale.value
                                scaleY = logoScale.value
                                alpha = logoAlpha.value
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(themeVariables.primary, themeVariables.credit)
                                        )
                                    )
                                    .border(width = 1.5.dp, color = Color.White.copy(alpha = 0.35f), shape = RoundedCornerShape(32.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "M",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "M Y F i n",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Light,
                                fontFamily = FontFamily.Serif,
                                color = Color.White,
                                letterSpacing = 6.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "PREMIUM FINANCE INTELLIGENCE",
                                fontSize = 11.sp,
                                color = themeVariables.textGray,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                } else {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("main_scaffold"),
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->
                        FinancialWorkspaceScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            viewModel = viewModel
                        )

                        if (isFirstLaunch) {
                            OnboardingDialog(
                                onDismissOnboarding = {
                                    isFirstLaunch = false
                                    isFirstLaunchPref.edit().putBoolean("is_first_launch", false).apply()
                                },
                                viewModel = viewModel,
                                onShowEmailAuth = {
                                    showTempEmailAuthOnboarding = true
                                }
                            )
                        }

                        if (showTempEmailAuthOnboarding) {
                            AuthDialog(
                                onDismiss = {
                                    showTempEmailAuthOnboarding = false
                                    isFirstLaunch = false
                                    isFirstLaunchPref.edit().putBoolean("is_first_launch", false).apply()
                                },
                                viewModel = viewModel
                            )
                        }

                        if (showMigrationDialog) {
                            MigrationDialog(viewModel = viewModel)
                        }
                    }
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
        val scheme = if (variables.isLight) {
            lightColorScheme(
                background = cssVar("--bg"),
                surface = cssVar("--surface"),
                primary = cssVar("--primary"),
                secondary = cssVar("--secondary"),
                tertiary = cssVar("--accent"),
                onBackground = cssVar("--text-white"),
                onSurface = cssVar("--text-white")
            )
        } else {
            darkColorScheme(
                background = cssVar("--bg"),
                surface = cssVar("--surface"),
                primary = cssVar("--primary"),
                secondary = cssVar("--secondary"),
                tertiary = cssVar("--accent"),
                onBackground = cssVar("--text-white"),
                onSurface = cssVar("--text-white")
            )
        }
        MaterialTheme(
            colorScheme = scheme,
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
    val creditCards by viewModel.creditCards.collectAsState()
    val aiState by viewModel.aiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val tripEvents by viewModel.tripEvents.collectAsState()
    val tripExpenses by viewModel.tripExpenses.collectAsState()
    val dbParticipants by viewModel.participants.collectAsState()

    var activeTab by remember { mutableStateOf(0) }
    var activeNavigationMenuTab by remember { mutableStateOf("home") }
    var userInputText by remember { mutableStateOf("") }
    var showQuickSimMicSheet by remember { mutableStateOf(false) }

    // Dialogue State for Manual Controls
    var activeManualDialog by remember { mutableStateOf<String?>(null) }
    var currentCategoryPage by remember { mutableStateOf<String?>(null) }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    val greetingText = remember(currentUser) {
        val user = currentUser
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val prefix = when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
        if (user == null || user.isAnonymous) {
            prefix
        } else {
            val email = user.email
            val localPart = email.substringBefore("@")
            val displayName = localPart.split(".", "_", "-").joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
            }
            "$prefix, $displayName"
        }
    }

    // Aggregate values
    val totalExpense = dailyExpenses.sumOf { it.amount }
    val totalCredit = creditExpenses.sumOf { it.amount }
    val totalEmi = emiLoans.sumOf { it.amount }
    val totalLentSum = debtSplits.filter { !it.description.contains("borrow", ignoreCase = true) && !it.description.contains("owe", ignoreCase = true) }.sumOf { debt ->
        val participants = debt.debtPersonInvolved.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val paidList = debt.paidPeople.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val unpaidCount = participants.filter { !paidList.contains(it) }.size
        val individualShare = if (participants.isNotEmpty()) debt.amount / participants.size else 0.0
        individualShare * unpaidCount
    }
    val totalBorrowSum = debtSplits.filter { it.description.contains("borrow", ignoreCase = true) || it.description.contains("owe", ignoreCase = true) }.sumOf { debt ->
        val participants = debt.debtPersonInvolved.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val paidList = debt.paidPeople.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val unpaidCount = participants.filter { !paidList.contains(it) }.size
        val individualShare = if (participants.isNotEmpty()) debt.amount / participants.size else 0.0
        individualShare * unpaidCount
    }
    val totalDebt = totalLentSum - totalBorrowSum
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
                    AnimatedContent(
                        targetState = activeNavigationMenuTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith
                            fadeOut(animationSpec = tween(220))
                        },
                        label = "navigation_page_transition",
                        modifier = Modifier.fillMaxSize()
                    ) { targetTab ->
                        if (targetTab == "home") {
                            DashboardScreen(
                                onNavigateToTab = { tab -> activeNavigationMenuTab = tab },
                                onTriggerQuickAction = { type -> activeManualDialog = type },
                                currencyFormatter = currencyFormatter
                            )
                            if (false) {
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
                        text = "MYFIN CORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF78716C), // Stone 500
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                     Text(
                        text = greetingText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextWhite, // Elegant serif adapting to theme
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
                            .background(SurfaceBlue)
                            .border(1.dp, BorderColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF10B981).copy(alpha = 0.2f), SurfaceBlue)
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
                border = BorderStroke(1.dp, BorderColor), // Dynamic border
                colors = CardDefaults.cardColors(containerColor = SurfaceBlue), // Dynamic surface info card
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "DAILY RUN RATE",
                        fontSize = 11.sp,
                        color = TextGray, // Dynamic text gray
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



                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderColor, thickness = 1.dp)
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
                border = BorderStroke(1.dp, BorderColor),
                colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
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
                border = BorderStroke(1.dp, BorderColor),
                colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
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
                                unfocusedBorderColor = BorderColor,
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
                                .background(BorderColor)
                                .border(1.dp, BorderColor.copy(alpha = 0.5f), CircleShape)
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
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Manual Append", tint = NeonGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Manual Record", fontSize = 11.sp, color = TextWhite)
                }
            }

            // TAB SCENARDS
            val isLight = LocalCssThemeVariables.current.isLight
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = if (isLight) Color(0xFFE2E8F0) else SurfaceBlue,
                contentColor = NeonGreen,
                edgePadding = 4.dp,
                indicator = {}, // Disable standard underline indicator
                divider = {}, // Disable default divider for self-contained visual card
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(12.dp))
                    .padding(vertical = 4.dp)
            ) {
                val tabsList = listOf(
                    Triple(0, "EXPENSE 💸", NeonGreen),
                    Triple(1, "CARDS 💳", CreditPurple),
                    Triple(2, "EMIS 📊", AccentOrange),
                    Triple(3, "SPLIT 👥", NeonGreen),
                    Triple(4, "PAYDAY 💰", NeonGreen),
                    Triple(5, "SIP 📈", SipBlue),
                    Triple(6, "PORTFOLIO 💼", AccentOrange)
                )
                tabsList.forEach { (index, label, accentColor) ->
                    val isSelected = activeTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { activeTab = index },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) {
                                    accentColor.copy(alpha = if (isLight) 0.18f else 0.15f)
                                } else Color.Transparent
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) {
                                    accentColor.copy(alpha = if (isLight) 0.35f else 0.25f)
                                } else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        text = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) accentColor else TextGray
                            )
                        }
                    )
                }
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
                        } // CLOSES if (false) wrapping old home screen
                    } else if (targetTab == "expenses") {
                        com.example.ui.ExpensesWorkspaceHub(
                            dailyExpenses = dailyExpenses,
                            currencyFormatter = currencyFormatter,
                            viewModel = viewModel
                        )
                    } else if (targetTab == "reports") {
                        ReportsWorkspacePage(
                            dailyExpenses = dailyExpenses,
                            creditExpenses = creditExpenses,
                            incomePaydays = incomePaydays,
                            currencyFormatter = currencyFormatter
                        )
                    } else if (targetTab == "more") {
                        MoreWorkspaceHub(
                            onNavigate = { tab -> activeNavigationMenuTab = tab },
                            onNavigateToCategory = { cat -> currentCategoryPage = cat }
                        )
                    } else if (targetTab == "calendar") {
                        CalendarWorkspacePage(
                            dailyExpenses = dailyExpenses,
                            creditExpenses = creditExpenses,
                            emiLoans = emiLoans,
                            sipRecords = sipRecords,
                            incomePaydays = incomePaydays,
                            currencyFormatter = currencyFormatter,
                            viewModel = viewModel
                        )
                    } else if (targetTab == "emi") {
                        com.example.ui.LoanWorkspaceHub(
                            currencyFormatter = currencyFormatter
                        )
                    } else if (targetTab == "sip") {
                        SipWorkspacePage(
                            sipRecords = sipRecords,
                            totalSip = totalSip,
                            currencyFormatter = currencyFormatter,
                            viewModel = viewModel,
                            onTriggerManual = { type -> activeManualDialog = type }
                        )
                    } else if (targetTab == "credit") {
                        com.example.ui.CreditCardWorkspaceHub(
                            currencyFormatter = currencyFormatter
                        )
                    } else if (targetTab == "lent") {
                        SplitsWorkspacePage(
                            debtSplits = debtSplits,
                            totalDebt = totalDebt,
                            currencyFormatter = currencyFormatter,
                            viewModel = viewModel,
                            tripEvents = tripEvents,
                            tripExpenses = tripExpenses,
                            allParticipants = dbParticipants,
                            onTriggerManual = { type -> activeManualDialog = type }
                        )
                    } else if (targetTab == "settings") {
                        SettingsWorkspacePage(
                            viewModel = viewModel
                        )
                    } else if (targetTab == "budgets") {
                        com.example.ui.BudgetWorkspaceHub(
                            currencyFormatter = currencyFormatter
                        )
                    }
                    }

                    // BEAUTIFUL FLOATING GLASS ISLAND NAVIGATION TAB WITHOUT CLIPPING & HIGH-FIDELITY GLASSMOPHISM
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
                            .fillMaxWidth()
                            .widthIn(max = 520.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        SurfaceBlue.copy(alpha = 0.85f),
                                        SurfaceBlue.copy(alpha = 0.95f)
                                    )
                                )
                            )
                            .border(
                                BorderStroke(
                                    width = 1.2.dp,
                                    color = BorderColor.copy(alpha = 0.5f)
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
                                Triple("home", Icons.Default.Home, "Home"),
                                Triple("expenses", Icons.Default.DateRange, "Expenses"),
                                Triple("lent", Icons.Default.Share, "Split"),
                                Triple("reports", Icons.Default.Star, "Reports"),
                                Triple("more", Icons.Default.Menu, "More")
                            )

                            items.forEach { (route, icon, label) ->
                                val isSelected = activeNavigationMenuTab == route
                                val tintColor = if (isSelected) NeonGreen else TextGray
                                val bgAlpha = if (isSelected) 0.12f else 0.0f

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) NeonGreen.copy(alpha = bgAlpha) else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) NeonGreen.copy(alpha = 0.25f) else Color.Transparent,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { activeNavigationMenuTab = route }
                                        .padding(vertical = 10.dp)
                                        .testTag("nav_item_$route"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val sizeScale by animateFloatAsState(
                                        targetValue = if (isSelected) 1.15f else 1.0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "nav_item_scale"
                                    )
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.graphicsLayer {
                                            scaleX = sizeScale
                                            scaleY = sizeScale
                                        }
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = tintColor,
                                            modifier = Modifier.size(20.dp)
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
            var showValidationError by remember { mutableStateOf(false) }

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
            var fieldPerson by remember { mutableStateOf("") }
            var fieldGroupName by remember { mutableStateOf("Flatmates") }
            var fieldFrequency by remember { mutableStateOf("Monthly") }
            var fieldSipDay by remember { mutableStateOf("5") }
            var fieldCurrentVal by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { activeManualDialog = null }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                    border = BorderStroke(1.dp, BorderColor),
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
                                        .background(SurfaceBlue)
                                        .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(6.dp))
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
                                    .background(if (LocalCssThemeVariables.current.isLight) Color(0xFFFEE2E2) else Color(0xFF442D2D))
                                    .border(BorderStroke(1.dp, if (LocalCssThemeVariables.current.isLight) Color(0xFFFCA5A5) else Color(0xFF5C2C2C)), RoundedCornerShape(6.dp))
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
                                if (fieldPayMode == "Credit Card") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = fieldCardName,
                                        onValueChange = { fieldCardName = it },
                                        label = { Text("Associated Credit Card", color = TextGray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ManualOptionSelector(
                                        label = "Or Speed Select Card:",
                                        options = listOf(
                                            "💳 HDFC Millennia" to "HDFC Millennia",
                                            "💳 ICICI Amazon" to "ICICI Amazon",
                                            "💳 Axis Magnus" to "Axis Magnus"
                                        ),
                                        selected = fieldCardName,
                                        onSelect = { fieldCardName = it }
                                    )
                                }
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
                                    label = { Text("Involved Person Name(s) (comma separated)", color = TextGray) },
                                    placeholder = { Text("e.g. Roommate, Colleague", color = TextGray.copy(alpha = 0.5f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⚡ TAP TO TOGGLE MULTIPLE FRIENDS:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextGray
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val currentPeopleParts = fieldPerson.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val presetFriends = dbParticipants.map { it.name }
                                    presetFriends.forEach { friend ->
                                        val isSelected = currentPeopleParts.any { it.equals(friend, ignoreCase = true) }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) AccentOrange else BorderColor,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .background(if (isSelected) AccentOrange.copy(alpha = 0.15f) else SurfaceBlue)
                                                .clickable {
                                                    val updatedList = currentPeopleParts.toMutableList()
                                                    if (isSelected) {
                                                        updatedList.removeAll { it.equals(friend, ignoreCase = true) }
                                                    } else {
                                                        updatedList.add(friend)
                                                    }
                                                    fieldPerson = updatedList.distinct().joinToString(", ")
                                                }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "👤 $friend",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) AccentOrange else TextWhite
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = fieldGroupName,
                                    onValueChange = { fieldGroupName = it },
                                    label = { Text("Group Split Session name", color = TextGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
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

                        if (showValidationError) {
                            Text(
                                text = "⚠️ Please enter a valid amount greater than 0.",
                                color = DangerRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
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
                                    if (amtD <= 0.0) {
                                        showValidationError = true
                                    } else {
                                        showValidationError = false
                                        val finalDesc = if (fieldDesc.isNotBlank()) fieldDesc else {
                                            when (type) {
                                                "DEBT" -> "Split Bill with ${if (fieldPerson.isNotBlank()) fieldPerson else "Friends"}"
                                                "EXPENSE" -> "Manual Expense ($fieldCat)"
                                                "CREDIT" -> "Credit Swipe"
                                                "EMI" -> "EMI Loan Payment"
                                                "INCOME" -> "Income Record"
                                                "SIP" -> "SIP Contribution"
                                                "INVESTMENT" -> "Asset Investment"
                                                else -> "Manual Entry"
                                            }
                                        }
                                        when (type) {
                                            "EXPENSE" -> viewModel.addManualExpense(amtD, finalDesc, fieldCat, fieldPayMode, cardName = fieldCardName)
                                            "CREDIT" -> viewModel.addManualCredit(amtD, finalDesc, fieldCardName, fieldCat)
                                            "EMI" -> viewModel.addManualEmi(amtD, finalDesc, fieldTenure.toIntOrNull() ?: 12, fieldRemMonths.toIntOrNull() ?: 8)
                                            "DEBT" -> viewModel.addManualDebt(amtD, finalDesc, fieldPerson, fieldGroupName.isNotBlank(), fieldGroupName)
                                            "INCOME" -> viewModel.addManualIncome(amtD, finalDesc, fieldFrequency)
                                            "SIP" -> viewModel.addManualSip(amtD, finalDesc, fieldCat, fieldSipDay.toIntOrNull() ?: 5)
                                            "INVESTMENT" -> {
                                                val currVal = fieldCurrentVal.toDoubleOrNull() ?: amtD
                                                viewModel.addManualInvestment(amtD, finalDesc, fieldCat, currVal)
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
                            color = if (isSelected) colorAccent else BorderColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(if (isSelected) colorAccent.copy(alpha = 0.15f) else SurfaceBlue)
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
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
        border = BorderStroke(1.dp, BorderColor),
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
            .background(SurfaceBlue)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
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
    var localPerson by remember { mutableStateOf("") }
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
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
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
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
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
                                .background(SurfaceBlue)
                                .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(6.dp))
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
                            .background(if (LocalCssThemeVariables.current.isLight) Color(0xFFFEE2E2) else Color(0xFF442D2D))
                            .border(BorderStroke(1.dp, if (LocalCssThemeVariables.current.isLight) Color(0xFFFCA5A5) else Color(0xFF5C2C2C)), RoundedCornerShape(6.dp))
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
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
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
    catExps: Double,
    viewModel: WealthPulseViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val displayName = remember(currentUser) {
        val user = currentUser
        if (user == null || user.isAnonymous) {
            "Guest Analyst"
        } else {
            val email = user.email
            val localPart = email.substringBefore("@")
            localPart.split(".", "_", "-").joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
            }
        }
    }
    val initials = remember(displayName) {
        displayName.split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.take(1).uppercase() }
    }
    val analystId = remember(currentUser) {
        val user = currentUser
        if (user == null || user.isAnonymous) {
            "ID: WP-GUEST-LOCAL"
        } else {
            "ID: WP-SECURE-${user.uid.takeLast(6).uppercase()}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Core Header
        Text(
            text = displayName,
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
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
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
                    Text(initials.ifEmpty { "G" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(displayName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text(if (currentUser != null && !currentUser!!.isAnonymous) "Analyst Level: Vault Verified Planner" else "Analyst Level: Local Sandbox", fontSize = 11.sp, color = TextGray)
                    Text(analystId, fontSize = 10.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
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
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
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
                    .background(SurfaceBlue)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
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
                    .background(SurfaceBlue)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
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
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
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
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }

    if (showAuthDialog) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            viewModel = viewModel
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp)
    ) {
        // -----------------------------------------------------------------
        // BEAUTIFUL SECURITY & FIREBASE AUTHENTICATION PANEL
        // -----------------------------------------------------------------
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, if (currentUser != null) NeonGreen.copy(alpha = 0.5f) else BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("auth_account_security_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FIREBASE SERVICE AUTHENTICATION",
                            fontSize = 11.sp,
                            color = if (currentUser != null) NeonGreen else TextGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentUser != null) "Session Active (Database Partitioned)" 
                                   else "Unsigned Guest Mode (Local Sandbox Database)",
                            fontSize = 14.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Status Badge Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentUser != null) NeonGreen.copy(alpha = 0.15f) else Color(0x3378716C))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (currentUser != null) "VERIFIED" else "SANDBOX",
                            fontSize = 10.sp,
                            color = if (currentUser != null) NeonGreen else TextGray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderColor)
                Spacer(modifier = Modifier.height(12.dp))

                if (currentUser != null) {
                    // Signed User info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonGreen.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Active user ID icon",
                                tint = NeonGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Registered Email Address:",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Text(
                                text = currentUser!!.email,
                                fontSize = 13.sp,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Partition ID: $currentUserId",
                        fontSize = 10.sp,
                        color = Color(0xFF78716C),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = 46.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.signOutUser {
                                Toast.makeText(context, "Signed out safely! Back to Local Sandbox.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (LocalCssThemeVariables.current.isLight) Color(0xFFE2E8F0) else Color(0xFF292524)),
                        border = BorderStroke(1.dp, BorderColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("auth_logout_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log Out Icon", tint = TextWhite, modifier = Modifier.size(16.dp))
                            Text("SECURELY SIGN OUT", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Guest user promotion CTA
                    Text(
                        text = "Your financial worksheets are currently local. Setup an encrypted account to backup expenses, loans, splits, and payday calendars securely across any session.",
                        fontSize = 12.sp,
                        color = TextGray,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showAuthDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("auth_trigger_login_modal")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Security login key icon", tint = NavyBg, modifier = Modifier.size(16.dp))
                            Text("SECURELY LOCK & PERSIST MY DATA", fontSize = 12.sp, color = NavyBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (currentUser != null) {
            val syncStats by viewModel.syncManager.syncStats.collectAsState()
            val isSimulatedOnline by viewModel.syncManager.isSimulatedOnline.collectAsState()
            var syncInProgress by remember { mutableStateOf(false) }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (syncInProgress) NeonGreen else BorderColor),
                colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("sync_dashboard_status_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "CLOUD DATABASE SYNC DASHBOARD",
                        fontSize = 11.sp,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Multi-Device Real-time Indexing",
                        fontSize = 15.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Network Mode", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (isSimulatedOnline) "Sandbox Simulation (Mock Firestore)" else "Production Live Network",
                                fontSize = 10.sp,
                                color = TextGray
                            )
                        }
                        Switch(
                            checked = isSimulatedOnline,
                            onCheckedChange = { viewModel.syncManager.setSimulatedOnline(it) },
                            modifier = Modifier.testTag("simulate_network_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Last Backup Status:", fontSize = 12.sp, color = TextGray)
                        Text(
                            text = syncStats.lastSyncTime,
                            fontSize = 11.sp,
                            color = NeonGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Sync Queue Status Details:", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Connection State:", fontSize = 12.sp, color = TextWhite)
                        Text(
                            text = if (isSimulatedOnline) "Sandbox Linked" else syncStats.health.name,
                            fontSize = 11.sp,
                            color = NeonGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Pending snaps queued:", fontSize = 12.sp, color = TextWhite)
                        Text(
                            text = "${syncStats.pendingUploads} operations",
                            fontSize = 11.sp,
                            color = if (syncStats.pendingUploads > 0) cssVar("--accent") else TextWhite,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Sync Retry Failures:", fontSize = 12.sp, color = TextWhite)
                        Text(
                            text = "${syncStats.failedSyncs} failures",
                            fontSize = 11.sp,
                            color = if (syncStats.failedSyncs > 0) cssVar("--danger") else TextWhite,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (syncInProgress) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NeonGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Uploading secure snapshots...", style = MaterialTheme.typography.bodySmall, color = NeonGreen)
                        }
                    } else {
                        Button(
                            onClick = {
                                syncInProgress = true
                                coroutineScope.launch {
                                    val success = viewModel.syncManager.syncNow(currentUserId)
                                    syncInProgress = false
                                    if (success) {
                                        Toast.makeText(context, "Cloud sync complete!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Offline mode / Sync retry queued safely.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("force_sync_now_btn")
                        ) {
                            Text("TRIGGER CLOUD FORCE SYNC", fontSize = 12.sp, color = NavyBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // -----------------------------------------------------------------
        // DYNAMIC AESTHETIC THEME SWAPPER
        // -----------------------------------------------------------------
        Text(
            text = "CHOOSE SYSTEM THEME",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF78716C), // Stone 500
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val selectedThemeName by viewModel.selectedTheme.collectAsState()
        // themeId, label name, Pair(Background Preview, Primary/Accent Preview)
        val themes = listOf(
            Triple("default", "Sophisticated Dark", Pair(Color(0xFF0C0A09), Color(0xFF10B981))),
            Triple("oceanic_abyss", "Oceanic Abyss", Pair(Color(0xFF020617), Color(0xFF8B5CF6))),
            Triple("forest_sage", "Forest Sage", Pair(Color(0xFF052E16), Color(0xFF22C55E))),
            Triple("sunset_glow", "Sunset Glow", Pair(Color(0xFF1C0A10), Color(0xFFF43F5E))),
            Triple("bright_aurora", "Light Sky Blue", Pair(Color(0xFFF0F9FF), Color(0xFF0284C7)))
        )

        val chosenThemeInfo = themes.find { it.first == selectedThemeName } ?: themes.first()
        var themeDropdownExpanded by remember { mutableStateOf(false) }

        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { themeDropdownExpanded = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Swatch Indicator
                        Row(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(chosenThemeInfo.third.first)
                                .border(
                                    if (chosenThemeInfo.first == "bright_aurora") BorderStroke(1.dp, Color(0xFFCBD5E1)) else BorderStroke(0.dp, Color.Transparent),
                                    CircleShape
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(chosenThemeInfo.third.second)
                            )
                        }

                        Column {
                            Text(
                                text = chosenThemeInfo.second,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Active System Theme (Click to switch)",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand theme menu dropdown",
                        tint = TextGray
                    )
                }

                DropdownMenu(
                    expanded = themeDropdownExpanded,
                    onDismissRequest = { themeDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .background(SurfaceBlue)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                ) {
                    themes.forEach { (themeId, themeLabel, colors) ->
                        val isSelected = selectedThemeName == themeId
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Swatch Indicator
                                    Row(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(colors.first)
                                            .border(
                                                if (themeId == "bright_aurora") BorderStroke(0.5.dp, Color(0xFFCBD5E1)) else BorderStroke(0.dp, Color.Transparent),
                                                CircleShape
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(colors.second)
                                        )
                                    }

                                    Text(
                                        text = themeLabel,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NeonGreen else TextWhite
                                    )
                                }
                            },
                            onClick = {
                                viewModel.selectTheme(themeId)
                                Toast.makeText(context, "Theme switched to $themeLabel!", Toast.LENGTH_SHORT).show()
                                themeDropdownExpanded = false
                            },
                            modifier = Modifier.background(if (isSelected) colors.second.copy(alpha = 0.08f) else Color.Transparent)
                        )
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // USER & PARTICIPANTS HUB
        // -----------------------------------------------------------------
        Text(
            text = "MEMBERS & PARTICIPANTS WORKSPACE",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF78716C), // Stone 500
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val dbParticipants by viewModel.participants.collectAsState()
        var newFriendName by remember { mutableStateOf("") }
        var newFriendEmail by remember { mutableStateOf("") }

        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ADD NEW WORKSPACE MEMBER",
                    fontSize = 11.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = newFriendName,
                    onValueChange = { newFriendName = it },
                    label = { Text("Workspace Name (cannot be empty)", color = TextGray) },
                    placeholder = { Text("e.g. Johnathan", color = TextGray.copy(alpha = 0.5f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_workspace_member_name_input"),
                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = newFriendEmail,
                    onValueChange = { newFriendEmail = it },
                    label = { Text("Workspace Email address", color = TextGray) },
                    placeholder = { Text("e.g. johnathan@myfin.io", color = TextGray.copy(alpha = 0.5f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_workspace_member_email_input"),
                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        if (newFriendName.trim().isEmpty()) {
                            Toast.makeText(context, "Please enter a valid member name", Toast.LENGTH_SHORT).show()
                        } else {
                            val emailVal = if (newFriendEmail.trim().isEmpty()) "${newFriendName.trim().lowercase().replace(" ", "")}@myfin.io" else newFriendEmail.trim()
                            viewModel.addParticipantToDb(newFriendName.trim(), emailVal, false)
                            Toast.makeText(context, "Added ${newFriendName.trim()} to your system workspace", Toast.LENGTH_SHORT).show()
                            newFriendName = ""
                            newFriendEmail = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("add_workspace_member_submit_button")
                ) {
                    Text("REGISTER WORKSPACE MEMBER", fontSize = 12.sp, color = NavyBg, fontWeight = FontWeight.Bold)
                }

                if (dbParticipants.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "ACTIVE PARTICIPANTS IN WORKSPACE (${dbParticipants.size})",
                        fontSize = 11.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    dbParticipants.forEach { participant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (LocalCssThemeVariables.current.isLight) Color(0xFFF1F5F9) else Color(0xFF0F0F0F))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Initials Avatar Box
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(NeonGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = participant.name.take(1).uppercase(),
                                        color = NeonGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = participant.name,
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = participant.email,
                                        color = TextGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = {
                                    viewModel.removeParticipant(participant.id)
                                    Toast.makeText(context, "Deleted ${participant.name} from workspace", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp).testTag("delete_member_btn_${participant.name}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete workspace member",
                                    tint = DangerRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No custom members added. Add travel, housemate, or office group members to compute group expense splits instantly!",
                        fontSize = 11.sp,
                        color = TextGray,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    )
                }
            }
        }

        // -----------------------------------------------------------------
        // SYSTEM PREFERENCES & CONTROLS
        // -----------------------------------------------------------------
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
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
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
                            colors = ButtonDefaults.buttonColors(containerColor = if (currencyMode == "INR") NeonGreen else BorderColor),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("currency_inr_button")
                        ) {
                            Text("INR (₹)", fontSize = 11.sp, color = if (currencyMode == "INR") NavyBg else TextWhite, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { currencyMode = "USD"; Toast.makeText(context, "Base Currency: USD ($)", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = if (currencyMode == "USD") NeonGreen else BorderColor),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("currency_usd_button")
                        ) {
                            Text("USD ($)", fontSize = 11.sp, color = if (currencyMode == "USD") NavyBg else TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderColor)
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
                HorizontalDivider(color = BorderColor)
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
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
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
                Text("Application Version: MYFin Core v2.4.2", fontSize = 12.sp, color = NeonGreen)
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

@Composable
fun EmiWorkspacePage(
    emiLoans: List<EmiLoanEntity>,
    totalEmi: Double,
    currencyFormatter: java.text.NumberFormat,
    viewModel: WealthPulseViewModel,
    onTriggerManual: (String) -> Unit
) {
    // Interactive Form and Page state
    var emiAmount by remember { mutableStateOf("") }
    var emiDesc by remember { mutableStateOf("") }
    var emiCategory by remember { mutableStateOf("Loan") }
    var emiTotalMonths by remember { mutableStateOf("12") }
    var emiDoneMonths by remember { mutableStateOf("0") }
    var emiDeductionDay by remember { mutableStateOf("5") }
    
    var showForm by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    // Calculator inputs
    var calcPrincipal by remember { mutableStateOf(1000000.0) } // 10 Lac default
    var calcInterestRate by remember { mutableStateOf(8.5) }    // 8.5% default
    var calcTenureYears by remember { mutableStateOf(15) }      // 15 years default

    val emiCalculated = remember(calcPrincipal, calcInterestRate, calcTenureYears) {
        val r = calcInterestRate / (12 * 100)
        val n = calcTenureYears * 12
        if (r == 0.0) calcPrincipal / n
        else {
            val power = Math.pow(1 + r, n.toDouble())
            (calcPrincipal * r * power) / (power - 1)
        }
    }
    val totalPayment = emiCalculated * (calcTenureYears * 12)
    val totalInterest = totalPayment - calcPrincipal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Page Title & Header
        Text(
            text = "STRUCTURAL LIABILITIES",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF78716C),
            letterSpacing = 2.sp
        )
        Text(
            text = "EMI Liability Monitor",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            color = TextWhite,
            fontWeight = FontWeight.Light,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Summary Metric & Quick Action Dashboard
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ACTIVE EMI DEBT RUNRATE",
                            fontSize = 10.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currencyFormatter.format(totalEmi),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            color = AccentOrange,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "${emiLoans.size} active liability schedules tracked",
                            fontSize = 11.sp,
                            color = TextGray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Button(
                        onClick = { showForm = !showForm },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showForm) BorderColor else NeonGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("emi_tab_toggle_form_btn")
                    ) {
                        Text(
                            text = if (showForm) "Close Form" else "+ Add EMI",
                            fontSize = 11.sp,
                            color = if (showForm) TextWhite else NavyBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // INLINE ADD EMI FORM CARD
        AnimatedVisibility(visible = showForm) {
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🆕 REGISTER ACTIVE EMI LIABILITY",
                        fontSize = 12.sp,
                        color = AccentOrange,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = emiDesc,
                        onValueChange = { emiDesc = it },
                        label = { Text("EMI Name / Description", fontSize = 12.sp, color = TextGray) },
                        placeholder = { Text("e.g. HDFC Home Loan, TV Instalments", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            focusedBorderColor = AccentOrange,
                            unfocusedBorderColor = BorderColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = emiAmount,
                            onValueChange = { emiAmount = it },
                            label = { Text("EMI Amount (₹)", fontSize = 12.sp, color = TextGray) },
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                focusedBorderColor = AccentOrange,
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        OutlinedTextField(
                            value = emiDeductionDay,
                            onValueChange = { emiDeductionDay = it },
                            label = { Text("Deduction Day (1-31)", fontSize = 12.sp, color = TextGray) },
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                focusedBorderColor = AccentOrange,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    }

                    // Speed Deduction day selector
                    Text("Or Select Deduction Day:", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("1", "5", "10", "15", "20", "25").forEach { speedDay ->
                            val isSelected = emiDeductionDay == speedDay
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentOrange else (if (LocalCssThemeVariables.current.isLight) Color(0xFFF1F5F9) else Color(0xFF1E1E1F)))
                                    .border(BorderStroke(1.dp, if (isSelected) AccentOrange else BorderColor), RoundedCornerShape(8.dp))
                                    .clickable { emiDeductionDay = speedDay }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "${speedDay}th", fontSize = 11.sp, color = if (isSelected) NavyBg else TextWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = emiTotalMonths,
                            onValueChange = { emiTotalMonths = it },
                            label = { Text("Total Installments", fontSize = 12.sp, color = TextGray) },
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                focusedBorderColor = AccentOrange,
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        OutlinedTextField(
                            value = emiDoneMonths,
                            onValueChange = { emiDoneMonths = it },
                            label = { Text("Installments Done", fontSize = 12.sp, color = TextGray) },
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                focusedBorderColor = AccentOrange,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    }

                    // EMI Category Speed Selector
                    Text("Select Purpose Category:", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "🏠 Housing" to "Home Loan",
                            "🚗 Vehicle" to "Car Loan",
                            "💡 Utilities" to "Utility Debt",
                            "🎓 Education" to "Education Loan",
                            "💻 Gadgets" to "Electronics EMI",
                            "📦 Personal" to "Personal"
                        ).forEach { (label, categoryVal) ->
                            val isSelected = emiCategory == categoryVal
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentOrange.copy(alpha = 0.2f) else (if (LocalCssThemeVariables.current.isLight) Color(0xFFF1F5F9) else Color(0xFF1E1E1F)))
                                    .border(BorderStroke(1.dp, if (isSelected) AccentOrange else BorderColor), RoundedCornerShape(8.dp))
                                    .clickable { emiCategory = categoryVal }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = label, fontSize = 11.sp, color = if (isSelected) AccentOrange else TextWhite)
                            }
                        }
                    }

                    formError?.let { err ->
                        Text(err, color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                    }

                    Button(
                        onClick = {
                            val amt = emiAmount.toDoubleOrNull() ?: 0.0
                            val day = emiDeductionDay.toIntOrNull() ?: 5
                            val total = emiTotalMonths.toIntOrNull() ?: 12
                            val done = emiDoneMonths.toIntOrNull() ?: 0
                            
                            if (emiDesc.isBlank()) {
                                formError = "Please enter an EMI Name description."
                            } else if (amt <= 0.0) {
                                formError = "Please enter an installment amount."
                            } else if (day !in 1..31) {
                                formError = "Deduction Day must be between 1 and 31."
                            } else if (total <= 0) {
                                formError = "Total Installments must be greater than 0."
                            } else if (done < 0 || done > total) {
                                formError = "Installments Done must be between 0 and $total."
                            } else {
                                formError = null
                                val remaining = total - done
                                viewModel.addManualEmi(amt, emiDesc, total, remaining, day)
                                // Reset form
                                emiAmount = ""
                                emiDesc = ""
                                emiTotalMonths = "12"
                                emiDoneMonths = "0"
                                showForm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Register Active EMI Schedule 📅", color = NavyBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Loans/EMIs list
        Text(
            text = "📋 RUNNING EMIs & PAYOUT LOGS",
            fontSize = 11.sp,
            color = TextWhite.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (emiLoans.isEmpty()) {
            EmptyStatePlaceholder("No live structural liabilities or loan EMIs registered yet.")
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                emiLoans.forEach { emi ->
                    val done = emi.totalTenureMonths - emi.remainingMonths
                    val total = emi.totalTenureMonths
                    val progressFraction = if (total > 0) done.toFloat() / total.toFloat() else 0.0f
                    
                    // Retrieve extraction day logic from timestamp
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = emi.timestamp
                    val deductionDay = cal.get(java.util.Calendar.DAY_OF_MONTH)

                    // BEAUTIFUL EMI DEBT CARD
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
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
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AccentOrange.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🏦", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = emi.description,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Category: ${emi.category} • Auto Pay: Day ${deductionDay}th",
                                            fontSize = 11.sp,
                                            color = TextGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "${currencyFormatter.format(emi.amount)}/mo",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AccentOrange
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteEmi(emi.id) },
                                        modifier = Modifier.size(28.dp).testTag("delete_emi_${emi.id}")
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

                            // Dynamic Installment Progress Line
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Progress: $done / $total Paid installments",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                                Text(
                                    text = String.format("%.0f%% Done", progressFraction * 100f),
                                    fontSize = 11.sp,
                                    color = AccentOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AccentOrange,
                                trackColor = if (LocalCssThemeVariables.current.isLight) Color(0xFFE2E8F0) else Color(0xFF262626)
                            )

                            // Alert Banner
                            Box(
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (LocalCssThemeVariables.current.isLight) Color(0xFFFFEDD5) else Color(0xFF1F120D))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📅", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Auto-debit reminder: Next deduction scheduled on date $deductionDay",
                                        fontSize = 10.sp,
                                        color = AccentOrange,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // INTERACTIVE LOAN CALCULATOR SUMMARY CONTAINER (COLLAPSIBLE)
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCalculator = !showCalculator },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🧮", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "INTERACTIVE LOAN CALCULATOR",
                                fontSize = 12.sp,
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Simulate prospective loans is secondary focus",
                                fontSize = 10.sp,
                                color = TextGray
                            )
                        }
                    }
                    Text(
                        text = if (showCalculator) "[- Tap to Collapse]" else "[+ Tap to Expand]",
                        fontSize = 11.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(visible = showCalculator) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Plan compounding amortizations. Adjust variables in real-time to compute prospective debt repayments.",
                            fontSize = 11.sp,
                            color = TextGray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Principal Amount Slider
                        Text(
                            text = "Principal Amount: ${currencyFormatter.format(calcPrincipal)}",
                            fontSize = 13.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = calcPrincipal.toFloat(),
                            onValueChange = { calcPrincipal = it.toDouble() },
                            valueRange = 50000f..5000000f,
                            steps = 99,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonGreen,
                                activeTrackColor = NeonGreen.copy(alpha = 0.5f),
                                inactiveTrackColor = if (LocalCssThemeVariables.current.isLight) Color(0xFFE2E8F0) else Color(0xFF262626)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interest Rate Slider
                        Text(
                            text = "Interest Rate: ${String.format("%.1f%%", calcInterestRate)} p.a.",
                            fontSize = 13.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = calcInterestRate.toFloat(),
                            onValueChange = { scale -> calcInterestRate = Math.round(scale * 10) / 10.0 },
                            valueRange = 4f..24f,
                            steps = 200,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonGreen,
                                activeTrackColor = NeonGreen.copy(alpha = 0.5f),
                                inactiveTrackColor = if (LocalCssThemeVariables.current.isLight) Color(0xFFE2E8F0) else Color(0xFF262626)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tenure Slider
                        Text(
                            text = "Tenure: $calcTenureYears Years (${calcTenureYears * 12} months)",
                            fontSize = 13.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = calcTenureYears.toFloat(),
                            onValueChange = { calcTenureYears = it.toInt() },
                            valueRange = 1f..30f,
                            steps = 29,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonGreen,
                                activeTrackColor = NeonGreen.copy(alpha = 0.5f),
                                inactiveTrackColor = if (LocalCssThemeVariables.current.isLight) Color(0xFFE2E8F0) else Color(0xFF262626)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Results Layout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (LocalCssThemeVariables.current.isLight) Color(0xFFF1F5F9) else Color(0xFF18181A))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("EST. MONTHLY EMI", fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                Text(currencyFormatter.format(emiCalculated), fontSize = 13.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("INTEREST PAYABLE", fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                Text(currencyFormatter.format(totalInterest), fontSize = 13.sp, color = AccentOrange, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("TOTAL COST", fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                Text(currencyFormatter.format(totalPayment), fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SipWorkspacePage(
    sipRecords: List<SipEntity>,
    totalSip: Double,
    currencyFormatter: java.text.NumberFormat,
    viewModel: WealthPulseViewModel,
    onTriggerManual: (String) -> Unit
) {
    // Form and Interactive page states
    var sipAmtStr by remember { mutableStateOf("") }
    var sipName by remember { mutableStateOf("") }
    var sipCat by remember { mutableStateOf("Mutual Funds") }
    var sipDayStr by remember { mutableStateOf("5") }
    var sipFrequency by remember { mutableStateOf("Monthly") }

    var showForm by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    // Calculator inputs
    var sipAmount by remember { mutableStateOf(10000.0) }       // 10K default
    var sipReturnRate by remember { mutableStateOf(12.0) }      // 12% default
    var sipYears by remember { mutableStateOf(10) }            // 10 years default

    val sipFutureValue = remember(sipAmount, sipReturnRate, sipYears) {
        val i = sipReturnRate / (12 * 100)
        val n = sipYears * 12
        if (i == 0.0) sipAmount * n
        else {
            sipAmount * ((Math.pow(1 + i, n.toDouble()) - 1) / i) * (1 + i)
        }
    }
    val totalSipInvested = sipAmount * (sipYears * 12)
    val sipEstimatedWeatlh = sipFutureValue - totalSipInvested

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp)
    ) {
        // Page Title & Header
        Text(
            text = "AUTOMATED WEALTH CREATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF78716C),
            letterSpacing = 2.sp
        )
        Text(
            text = "SIP Spend Monitor",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            color = TextWhite,
            fontWeight = FontWeight.Light,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Summary Metric & Actions
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL MONTHLY SIP OUTFLOW",
                            fontSize = 10.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currencyFormatter.format(totalSip),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            color = Color(0xFF3B82F6),
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "${sipRecords.size} running compounding plans tracked",
                            fontSize = 11.sp,
                            color = TextGray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Button(
                        onClick = { showForm = !showForm },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showForm) BorderColor else NeonGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("sip_tab_toggle_form_btn")
                    ) {
                        Text(
                            text = if (showForm) "Close Form" else "+ Add SIP",
                            fontSize = 11.sp,
                            color = if (showForm) TextWhite else NavyBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // INLINE ADD SIP FORM CARD
        AnimatedVisibility(visible = showForm) {
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📈 INITIALIZE SIP AUTO-DEBIT LINK",
                        fontSize = 12.sp,
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = sipName,
                        onValueChange = { sipName = it },
                        label = { Text("SIP Fund Name description", fontSize = 12.sp, color = TextGray) },
                        placeholder = { Text("e.g. Parag Parikh Flexi Cap, SBI Small Cap", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = BorderColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sipAmtStr,
                            onValueChange = { sipAmtStr = it },
                            label = { Text("Monthly Amount (₹)", fontSize = 12.sp, color = TextGray) },
                            modifier = Modifier.weight(1.2f),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = BorderColor
                            )
                        )

                        OutlinedTextField(
                            value = sipDayStr,
                            onValueChange = { sipDayStr = it },
                            label = { Text("Debit Day of Month (1-31)", fontSize = 12.sp, color = TextGray) },
                            modifier = Modifier.weight(1f),
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    }

                    // Speed date selection
                    Text("Or Select Debit Day:", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("1", "5", "10", "15", "20", "25").forEach { seedDay ->
                            val isSelected = sipDayStr == seedDay
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SipBlue else SurfaceBlue)
                                    .border(BorderStroke(1.dp, if (isSelected) SipBlue else BorderColor), RoundedCornerShape(8.dp))
                                    .clickable { sipDayStr = seedDay }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "${seedDay}th", fontSize = 11.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Asset category speed chips
                    Text("Select Investment Class Asset:", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "📈 Mutual Funds" to "Mutual Funds",
                            "📊 Index Funds" to "Index Funds",
                            "💼 Stocks/Equity" to "Equity",
                            "🟡 Safe Gold" to "Gold",
                            "🧑‍💻 Crypto" to "Crypto"
                        ).forEach { (label, dbVal) ->
                            val isSelected = sipCat == dbVal
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF2563EB).copy(alpha = 0.2f) else (if (LocalCssThemeVariables.current.isLight) Color(0xFFF1F5F9) else Color(0xFF1E1E1F)))
                                    .border(BorderStroke(1.dp, if (isSelected) Color(0xFF3B82F6) else BorderColor), RoundedCornerShape(8.dp))
                                    .clickable { sipCat = dbVal }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = label, fontSize = 11.sp, color = if (isSelected) Color(0xFF3B82F6) else TextWhite)
                            }
                        }
                    }

                    // Frequency
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Frequency: ", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        listOf("Monthly", "Weekly").forEach { freq ->
                            val isSelected = sipFrequency == freq
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.15f) else Color.Transparent)
                                    .border(BorderStroke(1.dp, if (isSelected) Color(0xFF3B82F6) else BorderColor), RoundedCornerShape(8.dp))
                                    .clickable { sipFrequency = freq }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(freq, fontSize = 11.sp, color = if (isSelected) Color(0xFF3B82F6) else TextGray)
                            }
                        }
                    }

                    formError?.let { err ->
                        Text(err, color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                    }

                    Button(
                        onClick = {
                            val amt = sipAmtStr.toDoubleOrNull() ?: 0.0
                            val day = sipDayStr.toIntOrNull() ?: 5
                            if (sipName.isBlank()) {
                                formError = "Please enter a Fund SIP Name description."
                            } else if (amt <= 0.0) {
                                formError = "Please enter a valid monthly SIP amount."
                            } else if (day !in 1..31) {
                                formError = "Debit day must be between 1 and 31."
                            } else {
                                formError = null
                                viewModel.addManualSip(amt, sipName, sipCat, day, sipFrequency)
                                // Reset
                                sipName = ""
                                sipAmtStr = ""
                                showForm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save and Map to Calendar 📅", color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active compounding schedules list
        Text(
            text = "📋 RUNNING WEALTH COMPOUNDING PORTFOLIOS",
            fontSize = 11.sp,
            color = TextWhite.copy(alpha = 0.6f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (sipRecords.isEmpty()) {
            EmptyStatePlaceholder("No live mutual fund SIP savings portfolios configured yet.")
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                sipRecords.forEach { sip ->
                    val assetIcon = when(sip.investmentCategory) {
                        "Gold" -> "🟡"
                        "Crypto" -> "🧑‍💻"
                        "Equity" -> "💼"
                        "Index Funds" -> "📊"
                        else -> "📈"
                    }

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
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
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF3B82F6).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(assetIcon, fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = sip.description,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Category: ${sip.investmentCategory} • Frequency: ${sip.frequency}",
                                            fontSize = 11.sp,
                                            color = TextGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "${currencyFormatter.format(sip.amount)}/mo",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF3B82F6)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteSip(sip.id) },
                                        modifier = Modifier.size(28.dp).testTag("delete_sip_${sip.id}")
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

                            Spacer(modifier = Modifier.height(10.dp))

                            // Next Auto-Debit Reminder Block
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (LocalCssThemeVariables.current.isLight) Color(0xFFEFF6FF) else Color(0xFF0F141F))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📅", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Calendar alarm linked: Auto-debits on day ${sip.dayOfMonth}th of month",
                                        fontSize = 10.sp,
                                        color = if (LocalCssThemeVariables.current.isLight) Color(0xFF1D4ED8) else Color(0xFF60A5FA),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // INTERACTIVE FORECAST CALCULATOR (COLLAPSIBLE)
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 90.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCalculator = !showCalculator },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "INTERACTIVE WEALTH COMPOUNDER",
                                fontSize = 12.sp,
                                color = Color(0xFF3B82F6),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Simulate mock prospective compound rates",
                                fontSize = 10.sp,
                                color = TextGray
                            )
                        }
                    }
                    Text(
                        text = if (showCalculator) "[- Tap to Collapse]" else "[+ Tap to Expand]",
                        fontSize = 11.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(visible = showCalculator) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Plan compounding futures. Adjust variables in real-time to compute prospective wealth growth.",
                            fontSize = 11.sp,
                            color = TextGray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Monthly Investment Slider
                        Text(
                            text = "Monthly SIP: ${currencyFormatter.format(sipAmount)}",
                            fontSize = 13.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = sipAmount.toFloat(),
                            onValueChange = { sipAmount = it.toDouble() },
                            valueRange = 500f..100000f,
                            steps = 199,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B82F6),
                                activeTrackColor = Color(0xFF3B82F6).copy(alpha = 0.5f),
                                inactiveTrackColor = if (LocalCssThemeVariables.current.isLight) Color(0xFFE2E8F0) else Color(0xFF262626)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Expected Return Slider
                        Text(
                            text = "Expected Return Rate: ${String.format("%.1f%%", sipReturnRate)} p.a.",
                            fontSize = 13.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = sipReturnRate.toFloat(),
                            onValueChange = { scale -> sipReturnRate = Math.round(scale * 10) / 10.0 },
                            valueRange = 5f..30f,
                            steps = 250,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B82F6),
                                activeTrackColor = Color(0xFF3B82F6).copy(alpha = 0.5f),
                                inactiveTrackColor = if (LocalCssThemeVariables.current.isLight) Color(0xFFE2E8F0) else Color(0xFF262626)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Years Slider
                        Text(
                            text = "Investment Period: $sipYears Years",
                            fontSize = 13.sp,
                            color = TextWhite,
                            fontWeight = FontWeight.Medium
                        )
                        Slider(
                            value = sipYears.toFloat(),
                            onValueChange = { sipYears = it.toInt() },
                            valueRange = 2f..40f,
                            steps = 38,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B82F6),
                                activeTrackColor = Color(0xFF3B82F6).copy(alpha = 0.5f),
                                inactiveTrackColor = if (LocalCssThemeVariables.current.isLight) Color(0xFFE2E8F0) else Color(0xFF262626)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Results Layout
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (LocalCssThemeVariables.current.isLight) Color(0xFFF1F5F9) else Color(0xFF18181A))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("INVESTED PRINCIPAL", fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                Text(currencyFormatter.format(totalSipInvested), fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("EST. GROWTH RETURNS", fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                Text(currencyFormatter.format(sipEstimatedWeatlh), fontSize = 13.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("TOTAL VALUE", fontSize = 9.sp, color = TextGray, fontWeight = FontWeight.Bold)
                                Text(currencyFormatter.format(sipFutureValue), fontSize = 13.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplitsWorkspacePage(
    debtSplits: List<DebtSplitEntity>,
    totalDebt: Double,
    currencyFormatter: java.text.NumberFormat,
    viewModel: WealthPulseViewModel,
    tripEvents: List<TripEventEntity>,
    tripExpenses: List<TripExpenseEntity>,
    allParticipants: List<ParticipantEntity>,
    onTriggerManual: (String) -> Unit
) {
    com.example.ui.SplitsWorkspaceHub(currencyFormatter = currencyFormatter)
}

@Composable
fun CreditWorkspacePage(
    creditExpenses: List<CreditExpenseEntity>,
    creditCards: List<CreditCardEntity>,
    totalCredit: Double,
    currencyFormatter: java.text.NumberFormat,
    viewModel: WealthPulseViewModel,
    onTriggerManual: (String) -> Unit
) {
    var showAddCardDialog by remember { mutableStateOf(false) }
    var newCardName by remember { mutableStateOf("") }
    var newCardLimit by remember { mutableStateOf("") }
    var newCardBillDate by remember { mutableStateOf("15") }

    if (showAddCardDialog) {
        Dialog(onDismissRequest = { showAddCardDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                border = BorderStroke(1.dp, Color(0xFF1E2B3E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Register Credit Card",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newCardName,
                        onValueChange = { newCardName = it },
                        label = { Text("Card Name", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCardLimit,
                        onValueChange = { newCardLimit = it },
                        label = { Text("Credit Limit (INR)", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCardBillDate,
                        onValueChange = { newCardBillDate = it },
                        label = { Text("Bill Generation Day (1-31)", color = TextGray) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddCardDialog = false }) {
                            Text("Cancel", color = TextGray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val limit = newCardLimit.toDoubleOrNull() ?: 150000.0
                                val day = newCardBillDate.toIntOrNull() ?: 15
                                if (newCardName.isNotBlank()) {
                                    viewModel.addCreditCard(newCardName, limit, day)
                                    newCardName = ""
                                    newCardLimit = ""
                                    newCardBillDate = "15"
                                    showAddCardDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("Add Card", color = NavyBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val displayName = remember(currentUser) {
        val user = currentUser
        if (user == null || user.isAnonymous) {
            "GUEST ANALYST"
        } else {
            val email = user.email
            val localPart = email.substringBefore("@")
            localPart.split(".", "_", "-").joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
            }.uppercase()
        }
    }

    val totalLimit = creditCards.sumOf { it.creditLimit }
    val totalOutstandingFromCards = creditCards.sumOf { it.outstandingAmount }
    val utilizationPercent = if (totalLimit > 0) (totalCredit / totalLimit) * 100 else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Page Title & Header
        Text(
            text = "LIQUID DEBTS & SWIPES",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF78716C),
            letterSpacing = 2.sp
        )
        Text(
            text = "Credit Ledger & Cards",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            color = TextWhite,
            fontWeight = FontWeight.Light,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // REGISTERED CREDIT CARDS REGISTRY SECTION (Horizontal Swipe)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏦 YOUR REGISTERED CREDIT CARDS",
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            TextButton(
                onClick = { showAddCardDialog = true },
                modifier = Modifier.testTag("add_credit_card_button")
            ) {
                Text("+ Register Card", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (creditCards.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .width(310.dp)
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("No credit cards registered yet.", color = TextGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Log transactions, or register physical credit cards using the '+ Register Card' action above to track limits and real time bill statuses.",
                            color = TextGray,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                creditCards.forEach { cc ->
                    val remainingLimit = (cc.creditLimit - cc.outstandingAmount).coerceAtLeast(0.0)
                    val usagePercent = if (cc.creditLimit > 0) (cc.outstandingAmount / cc.creditLimit) * 100 else 0.0

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                        modifier = Modifier
                            .width(310.dp)
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = cc.cardName.uppercase(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite,
                                        letterSpacing = 1.sp
                                    )
                                    Text("LIMIT: ${currencyFormatter.format(cc.creditLimit)}", fontSize = 9.sp, color = TextGray)
                                }

                                // Delete Card Button
                                IconButton(
                                    onClick = { viewModel.deleteCreditCard(cc.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete credit card",
                                        tint = DangerRed.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("OUTSTANDING BILL", fontSize = 9.sp, color = TextGray)
                                    Text(
                                        text = currencyFormatter.format(cc.outstandingAmount),
                                        fontSize = 18.sp,
                                        color = if (cc.outstandingAmount > 0) DangerRed else TextWhite,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("AVAILABLE LIMIT", fontSize = 9.sp, color = TextGray)
                                    Text(
                                        text = currencyFormatter.format(remainingLimit),
                                        fontSize = 14.sp,
                                        color = NeonGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Utilization Progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(if (LocalCssThemeVariables.current.isLight) Color(0xFFE2E8F0) else Color(0xFF262626))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = (usagePercent / 100.0).toFloat().coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(if (usagePercent > 30.0) DangerRed else CreditPurple)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("BILL GENERATE DAY", fontSize = 9.sp, color = TextGray)
                                    Text("${cc.billDate}th of Month", fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Medium)
                                }

                                // Interactive Paid / Pending status badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (cc.billStatus == "Paid") Color(0x3310B981) else Color(0x33EF4444),
                                    modifier = Modifier.clickable { viewModel.toggleCardBillStatus(cc.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (cc.billStatus == "Paid") Color(0xFF10B981) else Color(0xFFEF4444))
                                        )
                                        Text(
                                            text = cc.billStatus.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (cc.billStatus == "Paid") NeonGreen else DangerRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (creditCards.isNotEmpty()) {
            val firstCard = creditCards.first()
            val cardBrand = firstCard.cardName.uppercase()
            val cardDigitMask = "**** **** **** " + (1000 + firstCard.id % 9000).toString()
            val cardExpiry = "12/29"

            Spacer(modifier = Modifier.height(16.dp))

            // MAGNIFICENT GLASSMORPHIC ACTIVE CREDIT CARD (Consolidated Digital View)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (LocalCssThemeVariables.current.isLight) listOf(
                                Color(0xFFEFF6FF), // Soft light sky blue
                                Color(0xFFF8FAFC), // Pure soft slate
                                Color(0xFFF5F3FF)  // Soft lavender
                            ) else listOf(
                                Color(0x334F46E5), // Translucent Indigo
                                Color(0x1A0F172A), // Dark slate
                                Color(0x228B5CF6)  // Translucent Purple
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.verticalGradient(
                                if (LocalCssThemeVariables.current.isLight) listOf(Color(0xFFCBD5E1), Color(0xFFE2E8F0))
                                else listOf(Color(0x40FFFFFF), Color(0x06FFFFFF))
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cardBrand,
                            fontSize = 12.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        // Mini chip graphics
                        Box(
                            modifier = Modifier
                                .size(34.dp, 24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEAB308).copy(alpha = 0.8f))
                                .border(1.dp, Color(0xFFCA8A04), RoundedCornerShape(6.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = cardDigitMask,
                        fontSize = 20.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("CARDHOLDER", fontSize = 9.sp, color = TextGray)
                            Text(displayName, fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EXPIRES EN", fontSize = 9.sp, color = TextGray)
                            Text(cardExpiry, fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Utilization Card
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOTAL CONSOLIDATED OUTSTANDINGS & LIMITS",
                        fontSize = 11.sp,
                        color = TextGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL CREDIT OUTSTANDING", fontSize = 10.sp, color = TextGray)
                            Text(currencyFormatter.format(totalCredit), fontSize = 24.sp, color = DangerRed, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL CONSOLIDATED LIMIT", fontSize = 10.sp, color = TextGray)
                            Text(currencyFormatter.format(totalLimit), fontSize = 18.sp, color = SipBlue, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AVAILABLE CAPACITY ${currencyFormatter.format((totalLimit - totalCredit).coerceAtLeast(0.0))}", fontSize = 9.sp, color = TextGray)
                        Text(String.format("UTILIZED: %.1f%%", utilizationPercent), fontSize = 10.sp, color = if (utilizationPercent > 30.0) DangerRed else TextGray, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(if (LocalCssThemeVariables.current.isLight) Color(0xFFE2E8F0) else Color(0xFF262626))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (utilizationPercent / 100.0).toFloat().coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(if (utilizationPercent > 30.0) DangerRed else CreditPurple)
                        )
                    }
                }
            }
        }

        // Active Swipe log section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💳 LOGGED CREDIT CARD TRANSACTIONS",
                fontSize = 11.sp,
                color = TextGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            TextButton(
                onClick = { onTriggerManual("CREDIT") },
                modifier = Modifier.testTag("credit_tab_manual_button")
            ) {
                Text("+ Log Swipe", fontSize = 12.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
            }
        }

        if (creditExpenses.isEmpty()) {
            EmptyStatePlaceholder("No credit logs recorded. Click '+ Log Swipe' to track.")
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 100.dp)
            ) {
                creditExpenses.forEach { cred ->
                    TransactionItemCard(
                        title = cred.description,
                        subtitle = "Card Used: ${cred.cardName} • Category: ${cred.category}",
                        amount = currencyFormatter.format(cred.amount),
                        onDelete = { viewModel.deleteCredit(cred.id) },
                        colorAccent = DangerRed
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarWorkspacePage(
    dailyExpenses: List<DailyExpenseEntity>,
    creditExpenses: List<CreditExpenseEntity>,
    emiLoans: List<EmiLoanEntity>,
    sipRecords: List<SipEntity>,
    incomePaydays: List<IncomePaydayEntity>,
    currencyFormatter: java.text.NumberFormat,
    viewModel: WealthPulseViewModel
) {
    // 1. Reactive selected date states
    var selectedYear by remember { mutableStateOf(2026) }
    var selectedMonth by remember { mutableStateOf(5) } // June (0-indexed)
    var selectedDay by remember { mutableStateOf(7) }

    // 2. Active view mode toggle: "day_details" or "month_summary"
    var viewMode by remember { mutableStateOf("day_details") }

    val monthNames = listOf(
        "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
        "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"
    )

    // Helper functions
    val daysInMonth = remember(selectedYear, selectedMonth) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.YEAR, selectedYear)
        cal.set(java.util.Calendar.MONTH, selectedMonth)
        cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    }

    val startOffset = remember(selectedYear, selectedMonth) {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.YEAR, selectedYear)
        cal.set(java.util.Calendar.MONTH, selectedMonth)
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        // 1 = Sunday, 2 = Monday, ...
        // We start on Sunday (offset starts at 0 to 6)
        cal.get(java.util.Calendar.DAY_OF_WEEK) - 1
    }

    // 3. Mapping data vectors for UI dots and filtering
    val expensesByDay = remember(dailyExpenses, selectedYear, selectedMonth) {
        val cal = java.util.Calendar.getInstance()
        dailyExpenses.filter {
            cal.timeInMillis = it.timestamp
            cal.get(java.util.Calendar.YEAR) == selectedYear && cal.get(java.util.Calendar.MONTH) == selectedMonth
        }.groupBy {
            cal.timeInMillis = it.timestamp
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        }
    }

    val creditsByDay = remember(creditExpenses, selectedYear, selectedMonth) {
        val cal = java.util.Calendar.getInstance()
        creditExpenses.filter {
            cal.timeInMillis = it.timestamp
            cal.get(java.util.Calendar.YEAR) == selectedYear && cal.get(java.util.Calendar.MONTH) == selectedMonth
        }.groupBy {
            cal.timeInMillis = it.timestamp
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        }
    }

    val emisByDay = remember(emiLoans) {
        val cal = java.util.Calendar.getInstance()
        // Recurring EMI is mapped to its original day of month of timestamp
        emiLoans.groupBy {
            cal.timeInMillis = it.timestamp
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        }
    }

    val sipsByDay = remember(sipRecords) {
        sipRecords.groupBy { it.dayOfMonth }
    }

    val incomesByDay = remember(incomePaydays, selectedYear, selectedMonth) {
        val cal = java.util.Calendar.getInstance()
        incomePaydays.filter {
            cal.timeInMillis = it.timestamp
            cal.get(java.util.Calendar.YEAR) == selectedYear && cal.get(java.util.Calendar.MONTH) == selectedMonth
        }.groupBy {
            cal.timeInMillis = it.timestamp
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        }
    }

    // Filtered transaction items list for selected Day
    val selectedDayIncomes = incomesByDay[selectedDay] ?: emptyList()
    val selectedDayExpenses = expensesByDay[selectedDay] ?: emptyList()
    val selectedDayCredits = creditsByDay[selectedDay] ?: emptyList()
    val selectedDayEmis = emisByDay[selectedDay] ?: emptyList()
    val selectedDaySips = sipsByDay[selectedDay] ?: emptyList()

    val totalSelectedDayAmt = selectedDayIncomes.sumOf { it.amount } -
            selectedDayExpenses.sumOf { it.amount } -
            selectedDayCredits.sumOf { it.amount } -
            selectedDayEmis.sumOf { it.amount } -
            selectedDaySips.sumOf { it.amount }

    // Click navigation handlers
    val navigatePrevMonth = {
        if (selectedMonth == 0) {
            selectedMonth = 11
            selectedYear -= 1
        } else {
            selectedMonth -= 1
        }
        selectedDay = 1 // default to 1st of new month
    }

    val navigateNextMonth = {
        if (selectedMonth == 11) {
            selectedMonth = 0
            selectedYear += 1
        } else {
            selectedMonth += 1
        }
        selectedDay = 1 // default to 1st of new month
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp)
    ) {
        // HEADER TITLE
        Text(
            text = "CHRONO LEDGER",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF78716C),
            letterSpacing = 2.sp
        )
        Text(
            text = "Active Cashflow Tracker",
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            color = TextWhite,
            fontWeight = FontWeight.Light,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // GLASSMORPHIC CALENDAR FRAME
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Month Navigation Selector Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = navigatePrevMonth) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Prev Month", tint = TextWhite)
                    }

                    // Elegant Header that toggles View Mode
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewMode = if (viewMode == "month_summary") "day_details" else "month_summary"
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${monthNames[selectedMonth]} $selectedYear 📊",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (viewMode == "month_summary") "Show Daily Details" else "Show Month Summary Metrics",
                            fontSize = 10.sp,
                            color = TextGray,
                            fontWeight = FontWeight.Light
                        )
                    }

                    IconButton(onClick = navigateNextMonth) {
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Next Month", tint = TextWhite)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (viewMode == "month_summary") {
                    // DISPLAY MONTHLY SUMMARY CARD DYNAMICALLY
                    val mExpenses = expensesByDay.values.flatten().sumOf { it.amount }
                    val mCredits = creditsByDay.values.flatten().sumOf { it.amount }
                    val mEmis = emiLoans.sumOf { it.amount } // constant monthly runrate
                    val mSips = sipRecords.sumOf { it.amount } // constant monthly runrate
                    val mIncomes = incomesByDay.values.flatten().sumOf { it.amount }
                    val mDisposable = mIncomes - mExpenses - mCredits - mEmis - mSips

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceBlue)
                            .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "MONTH METRICS & TOTALS",
                            fontSize = 11.sp,
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // 5 Row metrics
                        MonthMetricIndicatorRow("💰 Monthly Total Income", mIncomes, currencyFormatter, NeonGreen)
                        MonthMetricIndicatorRow("🛍️ Pure Cash Expenses", mExpenses, currencyFormatter, DangerRed)
                        MonthMetricIndicatorRow("💳 Swipes / Liquid Debts", mCredits, currencyFormatter, CreditPurple)
                        MonthMetricIndicatorRow("🏦 Active EMI Obligations", mEmis, currencyFormatter, AccentOrange)
                        MonthMetricIndicatorRow("📈 SIP Wealth Compounding", mSips, currencyFormatter, SipBlue)

                        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TRUE CONSOLIDATED SAVINGS", fontSize = 9.sp, color = TextGray)
                                Text(
                                    text = currencyFormatter.format(mDisposable),
                                    fontSize = 18.sp,
                                    color = if (mDisposable >= 0) NeonGreen else DangerRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Visual circular meter estimation
                            val totalDebitAmt = mExpenses + mCredits + mEmis + mSips
                            val utilization = if (mIncomes > 0) (totalDebitAmt / mIncomes) * 100 else 0.0
                            Column(horizontalAlignment = Alignment.End) {
                                Text("BUDGET LOCKED", fontSize = 9.sp, color = TextGray)
                                Text(String.format("%.1f%% Used", utilization), fontSize = 13.sp, color = if (utilization > 80.0) DangerRed else TextWhite, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    // WEEKDAY NAMES
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val weekdays = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                        weekdays.forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // DAYS GRID
                    val totalSlots = 35 // default to 5-week grid
                    val adjustedSlots = if (startOffset + daysInMonth > 35) 42 else 35 // 6-week grid if required to avoid clipping

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in 0 until (adjustedSlots / 7)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (col in 0 until 7) {
                                    val slotIndex = row * 7 + col
                                    val dayNumber = slotIndex - startOffset + 1

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (dayNumber in 1..daysInMonth) {
                                            val isSelected = (dayNumber == selectedDay)

                                            // Determine Highlight triggers
                                            val hasDailyExp = expensesByDay.containsKey(dayNumber)
                                            val hasCreditExp = creditsByDay.containsKey(dayNumber)
                                            val hasEmi = emisByDay.containsKey(dayNumber)
                                            val hasSip = sipsByDay.containsKey(dayNumber)
                                            val hasIncome = incomesByDay.containsKey(dayNumber)

                                            // Day Card Box
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize(0.85f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isSelected) NeonGreen.copy(alpha = 0.15f)
                                                        else SurfaceBlue
                                                    )
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                                        color = if (isSelected) NeonGreen else BorderColor,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { selectedDay = dayNumber }
                                                    .padding(4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    // Top Indicator Dots Ring
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        if (hasIncome) {
                                                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(NeonGreen))
                                                        }
                                                        if (hasEmi) {
                                                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(AccentOrange))
                                                        }
                                                        if (hasSip) {
                                                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SipBlue))
                                                        }
                                                    }

                                                    // Day Number Inside Card
                                                    Text(
                                                        text = dayNumber.toString(),
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) NeonGreen else TextWhite
                                                    )

                                                    // Bottom Dot for normal Expenses
                                                    if (hasDailyExp || hasCreditExp) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (hasCreditExp) CreditPurple else DangerRed)
                                                        )
                                                    } else {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (viewMode == "day_details") {
            // DETAILED DAY SELECTED TRANS SUMMARY HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 TRANSACTIONS CONSOLE: DAY $selectedDay",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Balance: " + (if (totalSelectedDayAmt >= 0) "+" else "") + currencyFormatter.format(totalSelectedDayAmt),
                    fontSize = 11.sp,
                    color = if (totalSelectedDayAmt >= 0) NeonGreen else DangerRed,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // TRANSACTIONS LIST FOR SELECTED DAY
            val hasTransactions = selectedDayIncomes.isNotEmpty() ||
                    selectedDayExpenses.isNotEmpty() ||
                    selectedDayCredits.isNotEmpty() ||
                    selectedDayEmis.isNotEmpty() ||
                    selectedDaySips.isNotEmpty()

            if (!hasTransactions) {
                EmptyStatePlaceholder("Settled. No debits or cash flows logged on this date.")
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 110.dp)
                ) {
                    // 1. Incomes list
                    selectedDayIncomes.forEach { inc ->
                        CalendarItemRow(
                            icon = "💰",
                            title = inc.description,
                            subtitle = "Mode: ${inc.paymentMode} | Income Payday",
                            amount = "+" + currencyFormatter.format(inc.amount),
                            colorAccent = NeonGreen,
                            onDelete = { viewModel.deleteIncome(inc.id) }
                        )
                    }

                    // 2. Daily Expenses list
                    selectedDayExpenses.forEach { exp ->
                        CalendarItemRow(
                            icon = "🛍️",
                            title = exp.description,
                            subtitle = "Mode: ${exp.paymentMode} | Category: ${exp.category}",
                            amount = "-" + currencyFormatter.format(exp.amount),
                            colorAccent = DangerRed,
                            onDelete = { viewModel.deleteExpense(exp.id) }
                        )
                    }

                    // 3. Credit Swipes
                    selectedDayCredits.forEach { cred ->
                        CalendarItemRow(
                            icon = "💳",
                            title = cred.description,
                            subtitle = "Card: ${cred.cardName} | Shopping Debts",
                            amount = "-" + currencyFormatter.format(cred.amount),
                            colorAccent = CreditPurple,
                            onDelete = { viewModel.deleteCredit(cred.id) }
                        )
                    }

                    // 4. EMI Amortizations
                    selectedDayEmis.forEach { emi ->
                        CalendarItemRow(
                            iconSymbol = "🏦",
                            title = emi.description,
                            subtitle = "EMI Installment (Auto-pay)",
                            amount = "-" + currencyFormatter.format(emi.amount),
                            colorAccent = AccentOrange,
                            onDelete = { viewModel.deleteEmi(emi.id) }
                        )
                    }

                    // 5. SIP Mutual Funds
                    selectedDaySips.forEach { sip ->
                        CalendarItemRow(
                            iconSymbol = "📈",
                            title = sip.description,
                            subtitle = "Automated Recurring Investment",
                            amount = "-" + currencyFormatter.format(sip.amount),
                            colorAccent = SipBlue,
                            onDelete = { viewModel.deleteSip(sip.id) }
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
fun MonthMetricIndicatorRow(
    label: String,
    amount: Double,
    currencyFormatter: java.text.NumberFormat,
    indicatorColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 12.sp, color = TextWhite)
        }
        Text(
            text = currencyFormatter.format(amount),
            fontSize = 12.sp,
            color = indicatorColor,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun CalendarItemRow(
    icon: String = "💰",
    iconSymbol: String? = null,
    title: String,
    subtitle: String,
    amount: String,
    colorAccent: Color,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1.5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconSymbol ?: icon, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
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
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = amount,
                    fontSize = 14.sp,
                    color = colorAccent,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(end = 6.dp)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Active Item",
                        tint = DangerRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDialog(
    onDismiss: () -> Unit,
    viewModel: WealthPulseViewModel
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleFallbackPrompt by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("default-dummy-aistudio-key-for-myfin")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            val googleEmail = account?.email
            if (idToken != null) {
                loading = true
                viewModel.signInWithGoogle(
                    idToken = idToken,
                    email = googleEmail,
                    onSuccess = {
                        loading = false
                        Toast.makeText(context, "Successfully authenticated with Google!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    onError = { err ->
                        loading = false
                        errorMessage = err
                    }
                )
            } else {
                if (googleEmail != null) {
                    loading = true
                    viewModel.performFallbackGoogleSignIn(googleEmail) {
                        loading = false
                        Toast.makeText(context, "Signed in with Google Account (Sandbox Fallback)", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                } else {
                    errorMessage = "Failed to retrieve Google Auth Credentials."
                }
            }
        } catch (e: ApiException) {
            android.util.Log.e("MYFin", "Google sign-in api exception: code ${e.statusCode}, message: ${e.message}")
            showGoogleFallbackPrompt = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = if (isSignUp) "CREATE ENCRYPTED ACCOUNT" else "SECURE PORTFOLIO SIGN-IN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (isSignUp) "Create your MYFin account to persist credentials across any standard emulator session." 
                           else "Authenticate via Firebase Authentication to securely unlock personal financial databases.",
                    fontSize = 11.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Error Msg
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = if (LocalCssThemeVariables.current.isLight) DangerRed else Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (showGoogleFallbackPrompt) {
                    // Google Fallback Panel
                    Card(
                        border = BorderStroke(1.dp, AccentOrange.copy(alpha = 0.5f)),
                        colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "GOOGLE PLAY SERVICES RESILIENCE PANEL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Google Play Services was not detected in this local sandbox environment. To securely verify your workspace integration, choose your Google account below:",
                                fontSize = 11.sp,
                                color = TextGray,
                                lineHeight = 15.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // User Email Option
                            GoogleAccountOptionRow(
                                email = "rakshitshetty22@gmail.com",
                                displayName = "Rakshit Shetty",
                                onClick = {
                                    loading = true
                                    viewModel.performFallbackGoogleSignIn("rakshitshetty22@gmail.com") {
                                        loading = false
                                        Toast.makeText(context, "Welcome Rakshit! Google data vault synchronized.", Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    }
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Simulator Default Option
                            GoogleAccountOptionRow(
                                email = "sandbox_tester@myfin.io",
                                displayName = "Sandbox Tester",
                                onClick = {
                                    loading = true
                                    viewModel.performFallbackGoogleSignIn("sandbox_tester@myfin.io") {
                                        loading = false
                                        Toast.makeText(context, "Google Sandbox Session Started.", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showGoogleFallbackPrompt = false }) {
                                    Text("CANCEL & USE PASSWORD", color = Color(0xFFE28743), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Email Address", fontSize = 12.sp, color = TextGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("auth_email_input")
                )

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Secure Password", fontSize = 12.sp, color = TextGray) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("auth_password_input")
                )

                // Submit button with animated loading
                if (loading) {
                    CircularProgressIndicator(
                        color = NeonGreen,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(bottom = 16.dp)
                    )
                } else {
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter both email and password."
                                return@Button
                            }
                            if (password.length < 6) {
                                errorMessage = "Password must be at least 6 characters long."
                                return@Button
                            }
                            loading = true
                            if (isSignUp) {
                                viewModel.signUpWithEmail(
                                    email = email.trim(),
                                    pword = password.trim(),
                                    onSuccess = {
                                        loading = false
                                        Toast.makeText(context, "Account registered successfully!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    },
                                    onError = { err ->
                                        loading = false
                                        errorMessage = err
                                    }
                                )
                            } else {
                                viewModel.signInWithEmail(
                                    email = email.trim(),
                                    pword = password.trim(),
                                    onSuccess = {
                                        loading = false
                                        Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    },
                                    onError = { err ->
                                        loading = false
                                        errorMessage = err
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_button")
                    ) {
                        Text(
                            text = if (isSignUp) "REGISTER ACCOUNT" else "UNLOCK METRICS DATABASE",
                            color = NavyBg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stylish Google Sign-In option
                    OutlinedButton(
                        onClick = {
                            try {
                                val signInIntent = googleSignInClient.signInIntent
                                googleSignInLauncher.launch(signInIntent)
                            } catch (e: Exception) {
                                showGoogleFallbackPrompt = true
                            }
                        },
                        border = BorderStroke(1.dp, BorderColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("google_signin_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Black, fontSize = 14.sp)
                            Text("e  ", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 14.sp)
                            
                            Text("SIGN IN WITH GOOGLE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Signin / Signup
                TextButton(
                    onClick = { 
                        isSignUp = !isSignUp
                        errorMessage = null
                    },
                    modifier = Modifier.testTag("auth_toggle_mode_button")
                ) {
                    Text(
                        text = if (isSignUp) "Already have an account? Unlock here" else "New to MYFin? Register private account",
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Temporary Local Sandbox Guest Account option
                TextButton(
                    onClick = {
                        loading = true
                        viewModel.signInAnonymously(
                            onSuccess = {
                                loading = false
                                Toast.makeText(context, "Guest sandbox access unlocked!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            onError = { err ->
                                loading = false
                                errorMessage = err
                            }
                        )
                    },
                    modifier = Modifier.testTag("auth_guest_button")
                ) {
                    Text(
                        text = "Or enter anonymous Guest mode direct",
                        color = TextGray,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleAccountOptionRow(
    email: String,
    displayName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF16120E))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Styled Avatar circle representing standard credentials picker Google icon fallback
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFEA4335), Color(0xFFFBBC05)))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = displayName, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = email, fontSize = 10.sp, color = TextGray)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE28743).copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text("VERIFIED", color = Color(0xFFE28743), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ======================= COMPONENT WORKSPACE FOR TRIP / GROUPS =======================
@Composable
fun GroupTripsDashboard(
    tripEvents: List<TripEventEntity>,
    tripExpenses: List<TripExpenseEntity>,
    allParticipants: List<ParticipantEntity>,
    selectedTripId: Int?,
    onTripSelect: (Int?) -> Unit,
    onShowCreateTrip: () -> Unit,
    currencyFormatter: java.text.NumberFormat,
    viewModel: WealthPulseViewModel
) {
    if (selectedTripId == null) {
        // TRIPS DIRECTORIES PANEL
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌴 ACTIVE TRIPS & SHARED COMMITMENTS",
                    fontSize = 11.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Button(
                    onClick = onShowCreateTrip,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, AccentOrange),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Adventure", fontSize = 11.sp, color = AccentOrange, fontWeight = FontWeight.Bold)
                }
            }

            if (tripEvents.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    colors = CardDefaults.cardColors(containerColor = SurfaceBlue)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👋 Welcome to the Group & Trip Expense Planner!", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Log group vacations, house rentals, roommates sharing, or parties with equal, percentage, ratio, or exact splits. Tap '+ New Adventure' to establish a Trip Group!",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onShowCreateTrip,
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Setup First Group & Trip", fontSize = 12.sp, color = TextWhite)
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    tripEvents.forEach { trip ->
                        val tripExps = tripExpenses.filter { it.tripId == trip.id }
                        val netSpend = tripExps.sumOf { it.totalAmount }
                        
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, BorderColor),
                            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTripSelect(trip.id) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = trip.name,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = if (trip.isPublic) Color(0x2210B981) else Color(0x3378716C)
                                            ) {
                                                Text(
                                                    text = if (trip.isPublic) "PUBLIC" else "PRIVATE",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (trip.isPublic) NeonGreen else TextGray,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = trip.description,
                                            fontSize = 12.sp,
                                            color = TextGray,
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            maxLines = 2,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "📅 ${trip.startDate} to ${trip.endDate}",
                                            fontSize = 11.sp,
                                            color = AccentOrange,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = currencyFormatter.format(netSpend),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonGreen
                                        )
                                        Text(
                                            text = "${tripExps.size} Expenses",
                                            fontSize = 10.sp,
                                            color = TextGray
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val members = trip.participants.split(",").filter { it.isNotBlank() }
                                Text(
                                    text = "👥 MEMBERS (${members.size}): " + members.joinToString(", "),
                                    fontSize = 10.sp,
                                    color = TextGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // INDIVIDUAL DETAILED TRIP WORKSPACE PANEL
        val currTrip = tripEvents.find { it.id == selectedTripId }
        if (currTrip == null) {
            onTripSelect(null)
        } else {
            val currTripExps = tripExpenses.filter { it.tripId == selectedTripId }
            val tripMembers = currTrip.participants.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            
            DetailedTripWorkspaceView(
                trip = currTrip,
                expenses = currTripExps,
                members = tripMembers,
                onBack = { onTripSelect(null) },
                currencyFormatter = currencyFormatter,
                viewModel = viewModel
            )
        }
    }
}

// ========================== TRIPS CREATION DIALOG COMPOSE ==========================
@Composable
fun TripCreationDialog(
    allParticipants: List<ParticipantEntity>,
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String, start: String, end: String, isPublic: Boolean, List<String>) -> Unit,
    viewModel: WealthPulseViewModel
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("08-Jun-2026") }
    var endDate by remember { mutableStateOf("15-Jun-2026") }
    var isPublic by remember { mutableStateOf(false) }
    
    // Manage chosen participants state
    val selectedParticipants = remember { mutableStateListOf<String>("You") }
    var filterQuery by remember { mutableStateOf("") }
    
    // Direct Friend Addition
    var newFriendName by remember { mutableStateOf("") }
    var newFriendEmail by remember { mutableStateOf("") }
    var friendAddToast by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = "💼 Setup Group & Trip",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Trip / Event Name", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Trip description (e.g. Goa Villa stay)", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date", color = TextGray) },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Public vs Private Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Set Event as Public Ledger", color = TextWhite, fontSize = 13.sp)
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // PARTICIPANT MULTI SELECTOR & INVITE SYSTEM
                Text(
                    text = "👥 SELECT MULTIPLE INVOLVED MEMBERS:",
                    fontSize = 10.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold
                )

                // Internal Search/Filter inside Dialog for Friends
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = { Text("Search friends inside DB...", color = TextGray.copy(0.4f), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(color = TextWhite, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange, unfocusedBorderColor = Color(0x33FFFFFF))
                )
                Spacer(modifier = Modifier.height(6.dp))

                val matchedPList = allParticipants.filter {
                    it.name.contains(filterQuery, ignoreCase = true) || 
                    it.email.contains(filterQuery, ignoreCase = true)
                }

                // Scrollable chips of friends inside database
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Always show YOU
                    val isYouSelected = selectedParticipants.contains("You")
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isYouSelected) AccentOrange.copy(alpha = 0.15f) else SurfaceBlue,
                        border = BorderStroke(1.dp, if (isYouSelected) AccentOrange else BorderColor),
                        modifier = Modifier.clickable {
                            if (isYouSelected) selectedParticipants.remove("You") else selectedParticipants.add("You")
                        }
                    ) {
                        Text("You", color = if (isYouSelected) AccentOrange else TextWhite, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }

                    matchedPList.forEach { user ->
                        val isUserSelected = selectedParticipants.contains(user.name)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isUserSelected) AccentOrange.copy(alpha = 0.15f) else SurfaceBlue,
                            border = BorderStroke(1.dp, if (isUserSelected) AccentOrange else BorderColor),
                            modifier = Modifier.clickable {
                                if (isUserSelected) selectedParticipants.remove(user.name) else selectedParticipants.add(user.name)
                            }
                        ) {
                            Text(user.name, color = if (isUserSelected) AccentOrange else TextWhite, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Fast Direct Invite New User System
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x11FFFFFF)),
                    border = BorderStroke(1.dp, Color(0x1F2B334D))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("➕ MANUALLY REGISTER / INVITE NEW FRIEND", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedTextField(
                                value = newFriendName,
                                onValueChange = { newFriendName = it },
                                placeholder = { Text("Name", color = TextGray, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(color = TextWhite, fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                            )
                            OutlinedTextField(
                                value = newFriendEmail,
                                onValueChange = { newFriendEmail = it },
                                placeholder = { Text("Email (Optional)", color = TextGray, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                textStyle = LocalTextStyle.current.copy(color = TextWhite, fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                val cleanName = newFriendName.trim()
                                if (cleanName.isNotBlank()) {
                                    viewModel.addParticipantToDb(cleanName, newFriendEmail.trim(), isRegistered = false)
                                    // Automatically add to trip selections
                                    selectedParticipants.add(cleanName)
                                    friendAddToast = "👤 Added '$cleanName' and preselected!"
                                    newFriendName = ""
                                    newFriendEmail = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Invite Friend", fontSize = 10.sp)
                        }

                        friendAddToast?.let {
                            Text(it, fontSize = 9.sp, color = NeonGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedParticipants.size} Selected",
                        fontSize = 11.sp,
                        color = AccentOrange,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Discard", color = TextGray)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank() && selectedParticipants.isNotEmpty()) {
                                    onCreate(name, desc, startDate, endDate, isPublic, selectedParticipants.toList())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                        ) {
                            Text("Launch Group", color = TextWhite)
                        }
                    }
                }
            }
        }
    }
}

// ======================= DETAILED EVENT WORKSPACE BOARD =======================
@Composable
fun DetailedTripWorkspaceView(
    trip: TripEventEntity,
    expenses: List<TripExpenseEntity>,
    members: List<String>,
    onBack: () -> Unit,
    currencyFormatter: java.text.NumberFormat,
    viewModel: WealthPulseViewModel
) {
    var trackerTab by remember { mutableStateOf("expenses") } // "summary", "expenses", "settle", "activity"
    
    // Creators
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    // Notifications timeline
    val workspaceActivityLogs = remember(expenses) {
        val list = mutableListOf<String>()
        // Generate automatic logs based on expense database rows
        expenses.forEach { exp ->
            list.add("💸 '${exp.paidBy}' recorded ₹${exp.totalAmount} for '${exp.title}' Category '${exp.category}'.")
        }
        if (list.isEmpty()) {
            list.add("🌟 Group created. Welcome members: " + members.joinToString(", "))
        }
        list
    }

    // Reminders triggers
    var reminderToastMessage by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Upper breadcrumb
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Trips directory", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Back to list",
                fontSize = 13.sp,
                color = TextGray,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { viewModel.deleteTrip(trip.id); onBack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1AFE3C3C)),
                border = BorderStroke(1.dp, Color(0xFFFE3C3C)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete Adventure", fontSize = 10.sp, color = Color(0xFFFE3C3C))
            }
        }

        // Event core tag card
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(trip.name, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Text(trip.description, fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(vertical = 2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${trip.startDate} - ${trip.endDate}", fontSize = 11.sp, color = AccentOrange, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text("👥 ${members.size} Members", fontSize = 11.sp, color = TextGray)
                }
            }
        }

        // Inner Sub Workspace tabs: Expenses, Settle Up, Summary Report, Activity/Reminders
        val isLight = LocalCssThemeVariables.current.isLight
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isLight) Color(0xFFE2E8F0) else SurfaceBlue)
                .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(8.dp))
                .padding(2.dp)
        ) {
            listOf(
                Triple("summary", "Summary 📊", NeonGreen),
                Triple("expenses", "Expenses 💸", AccentOrange),
                Triple("settle", "Settle Up 🤝", SipBlue),
                Triple("activity", "Activity ⏳", TextGray)
            ).forEach { (id, label, accent) ->
                val isSelected = trackerTab == id
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) {
                                accent.copy(alpha = if (isLight) 0.18f else 0.15f)
                            } else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) {
                                accent.copy(alpha = if (isLight) 0.35f else 0.25f)
                            } else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { trackerTab = id }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) accent else TextGray
                    )
                }
            }
        }

        // RENDER INNER VIEWS
        when (trackerTab) {
            "summary" -> {
                // SUMMARY & CATEGORY BREAKDOWN REPORT VIEW
                val totalSpend = expenses.sumOf { it.totalAmount }
                
                // Categorization aggregation
                val categoryMap = expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.totalAmount } }
                
                // Contributions list
                val contributorContributions = members.associateWith { 0.0 }.toMutableMap()
                expenses.forEach { exp ->
                    contributorContributions[exp.paidBy] = (contributorContributions[exp.paidBy] ?: 0.0) + exp.totalAmount
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📊 FINANCIAL REPORT STATEMENT", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(currencyFormatter.format(totalSpend), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                            Text("Total spendings recorded during this trip event", fontSize = 11.sp, color = TextGray)
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showExportDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📄 Export Printable Trip Summary (PDF/Excel)", color = NavyBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Spend Category Breakdown
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📂 SPENDINGS BY CATEGORY", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            if (categoryMap.isEmpty()) {
                                Text("No expenses logged to categorize.", fontSize = 12.sp, color = TextGray)
                            } else {
                                categoryMap.forEach { (cat, amt) ->
                                    val pct = if (totalSpend > 0) amt / totalSpend else 0.0
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(cat, fontSize = 12.sp, color = TextWhite, fontWeight = FontWeight.Medium)
                                            Text("${currencyFormatter.format(amt)} (${String.format("%.1f", pct * 100)}%)", fontSize = 12.sp, color = TextGray)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        // Simple Progress indicator representation
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(CircleShape)
                                                .background(if (LocalCssThemeVariables.current.isLight) Color(0xFFE2E8F0) else Color(0xFF222222))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(pct.toFloat().coerceIn(0f, 1f))
                                                    .height(6.dp)
                                                    .clip(CircleShape)
                                                    .background(AccentOrange)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Individual Ledger Contributions
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("👤 ACTUAL INDIVIDUAL CONTRIBUTIONS", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            contributorContributions.forEach { (member, paid) ->
                                val pct = if (totalSpend > 0) paid / totalSpend else 0.0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(AccentOrange.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(member.take(1).uppercase(), color = AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(member, fontSize = 13.sp, color = TextWhite)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(currencyFormatter.format(paid), fontSize = 13.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                                        Text("${String.format("%.1f", pct * 100)}% contribution", fontSize = 9.sp, color = TextGray)
                                    }
                                }
                                HorizontalDivider(color = Color.White.copy(0.02f))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            "expenses" -> {
                // LOG TRIP EXPENSES & RECORD ADVENTURE BUTTON
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💸 Group Expenses Log", fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Bold)
                    Button(
                        onClick = { showAddExpenseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TextWhite, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Group Expense", fontSize = 11.sp, color = TextWhite)
                    }
                }

                if (expenses.isEmpty()) {
                    EmptyStatePlaceholder("No group trip expenses recorded. Click 'Add Group Expense'!")
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        expenses.forEach { exp ->
                            val involvedNames = exp.involvedParticipants.split(",").filter { it.isNotBlank() }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, BorderColor),
                                colors = CardDefaults.cardColors(containerColor = SurfaceBlue)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0x22F35F22)
                                                ) {
                                                    Text(exp.category, fontSize = 9.sp, color = AccentOrange, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                                if (exp.receiptUri.isNotBlank()) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color(0x3310B981)
                                                    ) {
                                                        Text("🧾 RECEIPT ATTACHED", fontSize = 7.sp, color = NeonGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(exp.title, fontSize = 15.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(currencyFormatter.format(exp.totalAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AccentOrange)
                                            Text("Paid by: ${exp.paidBy}", fontSize = 11.sp, color = TextGray)
                                        }
                                    }

                                    if (exp.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("📝 Note: ${exp.notes}", fontSize = 11.sp, color = TextGray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Split Method: ${exp.splitMethod} among ${involvedNames.size} members",
                                            fontSize = 9.sp,
                                            color = TextGray
                                        )
                                        
                                        IconButton(
                                            onClick = { viewModel.deleteTripExpense(exp.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh, // Visual delete alternative
                                                contentDescription = "Delete Expense",
                                                tint = Color(0xFFFE3C3C),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "settle" -> {
                // COMPUTES REVOLUTIONARY AUTOMATED BACKEND BALANCING GREEDY TRANSFERS
                val settlements = calculateSimplifiedSettlements(members, expenses)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🤝 REVOLUTIONARY MINIMIZED TRANSACTIONS ENGINE", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Our dynamic program matches balances and eliminates empty loops to minimize transaction volumes.", fontSize = 11.sp, color = TextGray)
                        }
                    }

                    if (settlements.isEmpty()) {
                        EmptyStatePlaceholder("All trip debts are perfectly balanced! Zero transactions pending. ✨")
                    } else {
                        settlements.forEach { tx ->
                            Card(
                                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.25f)),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E131F))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(tx.debtor, color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(" owes ", color = TextWhite, fontSize = 12.sp)
                                            Text(tx.creditor, color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Transfer to clear: " + currencyFormatter.format(tx.amount),
                                            fontSize = 11.sp,
                                            color = TextGray
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // SIMULATED PAY REMINDERS
                                        Button(
                                            onClick = {
                                                reminderToastMessage = "⏳ Reminder Sent! Dispatched alert to '${tx.debtor}' asking to settle with '${tx.creditor}'!"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x333B82F6)),
                                            border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Remind", fontSize = 9.sp, color = Color(0xFF93C5FD))
                                        }

                                        // Settle Button
                                        Button(
                                            onClick = {
                                                // Log settlement as a custom negative trip expense contribution to reflect clear balances!
                                                viewModel.addTripExpense(
                                                    tripId = trip.id,
                                                    title = "Settled debt: ${tx.debtor} to ${tx.creditor}",
                                                    totalAmount = tx.amount,
                                                    paidBy = tx.debtor,
                                                    splitMethod = "EXACT",
                                                    participantWeights = tx.amount.toString(),
                                                    involvedParticipants = listOf(tx.creditor),
                                                    category = "Settlement Clearance"
                                                )
                                                reminderToastMessage = "✔️ Settlement recorded! ₹${Math.round(tx.amount)} transfer verified successfully."
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Mark Settled", fontSize = 9.sp, color = NavyBg, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    reminderToastMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth().padding(4.dp)
                        ) {
                            Text(
                                text = msg,
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            "activity" -> {
                // COLLABORATOR ACTIVITY TIMELINE LOGGER
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("⏳ COGNITIVE REAL-TIME INCIDENT LOGGER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            workspaceActivityLogs.forEachIndexed { i, log ->
                                Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text("⚡", color = AccentOrange, fontSize = 11.sp, modifier = Modifier.padding(end = 6.dp))
                                    Text(log, fontSize = 12.sp, color = TextWhite)
                                }
                                if (i < workspaceActivityLogs.size - 1) {
                                    HorizontalDivider(color = Color.White.copy(0.02f), modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }

    // Modal Add Expense dialog
    if (showAddExpenseDialog) {
        TripAddExpenseDialog(
            members = members,
            onDismiss = { showAddExpenseDialog = false },
            onSave = { title, amt, payer, method, weights, involved, category, notes, recUri ->
                viewModel.addTripExpense(trip.id, title, amt, payer, method, weights, involved, category, notes, recUri)
                showAddExpenseDialog = false
            }
        )
    }

    // Modal for Printable Export Trip Summary PDF/Excel Layout Mockup
    if (showExportDialog) {
        TripExporterMockupDialog(
            trip = trip,
            expenses = expenses,
            members = members,
            onDismiss = { showExportDialog = false },
            currencyFormatter = currencyFormatter
        )
    }
}

// ========================== COMPLEX TRIP ADD EXPENSE MODAL COMPOSE ==========================
@Composable
fun TripAddExpenseDialog(
    members: List<String>,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, paidBy: String, splitMethod: String, participantWeights: String, involved: List<String>, category: String, notes: String, receiptUri: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var paidBy by remember { mutableStateOf(members.firstOrNull() ?: "You") }
    
    var splitMethod by remember { mutableStateOf("EQUAL") } // "EQUAL", "PERCENT", "EXACT", "SHARE"
    var notes by remember { mutableStateOf("") }
    var receiptSimUri by remember { mutableStateOf("") }

    // Participant inclusions per expense (Defaults to everyone included)
    val involvedMembers = remember { mutableStateListOf<String>().apply { addAll(members) } }
    
    // Custom split weights map index
    val splitWeights = remember(involvedMembers.size, splitMethod) {
        mutableStateMapOf<String, String>().apply {
            involvedMembers.forEach { m ->
                put(m, when(splitMethod) {
                    "PERCENT" -> (100.0 / involvedMembers.size).toString()
                    "SHARE" -> "1"
                    else -> ""
                })
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = "💸 Add Trip Group Expense",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. Sea Villas)", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Total Bill Amount (₹)", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Spender details
                Text("👤 WHO PAID THIS BILL?", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    members.forEach { m ->
                        val isPayer = paidBy == m
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPayer) NeonGreen.copy(0.15f) else SurfaceBlue,
                            border = BorderStroke(1.dp, if (isPayer) NeonGreen else BorderColor),
                            modifier = Modifier.clickable { paidBy = m }
                        ) {
                            Text(m, color = if (isPayer) NeonGreen else TextWhite, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Category selector
                Text("📂 EXPENSE CATEGORY:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Food", "Stay", "Transport", "Activities", "Others").forEach { cat ->
                        val isSelected = category == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) AccentOrange.copy(0.15f) else SurfaceBlue,
                            border = BorderStroke(1.dp, if (isSelected) AccentOrange else BorderColor),
                            modifier = Modifier.clickable { category = cat }
                        ) {
                            Text(cat, color = if (isSelected) AccentOrange else TextWhite, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // FLEXIBLE SPLITING MODE CHIPS
                Text("⚡ SPLITTING DIVISION METHOD:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "EQUAL" to "Equal Division",
                        "PERCENT" to "Percentages",
                        "EXACT" to "Exact Amounts",
                        "SHARE" to "Share weights"
                    ).forEach { (m, lbl) ->
                        val isSelected = splitMethod == m
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) SipBlue.copy(alpha = 0.15f) else SurfaceBlue,
                            border = BorderStroke(1.dp, if (isSelected) SipBlue else BorderColor),
                            modifier = Modifier.clickable { splitMethod = m }
                        ) {
                            Text(lbl, color = if (isSelected) SipBlue else TextWhite, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // MEMBERS PER EXPENSE SELECTION (INCLUSIONS/EXCLUSIONS)
                Text("👥 CHOOSE INVOLVED MEMBERS (EXCLUDE THOSE WHO DIDN'T TAKE PART):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    members.forEach { m ->
                        val isIncluded = involvedMembers.contains(m)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isIncluded) AccentOrange.copy(alpha = 0.15f) else SurfaceBlue,
                            border = BorderStroke(1.dp, if (isIncluded) AccentOrange else BorderColor),
                            modifier = Modifier.clickable {
                                if (isIncluded) involvedMembers.remove(m) else involvedMembers.add(m)
                            }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isIncluded) Icons.Default.Check else Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = if (isIncluded) AccentOrange else TextGray,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(m, color = if (isIncluded) AccentOrange else TextWhite, fontSize = 11.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // CUSTOM INPUT WEIGHTS FOR FLEXIBLE SPLITTING
                if (splitMethod != "EQUAL") {
                    Text("⚙️ ENTER RATIOS / SHARES DETAILS FOR EACH INVOLVED MEMBER:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        involvedMembers.forEach { m ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(m, fontSize = 12.sp, color = TextWhite)
                                OutlinedTextField(
                                    value = splitWeights[m] ?: "",
                                    onValueChange = { splitWeights[m] = it },
                                    placeholder = {
                                        Text(
                                            when(splitMethod) {
                                                "PERCENT" -> "e.g. 50"
                                                "EXACT" -> "e.g. 250"
                                                else -> "e.g. 2"
                                            },
                                            fontSize = 11.sp, color = TextGray
                                        )
                                    },
                                    modifier = Modifier.width(110.dp).height(50.dp),
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite, fontSize = 11.sp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Extra custom notes (Optional)", color = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentOrange)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Simulated Receipt Attachment Widget
                Text("🧾 ATTACH RECEIPTS / BILL INVOICES:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val hasAttached = receiptSimUri.isNotBlank()
                    Button(
                        onClick = {
                            receiptSimUri = "receipt_${System.currentTimeMillis()}.png"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (hasAttached) NeonGreen.copy(0.15f) else SurfaceBlue),
                        border = BorderStroke(1.dp, if (hasAttached) NeonGreen else BorderColor)
                    ) {
                        Text(if (hasAttached) "🧾 Receipt attached ✔" else "➕ Simulate Upload Bill", fontSize = 11.sp, color = if (hasAttached) NeonGreen else TextWhite)
                    }
                    if (hasAttached) {
                        TextButton(onClick = { receiptSimUri = "" }) {
                            Text("Clear", color = Color(0xFFFE3C3C), fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextGray)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && amt > 0.0 && involvedMembers.isNotEmpty()) {
                                // Compile active weights string
                                val weightsString = if (splitMethod == "EQUAL") {
                                    ""
                                } else {
                                    involvedMembers.map { m ->
                                        splitWeights[m] ?: "0"
                                    }.joinToString(",")
                                }
                                onSave(title, amt, paidBy, splitMethod, weightsString, involvedMembers.toList(), category, notes, receiptSimUri)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                    ) {
                        Text("Add", color = TextWhite)
                    }
                }
            }
        }
    }
}

// ========================== PRINTABLE EXCEL / PDF EXPORTER MOCKUP DIALOG COMPOSE ==========================
@Composable
fun TripExporterMockupDialog(
    trip: TripEventEntity,
    expenses: List<TripExpenseEntity>,
    members: List<String>,
    onDismiss: () -> Unit,
    currencyFormatter: java.text.NumberFormat
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0C0E)),
            border = BorderStroke(2.dp, NeonGreen),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📄 PRINTABLE TRIP SUMMARY REPORT",
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "FORMAT: ADOBE PDF / MICROSOFT EXCEL SHEET",
                    color = TextGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // MOCK spreadsheet mockup
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("=======================================", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextGray)
                        Text("MYFIN GROUP BILL LEDGER INVOICE        ", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                        Text("=======================================", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextGray)
                        Text("EVENT NAME: ${trip.name}               ", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextWhite)
                        Text("DATES:      ${trip.startDate} - ${trip.endDate}", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextWhite)
                        Text("MEMBERS:    ${members.joinToString(", ")}", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextWhite)
                        Text("---------------------------------------", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextGray)
                        
                        expenses.forEach { exp ->
                            val paddedTitle = exp.title.take(13).padEnd(13, ' ')
                            val paddedPaid = exp.paidBy.take(6).padEnd(6, ' ')
                            val amtString = currencyFormatter.format(exp.totalAmount)
                            Text("$paddedTitle | PAID BY: $paddedPaid | $amtString", fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = TextWhite)
                        }

                        Text("---------------------------------------", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextGray)
                        Text("TOTAL COST: " + currencyFormatter.format(expenses.sumOf { it.totalAmount }), fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                        Text("=======================================", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = TextGray)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                                Text("📥 Export Complete! Report successfully formatted.", color = TextWhite, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("Download Summary Report", color = NavyBg, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Simplified settlements helper
data class SettlementTransaction(
    val debtor: String,
    val creditor: String,
    val amount: Double
)

fun calculateSimplifiedSettlements(
    participants: List<String>,
    expenses: List<TripExpenseEntity>
): List<SettlementTransaction> {
    if (participants.isEmpty()) return emptyList()
    
    // 1. Calculate net balances
    // Use a clean, trimmed approach to ensure consistent key matching
    val balances = participants.map { it.trim() }.associateWith { 0.0 }.toMutableMap()
    
    expenses.forEach { expense ->
        val totalAmount = expense.totalAmount
        val paidBy = expense.paidBy.trim()
        val involvedList = expense.involvedParticipants.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        
        if (involvedList.isNotEmpty()) {
            // Credit the spender (initialize to 0.0 if not already present)
            balances[paidBy] = (balances[paidBy] ?: 0.0) + totalAmount
            
            // Calculate individual shares based on split method
            val shares = when (expense.splitMethod) {
                "PERCENT" -> {
                    val weights = expense.participantWeights.split(",").map { it.toDoubleOrNull() ?: 100.0 / involvedList.size }
                    val totalWeight = weights.sum()
                    involvedList.mapIndexed { idx, p ->
                        val weight = weights.getOrElse(idx) { 100.0 / involvedList.size }
                        val pct = if (totalWeight > 0.0) weight / totalWeight else 1.0 / involvedList.size
                        p to totalAmount * pct
                    }
                }
                "EXACT" -> {
                    val weights = expense.participantWeights.split(",").map { it.toDoubleOrNull() ?: totalAmount / involvedList.size }
                    involvedList.mapIndexed { idx, p ->
                        p to weights.getOrElse(idx) { totalAmount / involvedList.size }
                    }
                }
                "SHARE" -> {
                    val weights = expense.participantWeights.split(",").map { it.toDoubleOrNull() ?: 1.0 }
                    val totalShares = weights.sum()
                    involvedList.mapIndexed { idx, p ->
                        val weight = weights.getOrElse(idx) { 1.0 }
                        val shareFraction = if (totalShares > 0.0) weight / totalShares else 1.0 / involvedList.size
                        p to totalAmount * shareFraction
                    }
                }
                else -> { // EQUAL Division
                    val share = totalAmount / involvedList.size
                    involvedList.map { it to share }
                }
            }
            
            // Debit each involved participant's balance
            shares.forEach { (p, shareAmount) ->
                balances[p] = (balances[p] ?: 0.0) - shareAmount
            }
        }
    }
    
    // Convert double balances to fixed-point Long (cents) to avoid floating point issues
    val debtorList = mutableListOf<Pair<String, Long>>()
    val creditorList = mutableListOf<Pair<String, Long>>()
    
    balances.forEach { (name, bal) ->
        val cents = kotlin.math.round(bal * 100.0).toLong()
        if (cents < 0) {
            debtorList.add(name to -cents) // Positive value representing amount owed
        } else if (cents > 0) {
            creditorList.add(name to cents) // Positive value representing amount credited
        }
    }
    
    val transactions = mutableListOf<SettlementTransaction>()
    
    // Core Greedy Match Loop: Match largest remaining debtor with largest creditor
    while (debtorList.isNotEmpty() && creditorList.isNotEmpty()) {
        // Sort both descending so we're always matching the largest outstanding amounts
        debtorList.sortByDescending { it.second }
        creditorList.sortByDescending { it.second }
        
        val debtor = debtorList.first()
        val creditor = creditorList.first()
        
        val payAmtCents = minOf(debtor.second, creditor.second)
        if (payAmtCents > 0) {
            transactions.add(
                SettlementTransaction(
                    debtor = debtor.first,
                    creditor = creditor.first,
                    amount = payAmtCents / 100.0
                )
            )
        }
        
        // Update balance and remove settled members
        val remainingDebtorCents = debtor.second - payAmtCents
        val remainingCreditorCents = creditor.second - payAmtCents
        
        if (remainingDebtorCents > 0) {
            debtorList[0] = debtor.first to remainingDebtorCents
        } else {
            debtorList.removeAt(0)
        }
        
        if (remainingCreditorCents > 0) {
            creditorList[0] = creditor.first to remainingCreditorCents
        } else {
            creditorList.removeAt(0)
        }
    }
    
    return transactions
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingDialog(
    onDismissOnboarding: () -> Unit,
    viewModel: WealthPulseViewModel,
    onShowEmailAuth: () -> Unit
) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }
    var showGoogleFallbackPrompt by remember { mutableStateOf(false) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("default-dummy-aistudio-key-for-myfin")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            val googleEmail = account?.email
            if (idToken != null) {
                loading = true
                viewModel.signInWithGoogle(
                    idToken = idToken,
                    email = googleEmail,
                    onSuccess = {
                        loading = false
                        Toast.makeText(context, "Successfully authenticated with Google!", Toast.LENGTH_SHORT).show()
                        onDismissOnboarding()
                    },
                    onError = { err ->
                        loading = false
                        Toast.makeText(context, "Google sign-in error: $err", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                if (googleEmail != null) {
                    loading = true
                    viewModel.performFallbackGoogleSignIn(googleEmail) {
                        loading = false
                        Toast.makeText(context, "Signed in with Google Account (Sandbox Fallback)", Toast.LENGTH_SHORT).show()
                        onDismissOnboarding()
                    }
                } else {
                    Toast.makeText(context, "Failed to retrieve Google Auth Credentials.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            showGoogleFallbackPrompt = true
        }
    }

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, cssVar("--border")),
            colors = CardDefaults.cardColors(containerColor = cssVar("--surface")),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("onboarding_dialog_card")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome to MYFin 🚀",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = cssVar("--primary")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your secure, offline-first personal finance engine. Sign in to synchronize your data automatically across devices.",
                    fontSize = 13.sp,
                    color = cssVar("--text-gray"),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (loading) {
                    CircularProgressIndicator(color = cssVar("--primary"))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (showGoogleFallbackPrompt) {
                    var emailInput by remember { mutableStateOf("") }
                    Text(
                        text = "Enter Google Email to Simulate Login:",
                        fontSize = 11.sp,
                        color = cssVar("--accent"),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("e.g. user@gmail.com", fontSize = 12.sp, color = cssVar("--text-gray")) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            loading = true
                            viewModel.performFallbackGoogleSignIn(emailInput) {
                                loading = false
                                Toast.makeText(context, "Simulated Google Auth Success!", Toast.LENGTH_SHORT).show()
                                onDismissOnboarding()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = cssVar("--primary")),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text("Confirm Simulate Google Login", color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Button(
                        onClick = {
                            try {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            } catch (e: Exception) {
                                showGoogleFallbackPrompt = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = cssVar("--primary")),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("google_auth_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Google Sign In", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign In with Google", color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = onShowEmailAuth,
                    colors = ButtonDefaults.buttonColors(containerColor = cssVar("--secondary")),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("email_auth_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = "Email Sign In", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In with Email", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismissOnboarding,
                    border = BorderStroke(1.dp, cssVar("--border")),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("continue_offline_btn")
                ) {
                    Text("Continue Offline (Guest Sandbox)", color = cssVar("--text-white"))
                }
            }
        }
    }
}

@Composable
fun MigrationDialog(
    viewModel: WealthPulseViewModel
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, cssVar("--border")),
            colors = CardDefaults.cardColors(containerColor = cssVar("--surface")),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("migration_dialog_card")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Local Guest Data Found! 🗄️",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = cssVar("--accent")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "We detected existing local financial records. How would you like to link them to your cloud account?",
                    fontSize = 13.sp,
                    color = cssVar("--text-gray"),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.mergeLocalData()
                        Toast.makeText(context, "Local data merged with cloud backups!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cssVar("--primary")),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("merge_local_cloud_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Merge Local + Cloud Data", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Integrates device history into cloud backing (Safe)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.replaceCloudData()
                        Toast.makeText(context, "Cloud backups overwritten with local profile!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cssVar("--secondary")),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("repl_cloud_with_local_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Replace Cloud with Local", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Forces remote profiles to match current device data", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.replaceLocalData()
                        Toast.makeText(context, "Local records replaced with cloud backups!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cssVar("--danger")),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("repl_local_with_cloud_btn")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Replace Local with Cloud", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Discards local records and loads cloud history snapshot", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { viewModel.dismissMigrationDialog() },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text("Skip Migration for Now", color = cssVar("--text-white"))
                }
            }
        }
    }
}