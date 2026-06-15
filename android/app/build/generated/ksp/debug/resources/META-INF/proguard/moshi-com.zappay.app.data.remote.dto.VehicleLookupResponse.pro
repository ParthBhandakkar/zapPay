-if class com.zappay.app.data.remote.dto.VehicleLookupResponse
-keepnames class com.zappay.app.data.remote.dto.VehicleLookupResponse
-if class com.zappay.app.data.remote.dto.VehicleLookupResponse
-keep class com.zappay.app.data.remote.dto.VehicleLookupResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
