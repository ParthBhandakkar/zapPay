-if class com.zappay.app.data.remote.dto.VehicleUpdateRequest
-keepnames class com.zappay.app.data.remote.dto.VehicleUpdateRequest
-if class com.zappay.app.data.remote.dto.VehicleUpdateRequest
-keep class com.zappay.app.data.remote.dto.VehicleUpdateRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.zappay.app.data.remote.dto.VehicleUpdateRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.zappay.app.data.remote.dto.VehicleUpdateRequest
-keepclassmembers class com.zappay.app.data.remote.dto.VehicleUpdateRequest {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,boolean,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
