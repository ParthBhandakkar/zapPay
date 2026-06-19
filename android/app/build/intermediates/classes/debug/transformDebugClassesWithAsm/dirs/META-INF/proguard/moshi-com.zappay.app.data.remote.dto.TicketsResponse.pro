-if class com.zappay.app.data.remote.dto.TicketsResponse
-keepnames class com.zappay.app.data.remote.dto.TicketsResponse
-if class com.zappay.app.data.remote.dto.TicketsResponse
-keep class com.zappay.app.data.remote.dto.TicketsResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
