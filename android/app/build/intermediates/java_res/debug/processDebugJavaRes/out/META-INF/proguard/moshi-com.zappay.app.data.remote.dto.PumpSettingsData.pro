-if class com.zappay.app.data.remote.dto.PumpSettingsData
-keepnames class com.zappay.app.data.remote.dto.PumpSettingsData
-if class com.zappay.app.data.remote.dto.PumpSettingsData
-keep class com.zappay.app.data.remote.dto.PumpSettingsDataJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.zappay.app.data.remote.dto.PumpSettingsData
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.zappay.app.data.remote.dto.PumpSettingsData
-keepclassmembers class com.zappay.app.data.remote.dto.PumpSettingsData {
    public synthetic <init>(int,java.lang.String,java.lang.String,java.lang.String,boolean,java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
