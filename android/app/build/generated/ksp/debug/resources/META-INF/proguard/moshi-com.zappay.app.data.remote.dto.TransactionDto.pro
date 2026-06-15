-if class com.zappay.app.data.remote.dto.TransactionDto
-keepnames class com.zappay.app.data.remote.dto.TransactionDto
-if class com.zappay.app.data.remote.dto.TransactionDto
-keep class com.zappay.app.data.remote.dto.TransactionDtoJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
