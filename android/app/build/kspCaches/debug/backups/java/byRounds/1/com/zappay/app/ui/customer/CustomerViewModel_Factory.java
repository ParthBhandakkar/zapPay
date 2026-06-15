package com.zappay.app.ui.customer;

import com.zappay.app.data.repository.QRRepository;
import com.zappay.app.data.repository.TransactionRepository;
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
public final class CustomerViewModel_Factory implements Factory<CustomerViewModel> {
  private final Provider<WalletRepository> walletRepositoryProvider;

  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<QRRepository> qrRepositoryProvider;

  public CustomerViewModel_Factory(Provider<WalletRepository> walletRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<QRRepository> qrRepositoryProvider) {
    this.walletRepositoryProvider = walletRepositoryProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.qrRepositoryProvider = qrRepositoryProvider;
  }

  @Override
  public CustomerViewModel get() {
    return newInstance(walletRepositoryProvider.get(), transactionRepositoryProvider.get(), qrRepositoryProvider.get());
  }

  public static CustomerViewModel_Factory create(
      Provider<WalletRepository> walletRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<QRRepository> qrRepositoryProvider) {
    return new CustomerViewModel_Factory(walletRepositoryProvider, transactionRepositoryProvider, qrRepositoryProvider);
  }

  public static CustomerViewModel newInstance(WalletRepository walletRepository,
      TransactionRepository transactionRepository, QRRepository qrRepository) {
    return new CustomerViewModel(walletRepository, transactionRepository, qrRepository);
  }
}
