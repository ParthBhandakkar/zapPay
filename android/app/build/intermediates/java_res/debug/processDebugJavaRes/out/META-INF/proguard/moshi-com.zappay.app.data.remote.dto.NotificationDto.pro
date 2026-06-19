-if class com.zappay.app.data.remote.dto.NotificationDto
-keepnames class com.zappay.app.data.remote.dto.NotificationDto
-if class com.zappay.app.data.remote.dto.NotificationDto
-keep class com.zappay.app.data.remote.dto.NotificationDtoJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
