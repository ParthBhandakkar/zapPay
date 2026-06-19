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
public final class UserRepository_Factory implements Factory<UserRepository> {
  private final Provider<ZapPayApi> apiProvider;

  public UserRepository_Factory(Provider<ZapPayApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public UserRepository get() {
    return newInstance(apiProvider.get());
  }

  public static UserRepository_Factory create(Provider<ZapPayApi> apiProvider) {
    return new UserRepository_Factory(apiProvider);
  }

  public static UserRepository newInstance(ZapPayApi api) {
    return new UserRepository(api);
  }
}
