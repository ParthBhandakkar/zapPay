-if class com.zappay.app.data.remote.dto.FuelPricesResponse
-keepnames class com.zappay.app.data.remote.dto.FuelPricesResponse
-if class com.zappay.app.data.remote.dto.FuelPricesResponse
-keep class com.zappay.app.data.remote.dto.FuelPricesResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
