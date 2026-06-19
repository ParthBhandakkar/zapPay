-if class com.zappay.app.data.remote.dto.VehicleAddRequest
-keepnames class com.zappay.app.data.remote.dto.VehicleAddRequest
-if class com.zappay.app.data.remote.dto.VehicleAddRequest
-keep class com.zappay.app.data.remote.dto.VehicleAddRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.zappay.app.data.remote.dto.VehicleAddRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.zappay.app.data.remote.dto.VehicleAddRequest
-keepclassmembers class com.zappay.app.data.remote.dto.VehicleAddRequest {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,boolean,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
