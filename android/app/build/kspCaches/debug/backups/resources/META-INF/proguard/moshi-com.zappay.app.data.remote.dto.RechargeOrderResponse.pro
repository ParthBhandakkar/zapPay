-if class com.zappay.app.data.remote.dto.RechargeOrderResponse
-keepnames class com.zappay.app.data.remote.dto.RechargeOrderResponse
-if class com.zappay.app.data.remote.dto.RechargeOrderResponse
-keep class com.zappay.app.data.remote.dto.RechargeOrderResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
