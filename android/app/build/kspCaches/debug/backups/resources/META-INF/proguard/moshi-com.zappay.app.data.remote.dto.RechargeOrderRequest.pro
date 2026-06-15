-if class com.zappay.app.data.remote.dto.RechargeOrderRequest
-keepnames class com.zappay.app.data.remote.dto.RechargeOrderRequest
-if class com.zappay.app.data.remote.dto.RechargeOrderRequest
-keep class com.zappay.app.data.remote.dto.RechargeOrderRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.zappay.app.data.remote.dto.RechargeOrderRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.zappay.app.data.remote.dto.RechargeOrderRequest
-keepclassmembers class com.zappay.app.data.remote.dto.RechargeOrderRequest {
    public synthetic <init>(double,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
