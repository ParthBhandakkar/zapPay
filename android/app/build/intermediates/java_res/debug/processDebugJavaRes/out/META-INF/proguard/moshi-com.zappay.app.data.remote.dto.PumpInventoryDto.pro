-if class com.zappay.app.data.remote.dto.PumpInventoryDto
-keepnames class com.zappay.app.data.remote.dto.PumpInventoryDto
-if class com.zappay.app.data.remote.dto.PumpInventoryDto
-keep class com.zappay.app.data.remote.dto.PumpInventoryDtoJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
