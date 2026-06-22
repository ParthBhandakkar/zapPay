package com.zappay.app.data.remote.api

import com.zappay.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ZapPayApi {

    // ── Auth ───────────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<GenericResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @POST("auth/send-otp")
    suspend fun sendOTP(@Body request: OTPRequest): Response<GenericResponse>

    @POST("auth/login/otp")
    suspend fun loginWithOTP(@Body request: OTPVerifyRequest): Response<TokenResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponse>

    // ── Profile ────────────────────────────────────────────────────────
    @GET("users/profile")
    suspend fun getProfile(): Response<UserProfileDto>

    @PUT("users/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<UserProfileDto>

    @POST("users/kyc/submit")
    suspend fun submitKYC(@Body body: Map<String, String>): Response<GenericResponse>

    // ── Wallet ─────────────────────────────────────────────────────────
    @GET("wallet/balance")
    suspend fun getWalletBalance(): Response<WalletResponse>

    @GET("wallet/summary")
    suspend fun getWalletSummary(): Response<WalletSummaryResponse>

    @POST("wallet/recharge/create-order")
    suspend fun createRechargeOrder(@Body request: RechargeOrderRequest): Response<RechargeOrderResponse>

    @POST("wallet/test-recharge")
    suspend fun testRecharge(@Query("amount") amount: Double): Response<GenericResponse>

    // ── QR ─────────────────────────────────────────────────────────────
    @GET("qr/my-codes")
    suspend fun getMyQRCodes(@Query("active_only") activeOnly: Boolean = true): Response<List<QRCodeResponse>>

    @POST("qr/generate")
    suspend fun generateQR(@Body request: QRCodeGenerateRequest): Response<QRCodeResponse>

    @POST("qr/{id}/deactivate")
    suspend fun deactivateQR(@Path("id") id: Int): Response<GenericResponse>

    // ── Transactions ───────────────────────────────────────────────────
    @GET("transactions/history")
    suspend fun getTransactionHistory(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): Response<TransactionHistoryResponse>

    @GET("transactions/{id}")
    suspend fun getTransactionDetails(@Path("id") id: String): Response<TransactionDto>

    @GET("transactions/{id}/receipt")
    suspend fun getTransactionReceipt(@Path("id") id: String): Response<ReceiptDto>

    // ── Pumps ──────────────────────────────────────────────────────────
    @GET("pumps")
    suspend fun getPumps(
        @Query("city") city: String? = null,
        @Query("verified_only") verifiedOnly: Boolean = true,
    ): Response<List<PumpResponse>>

    @GET("pumps/my-pump")
    suspend fun getMyPump(): Response<PumpResponse>

    @PUT("pumps/{pump_id}")
    suspend fun updatePump(@Path("pump_id") pumpId: Int, @Body body: Map<String, String>): Response<PumpResponse>

    @POST("pumps/register")
    suspend fun registerPump(@Body request: PumpRegisterRequest): Response<GenericResponse>

    @GET("pumps/{id}/dashboard")
    suspend fun getPumpDashboard(@Path("id") pumpId: Int): Response<PumpDashboardResponse>

    @GET("pumps/{id}/settlements")
    suspend fun getPumpSettlements(@Path("id") pumpId: Int): Response<GenericResponse>

    // ── Pump Operations (Pump-specific endpoints) ──────────────────────
    @POST("vehicle/lookup")
    suspend fun lookupVehicle(@Body body: Map<String, String>): Response<VehicleLookupResponse>

    @POST("qr/validate")
    suspend fun validateQR(@Body request: QRValidateRequest): Response<ValidatedCustomerResponse>

    @POST("transactions/fuel-purchase")
    suspend fun fuelPurchase(@Body request: FuelPurchaseRequest): Response<GenericResponse>

    @POST("transactions/fuel-purchase-by-vehicle")
    suspend fun fuelPurchaseByVehicle(@Body request: PurchaseByVehicleRequest): Response<GenericResponse>

    @GET("settings/{pump_id}")
    suspend fun getPumpSettings(@Path("pump_id") pumpId: Int): Response<PumpSettingsResponse>

    @POST("settings/save")
    suspend fun savePumpSettings(@Body body: SaveSettingsRequest): Response<PumpSettingsResponse>

    // ── Phase 4: Nearby Pumps ──
    @GET("pumps/nearby")
    suspend fun getNearbyPumps(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius_km") radiusKm: Double = 10.0,
    ): Response<NearbyPumpsResponse>

    @GET("pumps/{id}/fuel-prices")
    suspend fun getPumpFuelPrices(@Path("id") pumpId: Int): Response<FuelPricesResponse>

    // ── Phase 4: Inventory ──
    @GET("pumps/{id}/inventory")
    suspend fun getPumpInventory(@Path("id") pumpId: Int): Response<PumpInventoryResponse>

    // ── Phase 4: Shifts ──
    @POST("pumps/{id}/shifts/start")
    suspend fun startShift(@Path("id") pumpId: Int, @Body body: ShiftStartRequest): Response<GenericResponse>

    @POST("pumps/{id}/shifts/end")
    suspend fun endShift(@Path("id") pumpId: Int, @Body body: Map<String, String>): Response<GenericResponse>

    @GET("pumps/{id}/shifts")
    suspend fun getPumpShifts(@Path("id") pumpId: Int): Response<ShiftsResponse>

    // ── Multi-Vehicle ──
    @GET("users/vehicles")
    suspend fun getMyVehicles(): Response<List<VehicleDto>>

    @POST("users/vehicles")
    suspend fun addVehicle(@Body body: VehicleAddRequest): Response<GenericResponse>

    @PUT("users/vehicles/{id}")
    suspend fun updateVehicle(@Path("id") vehicleId: Int, @Body body: VehicleUpdateRequest): Response<GenericResponse>

    @DELETE("users/vehicles/{id}")
    suspend fun removeVehicle(@Path("id") vehicleId: Int): Response<GenericResponse>

    // ── Notifications ──
    @GET("users/notifications")
    suspend fun getMyNotifications(): Response<NotificationsResponse>

    @POST("users/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") notifId: Int): Response<GenericResponse>

    // ── Support Tickets ──
    @POST("users/support-tickets")
    suspend fun createSupportTicket(@Body body: TicketCreateRequest): Response<GenericResponse>

    @GET("users/support-tickets")
    suspend fun getMySupportTickets(): Response<TicketsResponse>

    // ── Health ─────────────────────────────────────────────────────────
    @GET("../health")
    suspend fun healthCheck(): Response<GenericResponse>
}
