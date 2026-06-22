package com.zappay.app.ui.customer;

import com.zappay.app.data.local.TokenManager;
import com.zappay.app.data.repository.QRRepository;
import com.zappay.app.data.repository.TransactionRepository;
import com.zappay.app.data.repository.UserRepository;
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

  private final Provider<TokenManager> tokenManagerProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public CustomerViewModel_Factory(Provider<WalletRepository> walletRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<QRRepository> qrRepositoryProvider, Provider<TokenManager> tokenManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.walletRepositoryProvider = walletRepositoryProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.qrRepositoryProvider = qrRepositoryProvider;
    this.tokenManagerProvider = tokenManagerProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public CustomerViewModel get() {
    return newInstance(walletRepositoryProvider.get(), transactionRepositoryProvider.get(), qrRepositoryProvider.get(), tokenManagerProvider.get(), userRepositoryProvider.get());
  }

  public static CustomerViewModel_Factory create(
      Provider<WalletRepository> walletRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<QRRepository> qrRepositoryProvider, Provider<TokenManager> tokenManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new CustomerViewModel_Factory(walletRepositoryProvider, transactionRepositoryProvider, qrRepositoryProvider, tokenManagerProvider, userRepositoryProvider);
  }

  public static CustomerViewModel newInstance(WalletRepository walletRepository,
      TransactionRepository transactionRepository, QRRepository qrRepository,
      TokenManager tokenManager, UserRepository userRepository) {
    return new CustomerViewModel(walletRepository, transactionRepository, qrRepository, tokenManager, userRepository);
  }
}
