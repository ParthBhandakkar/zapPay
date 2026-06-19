-if class com.zappay.app.data.remote.dto.PumpSettingsData
-keepnames class com.zappay.app.data.remote.dto.PumpSettingsData
-if class com.zappay.app.data.remote.dto.PumpSettingsData
-keep class com.zappay.app.data.remote.dto.PumpSettingsDataJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
