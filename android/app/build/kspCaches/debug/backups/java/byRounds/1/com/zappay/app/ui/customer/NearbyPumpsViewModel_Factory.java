package com.zappay.app.ui.customer;

import com.zappay.app.data.repository.PumpRepository;
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
public final class NearbyPumpsViewModel_Factory implements Factory<NearbyPumpsViewModel> {
  private final Provider<PumpRepository> pumpRepositoryProvider;

  public NearbyPumpsViewModel_Factory(Provider<PumpRepository> pumpRepositoryProvider) {
    this.pumpRepositoryProvider = pumpRepositoryProvider;
  }

  @Override
  public NearbyPumpsViewModel get() {
    return newInstance(pumpRepositoryProvider.get());
  }

  public static NearbyPumpsViewModel_Factory create(
      Provider<PumpRepository> pumpRepositoryProvider) {
    return new NearbyPumpsViewModel_Factory(pumpRepositoryProvider);
  }

  public static NearbyPumpsViewModel newInstance(PumpRepository pumpRepository) {
    return new NearbyPumpsViewModel(pumpRepository);
  }
}
