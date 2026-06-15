-if class com.zappay.app.data.remote.dto.QRCodeResponse
-keepnames class com.zappay.app.data.remote.dto.QRCodeResponse
-if class com.zappay.app.data.remote.dto.QRCodeResponse
-keep class com.zappay.app.data.remote.dto.QRCodeResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
