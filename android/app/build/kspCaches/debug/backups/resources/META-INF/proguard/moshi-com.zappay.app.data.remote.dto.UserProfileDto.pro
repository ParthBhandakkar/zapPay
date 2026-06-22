-if class com.zappay.app.data.remote.dto.UserProfileDto
-keepnames class com.zappay.app.data.remote.dto.UserProfileDto
-if class com.zappay.app.data.remote.dto.UserProfileDto
-keep class com.zappay.app.data.remote.dto.UserProfileDtoJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
