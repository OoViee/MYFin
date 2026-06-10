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
import com.example.ui.viewmodel.TripUiState
import com.example.ui.viewmodel.TripViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TripWorkspaceMain(
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier,
    tripViewModel: TripViewModel = viewModel()
) {
    val uiState by tripViewModel.uiState.collectAsState()

    var showCreateTripModal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is TripUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is TripUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Database Error", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = state.message, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                        }
                    }
                }
            }
            is TripUiState.Success -> {
                if (state.currentTrip != null) {
                    // Render Active Selected Trip Detail Workspace
                    TripDetailWorkspace(
                        state = state,
                        currencyFormatter = currencyFormatter,
                        tripViewModel = tripViewModel,
                        onBack = { tripViewModel.selectTrip(null) }
                    )
                } else {
                    // Render list of vacations and custom tournaments
                    TripDashboardList(
                        state = state,
                        currencyFormatter = currencyFormatter,
                        tripViewModel = tripViewModel,
                        onCreateTrigger = { showCreateTripModal = true }
                    )
                }
            }
        }

        // Quick Creator Dialog
        if (showCreateTripModal) {
            CreateTripDialog(
                onDismiss = { showCreateTripModal = false },
                onSave = { name, desc, location, sDate, eDate, type, members ->
                    tripViewModel.createTrip(name, desc, sDate, eDate, location, type)
                    // If initial members was provided, we'll let user expand dynamically or add them in bulk later
                    showCreateTripModal = false
                }
            )
        }
    }
}

@Composable
fun TripDashboardList(
    state: TripUiState.Success,
    currencyFormatter: NumberFormat,
    tripViewModel: TripViewModel,
    onCreateTrigger: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        
        // Dynamic Quick Numbers Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "YOUR ACTIVE TRIP LOGS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${state.activeTripsCount} Active Adventures",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "My Debt: " + currencyFormatter.format(state.pendingSettlementsAmount),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // List
        if (state.trips.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No trips created yet.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create your first trip to start tracking group expenses, travel budgets, roles and ledger payments.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onCreateTrigger,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("create_first_trip_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Your First Trip")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("trips_lazy_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(state.trips) { trip ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { tripViewModel.selectTrip(trip.id) }
                            .testTag("trip_card_${trip.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ) {
                                            Text(
                                                text = trip.eventType.uppercase(),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (trip.location.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Place,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(10.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = trip.location,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = trip.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (trip.description.isNotBlank()) {
                                        Text(
                                            text = trip.description,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Badge(
                                    containerColor = if (trip.status == "Active") Color(0xFF15803D).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (trip.status == "Active") Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Text(
                                        text = trip.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${trip.startDate} to ${trip.endDate}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = "OPEN TRIP ➜",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                FloatingActionButton(
                    onClick = onCreateTrigger,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_trip_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add New Trip")
                }
            }
        }
    }
}

@Composable
fun TripDetailWorkspace(
    state: TripUiState.Success,
    currencyFormatter: NumberFormat,
    tripViewModel: TripViewModel,
    onBack: () -> Unit
) {
    val trip = state.currentTrip ?: return
    val analytics = state.analytics

    var currentDetailSubTab by remember { mutableStateOf("overview") } // overview, expenses, settlements, analytics, participants

    var showAddExpense by remember { mutableStateOf(false) }
    var showAddSettlement by remember { mutableStateOf(false) }
    var showAddParticipant by remember { mutableStateOf(false) }
    var showExportSummary by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        
        // Detail Navigation header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("trip_detail_back_btn")) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${trip.eventType} • ${trip.location}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            
            // PDF/Report preview button as requested in the Export Preparation requirement
            IconButton(
                onClick = { showExportSummary = true },
                modifier = Modifier.testTag("prepare_export_pdf_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Share, 
                    contentDescription = "Prepare Report Export",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = { tripViewModel.deleteTrip(trip.id) },
                modifier = Modifier.testTag("delete_trip_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete, 
                    contentDescription = "Delete Trip",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Scrollable local Sub-Tabs
        ScrollableTabRow(
            selectedTabIndex = when (currentDetailSubTab) {
                "overview" -> 0
                "expenses" -> 1
                "settlements" -> 2
                "analytics" -> 3
                else -> 4
            },
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = currentDetailSubTab == "overview", onClick = { currentDetailSubTab = "overview" }) {
                Text("Overview", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = currentDetailSubTab == "expenses", onClick = { currentDetailSubTab = "expenses" }) {
                Text("Expenses", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = currentDetailSubTab == "settlements", onClick = { currentDetailSubTab = "settlements" }) {
                Text("Settlements", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = currentDetailSubTab == "analytics", onClick = { currentDetailSubTab = "analytics" }) {
                Text("Analytics", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = currentDetailSubTab == "participants", onClick = { currentDetailSubTab = "participants" }) {
                Text("People", modifier = Modifier.padding(14.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Content
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (currentDetailSubTab) {
                "overview" -> TripOverviewSubPage(
                    trip = trip,
                    state = state,
                    currencyFormatter = currencyFormatter,
                    onNavigateToExpenses = { currentDetailSubTab = "expenses" }
                )
                "expenses" -> TripExpensesSubPage(
                    state = state,
                    currencyFormatter = currencyFormatter,
                    onAddExpenseTrigger = { showAddExpense = true },
                    onDeleteExpense = { id -> tripViewModel.deleteExpense(id, trip.groupId) }
                )
                "settlements" -> TripSettlementsSubPage(
                    state = state,
                    currencyFormatter = currencyFormatter,
                    onAddSettlementTrigger = { showAddSettlement = true },
                    onDeleteSettlement = { id -> tripViewModel.deleteSettlement(id, trip.groupId) }
                )
                "analytics" -> TripAnalyticsSubPage(
                    state = state,
                    currencyFormatter = currencyFormatter
                )
                "participants" -> TripParticipantsSubPage(
                    state = state,
                    onAddParticipantTrigger = { showAddParticipant = true },
                    onDeleteParticipant = { id -> tripViewModel.removeParticipant(id, trip.id) }
                )
            }
        }

        // Add Dialogs
        if (showAddExpense) {
            AddTripExpenseDialog(
                members = state.currentParticipants,
                onDismiss = { showAddExpense = false },
                onSave = { title, amount, paidBy, category, notes, splitType, shares, involved, receiptUri ->
                    tripViewModel.addExpense(trip.groupId, title, amount, paidBy, category, notes, splitType, shares, involved, receiptUri)
                    showAddExpense = false
                }
            )
        }

        if (showAddSettlement) {
            AddTripSettlementDialog(
                members = state.currentParticipants,
                onDismiss = { showAddSettlement = false },
                onSave = { payer, receiver, amount, notes ->
                    tripViewModel.addSettlement(trip.groupId, payer, receiver, amount, notes)
                    showAddSettlement = false
                }
            )
        }

        if (showAddParticipant) {
            AddTripParticipantDialog(
                onDismiss = { showAddParticipant = false },
                onSave = { name, role ->
                    tripViewModel.addParticipant(trip.id, name, role)
                    showAddParticipant = false
                }
            )
        }

        if (showExportSummary && analytics != null) {
            TripExportPreviewDialog(
                trip = trip,
                participants = state.currentParticipants,
                analytics = analytics,
                currencyFormatter = currencyFormatter,
                onDismiss = { showExportSummary = false }
            )
        }
    }
}

@Composable
fun TripOverviewSubPage(
    trip: TripEntity,
    state: TripUiState.Success,
    currencyFormatter: NumberFormat,
    onNavigateToExpenses: () -> Unit
) {
    val analytics = state.analytics ?: return
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // Primary stats summary
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "TRIP FINANCIAL BALANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Total Trip Cost", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = currencyFormatter.format(analytics.totalTripCost), fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Per Person", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = currencyFormatter.format(analytics.costPerPerson), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Outstanding balance for local user ("You")
                    val mySummary = analytics.participantSummaries.find { it.name == "You" }
                    val userBal = mySummary?.balance ?: 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Your Standalone Status", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (userBal > 0.0) "You are owed" else if (userBal < 0.0) "You owe" else "You are fully settled",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userBal > 0.0) Color(0xFF15803D) else if (userBal < 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = currencyFormatter.format(Math.abs(userBal)),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (userBal > 0.0) Color(0xFF15803D) else if (userBal < 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Timeline Feed Activity
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = "TRIP TIMELINE & ACTIVITY FEED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val activities = mutableListOf<TimelineItem>()
                    state.currentExpenses.forEach { exp ->
                        activities.add(
                            TimelineItem(
                                timestamp = exp.expenseDate,
                                text = "${exp.paidBy} added ${exp.category} Expense '${exp.title}' for ${currencyFormatter.format(exp.amount)}",
                                icon = Icons.Default.AddCircle,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    state.currentSettlements.forEach { set ->
                        activities.add(
                            TimelineItem(
                                timestamp = set.date,
                                text = "Settlement of ${currencyFormatter.format(set.amount)} recorded (${set.payer} ➜ ${set.receiver})",
                                icon = Icons.Default.CheckCircle,
                                color = Color(0xFF15803D)
                            )
                        )
                    }

                    val sortedActivities = activities.sortedByDescending { it.timestamp }
                    if (sortedActivities.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No activities logged. Add expenses or settlement transfers to populate the feed.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            sortedActivities.take(5).forEach { act ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = act.icon,
                                        contentDescription = null,
                                        tint = act.color,
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = act.text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                                        Text(text = sdf.format(Date(act.timestamp)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

data class TimelineItem(
    val timestamp: Long,
    val text: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
fun TripExpensesSubPage(
    state: TripUiState.Success,
    currencyFormatter: NumberFormat,
    onAddExpenseTrigger: () -> Unit,
    onDeleteExpense: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "ITEMIZED TRIP EXPENSES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Button(
                onClick = onAddExpenseTrigger,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp).testTag("add_trip_expense_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Expense", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (state.currentExpenses.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No expenses recorded under this trip yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().testTag("trip_expenses_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.currentExpenses) { exp ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ) {
                                        Text(text = exp.category.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = exp.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Paid by ${exp.paidBy} • Split: ${exp.splitType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currencyFormatter.format(exp.amount),
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { onDeleteExpense(exp.id) },
                                        modifier = Modifier.size(36.dp).testTag("delete_expense_${exp.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            
                            // Render Receipt URI attachment indicator if present
                            if (exp.receiptUri.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                     Icon(
                                         imageVector = Icons.Default.MailOutline,
                                         contentDescription = "Attachment Logo",
                                         size = 12.dp,
                                         tint = MaterialTheme.colorScheme.primary
                                     )
                                     Spacer(modifier = Modifier.width(4.dp))
                                     Text(
                                         text = "Bill: ${exp.receiptUri}",
                                         fontSize = 10.sp,
                                         fontWeight = FontWeight.Bold,
                                         color = MaterialTheme.colorScheme.primary
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

@Composable
private fun Icon(imageVector: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    Icon(imageVector = imageVector, contentDescription = contentDescription, modifier = Modifier.size(size), tint = tint)
}

@Composable
fun TripSettlementsSubPage(
    state: TripUiState.Success,
    currencyFormatter: NumberFormat,
    onAddSettlementTrigger: () -> Unit,
    onDeleteSettlement: (Int) -> Unit
) {
    val analytics = state.analytics

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        
        // Recommended Settlements (Dynamic Peer debts)
        if (analytics != null && analytics.recommendedSettlements.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                    Text(text = "RECOMMENDED SETTLEMENT FLOWS (OPTIMAL)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    analytics.recommendedSettlements.forEach { debt ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Badge(containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f)) {
                                    Text(text = debt.fromUser, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Text(text = " owes ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                    Text(text = debt.toUser, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text(text = currencyFormatter.format(debt.amount), fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "RECORDED SETTLEMENT CONTRACTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Button(
                onClick = onAddSettlementTrigger,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp).testTag("add_trip_settle_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Record Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (state.currentSettlements.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No settlements filed under this trip yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().testTag("trip_settles_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.currentSettlements) { set ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = set.payer, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(16.dp).padding(horizontal = 4.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(text = set.receiver, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                if (set.notes.isNotBlank()) {
                                    Text(text = set.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                Text(text = sdf.format(Date(set.date)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currencyFormatter.format(set.amount),
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 15.sp,
                                    color = Color(0xFF15803D)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onDeleteSettlement(set.id) },
                                    modifier = Modifier.size(36.dp).testTag("delete_settle_${set.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TripAnalyticsSubPage(
    state: TripUiState.Success,
    currencyFormatter: NumberFormat
) {
    val analytics = state.analytics
    if (analytics == null || analytics.totalTripCost == 0.0) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(text = "No analytics available. Log some trip expenses to populate categories spending distribution charts.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "EXPENSES CATEGORIES BREAKDOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }

        // Category bar charts
        items(analytics.categoryDistribution) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.category, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${currencyFormatter.format(item.amount)} (${String.format("%.1f", item.percentage)}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Simple custom bar gauge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (item.percentage / 100.0).toFloat())
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }

        // Display totals
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Total Trip Budget Spent", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = currencyFormatter.format(analytics.totalTripCost), fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun TripParticipantsSubPage(
    state: TripUiState.Success,
    onAddParticipantTrigger: () -> Unit,
    onDeleteParticipant: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "TRIP MEMBERS & CORRELATED ROLES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Button(
                onClick = onAddParticipantTrigger,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp).testTag("add_trip_member_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Person", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (state.currentParticipants.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No custom participants added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().testTag("trip_people_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.currentParticipants) { part ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = part.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = part.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Badge(
                                        containerColor = when (part.role) {
                                            "Organizer" -> Color(0xFF15803D).copy(alpha = 0.1f)
                                            "Viewer" -> MaterialTheme.colorScheme.surfaceVariant
                                            else -> MaterialTheme.colorScheme.secondaryContainer
                                        },
                                        contentColor = when (part.role) {
                                            "Organizer" -> Color(0xFF15803D)
                                            "Viewer" -> MaterialTheme.colorScheme.onSurfaceVariant
                                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    ) {
                                        Text(text = part.role.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            
                            // Prevent removing "You" (primary local user)
                            if (part.name != "You") {
                                IconButton(
                                    onClick = { onDeleteParticipant(part.participantId) },
                                    modifier = Modifier.testTag("delete_member_${part.participantId}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTripDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, desc: String, location: String, sDate: String, eDate: String, type: String, initialMembers: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
    // Dates Defaulted to current month range
    val cal = Calendar.getInstance()
    val sFormat = SimpleDateFormat("YYYY-MM-dd", Locale.getDefault())
    var startDate by remember { mutableStateOf(sFormat.format(cal.time)) }
    cal.add(Calendar.DAY_OF_MONTH, 5)
    var endDate by remember { mutableStateOf(sFormat.format(cal.time)) }

    var selectedType by remember { mutableStateOf("Trip") }
    val types = listOf("Trip", "Vacation", "Tournament", "Party", "Office Event", "Family Function", "Wedding", "Custom")
    var typesDropdownShow by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("create_trip_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = "Log New Adventure", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Type selector dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { typesDropdownShow = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Type: $selectedType")
                        }
                        DropdownMenu(expanded = typesDropdownShow, onDismissRequest = { typesDropdownShow = false }) {
                            types.forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = {
                                    selectedType = t
                                    typesDropdownShow = false
                                })
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (e.g. Goa 2026)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("trip_name_input")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("trip_desc_input")
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Destination / Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("trip_location_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("trip_start_date_input")
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("trip_end_date_input")
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, desc, location, startDate, endDate, selectedType, "") },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("trip_save_submit_btn")
                    ) {
                        Text("Establish Trip")
                    }
                }
            }
        }
    }
}

@Composable
fun AddTripExpenseDialog(
    members: List<TripParticipantEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, paidBy: String, category: String, notes: String, splitType: String, shares: String, involved: String, receiptUri: String) -> Unit
) {
    if (members.isEmpty()) {
        Dialog(onDismissRequest = onDismiss) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Need participants added to compile project expenses.")
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = onDismiss) { Text("OK") }
                }
            }
        }
        return
    }

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf(members[0].name) }
    var category by remember { mutableStateOf("Food") }
    val categories = listOf("Transport", "Fuel", "Hotel", "Food", "Activities", "Shopping", "Tickets", "Sports", "Emergency", "Miscellaneous")
    var notes by remember { mutableStateOf("") }
    var splitType by remember { mutableStateOf("EQUAL") } // EQUAL, EXACT, PERCENT, SHARE

    var receiptUriSimVal by remember { mutableStateOf("") } // local attachments

    // Selected involved members state (all by default)
    val involvedMembers = remember { mutableStateMapOf<String, Boolean>().apply { members.forEach { put(it.name, true) } } }

    // Dynamic split input maps
    val participantWeights = remember { mutableStateMapOf<String, String>().apply { members.forEach { put(it.name, "1") } } }

    var categoryDropdownShow by remember { mutableStateOf(false) }
    var paidByDropdownShow by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("add_expense_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = "Log Trip Expense", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g. Seafood Lunch)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("expense_title_input")
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount Spent") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Paid By dropdown picker
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { paidByDropdownShow = true }, modifier = Modifier.fillMaxWidth().testTag("expense_payer_trigger")) {
                            Text(text = "PaidBy: $paidBy", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = paidByDropdownShow, onDismissRequest = { paidByDropdownShow = false }) {
                            members.forEach { m ->
                                DropdownMenuItem(text = { Text(m.name) }, onClick = {
                                    paidBy = m.name
                                    paidByDropdownShow = false
                                })
                            }
                        }
                    }

                    // Category dropdown picker
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { categoryDropdownShow = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(text = "Cat: $category", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = categoryDropdownShow, onDismissRequest = { categoryDropdownShow = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat) }, onClick = {
                                    category = cat
                                    categoryDropdownShow = false
                                })
                            }
                        }
                    }
                }

                // Invoices / Bills simulation selection mapping
                Text(text = "BILLS & RECEIPTS (LOCAL)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { receiptUriSimVal = "receipt_${System.currentTimeMillis() % 10000}.png" },
                        modifier = Modifier.weight(1f).testTag("simulate_receipt_btn")
                    ) {
                        Icon(imageVector = Icons.Default.MailOutline, contentDescription = null, size = 14.dp, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (receiptUriSimVal.isBlank()) "Snap/Select Bill Photo" else "Attached PNG", fontSize = 11.sp)
                    }
                    if (receiptUriSimVal.isNotBlank()) {
                        IconButton(onClick = { receiptUriSimVal = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear Bill File")
                        }
                    }
                }

                // Split method strategy options
                Text(text = "SPLIT RATIO STRATEGY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("EQUAL" to "Equal", "EXACT" to "Exact", "PERCENT" to "%", "SHARE" to "Shares").forEach { (v, lbl) ->
                        val isSelected = splitType == v
                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = { splitType = v },
                            label = { Text(lbl, fontSize = 12.sp) },
                            modifier = Modifier.testTag("split_chip_$v")
                        )
                    }
                }

                // Involved checklist & sliders/inputs for unequal ratios
                Text(text = "INVOLVED PARTICIPANT RATIO DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                members.forEach { m ->
                    val name = m.name
                    val isInvolved = involvedMembers[name] ?: false
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isInvolved,
                                onCheckedChange = { involvedMembers[name] = it ?: false },
                                modifier = Modifier.testTag("checkbox_$name")
                            )
                            Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        if (isInvolved && splitType != "EQUAL") {
                            val rawVal = participantWeights[name] ?: "1"
                            OutlinedTextField(
                                value = rawVal,
                                onValueChange = { participantWeights[name] = it },
                                label = { Text(if (splitType == "EXACT") "Amount" else if (splitType == "PERCENT") "%" else "Share Weight") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(100.dp).testTag("split_input_$name")
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            val selectedParticipants = members.filter { involvedMembers[it.name] == true }
                            val participantNamesString = selectedParticipants.map { it.name }.joinToString(",")
                            
                            // Build shares comma separated list
                            val sharesStrList = selectedParticipants.map { part ->
                                val wVal = participantWeights[part.name]?.toDoubleOrNull() ?: when(splitType) {
                                    "PERCENT" -> 100.0 / selectedParticipants.size
                                    "SHARE" -> 1.0
                                    else -> amt / selectedParticipants.size
                                }
                                wVal
                            }.joinToString(",")

                            onSave(title, amt, paidBy, category, notes, splitType, sharesStrList, participantNamesString, receiptUriSimVal)
                        },
                        modifier = Modifier.testTag("expense_submit_btn"),
                        enabled = title.isNotBlank() && amount.isNotBlank()
                    ) {
                        Text("Add Expense")
                    }
                }
            }
        }
    }
}

@Composable
fun AddTripSettlementDialog(
    members: List<TripParticipantEntity>,
    onDismiss: () -> Unit,
    onSave: (payer: String, receiver: String, amount: Double, notes: String) -> Unit
) {
    if (members.size < 2) {
        Dialog(onDismissRequest = onDismiss) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Requires at least 2 participants to form settlements.")
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = onDismiss) { Text("OK") }
                }
            }
        }
        return
    }

    var payer by remember { mutableStateOf(members[0].name) }
    var receiver by remember { mutableStateOf(members[1].name) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var payerDropdownShow by remember { mutableStateOf(false) }
    var receiverDropdownShow by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("record_settle_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = "Log Settlement", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Payer dropdown selection
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { payerDropdownShow = true }, modifier = Modifier.fillMaxWidth().testTag("settle_payer_trigger")) {
                            Text(text = "From: $payer", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = payerDropdownShow, onDismissRequest = { payerDropdownShow = false }) {
                            members.forEach { m ->
                                DropdownMenuItem(text = { Text(m.name) }, onClick = {
                                    payer = m.name
                                    payerDropdownShow = false
                                })
                            }
                        }
                    }

                    // Receiver dropdown selection
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { receiverDropdownShow = true }, modifier = Modifier.fillMaxWidth().testTag("settle_receiver_trigger")) {
                            Text(text = "To: $receiver", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = receiverDropdownShow, onDismissRequest = { receiverDropdownShow = false }) {
                            members.forEach { m ->
                                DropdownMenuItem(text = { Text(m.name) }, onClick = {
                                    receiver = m.name
                                    receiverDropdownShow = false
                                })
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount Transferred") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settle_amount_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. Cash, UPI)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            onSave(payer, receiver, amt, notes)
                        },
                        enabled = amount.isNotBlank() && payer != receiver,
                        modifier = Modifier.testTag("settle_submit_btn")
                    ) {
                        Text("Record Settlement")
                    }
                }
            }
        }
    }
}

@Composable
fun AddTripParticipantDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, role: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Participant") }
    val roles = listOf("Participant", "Organizer", "Viewer")
    var rolesDropdownShow by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("add_member_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = "Add Trip Participant", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("member_name_input")
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { rolesDropdownShow = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Role: $role")
                    }
                    DropdownMenu(expanded = rolesDropdownShow, onDismissRequest = { rolesDropdownShow = false }) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = {
                                role = r
                                rolesDropdownShow = false
                            })
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, role) },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("member_submit_btn")
                    ) {
                        Text("Add Participant")
                    }
                }
            }
        }
    }
}

@Composable
fun TripExportPreviewDialog(
    trip: TripEntity,
    participants: List<TripParticipantEntity>,
    analytics: TripAnalytics,
    currencyFormatter: NumberFormat,
    onDismiss: () -> Unit
) {
    var copyNoticeShow by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(8.dp).testTag("export_summary_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Export Summary Preview", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "This report structure is fully serialized and ready for PDF/JSON export generation on Stage 8.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )

                // Structure Visual Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "TRIP: ${trip.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "LOCATION: ${trip.location} (${trip.startDate} to ${trip.endDate})", fontSize = 11.sp)
                        Text(text = "TOTAL COST: ${currencyFormatter.format(analytics.totalTripCost)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "COST PER PERSON: ${currencyFormatter.format(analytics.costPerPerson)}", fontSize = 11.sp)
                        
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        
                        Text(text = "PARTICIPANT CONTRIBUTIONS & OUTSTANDINGS:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        analytics.participantSummaries.forEach { ps ->
                            Text(
                                text = "• ${ps.name} (${ps.role}): Paid ${currencyFormatter.format(ps.totalPaid)}, Share ${currencyFormatter.format(ps.individualShare)}, Balance is ${if (ps.balance >= 0) "receives ${currencyFormatter.format(ps.balance)}" else "owes ${currencyFormatter.format(-ps.balance)}"} ",
                                fontSize = 10.sp
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(text = "RECOMMENDED CLEARING SETTLEMENTS:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        if (analytics.recommendedSettlements.isEmpty()) {
                            Text(text = "• All accounts fully settled", fontSize = 10.sp)
                        } else {
                            analytics.recommendedSettlements.forEach { debt ->
                                Text(text = "• ${debt.fromUser} pays ${debt.toUser} ${currencyFormatter.format(debt.amount)}", fontSize = 10.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { copyNoticeShow = true },
                        modifier = Modifier.weight(1f).testTag("simulate_pdf_btn")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, size = 14.dp, tint = Color.Unspecified)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate PDF Export", fontSize = 12.sp)
                    }
                }

                AnimatedVisibility(visible = copyNoticeShow) {
                    Text(
                        text = "PDF exported offline in memory. In Stage 8, this triggers Android's local print manager flow.",
                        color = Color(0xFF15803D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
