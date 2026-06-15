-if class com.zappay.app.data.remote.dto.OCRResponse
-keepnames class com.zappay.app.data.remote.dto.OCRResponse
-if class com.zappay.app.data.remote.dto.OCRResponse
-keep class com.zappay.app.data.remote.dto.OCRResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
