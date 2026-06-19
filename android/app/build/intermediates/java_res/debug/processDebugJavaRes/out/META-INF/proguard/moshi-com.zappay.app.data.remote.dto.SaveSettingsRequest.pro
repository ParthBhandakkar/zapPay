-if class com.zappay.app.data.remote.dto.SaveSettingsRequest
-keepnames class com.zappay.app.data.remote.dto.SaveSettingsRequest
-if class com.zappay.app.data.remote.dto.SaveSettingsRequest
-keep class com.zappay.app.data.remote.dto.SaveSettingsRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
