-if class com.zappay.app.data.remote.dto.FuelPurchaseRequest
-keepnames class com.zappay.app.data.remote.dto.FuelPurchaseRequest
-if class com.zappay.app.data.remote.dto.FuelPurchaseRequest
-keep class com.zappay.app.data.remote.dto.FuelPurchaseRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.zappay.app.data.remote.dto.FuelPurchaseRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.zappay.app.data.remote.dto.FuelPurchaseRequest
-keepclassmembers class com.zappay.app.data.remote.dto.FuelPurchaseRequest {
    public synthetic <init>(java.lang.String,int,java.lang.String,double,double,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
