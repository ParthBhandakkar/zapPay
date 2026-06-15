package com.zappay.app.ui.pump;

import com.zappay.app.data.repository.PumpRepository;
import com.zappay.app.data.repository.WalletRepository;
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
public final class PumpViewModel_Factory implements Factory<PumpViewModel> {
  private final Provider<PumpRepository> pumpRepositoryProvider;

  private final Provider<WalletRepository> walletRepositoryProvider;

  public PumpViewModel_Factory(Provider<PumpRepository> pumpRepositoryProvider,
      Provider<WalletRepository> walletRepositoryProvider) {
    this.pumpRepositoryProvider = pumpRepositoryProvider;
    this.walletRepositoryProvider = walletRepositoryProvider;
  }

  @Override
  public PumpViewModel get() {
    return newInstance(pumpRepositoryProvider.get(), walletRepositoryProvider.get());
  }

  public static PumpViewModel_Factory create(Provider<PumpRepository> pumpRepositoryProvider,
      Provider<WalletRepository> walletRepositoryProvider) {
    return new PumpViewModel_Factory(pumpRepositoryProvider, walletRepositoryProvider);
  }

  public static PumpViewModel newInstance(PumpRepository pumpRepository,
      WalletRepository walletRepository) {
    return new PumpViewModel(pumpRepository, walletRepository);
  }
}
