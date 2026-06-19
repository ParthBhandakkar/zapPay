package com.zappay.app.ui.pump

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
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    viewModel: TransactionDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Purple500) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(state.error!!, color = Red500, fontSize = 14.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack) { Text("Go Back") }
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
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "PAYMENT RECEIPT",
                            fontWeight = FontWeight.Bold,
                            color = Purple500,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("#${r.receiptNumber}", fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(16.dp))

                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))

                        r.pumpName?.let { name ->
                            SectionHeader("Pump")
                            Text(name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Gray900)
                            r.pumpAddress?.let { addr -> Text(addr, color = Gray500, fontSize = 13.sp) }
                            Spacer(Modifier.height(8.dp))
                        }

                        SectionHeader("Customer")
                        r.customerName?.let { DetailRow("Name", it) }
                        r.customerPhone?.let { DetailRow("Phone", it) }
                        r.vehicleNumber?.let { DetailRow("Vehicle", it) }
                        Spacer(Modifier.height(8.dp))

                        SectionHeader("Fuel Details")
                        r.fuelType?.let { DetailRow("Type", it) }
                        r.fuelQuantity?.let { DetailRow("Quantity", "${"%.2f".format(it)} L") }
                        r.fuelRate?.let { DetailRow("Rate", "₹ ${"%.2f".format(it)}/L") }
                        Spacer(Modifier.height(8.dp))

                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        DetailRow("Total Amount", "₹ ${"%.2f".format(r.amount)}", isBold = true)
                        r.commissionAmount?.let { DetailRow("Commission", "₹ ${"%.2f".format(it)}") }
                        Spacer(Modifier.height(8.dp))

                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        SectionHeader("Wallet")
                        r.walletBalanceBefore?.let { DetailRow("Balance Before", "₹ ${"%.2f".format(it)}") }
                        r.walletBalanceAfter?.let { DetailRow("Balance After", "₹ ${"%.2f".format(it)}") }
                        Spacer(Modifier.height(8.dp))

                        SectionHeader("Status")
                        val statusColor = when (r.status) {
                            "completed" -> Green500
                            "pending" -> Yellow500
                            "failed" -> Red500
                            else -> Gray500
                        }
                        Text(r.status.replaceFirstChar { it.uppercase() }, color = statusColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Created: ${r.createdAt.formatDate()}", color = Gray500, fontSize = 12.sp)
                        r.completedAt?.let {
                            Text("Completed: ${it.formatDate()}", color = Gray500, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Thank you for your business!", color = Gray500, fontSize = 12.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(label, fontWeight = FontWeight.SemiBold, color = Gray700, fontSize = 11.sp, letterSpacing = 1.sp)
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun DetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Gray500, fontSize = 13.sp)
        Text(value, fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal, fontSize = 13.sp)
    }
}
