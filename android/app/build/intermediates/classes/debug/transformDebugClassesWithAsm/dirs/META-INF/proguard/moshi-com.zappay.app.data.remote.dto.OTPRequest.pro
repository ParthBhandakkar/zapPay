-if class com.zappay.app.data.remote.dto.OTPRequest
-keepnames class com.zappay.app.data.remote.dto.OTPRequest
-if class com.zappay.app.data.remote.dto.OTPRequest
-keep class com.zappay.app.data.remote.dto.OTPRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.zappay.app.data.remote.dto.OTPRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.zappay.app.data.remote.dto.OTPRequest
-keepclassmembers class com.zappay.app.data.remote.dto.OTPRequest {
    public synthetic <init>(java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
