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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.viewmodel.LoanDashboardSummary
import com.example.ui.viewmodel.LoanUiState
import com.example.ui.viewmodel.LoanViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LoanWorkspaceHub(
    currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")),
    modifier: Modifier = Modifier,
    loanViewModel: LoanViewModel = viewModel()
) {
    val uiState by loanViewModel.uiState.collectAsState()
    
    var activeSubTab by remember { mutableStateOf("loans") } // "loans" or "calendar"
    var showAddLoanModal by remember { mutableStateOf(false) }
    var selectedLoanForDetail by remember { mutableStateOf<LoanEntity?>(null) }
    var showPaymentModalForLoan by remember { mutableStateOf<LoanEntity?>(null) }

    // Synchronize detail state inside popup if DB updates
    LaunchedEffect(uiState, selectedLoanForDetail) {
        val currentL = selectedLoanForDetail
        if (currentL != null && uiState is LoanUiState.Success) {
            val matching = (uiState as LoanUiState.Success).loans.find { it.id == currentL.id }
            selectedLoanForDetail = matching
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header Dashboard Widget
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = "DEBT & LIABILITY AGGREGATOR",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MYFin Debt Management Pro",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Serif
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Global summary counts
                    when (val state = uiState) {
                        is LoanUiState.Success -> {
                            val activeLoanCount = state.loans.filter { it.status == "Active" }.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "TOTAL LIABILITIES DUE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currencyFormatter.format(state.totalOutstanding),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.error,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$activeLoanCount Active Loans",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "OVERALL AMORTIZED PROGRESS",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = String.format("%.1f%%", state.overallProgressPercentage),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { (state.overallProgressPercentage / 100).toFloat().coerceIn(0f, 1f) },
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                            color = MaterialTheme.colorScheme.secondary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            // Tabs Selector: Premium Segmented controls matching Split design with micro-animations
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (activeSubTab == "loans") MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .clickable { activeSubTab = "loans" }
                        .testTag("loan_list_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (activeSubTab == "loans") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active Loans",
                            fontSize = 11.sp,
                            maxLines = 1,
                            fontWeight = if (activeSubTab == "loans") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeSubTab == "loans") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (activeSubTab == "calendar") MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .clickable { activeSubTab = "calendar" }
                        .testTag("emi_calendar_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = if (activeSubTab == "calendar") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EMI Calendar",
                            fontSize = 11.sp,
                            maxLines = 1,
                            fontWeight = if (activeSubTab == "calendar") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeSubTab == "calendar") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Main Contents
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val state = uiState) {
                    is LoanUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    is LoanUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is LoanUiState.Success -> {
                        if (activeSubTab == "loans") {
                            LoansTabContent(
                                state = state,
                                currencyFormatter = currencyFormatter,
                                onSelectLoan = { selectedLoanForDetail = it },
                                onRecordPayment = { showPaymentModalForLoan = it }
                            )
                        } else {
                            CalendarTabContent(
                                state = state,
                                currencyFormatter = currencyFormatter,
                                onSelectLoan = { loanId ->
                                    val match = state.loans.find { it.id == loanId }
                                    if (match != null) {
                                        selectedLoanForDetail = match
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Add Loan FAB
        FloatingActionButton(
            onClick = { showAddLoanModal = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 20.dp)
                .testTag("add_loan_fab_button"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Loan")
        }

        // Add Loan Dialog Form
        if (showAddLoanModal) {
            AddLoanDialog(
                onDismiss = { showAddLoanModal = false },
                onSave = { name, type, lender, principal, rate, tenure, start ->
                    loanViewModel.addLoan(name, type, lender, principal, rate, tenure, start) { isOk, msg ->
                        if (isOk) showAddLoanModal = false
                    }
                }
            )
        }

        // Loan Detail slide-over or dialog
        if (selectedLoanForDetail != null) {
            LoanDetailsDialog(
                loan = selectedLoanForDetail!!,
                currencyFormatter = currencyFormatter,
                loanViewModel = loanViewModel,
                onDismiss = { selectedLoanForDetail = null },
                onTriggerPayment = { showPaymentModalForLoan = selectedLoanForDetail }
            )
        }

        // Record Payment Dialog
        if (showPaymentModalForLoan != null) {
            RecordPaymentDialog(
                loan = showPaymentModalForLoan!!,
                currencyFormatter = currencyFormatter,
                onDismiss = { showPaymentModalForLoan = null },
                onPay = { amount, method, paymentType, notes, date ->
                    loanViewModel.recordPayment(
                        loanId = showPaymentModalForLoan!!.id,
                        amount = amount,
                        paymentMethod = method,
                        paymentType = paymentType,
                        notes = notes,
                        paymentDate = date
                    ) { isOk, msg ->
                        if (isOk) {
                            showPaymentModalForLoan = null
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun TabButton(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoansTabContent(
    state: LoanUiState.Success,
    currencyFormatter: NumberFormat,
    onSelectLoan: (LoanEntity) -> Unit,
    onRecordPayment: (LoanEntity) -> Unit
) {
    if (state.loans.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Empty",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No active loans found.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your first loan to start tracking repayments, schedules, and calculations.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.loans) { loan ->
                val matchingSchedules = state.schedules.filter { it.loanId == loan.id }
                val nextEmiSchedule = matchingSchedules.firstOrNull { it.paymentStatus == "Pending" }
                val remainingMonths = state.loans.size // safety count or from calculation
                
                // Format dueDate
                val simpleDueDate = if (nextEmiSchedule != null) {
                    SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(nextEmiSchedule.dueDate))
                } else {
                    "N/A"
                }

                val repProgress = if (loan.principalAmount > 0.0) {
                    ((loan.principalAmount - loan.outstandingBalance) / loan.principalAmount).toFloat().coerceIn(0f, 1f)
                } else {
                    1f
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectLoan(loan) }
                        .testTag("loan_card_${loan.id}"),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Badge(
                                    containerColor = getLoanTypeColor(loan.loanType).copy(alpha = 0.15f),
                                    contentColor = getLoanTypeColor(loan.loanType)
                                ) {
                                    Text(
                                        text = loan.loanType,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = loan.loanName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Lender: ${loan.lenderName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = currencyFormatter.format(loan.outstandingBalance),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Outstanding",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "EMI Amount", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = currencyFormatter.format(loan.emiAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column {
                                Text(text = "Next Due", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = simpleDueDate,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (simpleDueDate != "N/A") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Remaining", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val rem = matchingSchedules.filter { it.paymentStatus == "Pending" }.size
                                Text(text = "$rem / ${loan.tenureMonths} M", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { repProgress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = getLoanTypeColor(loan.loanType),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = String.format("%.0f%%", repProgress * 100),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (loan.status == "Closed") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF15803D).copy(alpha = 0.1f))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "LOAN CLOSED & FULLY REPAID", color = Color(0xFF15803D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarTabContent(
    state: LoanUiState.Success,
    currencyFormatter: NumberFormat,
    onSelectLoan: (Int) -> Unit
) {
    val context = LocalContext.current
    var calendarMonthOffset by remember { mutableStateOf(0) } // 0 = current month, 1 = next month, etc.

    val calendar = remember(calendarMonthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, calendarMonthOffset)
        }
    }
    
    val currentMonthLabel = remember(calendar) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
    }

    val daysInMonth = remember(calendar) {
        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val monthStartDayOfWeek = remember(calendar) {
        val cal = Calendar.getInstance()
        cal.time = calendar.time
        cal.set(Calendar.DAY_OF_MONTH, 1)
        // Convert to 0-indexed where 0 = Sun, 1 = Mon ... 6 = Sat
        cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    // EMI occurrences map for current calendar month
    val emiMapForMonth = remember(state.schedules, calendar) {
        val mapped = mutableMapOf<Int, MutableList<LoanScheduleEntity>>()
        val searchMonth = calendar.get(Calendar.MONTH)
        val searchYear = calendar.get(Calendar.YEAR)
        
        val calComp = Calendar.getInstance()
        for (sch in state.schedules) {
            calComp.timeInMillis = sch.dueDate
            if (calComp.get(Calendar.MONTH) == searchMonth && calComp.get(Calendar.YEAR) == searchYear) {
                val day = calComp.get(Calendar.DAY_OF_MONTH)
                mapped.getOrPut(day) { mutableListOf() }.add(sch)
            }
        }
        mapped
    }

    var selectedDayInMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) }

    // Aggregate monthly commitment
    val totalObligationForMonth = remember(emiMapForMonth) {
        emiMapForMonth.values.flatten().sumOf { it.emiAmount }
    }
    val totalPaidForMonth = remember(emiMapForMonth) {
        emiMapForMonth.values.flatten()
            .filter { it.paymentStatus == "Paid" || it.paymentStatus == "Prepaid" }
            .sumOf { it.emiAmount }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "MONTHLY BUDGETARY FLOW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currencyFormatter.format(totalObligationForMonth),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "PAID SO FAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text(
                        text = currencyFormatter.format(totalPaidForMonth),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Calendar Month selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { calendarMonthOffset-- }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev")
            }
            Text(
                text = currentMonthLabel.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = { calendarMonthOffset++ }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }

        // Grid Calendar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column {
                // Day of weeks row
                val dow = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(modifier = Modifier.fillMaxWidth()) {
                    dow.forEach { d ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = d, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days structure loop
                var dayCounter = 1
                for (week in 0..5) {
                    if (dayCounter > daysInMonth) break
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 0..6) {
                            val activeIndex = week * 7 + i
                            if (activeIndex < monthStartDayOfWeek || dayCounter > daysInMonth) {
                                Box(modifier = Modifier.weight(1f))
                            } else {
                                val currentDayConst = dayCounter
                                val schedulesOnDay = emiMapForMonth[currentDayConst] ?: emptyList()
                                val isSelected = selectedDayInMonth == currentDayConst
                                val hasSchedules = schedulesOnDay.isNotEmpty()
                                val allCompleted = hasSchedules && schedulesOnDay.all { it.paymentStatus == "Paid" || it.paymentStatus == "Prepaid" }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                allCompleted -> Color(0xFF15803D).copy(alpha = 0.15f)
                                                hasSchedules -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isSelected) 1.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedDayInMonth = currentDayConst }
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = currentDayConst.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = if (hasSchedules || isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                                allCompleted -> Color(0xFF15803D)
                                                hasSchedules -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.onBackground
                                            }
                                        )
                                        if (hasSchedules) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (allCompleted) Color(0xFF15803D) else MaterialTheme.colorScheme.error
                                                    )
                                            )
                                        }
                                    }
                                }
                                dayCounter++
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        // Details of selected day
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "EMI OBLIGATIONS FOR DAY $selectedDayInMonth",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        val activeDaySchedules = emiMapForMonth[selectedDayInMonth] ?: emptyList()
        if (activeDaySchedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No EMI dues scheduled for this day.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeDaySchedules) { sch ->
                    val matchingLoan = state.loans.find { it.id == sch.loanId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { matchingLoan?.let { onSelectLoan(it.id) } },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = matchingLoan?.loanName ?: "Active Loan Liability",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Installment #${sch.installmentNumber}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currencyFormatter.format(sch.emiAmount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                Badge(
                                    containerColor = when (sch.paymentStatus) {
                                        "Paid" -> Color(0xFF15803D).copy(alpha = 0.15f)
                                        "Prepaid" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    },
                                    contentColor = when (sch.paymentStatus) {
                                        "Paid" -> Color(0xFF15803D)
                                        "Prepaid" -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                ) {
                                    Text(
                                        text = sch.paymentStatus.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLoanDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, lender: String, principal: Double, rate: Double, tenure: Int, startDate: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Personal Loan") }
    var lender by remember { mutableStateOf("") }
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var tenure by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var expandedTypeDropdown by remember { mutableStateOf(false) }
    val loanTypes = listOf("Home Loan", "Personal Loan", "Car Loan", "Bike Loan", "Education Loan", "Gold Loan", "Consumer Loan", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Add New Loan Structure",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Loan Name (e.g. SBI Home Loan)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_loan_name_input"),
                    singleLine = true
                )

                // Dropdown selector for Loan Type
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedTypeDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("add_loan_type_trigger")
                    ) {
                        Text(text = "Type: $type", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false }
                    ) {
                        loanTypes.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(text = t) },
                                onClick = {
                                    type = t
                                    expandedTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = lender,
                    onValueChange = { lender = it },
                    label = { Text("Lender / Bank") },
                    modifier = Modifier.fillMaxWidth().testTag("add_loan_lender_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = principal,
                    onValueChange = { principal = it },
                    label = { Text("Principal Amount (e.g. 2500000)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("add_loan_principal_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Annual Interest Rate % (e.g. 8.5)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("add_loan_rate_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = tenure,
                    onValueChange = { tenure = it },
                    label = { Text("Tenure in Months (e.g. 240)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("add_loan_tenure_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.testTag("add_loan_submit_btn"),
                        onClick = {
                            val pVal = principal.toDoubleOrNull() ?: 0.0
                            val rVal = rate.toDoubleOrNull() ?: 0.0
                            val tVal = tenure.toIntOrNull() ?: 0
                            onSave(name, type, lender, pVal, rVal, tVal, startDate)
                        }
                    ) {
                        Text("Generate Schedule")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoanDetailsDialog(
    loan: LoanEntity,
    currencyFormatter: NumberFormat,
    loanViewModel: LoanViewModel,
    onDismiss: () -> Unit,
    onTriggerPayment: () -> Unit
) {
    val schedulesFlow = remember(loan.id) { loanViewModel.repository.getSchedulesForLoan(loan.id) }
    val paymentsFlow = remember(loan.id) { loanViewModel.repository.getPaymentsForLoan(loan.id) }

    val schedules by schedulesFlow.collectAsState(initial = emptyList())
    val payments by paymentsFlow.collectAsState(initial = emptyList())

    var activeDetailTab by remember { mutableStateOf("summary") } // "summary", "schedule", "payments"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with custom title & closing button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = loan.loanName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Lender: ${loan.lenderName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Inner navigation row
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { activeDetailTab = "summary" }) {
                        Text(
                            text = "SUMMARY",
                            fontWeight = if (activeDetailTab == "summary") FontWeight.Bold else FontWeight.Normal,
                            color = if (activeDetailTab == "summary") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    TextButton(onClick = { activeDetailTab = "schedule" }) {
                        Text(
                            text = "EMI SCHEDULE",
                            fontWeight = if (activeDetailTab == "schedule") FontWeight.Bold else FontWeight.Normal,
                            color = if (activeDetailTab == "schedule") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    TextButton(onClick = { activeDetailTab = "payments" }) {
                        Text(
                            text = "PAYMENTS",
                            fontWeight = if (activeDetailTab == "payments") FontWeight.Bold else FontWeight.Normal,
                            color = if (activeDetailTab == "payments") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Divider()

                // Content box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    when (activeDetailTab) {
                        "summary" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Key Metrics Cards
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Text(text = "OUTSTANDING PRINCIPAL BALANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(
                                            text = currencyFormatter.format(loan.outstandingBalance),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val repPrincipal = (loan.principalAmount - loan.outstandingBalance).coerceAtLeast(0.0)
                                        Text(
                                            text = "Repaid Principal: ${currencyFormatter.format(repPrincipal)} / ${currencyFormatter.format(loan.principalAmount)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Quick details Grid
                                Row {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Interest Rate", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "${loan.interestRate}% P.A.", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Total Tenure", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "${loan.tenureMonths} Months", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                Row {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Emi Amount", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = currencyFormatter.format(loan.emiAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Start Date", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        val simpleStart = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(loan.startDate))
                                        Text(text = simpleStart, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                Row {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Total Interest Computed", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        val totalIntComputed = schedules.sumOf { it.interestComponent }
                                        Text(text = currencyFormatter.format(totalIntComputed), fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Interest Repaid Already", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        val interestPaid = schedules.filter { it.paymentStatus == "Paid" }.sumOf { it.interestComponent }
                                        Text(text = currencyFormatter.format(interestPaid), fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }

                                val forecastMonths = remember(loan, schedules) {
                                    loanViewModel.repository.calculateRemainingMonths(
                                        outstanding = loan.outstandingBalance,
                                        emi = loan.emiAmount,
                                        annualRate = loan.interestRate
                                    )
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Text(text = "FORECAST EXTRA ACTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Estimated Closure in $forecastMonths Months",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Prepayments direct-debit outstanding metrics to shorten expected end dates.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        modifier = Modifier.weight(1f).testTag("record_payment_trigger_${loan.id}"),
                                        onClick = onTriggerPayment,
                                        enabled = loan.status == "Active"
                                    ) {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Record Payment", fontSize = 12.sp)
                                    }
                                    Button(
                                        modifier = Modifier.weight(0.9f).testTag("delete_loan_btn_${loan.id}"),
                                        onClick = {
                                            loanViewModel.deleteLoan(loan.id)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        "schedule" -> {
                            if (schedules.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(text = "No EMI schedule generated.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(schedules) { sch ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    val simpleDueStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(sch.dueDate))
                                                    Text(
                                                        text = "Installment #${sch.installmentNumber}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = "Due: $simpleDueStr",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "P: ${currencyFormatter.format(sch.principalComponent)} | I: ${currencyFormatter.format(sch.interestComponent)}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }

                                                Badge(
                                                    containerColor = when (sch.paymentStatus) {
                                                        "Paid" -> Color(0xFF15803D).copy(alpha = 0.15f)
                                                        "Prepaid" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                                    },
                                                    contentColor = when (sch.paymentStatus) {
                                                        "Paid" -> Color(0xFF15803D)
                                                        "Prepaid" -> MaterialTheme.colorScheme.primary
                                                        else -> MaterialTheme.colorScheme.error
                                                    }
                                                ) {
                                                    Text(
                                                        text = sch.paymentStatus.uppercase(),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "payments" -> {
                            if (payments.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(text = "No completed or extra payments recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(payments) { p ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    val simplePayDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(p.paymentDate))
                                                    Text(
                                                        text = if (p.paymentType == "Regular") "Installment Repayment" else p.paymentType,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = "Paid on: $simplePayDateStr | ${p.paymentMethod}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    if (p.notes.isNotBlank()) {
                                                        Text(
                                                            text = p.notes,
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = currencyFormatter.format(p.amountPaid),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFF15803D)
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentDialog(
    loan: LoanEntity,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit,
    onPay: (amount: Double, method: String, paymentType: String, notes: String, date: Long) -> Unit
) {
    var paymentType by remember { mutableStateOf("Regular") } // "Regular", "Prepayment", "Foreclosure"
    var paymentMethod by remember { mutableStateOf("Bank Transfer") }
    var notes by remember { mutableStateOf("") }
    var inputAmount by remember { mutableStateOf(loan.emiAmount.toInt().toString()) }

    var expandedTypeDropdown by remember { mutableStateOf(false) }
    var expandedMethodDropdown by remember { mutableStateOf(false) }

    val paymentTypes = listOf("Regular", "Prepayment", "Foreclosure")
    val paymentMethods = listOf("Bank Transfer", "UPI", "Net Banking", "Cash", "Cheque")

    // Automatically change amount for Foreclosure type
    LaunchedEffect(paymentType) {
        if (paymentType == "Foreclosure") {
            inputAmount = loan.outstandingBalance.toInt().toString()
        } else if (paymentType == "Regular") {
            inputAmount = loan.emiAmount.toInt().toString()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Record Loan Payment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Dropdown for Payment Type
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedTypeDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("payment_type_trigger")
                    ) {
                        Text(text = "Payment Type: $paymentType", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedTypeDropdown,
                        onDismissRequest = { expandedTypeDropdown = false }
                    ) {
                        paymentTypes.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(text = t) },
                                onClick = {
                                    paymentType = t
                                    expandedTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = inputAmount,
                    onValueChange = { inputAmount = it },
                    label = { Text("Amount Paid") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input"),
                    singleLine = true,
                    enabled = paymentType != "Foreclosure"
                )

                // Dropdown for Payment Method
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedMethodDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("payment_method_trigger")
                    ) {
                        Text(text = "Method: $paymentMethod", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedMethodDropdown,
                        onDismissRequest = { expandedMethodDropdown = false }
                    ) {
                        paymentMethods.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(text = m) },
                                onClick = {
                                    paymentMethod = m
                                    expandedMethodDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Ref details") },
                    modifier = Modifier.fillMaxWidth().testTag("payment_notes_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.testTag("payment_submit_button"),
                        onClick = {
                            val doubleAmount = inputAmount.toDoubleOrNull() ?: 0.0
                            onPay(doubleAmount, paymentMethod, paymentType, notes, System.currentTimeMillis())
                        }
                    ) {
                        Text("Submit Payment")
                    }
                }
            }
        }
    }
}

fun getLoanTypeColor(loanType: String): Color {
    return when (loanType) {
        "Home Loan" -> Color(0xFF0369A1) // Sky blue
        "Personal Loan" -> Color(0xFF6D28D9) // Purple
        "Car Loan" -> Color(0xFFE11D48) // Rose
        "Bike Loan" -> Color(0xFFD97706) // Amber
        "Education Loan" -> Color(0xFF059669) // Emerald
        "Gold Loan" -> Color(0xFFCA8A04) // Yellow
        "Consumer Loan" -> Color(0xFF4F46E5) // Indigo
        else -> Color(0xFF6B7280) // Gray
    }
}
