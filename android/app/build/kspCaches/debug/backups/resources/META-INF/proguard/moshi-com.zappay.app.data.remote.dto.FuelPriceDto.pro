-if class com.zappay.app.data.remote.dto.FuelPriceDto
-keepnames class com.zappay.app.data.remote.dto.FuelPriceDto
-if class com.zappay.app.data.remote.dto.FuelPriceDto
-keep class com.zappay.app.data.remote.dto.FuelPriceDtoJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
