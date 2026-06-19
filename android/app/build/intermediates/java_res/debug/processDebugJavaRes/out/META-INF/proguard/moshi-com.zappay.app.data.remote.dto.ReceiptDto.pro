-if class com.zappay.app.data.remote.dto.ReceiptDto
-keepnames class com.zappay.app.data.remote.dto.ReceiptDto
-if class com.zappay.app.data.remote.dto.ReceiptDto
-keep class com.zappay.app.data.remote.dto.ReceiptDtoJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
