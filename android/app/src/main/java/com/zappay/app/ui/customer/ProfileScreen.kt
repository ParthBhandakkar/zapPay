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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.data.remote.dto.UserProfileDto
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
    profileData: UserProfileDto? = null,
    onSaveProfile: ((Map<String, String>) -> Unit)? = null,
) {
    val state by viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) }
    var isEditing by remember { mutableStateOf(false) }

    var editName by remember(profileData) { mutableStateOf(profileData?.fullName ?: userName ?: "") }
    var editEmail by remember(profileData) { mutableStateOf(profileData?.email ?: "") }
    var editAddress by remember(profileData) { mutableStateOf(profileData?.address ?: "") }
    var editCity by remember(profileData) { mutableStateOf(profileData?.city ?: "") }
    var editState by remember(profileData) { mutableStateOf(profileData?.state ?: "") }
    var editPincode by remember(profileData) { mutableStateOf(profileData?.pincode ?: "") }
    var editVehicleNumber by remember(profileData) { mutableStateOf(profileData?.vehicleNumber ?: "") }
    var editVehicleType by remember(profileData) { mutableStateOf(profileData?.vehicleType ?: "") }

    val isCustomer = viewModel != null

    LaunchedEffect(Unit) { viewModel?.loadVehicleInfo() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back", color = Purple500) } },
                actions = {
                    if (profileData != null) {
                        TextButton(onClick = { isEditing = !isEditing }) {
                            Text(if (isEditing) "View" else "Edit", color = Purple500)
                        }
                    }
                },
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
                    ((profileData?.fullName ?: userName)?.firstOrNull()?.uppercase() ?: "U"),
                    fontSize = 32.sp, fontWeight = FontWeight.Bold, color = White,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(profileData?.fullName ?: userName ?: "User", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Gray900)
            Text(profileData?.phoneNumber ?: userPhone ?: "", fontSize = 14.sp, color = Gray500)

            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isCustomer) Green100 else Blue100),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    if (isCustomer) "Customer" else "Pump Owner",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = if (isCustomer) Green500 else Blue500,
                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(32.dp))

            if (isEditing && profileData != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Edit Profile", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Gray900)
                        Spacer(Modifier.height(16.dp))
                        ZapPayInput(value = editName, onValueChange = { editName = it }, label = "Full Name")
                        Spacer(Modifier.height(12.dp))
                        ZapPayInput(value = editEmail, onValueChange = { editEmail = it }, label = "Email", keyboardType = KeyboardType.Email)
                        Spacer(Modifier.height(12.dp))
                        ZapPayInput(value = editAddress, onValueChange = { editAddress = it }, label = "Address")
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ZapPayInput(value = editCity, onValueChange = { editCity = it }, label = "City", modifier = Modifier.weight(1f))
                            ZapPayInput(value = editState, onValueChange = { editState = it }, label = "State", modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        ZapPayInput(value = editPincode, onValueChange = { editPincode = it }, label = "Pincode", keyboardType = KeyboardType.Number)
                        Spacer(Modifier.height(12.dp))
                        ZapPayInput(value = editVehicleNumber, onValueChange = { editVehicleNumber = it }, label = "Vehicle Number")
                        Spacer(Modifier.height(12.dp))
                        Text("Vehicle Type", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Gray700)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Bike", "Scooty", "Car", "Auto").forEach { vt ->
                                FilterChip(
                                    selected = editVehicleType == vt,
                                    onClick = { editVehicleType = vt },
                                    label = { Text(vt, fontSize = 13.sp) },
                                )
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        ZapPayButton(
                            text = "Save Changes",
                            onClick = {
                                onSaveProfile?.invoke(
                                    mapOf(
                                        "full_name" to editName,
                                        "email" to editEmail,
                                        "address" to editAddress,
                                        "city" to editCity,
                                        "state" to editState,
                                        "pincode" to editPincode,
                                        "vehicle_number" to editVehicleNumber,
                                        "vehicle_type" to editVehicleType,
                                    ).filter { it.value.isNotEmpty() }
                                )
                                isEditing = false
                            },
                            enabled = editName.isNotEmpty(),
                        )
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        ProfileRow("Name", profileData?.fullName ?: userName ?: "-")
                        ProfileDivider()
                        ProfileRow("Phone", profileData?.phoneNumber ?: userPhone ?: "-")
                        ProfileDivider()
                        ProfileRow("Email", profileData?.email ?: "-")
                        ProfileDivider()
                        ProfileRow("Address", profileData?.address ?: "-")
                        ProfileDivider()
                        ProfileRow("City", profileData?.city ?: "-")
                        ProfileDivider()
                        ProfileRow("State", profileData?.state ?: "-")
                        ProfileDivider()
                        ProfileRow("Pincode", profileData?.pincode ?: "-")
                        ProfileDivider()
                        ProfileRow("Vehicle", profileData?.vehicleNumber ?: "-")
                        ProfileDivider()
                        ProfileRow("Role", if (isCustomer) "Customer" else "Pump Owner")
                    }
                }
            }

            if (isCustomer) {
                Spacer(Modifier.height(24.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Vehicle Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Gray900)
                        Spacer(Modifier.height(16.dp))
                        val vehicleNumber = state?.vehicleNumber ?: profileData?.vehicleNumber ?: ""
                        val vehicleType = state?.vehicleType ?: profileData?.vehicleType ?: ""
                        ZapPayInput(
                            value = vehicleNumber,
                            onValueChange = { viewModel?.saveVehicleInfo(it, vehicleType) },
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
                                    onClick = { viewModel?.saveVehicleInfo(vehicleNumber, vt) },
                                    label = { Text(vt, fontSize = 13.sp) },
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        ZapPayButton(
                            text = "Save Vehicle",
                            onClick = { viewModel?.saveVehicleInfo(vehicleNumber, vehicleType) },
                            enabled = vehicleNumber.length >= 4 && vehicleType.isNotEmpty(),
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

@Composable
private fun ProfileDivider() {
    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Gray100)
}
