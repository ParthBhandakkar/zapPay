package com.zappay.app.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zappay.app.ui.auth.AuthViewModel
import com.zappay.app.ui.auth.LoginScreen
import com.zappay.app.ui.auth.WelcomeScreen
import com.zappay.app.ui.customer.*
import com.zappay.app.ui.pump.*

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login/{role}"
    const val CUSTOMER_DASHBOARD = "customer_dashboard"
    const val CUSTOMER_WALLET = "customer_wallet"
    const val CUSTOMER_QR = "customer_qr"
    const val CUSTOMER_HISTORY = "customer_history"
    const val CUSTOMER_PROFILE = "customer_profile"
    const val CUSTOMER_VEHICLES = "customer_vehicles"
    const val CUSTOMER_NOTIFICATIONS = "customer_notifications"
    const val CUSTOMER_TICKETS = "customer_tickets"
    const val NEARBY_PUMPS = "nearby_pumps"
    const val PUMP_DETAIL = "pump_detail/{pumpId}?pumpName={pumpName}&address={address}&distanceKm={distanceKm}&isOpen={isOpen}"

    fun pumpDetailRoute(
        pumpId: Int,
        pumpName: String = "",
        address: String = "",
        distanceKm: Double = 0.0,
        isOpen: Boolean = true,
    ) = "pump_detail/$pumpId?pumpName=${java.net.URLEncoder.encode(pumpName, "UTF-8")}&address=${java.net.URLEncoder.encode(address, "UTF-8")}&distanceKm=$distanceKm&isOpen=$isOpen"
    const val PUMP_DASHBOARD = "pump_dashboard"
    const val PUMP_SCANNER = "pump_scanner"
    const val PUMP_SETTINGS = "pump_settings"
    const val SETUP_PUMP = "setup_pump"
    const val PUMP_PROFILE = "pump_profile"
    const val TRANSACTION_DETAIL = "transaction_detail/{transactionId}"

    fun loginRoute(role: String) = "login/$role"
    fun transactionDetailRoute(transactionId: String) = "transaction_detail/$transactionId"
}

@Composable
fun ZapPayNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.WELCOME,
) {
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = startDestination) {
        // ── Welcome ──
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onCustomerClick = { navController.navigate(Routes.loginRoute("customer")) },
                onPumpClick = { navController.navigate(Routes.loginRoute("pump_owner")) },
                onDirectToCustomer = {
                    navController.navigate(Routes.CUSTOMER_DASHBOARD) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onDirectToPump = {
                    navController.navigate(Routes.PUMP_DASHBOARD) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                authViewModel = authViewModel,
            )
        }

        // ── Login ──
        composable(
            route = Routes.LOGIN,
            arguments = listOf(navArgument("role") { type = NavType.StringType }),
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "customer"
            LoginScreen(
                role = role,
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLoginSuccess = { r ->
                    when (r) {
                        "pump_operator", "pump_owner" -> {
                            navController.navigate(Routes.PUMP_DASHBOARD) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                            }
                        }
                        else -> {
                            navController.navigate(Routes.CUSTOMER_DASHBOARD) {
                                popUpTo(Routes.WELCOME) { inclusive = true }
                            }
                        }
                    }
                },
            )
        }

        // ── Customer ──
        composable(Routes.CUSTOMER_DASHBOARD) {
            val vm: CustomerViewModel = hiltViewModel()
            CustomerDashboardScreen(
                viewModel = vm,
                onNavigateToWallet = { navController.navigate(Routes.CUSTOMER_WALLET) },
                onNavigateToQR = { navController.navigate(Routes.CUSTOMER_QR) },
                onNavigateToProfile = { navController.navigate(Routes.CUSTOMER_PROFILE) },
                onNavigateToVehicles = { navController.navigate(Routes.CUSTOMER_VEHICLES) },
                onNavigateToNotifications = { navController.navigate(Routes.CUSTOMER_NOTIFICATIONS) },
                onNavigateToTickets = { navController.navigate(Routes.CUSTOMER_TICKETS) },
                onNavigateToNearbyPumps = { navController.navigate(Routes.NEARBY_PUMPS) },
            )
        }

        composable(Routes.CUSTOMER_WALLET) {
            val vm: CustomerViewModel = hiltViewModel()
            WalletScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.CUSTOMER_QR) {
            val vm: CustomerViewModel = hiltViewModel()
            QRCodeScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.CUSTOMER_HISTORY) {
            val vm: CustomerViewModel = hiltViewModel()
            HistoryScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onTransactionClick = { transactionId ->
                    navController.navigate(Routes.transactionDetailRoute(transactionId))
                },
            )
        }

        composable(Routes.CUSTOMER_PROFILE) {
            val vm: CustomerViewModel = hiltViewModel()
            ProfileScreen(
                userName = null,
                userPhone = null,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                viewModel = vm,
            )
        }

        composable(Routes.CUSTOMER_VEHICLES) {
            val vm: VehiclesViewModel = hiltViewModel()
            VehiclesScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.CUSTOMER_NOTIFICATIONS) {
            val vm: NotificationsViewModel = hiltViewModel()
            NotificationsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.CUSTOMER_TICKETS) {
            val vm: SupportTicketsViewModel = hiltViewModel()
            SupportTicketsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.NEARBY_PUMPS) {
            val vm: NearbyPumpsViewModel = hiltViewModel()
            NearbyPumpsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onPumpClick = { pump ->
                    navController.navigate(
                        Routes.pumpDetailRoute(
                            pumpId = pump.id,
                            pumpName = pump.pumpName,
                            address = pump.address,
                            distanceKm = pump.distanceKm,
                            isOpen = pump.isOpen ?: true,
                        )
                    )
                },
            )
        }

        composable(
            route = Routes.PUMP_DETAIL,
            arguments = listOf(
                navArgument("pumpId") { type = NavType.IntType },
                navArgument("pumpName") { type = NavType.StringType; defaultValue = "Pump" },
                navArgument("address") { type = NavType.StringType; defaultValue = "" },
                navArgument("distanceKm") { type = NavType.StringType; defaultValue = "0.0" },
                navArgument("isOpen") { type = NavType.StringType; defaultValue = "true" },
            ),
        ) {
            val vm: PumpDetailViewModel = hiltViewModel()
            PumpDetailScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        // ── Pump ──
        composable(Routes.PUMP_DASHBOARD) {
            val vm: PumpViewModel = hiltViewModel()
            PumpDashboardScreen(
                viewModel = vm,
                onNavigateToScanner = { navController.navigate(Routes.PUMP_SCANNER) },
                onNavigateToSettings = { navController.navigate(Routes.PUMP_SETTINGS) },
                onSetupPump = { navController.navigate(Routes.SETUP_PUMP) },
                onNavigateToProfile = { navController.navigate(Routes.PUMP_PROFILE) },
            )
        }

        composable(Routes.PUMP_SCANNER) {
            val vm: PumpViewModel = hiltViewModel()
            ScannerScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.PUMP_SETTINGS) {
            val vm: PumpViewModel = hiltViewModel()
            PumpSettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETUP_PUMP) {
            val vm: PumpViewModel = hiltViewModel()
            SetupPumpScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Routes.PUMP_DASHBOARD) {
                        popUpTo(Routes.PUMP_DASHBOARD) { inclusive = true }
                    }
                },
            )
        }

        // ── Pump Profile ──
        composable(Routes.PUMP_PROFILE) {
            ProfileScreen(
                userName = null,
                userPhone = null,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        // ── Transaction Detail ──
        composable(
            route = Routes.TRANSACTION_DETAIL,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType }),
        ) {
            val vm: TransactionDetailViewModel = hiltViewModel()
            TransactionDetailScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
