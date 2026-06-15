-if class com.zappay.app.data.remote.dto.RechargeOrderData
-keepnames class com.zappay.app.data.remote.dto.RechargeOrderData
-if class com.zappay.app.data.remote.dto.RechargeOrderData
-keep class com.zappay.app.data.remote.dto.RechargeOrderDataJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
