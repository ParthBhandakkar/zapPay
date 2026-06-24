package com.zappay.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.data.remote.dto.UserProfileDto
import com.zappay.app.data.remote.dto.VehicleDto
import com.zappay.app.ui.components.ZapPayButton
import com.zappay.app.ui.components.ZapPayInput
import com.zappay.app.ui.components.ButtonVariant
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
    vehicles: List<VehicleDto> = emptyList(),
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

    LaunchedEffect(Unit) {
        viewModel?.loadVehicleInfo()
        viewModel?.loadVehicles()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Premium Profile Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Primary900, Primary700),
                            start = Offset(0f, 0f),
                            end = Offset(1000f, 600f),
                        )
                    )
                    .padding(top = 16.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // Top bar row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                        }
                        Text("Profile", fontWeight = FontWeight.SemiBold, color = White, fontSize = 18.sp)
                        if (profileData != null) {
                            TextButton(onClick = { isEditing = !isEditing }) {
                                Text(
                                    if (isEditing) "View" else "Edit",
                                    color = Accent400,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        } else {
                            Spacer(Modifier.width(48.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Primary500, Teal500),
                                    start = Offset.Zero,
                                    end = Offset(200f, 200f),
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            ((profileData?.fullName ?: userName)?.firstOrNull()?.uppercase() ?: "U"),
                            fontSize = 36.sp, fontWeight = FontWeight.Bold, color = White,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        profileData?.fullName ?: userName ?: "User",
                        fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White,
                    )
                    Text(
                        profileData?.phoneNumber ?: userPhone ?: "",
                        fontSize = 14.sp, color = Primary200,
                    )
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCustomer) Success500.copy(alpha = 0.2f) else Info500.copy(alpha = 0.2f),
                        ),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            if (isCustomer) "Customer" else "Pump Owner",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            color = if (isCustomer) Success400 else Info400,
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // ── Content ──
            Column(Modifier.padding(16.dp)) {
                if (isEditing && profileData != null) {
                    // Edit form
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Edit Profile", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
                            Text("Vehicle Type", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Neutral700)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Bike", "Scooty", "Car", "Auto").forEach { vt ->
                                    FilterChip(
                                        selected = editVehicleType == vt,
                                        onClick = { editVehicleType = vt },
                                        label = { Text(vt, fontSize = 13.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Primary50,
                                            selectedLabelColor = Primary700,
                                        ),
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
                    // ── Profile Info Sections ──
                    ProfileSection(title = "Account") {
                        ProfileRow(icon = Icons.Outlined.Person, label = "Name", value = profileData?.fullName ?: userName ?: "-")
                        ProfileDivider()
                        ProfileRow(icon = Icons.Outlined.Phone, label = "Phone", value = profileData?.phoneNumber ?: userPhone ?: "-")
                        ProfileDivider()
                        ProfileRow(icon = Icons.Outlined.Email, label = "Email", value = profileData?.email ?: "-")
                    }

                    Spacer(Modifier.height(16.dp))

                    ProfileSection(title = "Address") {
                        ProfileRow(icon = Icons.Outlined.Home, label = "Address", value = profileData?.address ?: "-")
                        ProfileDivider()
                        ProfileRow(icon = Icons.Outlined.LocationCity, label = "City", value = profileData?.city ?: "-")
                        ProfileDivider()
                        ProfileRow(icon = Icons.Outlined.Map, label = "State", value = profileData?.state ?: "-")
                        ProfileDivider()
                        ProfileRow(icon = Icons.Outlined.Pin, label = "Pincode", value = profileData?.pincode ?: "-")
                    }

                    Spacer(Modifier.height(16.dp))

                    ProfileSection(title = "Vehicle") {
                        ProfileRow(icon = Icons.Outlined.DirectionsCar, label = "Vehicle", value = profileData?.vehicleNumber ?: "-")
                        ProfileDivider()
                        ProfileRow(icon = Icons.Outlined.Category, label = "Type", value = profileData?.vehicleType ?: "-")
                    }
                }

                // ── My Vehicles ──
                if (isCustomer) {
                    Spacer(Modifier.height(16.dp))
                    ProfileSection(title = "My Vehicles") {
                        if (vehicles.isEmpty()) {
                            Text("No vehicles added", fontSize = 14.sp, color = Neutral500, modifier = Modifier.padding(8.dp))
                        } else {
                            vehicles.forEach { v ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Primary50),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(Icons.Outlined.DirectionsCar, contentDescription = null, tint = Primary500, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(v.vehicleNumber, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Row {
                                            v.vehicleType?.let {
                                                Text(it, fontSize = 12.sp, color = Neutral500)
                                            }
                                            if (v.isPrimary) {
                                                Text("  ·  Primary", fontSize = 12.sp, color = Accent600, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                                if (v != vehicles.last()) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Logout ──
                ZapPayButton(
                    text = "Logout",
                    onClick = onLogout,
                    variant = ButtonVariant.OUTLINE,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Text(
            title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Neutral500,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Primary500, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, color = Neutral500, fontSize = 14.sp, modifier = Modifier.width(70.dp))
        Spacer(Modifier.width(8.dp))
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
}
