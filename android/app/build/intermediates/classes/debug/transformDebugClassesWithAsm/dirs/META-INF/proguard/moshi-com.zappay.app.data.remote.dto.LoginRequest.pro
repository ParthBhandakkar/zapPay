-if class com.zappay.app.data.remote.dto.LoginRequest
-keepnames class com.zappay.app.data.remote.dto.LoginRequest
-if class com.zappay.app.data.remote.dto.LoginRequest
-keep class com.zappay.app.data.remote.dto.LoginRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
