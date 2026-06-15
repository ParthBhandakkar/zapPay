package com.zappay.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zappay.app.ui.components.ZapPayCard
import com.zappay.app.ui.theme.*

@Composable
fun WelcomeScreen(
    onCustomerClick: () -> Unit,
    onPumpClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.2f))

        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Purple500, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("Z", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = White)
        }
        Spacer(Modifier.height(16.dp))
        Text("ZapPay", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gray900)
        Text(
            "The smart way to pay for fuel",
            fontSize = 14.sp,
            color = Gray500,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(0.3f))

        // Role Selection
        Text("SELECT YOUR ROLE", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Gray500)
        Spacer(Modifier.height(16.dp))

        ZapPayCard(onClick = onCustomerClick) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(48.dp).background(Purple50, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("U", fontSize = 22.sp, color = Purple500, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Customer", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                    Text("I want to pay for fuel & track expenses", fontSize = 13.sp, color = Gray500)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        ZapPayCard(onClick = onPumpClick) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(48.dp).background(Blue100, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("P", fontSize = 22.sp, color = Blue500, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Pump Operator", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                    Text("I want to scan codes & accept payments", fontSize = 13.sp, color = Gray500)
                }
            }
        }

        Spacer(Modifier.weight(0.2f))
    }
}
