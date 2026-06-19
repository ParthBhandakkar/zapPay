-if class com.zappay.app.data.remote.dto.NotificationsResponse
-keepnames class com.zappay.app.data.remote.dto.NotificationsResponse
-if class com.zappay.app.data.remote.dto.NotificationsResponse
-keep class com.zappay.app.data.remote.dto.NotificationsResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
