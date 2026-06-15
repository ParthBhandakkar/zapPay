-if class com.zappay.app.data.remote.dto.GenericResponse
-keepnames class com.zappay.app.data.remote.dto.GenericResponse
-if class com.zappay.app.data.remote.dto.GenericResponse
-keep class com.zappay.app.data.remote.dto.GenericResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.zappay.app.data.remote.dto.GenericResponse
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.zappay.app.data.remote.dto.GenericResponse
-keepclassmembers class com.zappay.app.data.remote.dto.GenericResponse {
    public synthetic <init>(boolean,java.lang.String,java.util.Map,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
