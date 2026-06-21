package com.zappay.app.ui.customer;

import com.zappay.app.data.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class VehiclesViewModel_Factory implements Factory<VehiclesViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  public VehiclesViewModel_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public VehiclesViewModel get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static VehiclesViewModel_Factory create(Provider<UserRepository> userRepositoryProvider) {
    return new VehiclesViewModel_Factory(userRepositoryProvider);
  }

  public static VehiclesViewModel newInstance(UserRepository userRepository) {
    return new VehiclesViewModel(userRepository);
  }
}
