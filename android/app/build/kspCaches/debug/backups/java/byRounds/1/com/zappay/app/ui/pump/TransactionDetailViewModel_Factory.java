package com.zappay.app.ui.pump;

import androidx.lifecycle.SavedStateHandle;
import com.zappay.app.data.repository.TransactionRepository;
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
public final class TransactionDetailViewModel_Factory implements Factory<TransactionDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<TransactionRepository> repositoryProvider;

  public TransactionDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<TransactionRepository> repositoryProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public TransactionDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), repositoryProvider.get());
  }

  public static TransactionDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<TransactionRepository> repositoryProvider) {
    return new TransactionDetailViewModel_Factory(savedStateHandleProvider, repositoryProvider);
  }

  public static TransactionDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      TransactionRepository repository) {
    return new TransactionDetailViewModel(savedStateHandle, repository);
  }
}
