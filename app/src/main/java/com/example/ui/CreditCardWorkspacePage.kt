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
import androidx.compose.material.icons.automirrored.filled.*
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
import com.example.ui.viewmodel.CreditCardUiState
import com.example.ui.viewmodel.CreditCardViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun CreditCardWorkspaceHub(
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier,
    creditCardViewModel: CreditCardViewModel = viewModel()
) {
    val uiState by creditCardViewModel.uiState.collectAsState()

    var showAddCardModal by remember { mutableStateOf(false) }
    var selectedCardForDetail by remember { mutableStateOf<CreditCardWithDetails?>(null) }
    var showRecordPaymentModalForCard by remember { mutableStateOf<CreditCardEntity?>(null) }
    var showAddStatementModalForCard by remember { mutableStateOf<CreditCardEntity?>(null) }
    var showAddEMIModalForCard by remember { mutableStateOf<CreditCardEntity?>(null) }

    // Synchronize detail state inside popup if DB updates
    LaunchedEffect(uiState, selectedCardForDetail) {
        val currentD = selectedCardForDetail
        if (currentD != null && uiState is CreditCardUiState.Success) {
            val matching = (uiState as CreditCardUiState.Success).cardsWithDetails
                .find { it.card.id == currentD.card.id }
            selectedCardForDetail = matching
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is CreditCardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is CreditCardUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error Logo",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error Loading Credit Card System",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            is CreditCardUiState.Success -> {
                CreditCardDashboardScreen(
                    cards = state.cardsWithDetails,
                    totalOutstanding = state.totalOutstanding,
                    totalLimit = state.totalLimit,
                    overallUtilizationPercentage = state.overallUtilizationPercentage,
                    activeEMIs = state.activeEMIs,
                    currencyFormatter = currencyFormatter,
                    onCardClick = { selectedCardForDetail = it },
                    onAddCardClick = { showAddCardModal = true },
                    onRecordPaymentClick = { showRecordPaymentModalForCard = it },
                    onAddStatementClick = { showAddStatementModalForCard = it },
                    onAddEMIClick = { showAddEMIModalForCard = it }
                )
            }
        }

        // 1. Add Credit Card Modal
        if (showAddCardModal) {
            AddCreditCardDialog(
                viewModel = creditCardViewModel,
                onDismiss = { showAddCardModal = false }
            )
        }

        // 2. Comprehensive Details Dialog
        val detailCard = selectedCardForDetail
        if (detailCard != null) {
            CardDetailsDialog(
                cardDetails = detailCard,
                currencyFormatter = currencyFormatter,
                viewModel = creditCardViewModel,
                onDismiss = { selectedCardForDetail = null },
                onRecordPayment = { showRecordPaymentModalForCard = it },
                onAddStatement = { showAddStatementModalForCard = it },
                onAddEMI = { showAddEMIModalForCard = it }
            )
        }

        // 3. Record Payment Screen popup
        val paymentCard = showRecordPaymentModalForCard
        if (paymentCard != null) {
            RecordPaymentDialog(
                card = paymentCard,
                currencyFormatter = currencyFormatter,
                viewModel = creditCardViewModel,
                onDismiss = { showRecordPaymentModalForCard = null }
            )
        }

        // 4. Add Statement popup
        val statementCard = showAddStatementModalForCard
        if (statementCard != null) {
            AddStatementDialog(
                card = statementCard,
                viewModel = creditCardViewModel,
                onDismiss = { showAddStatementModalForCard = null }
            )
        }

        // 5. Add EMI dialog
        val emiCard = showAddEMIModalForCard
        if (emiCard != null) {
            AddEMIDialog(
                card = emiCard,
                viewModel = creditCardViewModel,
                onDismiss = { showAddEMIModalForCard = null }
            )
        }
    }
}

@Composable
fun CreditCardDashboardScreen(
    cards: List<CreditCardWithDetails>,
    totalOutstanding: Double,
    totalLimit: Double,
    overallUtilizationPercentage: Double,
    activeEMIs: List<CardEMIEntity>,
    currencyFormatter: NumberFormat,
    onCardClick: (CreditCardWithDetails) -> Unit,
    onAddCardClick: () -> Unit,
    onRecordPaymentClick: (CreditCardEntity) -> Unit,
    onAddStatementClick: (CreditCardEntity) -> Unit,
    onAddEMIClick: (CreditCardEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // App Launcher header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CREDIT CARD MANAGER PRO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "MYFin Cards",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Button(
                onClick = onAddCardClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("add_card_header_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Card", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (cards.isEmpty()) {
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
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "No Credit Cards",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No credit cards added yet.",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add your first credit card to start tracking spending, monitoring credit utilization, and planning payment due dates.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onAddCardClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("setup_first_card_btn")
                ) {
                    Text("Add Credit Card Manual Portfolio", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // General Outstanding Panel
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOTAL CREDIT DEBT OVERVIEW",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Overall Outstanding", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                currencyFormatter.format(totalOutstanding),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (overallUtilizationPercentage > 75.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Approved Limit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                currencyFormatter.format(totalLimit),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Utilization Meter Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Utilization: ${overallUtilizationPercentage.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = getRatioColor(overallUtilizationPercentage)
                        )

                        val categoryLevelString = when {
                            overallUtilizationPercentage > 100.0 -> "Exceeded"
                            overallUtilizationPercentage >= 76.0 -> "Critical"
                            overallUtilizationPercentage >= 51.0 -> "High"
                            overallUtilizationPercentage >= 31.0 -> "Moderate"
                            else -> "Healthy"
                        }
                        Text(
                            text = categoryLevelString,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = getRatioColor(overallUtilizationPercentage),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(getRatioColor(overallUtilizationPercentage).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                                .fillMaxWidth((overallUtilizationPercentage / 100).toFloat().coerceIn(0f, 1f))
                                .clip(CircleShape)
                                .background(getRatioColor(overallUtilizationPercentage))
                        )
                    }
                }
            }

            // Cards Scroll List header
            Text(
                text = "YOUR ACTIVE PLASTIC CARDS (${cards.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(cards) { cardDetails ->
                    CardListItemRow(
                        details = cardDetails,
                        currencyFormatter = currencyFormatter,
                        onClick = { onCardClick(cardDetails) }
                    )
                }
            }
        }
    }
}

@Composable
fun CardListItemRow(
    details: CreditCardWithDetails,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit
) {
    val card = details.card
    val networkLogo = when (card.cardNetwork.lowercase()) {
        "visa" -> "💳 Visa"
        "mastercard" -> "💳 Mastercard"
        "rupay" -> "💳 RuPay"
        "american express" -> "💳 Amex"
        "diners club" -> "💳 Diners"
        else -> "💳 Card"
    }

    val pct = details.utilizationPercentage
    val levelColor = getRatioColor(pct)

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("card_portfolio_${card.cardName.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // First Row: Bank Name & Network
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(levelColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (card.bankName.isNotEmpty()) card.bankName.take(2).uppercase() else "CC",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelColor
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = card.cardName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${card.bankName} • ${card.cardType}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Digits & Network Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (card.lastFourDigits.isNotEmpty()) "**** ${card.lastFourDigits}" else networkLogo,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Due: ${card.dueDate}th of month",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Limits & Owed Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Owed Outstanding", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        currencyFormatter.format(card.outstandingAmount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Available limit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        currencyFormatter.format(details.availableCredit),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Approved Limit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        currencyFormatter.format(card.creditLimit),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar & Level
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((pct / 100).toFloat().coerceIn(0f, 1f))
                            .clip(CircleShape)
                            .background(levelColor)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${pct.toInt()}% Utilized",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = levelColor
                )
            }
        }
    }
}

// Dialog: Add Credit Card Model Form
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddCreditCardDialog(
    viewModel: CreditCardViewModel,
    onDismiss: () -> Unit
) {
    var cardName by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("Premium") }
    var cardNetwork by remember { mutableStateOf("Visa") }
    var lastFourDigits by remember { mutableStateOf("") }
    var creditLimitStr by remember { mutableStateOf("") }
    var billingDateStr by remember { mutableStateOf("15") }
    var dueDateStr by remember { mutableStateOf("5") }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("add_credit_card_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "REGISTER NEW CREDIT CARD",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Configure Credit Line",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (errorMsg != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = errorMsg ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Inputs
                OutlinedTextField(
                    value = cardName,
                    onValueChange = { cardName = it },
                    label = { Text("Card Nickname (e.g., ICICI Amazon Pay)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name (e.g., ICICI Bank)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Selectors for Network & Type
                Text("Card Network", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Visa", "Mastercard", "RuPay", "American Express", "Diners Club", "Other").forEach { net ->
                        val isSel = cardNetwork == net
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { cardNetwork = net }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(net, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                OutlinedTextField(
                    value = creditLimitStr,
                    onValueChange = { creditLimitStr = it },
                    label = { Text("Credit Limit (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = billingDateStr,
                        onValueChange = { billingDateStr = it },
                        label = { Text("Billing Date (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = dueDateStr,
                        onValueChange = { dueDateStr = it },
                        label = { Text("Payment Due Date") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = lastFourDigits,
                        onValueChange = { lastFourDigits = it },
                        label = { Text("Last 4 digits") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = cardType,
                        onValueChange = { cardType = it },
                        label = { Text("Card Type (Cashback, Premium, standard)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val limit = creditLimitStr.toDoubleOrNull() ?: 0.0
                            val billDay = billingDateStr.toIntOrNull() ?: 15
                            val dueDay = dueDateStr.toIntOrNull() ?: 5

                            if (cardName.isBlank() || bankName.isBlank()) {
                                errorMsg = "Card name and Bank name cannot be blank."
                                return@Button
                            }
                            if (limit <= 0) {
                                errorMsg = "Approved limit must be greater than 0."
                                return@Button
                            }
                            if (billDay !in 1..31 || dueDay !in 1..31) {
                                errorMsg = "Days must be between 1 and 31."
                                return@Button
                            }

                            viewModel.addCreditCard(
                                cardName = cardName,
                                bankName = bankName,
                                cardType = cardType,
                                cardNetwork = cardNetwork,
                                lastFourDigits = lastFourDigits,
                                creditLimit = limit,
                                billingDate = billDay,
                                dueDate = dueDay,
                                onResult = { success, msg ->
                                    if (success) {
                                        onDismiss()
                                    } else {
                                        errorMsg = msg
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("submit_credit_card_btn")
                    ) {
                        Text("Add Manual Card", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Dialog: Cards Detail Popup Screen
@Composable
fun CardDetailsDialog(
    cardDetails: CreditCardWithDetails,
    currencyFormatter: NumberFormat,
    viewModel: CreditCardViewModel,
    onDismiss: () -> Unit,
    onRecordPayment: (CreditCardEntity) -> Unit,
    onAddStatement: (CreditCardEntity) -> Unit,
    onAddEMI: (CreditCardEntity) -> Unit
) {
    val card = cardDetails.card
    val outstanding = card.outstandingAmount
    val limit = card.creditLimit
    val available = cardDetails.availableCredit
    val pct = cardDetails.utilizationPercentage

    val statementsState = remember { viewModel.getStatementsFlow(card.id) }.collectAsState(initial = emptyList())
    val statements = statementsState.value

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("card_detail_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${card.bankName.uppercase()} PORTFOLIO",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = card.cardName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // outstanding and limits block
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Current Outstanding", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Available Credit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currencyFormatter.format(outstanding),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = getRatioColor(pct)
                            )
                            Text(
                                text = currencyFormatter.format(available),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((pct / 100).toFloat().coerceIn(0f, 1f))
                                    .clip(CircleShape)
                                    .background(getRatioColor(pct))
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${pct.toInt()}% Utilized • ${getRatioLevel(pct)}", fontSize = 11.sp, color = getRatioColor(pct), fontWeight = FontWeight.Bold)
                            Text("Full Limit: ${currencyFormatter.format(limit)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Billing info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Billing Cycle Day", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${card.billingDate}th / month", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Payment Due", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${card.dueDate}th / month", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Core quick payment details button actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onRecordPayment(card) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Pay", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onAddStatement(card) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Statement", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Statement", fontSize = 11.sp)
                    }
                }

                // statements tracker
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MANUAL STATEMENT HISTORY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }

                if (statements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No manual statements added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            statements.forEachIndexed { idx, st ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        val endStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(st.statementEndDate))
                                        Text("Statement ending $endStr", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Min Due: ${currencyFormatter.format(st.minimumDue)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            currencyFormatter.format(st.statementAmount),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )

                                        val badgeColor = if (st.paymentStatus == "Paid") Color(0xFF10B981) else Color(0xFFEF4444)
                                        Text(
                                            st.paymentStatus,
                                            color = badgeColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(badgeColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (idx < statements.size - 1) {
                                    Divider()
                                }
                            }
                        }
                    }
                }

                // EMIs Tracker
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONVERTED EMIS & RECURRING LOANS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = { onAddEMI(card) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add EMI", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }

                val emisState = remember { viewModel.getStatementsFlow(card.id) } // fallback or collect EMIs
                // Fetch live from viewmodel success UIState
                val uiSuccess = viewModel.uiState.value as? CreditCardUiState.Success
                val cardEMIs = uiSuccess?.activeEMIs?.filter { it.cardId == card.id } ?: emptyList()

                if (cardEMIs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No EMIs converted yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            cardEMIs.forEachIndexed { idx, emi ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(emi.description, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("EMI amount: ${currencyFormatter.format(emi.emiAmount)} / month", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${emi.remainingInstallments} / ${emi.tenureMonths} remaining", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Pay: " + currencyFormatter.format(emi.purchaseAmount),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (idx < cardEMIs.size - 1) {
                                    Divider()
                                }
                            }
                        }
                    }
                }

                // Recent direct spend
                Text(
                    text = "RECENT DIRECT TRANSACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (cardDetails.recentTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions logged on this card.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            cardDetails.recentTransactions.forEachIndexed { idx, tx ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(tx.description, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        val dfStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(tx.timestamp))
                                        Text("$dfStr • ${tx.category}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        currencyFormatter.format(tx.amount),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (idx < cardDetails.recentTransactions.size - 1) {
                                    Divider()
                                }
                            }
                        }
                    }
                }

                // Delete card
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(
                        onClick = {
                            viewModel.deleteCreditCard(card.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Plastic Card Portfolio")
                    }
                }
            }
        }
    }
}

// Dialog: Record Payment Modal
@Composable
fun RecordPaymentDialog(
    card: CreditCardEntity,
    currencyFormatter: NumberFormat,
    viewModel: CreditCardViewModel,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var sourceAccount by remember { mutableStateOf("Primary Bank Account") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("record_payment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "RECORD CARD PAYMENT",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Clear Card Debt",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (errorMsg != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(errorMsg ?: "", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Payment Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = sourceAccount,
                    onValueChange = { sourceAccount = it },
                    label = { Text("Source Account (e.g., HDFC Salary Bank)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Memo / Notes") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt <= 0) {
                                errorMsg = "Amount must be a positive number."
                                return@Button
                            }

                            viewModel.recordPayment(
                                cardId = card.id,
                                amount = amt,
                                paymentDate = System.currentTimeMillis(),
                                source = sourceAccount,
                                notes = notes,
                                onResult = { success, msg ->
                                    if (success) {
                                        onDismiss()
                                    } else {
                                        errorMsg = msg
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).testTag("confirm_payment_btn")
                    ) {
                        Text("Pay Off Debt", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Dialog: Add Statement Modal
@Composable
fun AddStatementDialog(
    card: CreditCardEntity,
    viewModel: CreditCardViewModel,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var minDue by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("add_statement_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "RECORD BILL STATEMENT",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Load New Statement",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (errorMsg != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(errorMsg ?: "", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Statement Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = minDue,
                    onValueChange = { minDue = it },
                    label = { Text("Minimum Amount Due (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: -1.0
                            val minVal = minDue.toDoubleOrNull() ?: 0.0

                            if (amt < 0) {
                                errorMsg = "Statement amount cannot be negative."
                                return@Button
                            }

                            viewModel.addStatement(
                                cardId = card.id,
                                amount = amt,
                                minimumDue = minVal,
                                startDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30),
                                endDate = System.currentTimeMillis(),
                                dueDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(15),
                                onResult = { success, msg ->
                                    if (success) {
                                        onDismiss()
                                    } else {
                                        errorMsg = msg
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).testTag("save_statement_btn")
                    ) {
                        Text("Apply Statement", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Dialog: Add EMI tracking Modal
@Composable
fun AddEMIDialog(
    card: CreditCardEntity,
    viewModel: CreditCardViewModel,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var tenureStr by remember { mutableStateOf("6") }
    var emiAmtStr by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("add_emi_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "ESTABLISH EMI RECURRING LIAB",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "EMI Purchase Convert",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (errorMsg != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Text(errorMsg ?: "", color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details (e.g., iPhone 15 Pro Max)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it
                        val amt = it.toDoubleOrNull() ?: 0.0
                        val ten = tenureStr.toIntOrNull() ?: 6
                        if (amt > 0 && ten > 0) {
                            emiAmtStr = (amt / ten).toInt().toString()
                        }
                    },
                    label = { Text("Purchase Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = tenureStr,
                        onValueChange = { 
                            tenureStr = it
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            val ten = it.toIntOrNull() ?: 6
                            if (amt > 0 && ten > 0) {
                                emiAmtStr = (amt / ten).toInt().toString()
                            }
                        },
                        label = { Text("Tenure Months") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = emiAmtStr,
                        onValueChange = { emiAmtStr = it },
                        label = { Text("EMI / month") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val purchaseVal = amount.toDoubleOrNull() ?: 0.0
                            val tenureVal = tenureStr.toIntOrNull() ?: 0
                            val emiValue = emiAmtStr.toDoubleOrNull() ?: 0.0

                            if (description.isBlank()) {
                                errorMsg = "Description cannot be empty."
                                return@Button
                            }
                            if (purchaseVal <= 0 || tenureVal <= 0 || emiValue <= 0) {
                                errorMsg = "Please input valid parameters."
                                return@Button
                            }

                            viewModel.createCardEMI(
                                cardId = card.id,
                                description = description,
                                purchaseAmount = purchaseVal,
                                purchaseDate = System.currentTimeMillis(),
                                tenure = tenureVal,
                                emiAmount = emiValue,
                                onResult = { success, msg ->
                                    if (success) {
                                        onDismiss()
                                    } else {
                                        errorMsg = msg
                                    }
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).testTag("save_emi_btn")
                    ) {
                        Text("Convert Purchase", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Utility Ratio styling mappings
@Composable
private fun getRatioColor(percentage: Double): Color {
    return when {
        percentage > 100.0 -> Color(0xFFEF4444)
        percentage >= 76.0 -> Color(0xFFEF4444)
        percentage >= 51.0 -> Color(0xFFF97316)
        percentage >= 31.0 -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }
}

fun getRatioLevel(percentage: Double): String {
    return when {
        percentage > 100.0 -> "Exceeded Limit"
        percentage >= 76.0 -> "Critical (>=76%)"
        percentage >= 51.0 -> "High (51-75%)"
        percentage >= 31.0 -> "Moderate (31-50%)"
        else -> "Healthy (<30d)"
    }
}
