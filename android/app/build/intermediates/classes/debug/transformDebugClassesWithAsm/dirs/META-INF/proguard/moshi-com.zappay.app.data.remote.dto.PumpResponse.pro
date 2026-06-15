-if class com.zappay.app.data.remote.dto.PumpResponse
-keepnames class com.zappay.app.data.remote.dto.PumpResponse
-if class com.zappay.app.data.remote.dto.PumpResponse
-keep class com.zappay.app.data.remote.dto.PumpResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
