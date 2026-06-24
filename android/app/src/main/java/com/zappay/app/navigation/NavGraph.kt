package com.zappay.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zappay.app.ui.auth.AuthViewModel
import com.zappay.app.ui.auth.LoginScreen
import com.zappay.app.ui.auth.SplashScreen
import com.zappay.app.ui.auth.WelcomeScreen
import com.zappay.app.ui.customer.*
import com.zappay.app.ui.pump.*
import com.zappay.app.ui.theme.*
import com.zappay.app.util.LocationHelper

object Routes {
    const val SPLASH = "splash"
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
    startDestination: String = Routes.SPLASH,
) {
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { screenEnterTransition },
        exitTransition = { screenExitTransition },
        popEnterTransition = { screenPopEnterTransition },
        popExitTransition = { screenPopExitTransition },
    ) {
        // ── Splash ──
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

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

        // ── Customer Main (with Bottom Navigation) ──
        composable(Routes.CUSTOMER_DASHBOARD) {
            val vm: CustomerViewModel = hiltViewModel()
            var selectedTab by remember { mutableStateOf(0) }

            CustomerMainScreen(
                selectedTab = selectedTab,
                onTabChanged = { selectedTab = it },
            ) { padding ->
                when (selectedTab) {
                    0 -> CustomerDashboardScreen(
                        viewModel = vm,
                        onNavigateToWallet = { navController.navigate(Routes.CUSTOMER_WALLET) },
                        onNavigateToQR = { selectedTab = 1 },
                        onNavigateToProfile = { selectedTab = 4 },
                        onNavigateToVehicles = { navController.navigate(Routes.CUSTOMER_VEHICLES) },
                        onNavigateToNotifications = { navController.navigate(Routes.CUSTOMER_NOTIFICATIONS) },
                        onNavigateToTickets = { navController.navigate(Routes.CUSTOMER_TICKETS) },
                        onNavigateToNearbyPumps = { selectedTab = 2 },
                    )
                    1 -> QRCodeScreen(
                        viewModel = vm,
                        onBack = { selectedTab = 0 },
                    )
                    2 -> {
                        val nearbyVm: NearbyPumpsViewModel = hiltViewModel()
                        val context = LocalContext.current
                        val locationHelper = remember { LocationHelper(context) }
                        NearbyPumpsScreen(
                            viewModel = nearbyVm,
                            locationHelper = locationHelper,
                            onBack = { selectedTab = 0 },
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
                    3 -> HistoryScreen(
                        viewModel = vm,
                        onBack = { selectedTab = 0 },
                        onTransactionClick = { transactionId ->
                            navController.navigate(Routes.transactionDetailRoute(transactionId))
                        },
                    )
                    4 -> {
                        LaunchedEffect(Unit) { vm.loadProfile(); vm.loadVehicles() }
                        val state = vm.uiState.collectAsState().value
                        ProfileScreen(
                            userName = state.profile?.fullName,
                            userPhone = state.profile?.phoneNumber,
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Routes.WELCOME) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onBack = { selectedTab = 0 },
                            viewModel = vm,
                            profileData = state.profile,
                            onSaveProfile = { body -> vm.saveProfile(body) },
                            vehicles = state.vehicles,
                        )
                    }
                }
            }
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
            LaunchedEffect(Unit) { vm.loadProfile(); vm.loadVehicles() }
            val state = vm.uiState.collectAsState().value
            ProfileScreen(
                userName = state.profile?.fullName,
                userPhone = state.profile?.phoneNumber,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                viewModel = vm,
                profileData = state.profile,
                onSaveProfile = { body -> vm.saveProfile(body) },
                vehicles = state.vehicles,
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
            val context = LocalContext.current
            val locationHelper = remember { LocationHelper(context) }
            NearbyPumpsScreen(
                viewModel = vm,
                locationHelper = locationHelper,
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

        // ── Pump (bottom nav: Dashboard, Scanner, Settings, Profile) ──
        composable(Routes.PUMP_DASHBOARD) {
            val vm: PumpViewModel = hiltViewModel()
            PumpMainScreen(
                pumpViewModel = vm,
                onNavigateToSetupPump = { navController.navigate(Routes.SETUP_PUMP) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.SETUP_PUMP) {
            val vm: PumpViewModel = hiltViewModel()
            val context = LocalContext.current
            val locationHelper = remember { LocationHelper(context) }
            SetupPumpScreen(
                viewModel = vm,
                locationHelper = locationHelper,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Routes.PUMP_DASHBOARD) {
                        popUpTo(Routes.PUMP_DASHBOARD) { inclusive = true }
                    }
                },
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
