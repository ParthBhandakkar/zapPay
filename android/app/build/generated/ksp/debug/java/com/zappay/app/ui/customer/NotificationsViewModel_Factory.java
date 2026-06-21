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
public final class NotificationsViewModel_Factory implements Factory<NotificationsViewModel> {
  private final Provider<UserRepository> userRepositoryProvider;

  public NotificationsViewModel_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public NotificationsViewModel get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static NotificationsViewModel_Factory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new NotificationsViewModel_Factory(userRepositoryProvider);
  }

  public static NotificationsViewModel newInstance(UserRepository userRepository) {
    return new NotificationsViewModel(userRepository);
  }
}
