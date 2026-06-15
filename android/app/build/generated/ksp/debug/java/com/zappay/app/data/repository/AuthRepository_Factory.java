package com.zappay.app.data.repository;

import com.zappay.app.data.local.TokenManager;
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
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<ZapPayApi> apiProvider;

  private final Provider<TokenManager> tokenManagerProvider;

  public AuthRepository_Factory(Provider<ZapPayApi> apiProvider,
      Provider<TokenManager> tokenManagerProvider) {
    this.apiProvider = apiProvider;
    this.tokenManagerProvider = tokenManagerProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(apiProvider.get(), tokenManagerProvider.get());
  }

  public static AuthRepository_Factory create(Provider<ZapPayApi> apiProvider,
      Provider<TokenManager> tokenManagerProvider) {
    return new AuthRepository_Factory(apiProvider, tokenManagerProvider);
  }

  public static AuthRepository newInstance(ZapPayApi api, TokenManager tokenManager) {
    return new AuthRepository(api, tokenManager);
  }
}
