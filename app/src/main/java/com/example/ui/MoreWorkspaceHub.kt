package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.viewmodel.WealthPulseViewModel
import com.example.ui.theme.*
import com.example.ui.components.*

@Composable
fun MoreWorkspaceHub(
    onNavigate: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WealthPulseViewModel = viewModel()
) {
    var showAltNotifications by remember { mutableStateOf(false) }
    val creditCards by viewModel.creditCards.collectAsState()
    val emiLoans by viewModel.emiLoans.collectAsState()
    val sipRecords by viewModel.sipRecords.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("more_workspace_hub")
    ) {
        // More page header
        Text(
            text = "MORE ACTIONS & MODULES",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF78716C), // Stone 500
            letterSpacing = 2.sp,
            fontFamily = FontFamily.SansSerif
        )
        Text(
            text = "Advanced Workspace",
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = TextWhite,
            fontFamily = FontFamily.Serif,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Notification toggle statement card
        if (showAltNotifications) {
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔔 ACTIVE NOTIFICATIONS",
                            fontSize = 10.sp,
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hide Alerts",
                            tint = TextGray,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { showAltNotifications = false }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val alertsList = remember(creditCards, emiLoans, sipRecords) {
                        val list = mutableListOf<String>()
                        creditCards.forEach { cc ->
                            if (cc.outstandingAmount > 0) {
                                list.add("• ${cc.cardName} bill payment of ₹${Math.round(cc.outstandingAmount)} is due soon (Bill date: ${cc.billDate}th).")
                            }
                        }
                        emiLoans.forEach { emi ->
                            if (emi.remainingMonths > 0) {
                                list.add("• Loan liability ${emi.description}: ₹${Math.round(emi.amount)} due monthly (${emi.remainingMonths} months left).")
                            }
                        }
                        sipRecords.forEach { sip ->
                            list.add("• Monthly investment SIP of ₹${Math.round(sip.amount)} for ${sip.description} scheduled on ${sip.dayOfMonth}th.")
                        }
                        if (list.isEmpty()) {
                            list.add("• Vault pristine: All credit card outstanding bills, loans, and investment pipelines are fully settled and clear! No alerts.")
                        }
                        list
                    }

                    alertsList.forEach { alert ->
                        Text(
                            text = alert,
                            fontSize = 11.sp,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // Feature grids
        AppSectionHeader(title = "Management Spaces")

        val items = listOf(
            Triple("Credit Cards", Icons.Default.Send, "credit"),
            Triple("EMI Loans", Icons.Default.AccountBox, "emi"),
            Triple("SIP Trackers", Icons.Default.PlayArrow, "sip"),
            Triple("Budget Goals", Icons.Default.Star, "budgets"),
            Triple("Investments", Icons.Default.Favorite, "portfolio"),
            Triple("Shared Trips", Icons.Default.Share, "trips"),
            Triple("Notifications", Icons.Default.Notifications, "alerts"),
            Triple("App Settings", Icons.Default.Settings, "settings")
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (i in items.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val firstItem = items[i]
                    MoreHubGridCard(
                        title = firstItem.first,
                        icon = firstItem.second,
                        onClick = {
                            when (firstItem.third) {
                                "portfolio" -> onNavigateToCategory("Equity Portfolio")
                                "trips" -> onNavigate("lent") // Maps to Splitting and Trips workspace!
                                "alerts" -> showAltNotifications = true
                                else -> onNavigate(firstItem.third)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (i + 1 < items.size) {
                        val secondItem = items[i + 1]
                        MoreHubGridCard(
                            title = secondItem.first,
                            icon = secondItem.second,
                            onClick = {
                                when (secondItem.third) {
                                    "portfolio" -> onNavigateToCategory("Equity Portfolio")
                                    "trips" -> onNavigate("lent") // Maps to Groups & Trips!
                                    "alerts" -> showAltNotifications = true
                                    else -> onNavigate(secondItem.third)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun MoreHubGridCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        onClick = onClick,
        modifier = modifier
            .height(110.dp)
            .testTag("more_hub_${title.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(NavyBg)
                    .border(BorderStroke(1.dp, BorderColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NeonGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
    }
}
