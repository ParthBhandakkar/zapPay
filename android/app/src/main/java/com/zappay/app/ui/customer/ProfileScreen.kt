package com.zappay.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.ZapPayButton
import com.zappay.app.ui.components.ZapPayInput
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String?,
    userPhone: String?,
    onLogout: () -> Unit,
    onBack: () -> Unit,
    viewModel: CustomerViewModel? = null,
) {
    val state by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    var vehicleNumber by remember(state?.vehicleNumber) { mutableStateOf(state?.vehicleNumber ?: "") }
    var vehicleType by remember(state?.vehicleType) { mutableStateOf(state?.vehicleType ?: "") }
    var vehicleSaved by remember { mutableStateOf(false) }

    val isCustomer = viewModel != null

    LaunchedEffect(Unit) {
        viewModel?.loadVehicleInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Purple500) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier.size(80.dp).background(Purple500, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    (userName?.firstOrNull()?.uppercase() ?: "U"),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(userName ?: "User", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Text(userPhone ?: "", fontSize = 14.sp, color = Gray500)

            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isCustomer) Green100 else Blue100),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(if (isCustomer) "Customer" else "Pump Owner", modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = if (isCustomer) Green500 else Blue500, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(32.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    ProfileRow("Name", userName ?: "-")
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Gray100)
                    ProfileRow("Phone", userPhone ?: "-")
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Gray100)
                    ProfileRow("Role", if (isCustomer) "Customer" else "Pump Owner")
                }
            }

            if (isCustomer) {
                Spacer(Modifier.height(24.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Vehicle Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Gray900)
                        Spacer(Modifier.height(16.dp))

                        ZapPayInput(
                            value = vehicleNumber,
                            onValueChange = { vehicleNumber = it; vehicleSaved = false },
                            label = "Vehicle Number",
                            placeholder = "MH01AB1234",
                        )
                        Spacer(Modifier.height(12.dp))

                        Text("Vehicle Type", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray700)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Bike", "Scooty", "Car", "Auto").forEach { vt ->
                                FilterChip(
                                    selected = vehicleType == vt,
                                    onClick = { vehicleType = vt; vehicleSaved = false },
                                    label = { Text(vt, fontSize = 13.sp) },
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        ZapPayButton(
                            text = if (vehicleSaved) "Saved" else "Save Vehicle",
                            onClick = {
                                viewModel?.saveVehicleInfo(vehicleNumber, vehicleType)
                                vehicleSaved = true
                            },
                            enabled = vehicleNumber.length >= 4 && vehicleType.isNotEmpty() && !vehicleSaved,
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            ZapPayButton(
                text = "Logout",
                onClick = onLogout,
                variant = com.zappay.app.ui.components.ButtonVariant.OUTLINE,
            )
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Gray500, fontSize = 14.sp)
        Text(value, color = Gray900, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
