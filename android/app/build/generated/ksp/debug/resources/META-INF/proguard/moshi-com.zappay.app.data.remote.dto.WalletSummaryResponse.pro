-if class com.zappay.app.data.remote.dto.WalletSummaryResponse
-keepnames class com.zappay.app.data.remote.dto.WalletSummaryResponse
-if class com.zappay.app.data.remote.dto.WalletSummaryResponse
-keep class com.zappay.app.data.remote.dto.WalletSummaryResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
