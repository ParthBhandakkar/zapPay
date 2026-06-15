package com.zappay.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
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

    fun loginRoute(role: String) = "login/$role"
}

@Composable
fun ZapPayNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.WELCOME,
) {
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {
        // ── Welcome ──
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onCustomerClick = { navController.navigate(Routes.loginRoute("customer")) },
                onPumpClick = { navController.navigate(Routes.loginRoute("pump_operator")) },
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
            val vm: CustomerViewModel = viewModel()
            CustomerDashboardScreen(
                viewModel = vm,
                onNavigateToWallet = { navController.navigate(Routes.CUSTOMER_WALLET) },
                onNavigateToQR = { navController.navigate(Routes.CUSTOMER_QR) },
            )
        }

        composable(Routes.CUSTOMER_WALLET) {
            val vm: CustomerViewModel = viewModel()
            WalletScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.CUSTOMER_QR) {
            val vm: CustomerViewModel = viewModel()
            QRCodeScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.CUSTOMER_HISTORY) {
            val vm: CustomerViewModel = viewModel()
            HistoryScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.CUSTOMER_PROFILE) {
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

        // ── Pump ──
        composable(Routes.PUMP_DASHBOARD) {
            val vm: PumpViewModel = viewModel()
            PumpDashboardScreen(
                viewModel = vm,
                onNavigateToScanner = { navController.navigate(Routes.PUMP_SCANNER) },
                onNavigateToSettings = { navController.navigate(Routes.PUMP_SETTINGS) },
                onSetupPump = { navController.navigate(Routes.SETUP_PUMP) },
            )
        }

        composable(Routes.PUMP_SCANNER) {
            val vm: PumpViewModel = viewModel()
            ScannerScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.PUMP_SETTINGS) {
            val vm: PumpViewModel = viewModel()
            PumpSettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETUP_PUMP) {
            val vm: PumpViewModel = viewModel()
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
    }
}
