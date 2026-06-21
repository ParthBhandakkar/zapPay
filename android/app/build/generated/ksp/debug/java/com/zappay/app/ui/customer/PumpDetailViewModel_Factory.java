package com.zappay.app.ui.customer;

import androidx.lifecycle.SavedStateHandle;
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
public final class PumpDetailViewModel_Factory implements Factory<PumpDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<PumpRepository> pumpRepositoryProvider;

  public PumpDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<PumpRepository> pumpRepositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.pumpRepositoryProvider = pumpRepositoryProvider;
  }

  @Override
  public PumpDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), pumpRepositoryProvider.get());
  }

  public static PumpDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<PumpRepository> pumpRepositoryProvider) {
    return new PumpDetailViewModel_Factory(savedStateHandleProvider, pumpRepositoryProvider);
  }

  public static PumpDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      PumpRepository pumpRepository) {
    return new PumpDetailViewModel(savedStateHandle, pumpRepository);
  }
}
