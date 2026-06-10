package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.text.NumberFormat

@Composable
fun ReportsWorkspacePage(
    dailyExpenses: List<DailyExpenseEntity>,
    creditExpenses: List<CreditExpenseEntity>,
    incomePaydays: List<IncomePaydayEntity>,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
    val totalIncome = incomePaydays.sumOf { it.amount }
    val totalExpense = dailyExpenses.sumOf { it.amount } + creditExpenses.sumOf { it.amount }
    val totalSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) (totalSavings / totalIncome) * 100 else 0.0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("reports_workspace_page")
    ) {
        // Report Page Header
        Text(
            text = "FINANCIAL REPORTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF78716C), // Stone 500
            letterSpacing = 2.sp,
            fontFamily = FontFamily.SansSerif
        )
        Text(
            text = "Analysis & Trends",
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = TextWhite,
            fontFamily = FontFamily.Serif,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Hero Savings Rate Card
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "MONTHLY SAVINGS RATE",
                    fontSize = 10.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.1f%%", savingsRate.coerceAtLeast(0.0)),
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Light,
                        color = if (savingsRate >= 20.0) NeonGreen else AccentOrange
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                if (savingsRate >= 20.0) NeonGreen.copy(alpha = 0.15f) else AccentOrange.copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (savingsRate >= 20.0) "HEALTHY 🎉" else "KEEP TRACK 📋",
                            fontSize = 9.sp,
                            color = if (savingsRate >= 20.0) NeonGreen else AccentOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Cash Flow Statement Block
        Text(
            text = "▪ Cashflow Breakdown",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportStatementRow(
                    label = "Total Income Inflow",
                    amount = currencyFormatter.format(totalIncome),
                    color = NeonGreen
                )
                HorizontalDivider(color = BorderColor)
                ReportStatementRow(
                    label = "Logged Expenses Outflow",
                    amount = currencyFormatter.format(totalExpense),
                    color = TextWhite
                )
                HorizontalDivider(color = BorderColor)
                ReportStatementRow(
                    label = "Net Current Balance",
                    amount = currencyFormatter.format(totalSavings),
                    color = if (totalSavings >= 0) NeonGreen else DangerRed
                )
            }
        }

        // Category Spend Share Block
        Text(
            text = "▪ Category Outflow Breakdown",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextGray,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderColor),
            colors = CardDefaults.cardColors(containerColor = SurfaceBlue),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 100.dp)
        ) {
            val allExpenses = dailyExpenses + creditExpenses.map {
                DailyExpenseEntity(
                    amount = it.amount,
                    currency = it.currency,
                    description = it.description,
                    category = it.category,
                    paymentMode = "Credit Card",
                    userId = it.userId,
                    timestamp = it.timestamp
                )
            }
            
            if (allExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No outflow records located", fontSize = 11.sp, color = TextGray)
                }
            } else {
                val groupExps = allExpenses.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amount } }
                val sortedGroupExps = groupExps.entries.sortedByDescending { it.value }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    sortedGroupExps.forEach { (cat, spent) ->
                        val pct = if (totalExpense > 0) (spent / totalExpense).toFloat() else 0.0f
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = cat, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text(
                                    text = "${currencyFormatter.format(spent)} (${String.format("%.1f", pct * 100)}%)",
                                    fontSize = 11.sp,
                                    color = TextGray
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
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
                                        .fillMaxWidth(pct)
                                        .clip(CircleShape)
                                        .background(NeonGreen)
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
fun ReportStatementRow(
    label: String,
    amount: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextGray, fontWeight = FontWeight.Medium)
        Text(text = amount, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
