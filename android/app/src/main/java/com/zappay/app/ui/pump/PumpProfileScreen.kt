package com.zappay.app.ui.pump

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.*
import com.zappay.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumpProfileScreen(
    viewModel: PumpViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    var editPumpName by remember { mutableStateOf("") }
    var editAddress by remember { mutableStateOf("") }
    var editCity by remember { mutableStateOf("") }
    var editState by remember { mutableStateOf("") }
    var editPincode by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadMyPump() }

    LaunchedEffect(state.myPump) {
        state.myPump?.let {
            editPumpName = it.pumpName
            editAddress = it.address
            editCity = it.city
            editState = it.state
            editPincode = it.pincode
            editPhone = it.phoneNumber
            editEmail = it.email ?: ""
        }
    }

    Scaffold(
        topBar = {
            ZapPayTopBar(
                title = "Profile",
                onBack = onBack,
                actions = {
                    if (state.myPump != null) {
                        TextButton(onClick = { isEditing = !isEditing }) {
                            Text(if (isEditing) "Cancel" else "Edit", color = Primary500, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
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
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier.size(88.dp).background(Primary500, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    (state.myPump?.pumpName?.firstOrNull()?.uppercase() ?: "P"),
                    fontSize = 36.sp, fontWeight = FontWeight.Bold, color = White,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(state.myPump?.pumpName ?: "Pump Station", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(state.myPump?.phoneNumber ?: "", fontSize = 14.sp, color = Neutral500)

            Spacer(Modifier.height(12.dp))
            ZapPayBadge(text = "Pump Owner", color = Info500)

            Spacer(Modifier.height(32.dp))

            if (state.myPump == null && state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp), color = Primary500)
            } else if (state.myPump != null && isEditing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text("Edit Pump Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(20.dp))
                        ZapPayInput(value = editPumpName, onValueChange = { editPumpName = it }, label = "Pump Name")
                        Spacer(Modifier.height(16.dp))
                        ZapPayInput(value = editAddress, onValueChange = { editAddress = it }, label = "Address")
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ZapPayInput(value = editCity, onValueChange = { editCity = it }, label = "City", modifier = Modifier.weight(1f))
                            ZapPayInput(value = editState, onValueChange = { editState = it }, label = "State", modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(16.dp))
                        ZapPayInput(value = editPincode, onValueChange = { editPincode = it }, label = "Pincode", keyboardType = KeyboardType.Number)
                        Spacer(Modifier.height(16.dp))
                        ZapPayInput(value = editPhone, onValueChange = { editPhone = it }, label = "Phone", keyboardType = KeyboardType.Phone)
                        Spacer(Modifier.height(16.dp))
                        ZapPayInput(value = editEmail, onValueChange = { editEmail = it }, label = "Email", keyboardType = KeyboardType.Email)
                        Spacer(Modifier.height(24.dp))
                        ZapPayButton(
                            text = "Save Changes",
                            onClick = {
                                viewModel.updatePump(
                                    state.myPump!!.id,
                                    editPumpName, editAddress, editCity, editState,
                                    editPincode, editPhone, editEmail
                                )
                                isEditing = false
                            },
                            enabled = editPumpName.isNotEmpty(),
                        )
                    }
                }
            } else if (state.myPump != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        PumpProfileRow("Pump Name", state.myPump!!.pumpName)
                        PumpProfileDivider()
                        PumpProfileRow("License", state.myPump!!.licenseNumber)
                        PumpProfileDivider()
                        PumpProfileRow("Address", state.myPump!!.address)
                        PumpProfileDivider()
                        PumpProfileRow("City", state.myPump!!.city)
                        PumpProfileDivider()
                        PumpProfileRow("State", state.myPump!!.state)
                        PumpProfileDivider()
                        PumpProfileRow("Pincode", state.myPump!!.pincode)
                        PumpProfileDivider()
                        PumpProfileRow("Phone", state.myPump!!.phoneNumber)
                        PumpProfileDivider()
                        PumpProfileRow("Email", state.myPump!!.email ?: "-")
                        PumpProfileDivider()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Status", color = Neutral500, fontSize = 14.sp)
                            ZapPayBadge(
                                text = if (state.myPump!!.isOpen == true) "Open" else "Closed",
                                color = if (state.myPump!!.isOpen == true) Success500 else Danger500
                            )
                        }
                    }
                }
            }

            if (state.error != null) {
                Spacer(Modifier.height(16.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Danger50), shape = RoundedCornerShape(12.dp)) {
                    Text(state.error!!, color = Danger500, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                }
            }

            Spacer(Modifier.height(40.dp))
            ZapPayButton(
                text = "Logout",
                onClick = onLogout,
                variant = ButtonVariant.OUTLINE,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PumpProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Neutral500, fontSize = 14.sp)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PumpProfileDivider() {
    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
}
