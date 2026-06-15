package com.zappay.app.data.repository;

import com.zappay.app.data.remote.api.ZapPayApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class QRRepository_Factory implements Factory<QRRepository> {
  private final Provider<ZapPayApi> apiProvider;

  public QRRepository_Factory(Provider<ZapPayApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public QRRepository get() {
    return newInstance(apiProvider.get());
  }

  public static QRRepository_Factory create(Provider<ZapPayApi> apiProvider) {
    return new QRRepository_Factory(apiProvider);
  }

  public static QRRepository newInstance(ZapPayApi api) {
    return new QRRepository(api);
  }
}
