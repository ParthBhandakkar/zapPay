-if class com.zappay.app.data.remote.dto.OTPVerifyRequest
-keepnames class com.zappay.app.data.remote.dto.OTPVerifyRequest
-if class com.zappay.app.data.remote.dto.OTPVerifyRequest
-keep class com.zappay.app.data.remote.dto.OTPVerifyRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.zappay.app.data.remote.dto.OTPVerifyRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.zappay.app.data.remote.dto.OTPVerifyRequest
-keepclassmembers class com.zappay.app.data.remote.dto.OTPVerifyRequest {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
