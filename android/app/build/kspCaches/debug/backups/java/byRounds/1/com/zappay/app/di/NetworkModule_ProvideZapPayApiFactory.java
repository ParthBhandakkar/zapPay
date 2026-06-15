package com.zappay.app.di;

import com.zappay.app.data.remote.api.ZapPayApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class NetworkModule_ProvideZapPayApiFactory implements Factory<ZapPayApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideZapPayApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public ZapPayApi get() {
    return provideZapPayApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideZapPayApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideZapPayApiFactory(retrofitProvider);
  }

  public static ZapPayApi provideZapPayApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideZapPayApi(retrofit));
  }
}
