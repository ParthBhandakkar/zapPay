-if class com.zappay.app.data.remote.dto.VehicleDto
-keepnames class com.zappay.app.data.remote.dto.VehicleDto
-if class com.zappay.app.data.remote.dto.VehicleDto
-keep class com.zappay.app.data.remote.dto.VehicleDtoJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
