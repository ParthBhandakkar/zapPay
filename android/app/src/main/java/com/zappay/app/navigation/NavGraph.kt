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
