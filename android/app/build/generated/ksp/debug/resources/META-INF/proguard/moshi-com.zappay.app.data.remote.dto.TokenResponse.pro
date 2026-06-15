-if class com.zappay.app.data.remote.dto.TokenResponse
-keepnames class com.zappay.app.data.remote.dto.TokenResponse
-if class com.zappay.app.data.remote.dto.TokenResponse
-keep class com.zappay.app.data.remote.dto.TokenResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
