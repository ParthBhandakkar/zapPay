-if class com.zappay.app.data.remote.dto.PumpSettingsResponse
-keepnames class com.zappay.app.data.remote.dto.PumpSettingsResponse
-if class com.zappay.app.data.remote.dto.PumpSettingsResponse
-keep class com.zappay.app.data.remote.dto.PumpSettingsResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
