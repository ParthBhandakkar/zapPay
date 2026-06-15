-if class com.zappay.app.data.remote.dto.FuelPurchaseRequest
-keepnames class com.zappay.app.data.remote.dto.FuelPurchaseRequest
-if class com.zappay.app.data.remote.dto.FuelPurchaseRequest
-keep class com.zappay.app.data.remote.dto.FuelPurchaseRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
