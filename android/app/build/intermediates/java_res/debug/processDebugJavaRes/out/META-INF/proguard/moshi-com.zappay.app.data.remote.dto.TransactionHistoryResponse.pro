-if class com.zappay.app.data.remote.dto.TransactionHistoryResponse
-keepnames class com.zappay.app.data.remote.dto.TransactionHistoryResponse
-if class com.zappay.app.data.remote.dto.TransactionHistoryResponse
-keep class com.zappay.app.data.remote.dto.TransactionHistoryResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
