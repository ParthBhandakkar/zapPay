package com.zappay.app.ui.pump

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.util.formatDate
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    viewModel: TransactionDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { ZapPayTopBar(title = "Receipt", onBack = onBack) },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary500)
            }
        } else if (state.error != null) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.error!!, color = Danger500, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                ZapPayButton(text = "Go Back", onClick = onBack, variant = ButtonVariant.OUTLINE)
            }
        } else {
            val r = state.receipt ?: return@Scaffold

            Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            "PAYMENT RECEIPT",
                            fontWeight = FontWeight.Bold,
                            color = Primary500,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("#${r.receiptNumber}", fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(20.dp))

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))

                        r.pumpName?.let { name ->
                            SectionHeader("Pump")
                            Spacer(Modifier.height(4.dp))
                            Text(name, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                            r.pumpAddress?.let { addr -> Text(addr, color = Neutral500, fontSize = 13.sp) }
                            Spacer(Modifier.height(16.dp))
                        }

                        SectionHeader("Customer")
                        Spacer(Modifier.height(4.dp))
                        r.customerName?.let { DetailRow("Name", it) }
                        r.customerPhone?.let { DetailRow("Phone", it) }
                        r.vehicleNumber?.let { DetailRow("Vehicle", it) }
                        Spacer(Modifier.height(16.dp))

                        SectionHeader("Fuel Details")
                        Spacer(Modifier.height(4.dp))
                        r.fuelType?.let { DetailRow("Type", it) }
                        r.fuelQuantity?.let { DetailRow("Quantity", "${"%.2f".format(it)} L") }
                        r.fuelRate?.let { DetailRow("Rate", "₹ ${"%.2f".format(it)}/L") }
                        Spacer(Modifier.height(16.dp))

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))

                        DetailRow("Total Amount", "₹ ${"%.2f".format(r.amount)}", isBold = true)
                        r.commissionAmount?.let { DetailRow("Commission", "₹ ${"%.2f".format(it)}") }
                        Spacer(Modifier.height(16.dp))

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))

                        SectionHeader("Wallet")
                        Spacer(Modifier.height(4.dp))
                        r.walletBalanceBefore?.let { DetailRow("Balance Before", "₹ ${"%.2f".format(it)}") }
                        r.walletBalanceAfter?.let { DetailRow("Balance After", "₹ ${"%.2f".format(it)}") }
                        Spacer(Modifier.height(16.dp))

                        SectionHeader("Status")
                        Spacer(Modifier.height(4.dp))
                        val statusColor = when (r.status) {
                            "completed" -> Success500
                            "pending" -> Warning500
                            "failed" -> Danger500
                            else -> Neutral500
                        }
                        ZapPayBadge(text = r.status.replaceFirstChar { it.uppercase() }, color = statusColor)
                        Spacer(Modifier.height(8.dp))
                        Text("Created: ${r.createdAt.formatDate()}", color = Neutral500, fontSize = 12.sp)
                        r.completedAt?.let {
                            Text("Completed: ${it.formatDate()}", color = Neutral500, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Thank you for your business!", color = Neutral500, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Neutral500, fontSize = 14.sp)
        Text(
            value,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            fontSize = if (isBold) 16.sp else 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
