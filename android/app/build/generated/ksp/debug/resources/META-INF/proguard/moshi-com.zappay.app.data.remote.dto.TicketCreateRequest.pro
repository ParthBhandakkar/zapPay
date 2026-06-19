-if class com.zappay.app.data.remote.dto.TicketCreateRequest
-keepnames class com.zappay.app.data.remote.dto.TicketCreateRequest
-if class com.zappay.app.data.remote.dto.TicketCreateRequest
-keep class com.zappay.app.data.remote.dto.TicketCreateRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.zappay.app.data.remote.dto.TicketCreateRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.zappay.app.data.remote.dto.TicketCreateRequest
-keepclassmembers class com.zappay.app.data.remote.dto.TicketCreateRequest {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
