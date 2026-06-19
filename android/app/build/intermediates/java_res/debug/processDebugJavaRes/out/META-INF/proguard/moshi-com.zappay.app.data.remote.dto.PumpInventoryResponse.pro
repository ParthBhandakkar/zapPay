-if class com.zappay.app.data.remote.dto.PumpInventoryResponse
-keepnames class com.zappay.app.data.remote.dto.PumpInventoryResponse
-if class com.zappay.app.data.remote.dto.PumpInventoryResponse
-keep class com.zappay.app.data.remote.dto.PumpInventoryResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
