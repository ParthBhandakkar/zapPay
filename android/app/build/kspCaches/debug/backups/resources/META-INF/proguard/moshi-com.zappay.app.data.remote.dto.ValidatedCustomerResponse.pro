-if class com.zappay.app.data.remote.dto.ValidatedCustomerResponse
-keepnames class com.zappay.app.data.remote.dto.ValidatedCustomerResponse
-if class com.zappay.app.data.remote.dto.ValidatedCustomerResponse
-keep class com.zappay.app.data.remote.dto.ValidatedCustomerResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
