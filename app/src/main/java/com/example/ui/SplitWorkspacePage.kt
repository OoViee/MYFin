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
import com.example.ui.viewmodel.SplitUiState
import com.example.ui.viewmodel.SplitViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SplitsWorkspaceHub(
    currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")),
    modifier: Modifier = Modifier,
    splitViewModel: SplitViewModel = viewModel(),
    tripViewModel: com.example.ui.viewmodel.TripViewModel = viewModel()
) {
    val uiState by splitViewModel.uiState.collectAsState()
    
    var localMainTab by remember { mutableStateOf("personal") } // "personal" (Mode 1) or "groups" (Mode 2) or "trips" (Mode 3)
    var showCreateGroupModal by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            if (localMainTab != "trips") {
                // Header Aggregate Block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column {
                        Text(
                            text = "SPLITWISE-STYLE DEBT SYSTEM",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MYFin Peer Splits & Ledger",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.Serif
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Mini Aggregates Card
                        when (val state = uiState) {
                            is SplitUiState.Success -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "YOU ARE OWED",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = currencyFormatter.format(state.totalLent),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF15803D),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = "YOU OWE THEM",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = currencyFormatter.format(state.totalBorrowed),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.error,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Mode Selector tabs (Only if NO active group is clicked to detail)
            val isEditingOrViewingGroup = (uiState as? SplitUiState.Success)?.currentGroup != null
            if (!isEditingOrViewingGroup) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TabButton(
                        label = "Personal Tracker",
                        isSelected = localMainTab == "personal",
                        icon = Icons.Default.Person,
                        onClick = { localMainTab = "personal" },
                        modifier = Modifier.testTag("personal_debts_tab")
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TabButton(
                        label = "Split Groups",
                        isSelected = localMainTab == "groups",
                        icon = Icons.Default.Share,
                        onClick = { localMainTab = "groups" },
                        modifier = Modifier.testTag("group_debts_tab")
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TabButton(
                        label = "Trips & Events",
                        isSelected = localMainTab == "trips",
                        icon = Icons.Default.Place,
                        onClick = { localMainTab = "trips" },
                        modifier = Modifier.testTag("trip_events_tab")
                    )
                }
            }

            // Central Core Interface
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (localMainTab == "trips") {
                    TripWorkspaceMain(
                        currencyFormatter = currencyFormatter,
                        tripViewModel = tripViewModel
                    )
                } else {
                    when (val state = uiState) {
                        is SplitUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        is SplitUiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        is SplitUiState.Success -> {
                            if (state.currentGroup != null) {
                                // Render Group Detail Screen
                                GroupDetailsWorkspace(
                                    state = state,
                                    currencyFormatter = currencyFormatter,
                                    splitViewModel = splitViewModel,
                                    onBack = { splitViewModel.selectGroup(null) }
                                )
                            } else {
                                if (localMainTab == "personal") {
                                    // Mode 1 Simple lent/borrowed tracker
                                    PersonalDebtTrackerScreen(
                                        state = state,
                                        currencyFormatter = currencyFormatter,
                                        splitViewModel = splitViewModel
                                    )
                                } else {
                                    // Mode 2 Group Share dashboard
                                    GroupDashboardScreen(
                                        state = state,
                                        currencyFormatter = currencyFormatter,
                                        splitViewModel = splitViewModel,
                                        onCreateGroupTrigger = { showCreateGroupModal = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Group Floating Action Button (Only on the groups split tab list!)
        val showFab = !(uiState as? SplitUiState.Success)?.currentGroup.let { it != null } && localMainTab == "groups"
        if (showFab) {
            FloatingActionButton(
                onClick = { showCreateGroupModal = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_group_fab_button"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Group")
            }
        }

        // Create Group Dialog
        if (showCreateGroupModal) {
            CreateGroupDialog(
                onDismiss = { showCreateGroupModal = false },
                onSave = { name, desc, type, members ->
                    splitViewModel.addGroup(name, desc, type, members)
                    showCreateGroupModal = false
                }
            )
        }
    }
}

@Composable
fun PersonalDebtTrackerScreen(
    state: SplitUiState.Success,
    currencyFormatter: NumberFormat,
    splitViewModel: SplitViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Balanced Core
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (state.netBalance >= 0.0) {
                    Color(0xFF15803D).copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                }
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (state.netBalance >= 0.0) Color(0xFF15803D).copy(alpha = 0.3f) else MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "AGGREGATED NET LEDGERS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (state.netBalance >= 0.0) "Net Owed To You" else "Net You Owe Peers",
                        fontSize = 13.sp,
                        color = if (state.netBalance >= 0.0) Color(0xFF15803D) else MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    text = currencyFormatter.format(Math.abs(state.netBalance)),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (state.netBalance >= 0.0) Color(0xFF15803D) else MaterialTheme.colorScheme.error
                )
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Who owes you
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 6.dp)
            ) {
                Text(
                    text = "WHO OWES YOU ⬇",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (state.youAreOwedList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pending recoveries.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.youAreOwedList) { peer ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = peer.memberName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = currencyFormatter.format(peer.netBalance),
                                        color = Color(0xFF15803D),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Whom you owe
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 6.dp)
            ) {
                Text(
                    text = "WHOM YOU OWE ⬆",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (state.youOweList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No outstanding splits.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(state.youOweList) { peer ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = peer.memberName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = currencyFormatter.format(peer.netBalance),
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
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
fun GroupDashboardScreen(
    state: SplitUiState.Success,
    currencyFormatter: NumberFormat,
    splitViewModel: SplitViewModel,
    onCreateGroupTrigger: () -> Unit
) {
    if (state.groups.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Empty",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No groups created yet.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your first group to start tracking shared expenses, event splits, and settlements.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreateGroupTrigger,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create New Group")
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.groups) { group ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { splitViewModel.selectGroup(group.id) }
                        .testTag("group_card_${group.id}"),
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
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ) {
                                    Text(
                                        text = group.groupType.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = group.groupName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = group.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Net balance context
                            Column(horizontalAlignment = Alignment.End) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Split Group Context",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "TAP TO WORKSPACE",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupDetailsWorkspace(
    state: SplitUiState.Success,
    currencyFormatter: NumberFormat,
    splitViewModel: SplitViewModel,
    onBack: () -> Unit
) {
    val group = state.currentGroup ?: return
    var activeSubTab by remember { mutableStateOf("overview") } // "overview", "members", "expenses", "settlements"
    
    var showAddExpenseModal by remember { mutableStateOf(false) }
    var showAddSettlementModal by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Back toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = group.groupName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = group.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { splitViewModel.deleteGroup(group.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Group",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Subtabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = { activeSubTab = "overview" }) {
                Text(
                    text = "OVERVIEW",
                    fontWeight = if (activeSubTab == "overview") FontWeight.Bold else FontWeight.Normal,
                    color = if (activeSubTab == "overview") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = { activeSubTab = "members" }) {
                Text(
                    text = "MEMBERS",
                    fontWeight = if (activeSubTab == "members") FontWeight.Bold else FontWeight.Normal,
                    color = if (activeSubTab == "members") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = { activeSubTab = "expenses" }) {
                Text(
                    text = "EXPENSES",
                    fontWeight = if (activeSubTab == "expenses") FontWeight.Bold else FontWeight.Normal,
                    color = if (activeSubTab == "expenses") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            TextButton(onClick = { activeSubTab = "settlements" }) {
                Text(
                    text = "SETTLEMENTS",
                    fontWeight = if (activeSubTab == "settlements") FontWeight.Bold else FontWeight.Normal,
                    color = if (activeSubTab == "settlements") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        Divider()

        // Tab views and Bottom floating button actions
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (activeSubTab) {
                "overview" -> GroupOverviewTab(state, currencyFormatter, onAddExpense = { showAddExpenseModal = true }, onAddSettlement = { showAddSettlementModal = true })
                "members" -> GroupMembersTab(state, currencyFormatter)
                "expenses" -> GroupExpensesTab(state, currencyFormatter, splitViewModel)
                "settlements" -> GroupSettlementsTab(state, currencyFormatter, splitViewModel)
            }
        }
    }

    // Modal dialogs
    if (showAddExpenseModal) {
        AddExpenseDialog(
            members = state.currentMembers,
            onDismiss = { showAddExpenseModal = false },
            onSave = { title, amt, paidBy, cat, notes, type, shares, involved ->
                splitViewModel.addExpense(group.id, title, amt, paidBy, cat, notes, type, shares, involved)
                showAddExpenseModal = false
            }
        )
    }

    if (showAddSettlementModal) {
        AddSettlementDialog(
            members = state.currentMembers,
            onDismiss = { showAddSettlementModal = false },
            onSave = { payer, receiver, amt, notes ->
                splitViewModel.addSettlement(group.id, payer, receiver, amt, notes)
                showAddSettlementModal = false
            }
        )
    }
}

@Composable
fun GroupOverviewTab(
    state: SplitUiState.Success,
    currencyFormatter: NumberFormat,
    onAddExpense: () -> Unit,
    onAddSettlement: () -> Unit
) {
    val totalExpense = state.currentExpenses.sumOf { it.amount }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Cards Block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "TOTAL EXPENSES", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormatter.format(totalExpense),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                val myBalInGroup = state.currentBalances.find { it.memberName == "You" }?.netBalance ?: 0.0
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "YOUR BALANCE", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currencyFormatter.format(myBalInGroup),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (myBalInGroup >= 0.0) Color(0xFF15803D) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Quick Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAddExpense,
                modifier = Modifier.weight(1f).testTag("group_add_expense_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Expense")
            }

            Button(
                onClick = onAddSettlement,
                modifier = Modifier.weight(1f).testTag("group_add_settlement_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Settle Up")
            }
        }

        // Simplified Debt suggestions
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DEBT SIMPLIFICATION PLAN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (state.currentSimplifiedDebts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "All balances cleared! No settlements needed. ✨",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.currentSimplifiedDebts.forEach { debt ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = debt.fromUser,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = " pays ",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = debt.toUser,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF15803D)
                                    )
                                }
                                Text(
                                    text = currencyFormatter.format(debt.amount),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
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

@Composable
fun GroupMembersTab(
    state: SplitUiState.Success,
    currencyFormatter: NumberFormat
) {
    if (state.currentMembers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No members registered.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.currentMembers) { member ->
                val netBal = state.currentBalances.find { it.memberName == member.memberName }?.netBalance ?: 0.0
                val customAvatarColors = listOf(
                    Color(0xFF3B82F6), Color(0xFFEF4444), Color(0xFF10B981), Color(0xFFF59E0B),
                    Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF14B8A6)
                )
                val avatarBg = customAvatarColors.getOrElse(member.colorIdentifier) { Color.Gray }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(avatarBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.memberName.take(1).uppercase(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = member.memberName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = currencyFormatter.format(netBal),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (netBal >= 0.0) com.example.NeonGreen else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = if (netBal >= 0.0) "is owed" else "owes",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupExpensesTab(
    state: SplitUiState.Success,
    currencyFormatter: NumberFormat,
    splitViewModel: SplitViewModel
) {
    if (state.currentExpenses.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No group expenses recorded yet.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.currentExpenses) { expense ->
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expense.expenseDate))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                    Text(
                                        text = expense.category.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = expense.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Paid by ${expense.paidBy} on $dateStr",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currencyFormatter.format(expense.amount),
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { splitViewModel.deleteExpense(expense.id, expense.groupId) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (expense.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Notes: ${expense.notes}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupSettlementsTab(
    state: SplitUiState.Success,
    currencyFormatter: NumberFormat,
    splitViewModel: SplitViewModel
) {
    if (state.currentSettlements.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No settlements entered yet.")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.currentSettlements) { set ->
                val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(set.date))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = set.payer, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp).padding(horizontal = 2.dp)
                                )
                                Text(text = set.receiver, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                            Text(text = "Settled on $dateStr", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (set.notes.isNotBlank()) {
                                Text(text = "Notes: ${set.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currencyFormatter.format(set.amount),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(onClick = { splitViewModel.deleteSettlement(set.id, set.groupId) }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onSave: (groupName: String, desc: String, type: String, members: List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Trip") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var currentMemberInput by remember { mutableStateOf("") }
    val membersList = remember { mutableStateListOf<String>() }

    val types = listOf("Trip", "Home", "Sports", "Other")

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
                    text = "Create Split Group",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name (e.g. Goa Trip)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_group_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (e.g. Flat expenses)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_group_desc_input"),
                    singleLine = true
                )

                // Dropdown Selector for group type
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("group_type_trigger")
                    ) {
                        Text(text = "Type: $selectedType", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    selectedType = t
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // Add Member Flow
                Text(
                    text = "MEMBERS INVOLVED (Note: 'You' is auto-added)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentMemberInput,
                        onValueChange = { currentMemberInput = it },
                        label = { Text("Member Name") },
                        modifier = Modifier.weight(1f).testTag("group_member_input_field"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (currentMemberInput.isNotBlank() && !membersList.contains(currentMemberInput) && currentMemberInput != "You") {
                                membersList.add(currentMemberInput.trim())
                                currentMemberInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add member chip")
                    }
                }

                // Render dynamic member chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Default core 'You'
                    SuggestionChip(
                        onClick = {},
                        label = { Text("You (Myself)") }
                    )
                    membersList.forEach { m ->
                        InputChip(
                            selected = true,
                            onClick = { membersList.remove(m) },
                            label = { Text(m) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(name, desc, selectedType, membersList.toList())
                        },
                        modifier = Modifier.testTag("group_save_btn"),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save Group")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExpenseDialog(
    members: List<MemberEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, paidBy: String, category: String, notes: String, splitType: String, shares: String, involved: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf("You") }
    var activeCategory by remember { mutableStateOf("Food") }
    var notes by remember { mutableStateOf("") }
    
    var selectedSplitType by remember { mutableStateOf("EQUAL") } // EQUAL, EXACT, PERCENT, SHARE
    
    // Dropdowns
    var expandedPayerDropdown by remember { mutableStateOf(false) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    var expandedSplitDropdown by remember { mutableStateOf(false) }

    val categories = listOf("Food", "Travel", "Fuel", "Hotel", "Entertainment", "Sports", "Shopping", "Utilities", "Other")
    val splitTypes = listOf("EQUAL", "EXACT", "PERCENT", "SHARE")

    // Dynamic inputs mapped per member for exact/percent/share values!
    val participantMap = remember { mutableStateMapOf<String, String>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Add Group Expense",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title (e.g. Dinner)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_expense_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("add_expense_amount_input"),
                    singleLine = true
                )

                // Paid By Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedPayerDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("expense_payer_trigger")
                    ) {
                        Text(text = "Paid By: $paidBy", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedPayerDropdown,
                        onDismissRequest = { expandedPayerDropdown = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.memberName) },
                                onClick = {
                                    paidBy = m.memberName
                                    expandedPayerDropdown = false
                                }
                            )
                        }
                    }
                }

                // Category Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedCategoryDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Category: $activeCategory", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedCategoryDropdown,
                        onDismissRequest = { expandedCategoryDropdown = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    activeCategory = c
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                // Split Type Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedSplitDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("expense_split_trigger")
                    ) {
                        Text(text = "Split Type: $selectedSplitType", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedSplitDropdown,
                        onDismissRequest = { expandedSplitDropdown = false }
                    ) {
                        splitTypes.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    selectedSplitType = s
                                    expandedSplitDropdown = false
                                }
                            )
                        }
                    }
                }

                // Render specific input fields for EXACT, PERCENT, or SHARE
                if (selectedSplitType != "EQUAL") {
                    Text(
                        text = "ENTER INDIVIDUAL WEIGHTS:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    members.forEach { member ->
                        val name = member.memberName
                        var currentVal by remember(selectedSplitType) { mutableStateOf("") }
                        OutlinedTextField(
                            value = participantMap[name] ?: "",
                            onValueChange = {
                                participantMap[name] = it
                            },
                            label = {
                                val literalLabel = when (selectedSplitType) {
                                    "EXACT" -> "Amount for $name"
                                    "PERCENT" -> "Percent % for $name"
                                    "SHARE" -> "Shares / Ratio weight for $name"
                                    else -> "Value"
                                }
                                Text(literalLabel)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
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
                        onClick = {
                            val amtVal = amount.toDoubleOrNull() ?: 0.0
                            val namesStr = members.map { it.memberName }.joinToString(",")
                            
                            // Build comma-separated weights list
                            val sharesStr = members.map { m ->
                                val inputVal = participantMap[m.memberName]
                                inputVal?.toDoubleOrNull() ?: when (selectedSplitType) {
                                    "PERCENT" -> 100.0 / members.size
                                    "SHARE" -> 1.0
                                    else -> amtVal / members.size
                                }
                            }.joinToString(",")

                            onSave(title, amtVal, paidBy, activeCategory, notes, selectedSplitType, sharesStr, namesStr)
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
fun AddSettlementDialog(
    members: List<MemberEntity>,
    onDismiss: () -> Unit,
    onSave: (payer: String, receiver: String, amount: Double, notes: String) -> Unit
) {
    if (members.size < 2) {
        Dialog(onDismissRequest = onDismiss) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Need at least 2 members to register settlement.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onDismiss) { Text("OK") }
                }
            }
        }
        return
    }

    var payer by remember { mutableStateOf(members[0].memberName) }
    var receiver by remember { mutableStateOf(members[1].memberName) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var expandedPayerDropdown by remember { mutableStateOf(false) }
    var expandedReceiverDropdown by remember { mutableStateOf(false) }

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
                    text = "Record Settle Payment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Payer Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedPayerDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("settle_payer_trigger")
                    ) {
                        Text(text = "Payer (Paid Money): $payer", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedPayerDropdown,
                        onDismissRequest = { expandedPayerDropdown = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.memberName) },
                                onClick = {
                                    payer = m.memberName
                                    expandedPayerDropdown = false
                                }
                            )
                        }
                    }
                }

                // Receiver Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedReceiverDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("settle_receiver_trigger")
                    ) {
                        Text(text = "Receiver (Got Money): $receiver", fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(
                        expanded = expandedReceiverDropdown,
                        onDismissRequest = { expandedReceiverDropdown = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.memberName) },
                                onClick = {
                                    receiver = m.memberName
                                    expandedReceiverDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("settle_amount_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (e.g. UPI transfer)") },
                    modifier = Modifier.fillMaxWidth(),
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
                        onClick = {
                            val amtVal = amount.toDoubleOrNull() ?: 0.0
                            onSave(payer, receiver, amtVal, notes)
                        },
                        modifier = Modifier.testTag("settle_submit_btn"),
                        enabled = amount.isNotBlank() && payer != receiver
                    ) {
                        Text("Record Settle")
                    }
                }
            }
        }
    }
}
