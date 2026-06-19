-if class com.zappay.app.data.remote.dto.ShiftsResponse
-keepnames class com.zappay.app.data.remote.dto.ShiftsResponse
-if class com.zappay.app.data.remote.dto.ShiftsResponse
-keep class com.zappay.app.data.remote.dto.ShiftsResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
