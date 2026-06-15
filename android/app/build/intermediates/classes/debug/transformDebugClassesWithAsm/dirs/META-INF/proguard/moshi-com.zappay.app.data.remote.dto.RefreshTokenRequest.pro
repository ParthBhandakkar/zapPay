-if class com.zappay.app.data.remote.dto.RefreshTokenRequest
-keepnames class com.zappay.app.data.remote.dto.RefreshTokenRequest
-if class com.zappay.app.data.remote.dto.RefreshTokenRequest
-keep class com.zappay.app.data.remote.dto.RefreshTokenRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
