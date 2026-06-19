-if class com.zappay.app.data.remote.dto.DeviceRegisterRequest
-keepnames class com.zappay.app.data.remote.dto.DeviceRegisterRequest
-if class com.zappay.app.data.remote.dto.DeviceRegisterRequest
-keep class com.zappay.app.data.remote.dto.DeviceRegisterRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
