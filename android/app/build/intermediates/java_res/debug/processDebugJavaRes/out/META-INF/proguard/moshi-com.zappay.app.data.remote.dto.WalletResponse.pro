-if class com.zappay.app.data.remote.dto.WalletResponse
-keepnames class com.zappay.app.data.remote.dto.WalletResponse
-if class com.zappay.app.data.remote.dto.WalletResponse
-keep class com.zappay.app.data.remote.dto.WalletResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
