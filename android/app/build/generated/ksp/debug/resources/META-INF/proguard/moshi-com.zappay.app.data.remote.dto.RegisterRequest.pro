-if class com.zappay.app.data.remote.dto.RegisterRequest
-keepnames class com.zappay.app.data.remote.dto.RegisterRequest
-if class com.zappay.app.data.remote.dto.RegisterRequest
-keep class com.zappay.app.data.remote.dto.RegisterRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.zappay.app.data.remote.dto.RegisterRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.zappay.app.data.remote.dto.RegisterRequest
-keepclassmembers class com.zappay.app.data.remote.dto.RegisterRequest {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
