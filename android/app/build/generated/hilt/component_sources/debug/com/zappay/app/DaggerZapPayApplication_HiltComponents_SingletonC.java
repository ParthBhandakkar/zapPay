package com.zappay.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.squareup.moshi.Moshi;
import com.zappay.app.data.local.TokenManager;
import com.zappay.app.data.remote.api.ZapPayApi;
import com.zappay.app.data.remote.interceptor.AuthInterceptor;
import com.zappay.app.data.repository.AuthRepository;
import com.zappay.app.data.repository.PumpRepository;
import com.zappay.app.data.repository.QRRepository;
import com.zappay.app.data.repository.TransactionRepository;
import com.zappay.app.data.repository.UserRepository;
import com.zappay.app.data.repository.WalletRepository;
import com.zappay.app.di.NetworkModule_ProvideMoshiFactory;
import com.zappay.app.di.NetworkModule_ProvideOkHttpClientFactory;
import com.zappay.app.di.NetworkModule_ProvideRetrofitFactory;
import com.zappay.app.di.NetworkModule_ProvideZapPayApiFactory;
import com.zappay.app.ui.auth.AuthViewModel;
import com.zappay.app.ui.auth.AuthViewModel_HiltModules;
import com.zappay.app.ui.auth.AuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.zappay.app.ui.auth.AuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.zappay.app.ui.customer.CustomerViewModel;
import com.zappay.app.ui.customer.CustomerViewModel_HiltModules;
import com.zappay.app.ui.customer.CustomerViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.zappay.app.ui.customer.CustomerViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.zappay.app.ui.customer.NearbyPumpsViewModel;
import com.zappay.app.ui.customer.NearbyPumpsViewModel_HiltModules;
import com.zappay.app.ui.customer.NearbyPumpsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.zappay.app.ui.customer.NearbyPumpsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.zappay.app.ui.customer.NotificationsViewModel;
import com.zappay.app.ui.customer.NotificationsViewModel_HiltModules;
import com.zappay.app.ui.customer.NotificationsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.zappay.app.ui.customer.NotificationsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.zappay.app.ui.customer.PumpDetailViewModel;
import com.zappay.app.ui.customer.PumpDetailViewModel_HiltModules;
import com.zappay.app.ui.customer.PumpDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.zappay.app.ui.customer.PumpDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.zappay.app.ui.customer.SupportTicketsViewModel;
import com.zappay.app.ui.customer.SupportTicketsViewModel_HiltModules;
import com.zappay.app.ui.customer.SupportTicketsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.zappay.app.ui.customer.SupportTicketsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.zappay.app.ui.customer.VehiclesViewModel;
import com.zappay.app.ui.customer.VehiclesViewModel_HiltModules;
import com.zappay.app.ui.customer.VehiclesViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.zappay.app.ui.customer.VehiclesViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.zappay.app.ui.pump.PumpViewModel;
import com.zappay.app.ui.pump.PumpViewModel_HiltModules;
import com.zappay.app.ui.pump.PumpViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.zappay.app.ui.pump.PumpViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import com.zappay.app.ui.pump.TransactionDetailViewModel;
import com.zappay.app.ui.pump.TransactionDetailViewModel_HiltModules;
import com.zappay.app.ui.pump.TransactionDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import com.zappay.app.ui.pump.TransactionDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerZapPayApplication_HiltComponents_SingletonC {
  private DaggerZapPayApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public ZapPayApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements ZapPayApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public ZapPayApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements ZapPayApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public ZapPayApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements ZapPayApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public ZapPayApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements ZapPayApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public ZapPayApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements ZapPayApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public ZapPayApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements ZapPayApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public ZapPayApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements ZapPayApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public ZapPayApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends ZapPayApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends ZapPayApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends ZapPayApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends ZapPayApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(9).put(AuthViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, AuthViewModel_HiltModules.KeyModule.provide()).put(CustomerViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CustomerViewModel_HiltModules.KeyModule.provide()).put(NearbyPumpsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, NearbyPumpsViewModel_HiltModules.KeyModule.provide()).put(NotificationsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, NotificationsViewModel_HiltModules.KeyModule.provide()).put(PumpDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PumpDetailViewModel_HiltModules.KeyModule.provide()).put(PumpViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, PumpViewModel_HiltModules.KeyModule.provide()).put(SupportTicketsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SupportTicketsViewModel_HiltModules.KeyModule.provide()).put(TransactionDetailViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TransactionDetailViewModel_HiltModules.KeyModule.provide()).put(VehiclesViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, VehiclesViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends ZapPayApplication_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AuthViewModel> authViewModelProvider;

    private Provider<CustomerViewModel> customerViewModelProvider;

    private Provider<NearbyPumpsViewModel> nearbyPumpsViewModelProvider;

    private Provider<NotificationsViewModel> notificationsViewModelProvider;

    private Provider<PumpDetailViewModel> pumpDetailViewModelProvider;

    private Provider<PumpViewModel> pumpViewModelProvider;

    private Provider<SupportTicketsViewModel> supportTicketsViewModelProvider;

    private Provider<TransactionDetailViewModel> transactionDetailViewModelProvider;

    private Provider<VehiclesViewModel> vehiclesViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.authViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.customerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.nearbyPumpsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.notificationsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.pumpDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.pumpViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.supportTicketsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.transactionDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
      this.vehiclesViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 8);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(9).put(AuthViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) authViewModelProvider)).put(CustomerViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) customerViewModelProvider)).put(NearbyPumpsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) nearbyPumpsViewModelProvider)).put(NotificationsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) notificationsViewModelProvider)).put(PumpDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) pumpDetailViewModelProvider)).put(PumpViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) pumpViewModelProvider)).put(SupportTicketsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) supportTicketsViewModelProvider)).put(TransactionDetailViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) transactionDetailViewModelProvider)).put(VehiclesViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) vehiclesViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.zappay.app.ui.auth.AuthViewModel 
          return (T) new AuthViewModel(singletonCImpl.authRepositoryProvider.get());

          case 1: // com.zappay.app.ui.customer.CustomerViewModel 
          return (T) new CustomerViewModel(singletonCImpl.walletRepositoryProvider.get(), singletonCImpl.transactionRepositoryProvider.get(), singletonCImpl.qRRepositoryProvider.get(), singletonCImpl.tokenManagerProvider.get(), singletonCImpl.userRepositoryProvider.get());

          case 2: // com.zappay.app.ui.customer.NearbyPumpsViewModel 
          return (T) new NearbyPumpsViewModel(singletonCImpl.pumpRepositoryProvider.get());

          case 3: // com.zappay.app.ui.customer.NotificationsViewModel 
          return (T) new NotificationsViewModel(singletonCImpl.userRepositoryProvider.get());

          case 4: // com.zappay.app.ui.customer.PumpDetailViewModel 
          return (T) new PumpDetailViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.pumpRepositoryProvider.get());

          case 5: // com.zappay.app.ui.pump.PumpViewModel 
          return (T) new PumpViewModel(singletonCImpl.pumpRepositoryProvider.get(), singletonCImpl.walletRepositoryProvider.get());

          case 6: // com.zappay.app.ui.customer.SupportTicketsViewModel 
          return (T) new SupportTicketsViewModel(singletonCImpl.userRepositoryProvider.get());

          case 7: // com.zappay.app.ui.pump.TransactionDetailViewModel 
          return (T) new TransactionDetailViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.transactionRepositoryProvider.get());

          case 8: // com.zappay.app.ui.customer.VehiclesViewModel 
          return (T) new VehiclesViewModel(singletonCImpl.userRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends ZapPayApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends ZapPayApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends ZapPayApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<TokenManager> tokenManagerProvider;

    private Provider<AuthInterceptor> authInterceptorProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Moshi> provideMoshiProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<ZapPayApi> provideZapPayApiProvider;

    private Provider<AuthRepository> authRepositoryProvider;

    private Provider<WalletRepository> walletRepositoryProvider;

    private Provider<TransactionRepository> transactionRepositoryProvider;

    private Provider<QRRepository> qRRepositoryProvider;

    private Provider<UserRepository> userRepositoryProvider;

    private Provider<PumpRepository> pumpRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.tokenManagerProvider = DoubleCheck.provider(new SwitchingProvider<TokenManager>(singletonCImpl, 5));
      this.authInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<AuthInterceptor>(singletonCImpl, 4));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 3));
      this.provideMoshiProvider = DoubleCheck.provider(new SwitchingProvider<Moshi>(singletonCImpl, 6));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 2));
      this.provideZapPayApiProvider = DoubleCheck.provider(new SwitchingProvider<ZapPayApi>(singletonCImpl, 1));
      this.authRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepository>(singletonCImpl, 0));
      this.walletRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<WalletRepository>(singletonCImpl, 7));
      this.transactionRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<TransactionRepository>(singletonCImpl, 8));
      this.qRRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<QRRepository>(singletonCImpl, 9));
      this.userRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<UserRepository>(singletonCImpl, 10));
      this.pumpRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PumpRepository>(singletonCImpl, 11));
    }

    @Override
    public void injectZapPayApplication(ZapPayApplication arg0) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.zappay.app.data.repository.AuthRepository 
          return (T) new AuthRepository(singletonCImpl.provideZapPayApiProvider.get(), singletonCImpl.tokenManagerProvider.get());

          case 1: // com.zappay.app.data.remote.api.ZapPayApi 
          return (T) NetworkModule_ProvideZapPayApiFactory.provideZapPayApi(singletonCImpl.provideRetrofitProvider.get());

          case 2: // retrofit2.Retrofit 
          return (T) NetworkModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.provideMoshiProvider.get());

          case 3: // okhttp3.OkHttpClient 
          return (T) NetworkModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.authInterceptorProvider.get());

          case 4: // com.zappay.app.data.remote.interceptor.AuthInterceptor 
          return (T) new AuthInterceptor(singletonCImpl.tokenManagerProvider.get());

          case 5: // com.zappay.app.data.local.TokenManager 
          return (T) new TokenManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.squareup.moshi.Moshi 
          return (T) NetworkModule_ProvideMoshiFactory.provideMoshi();

          case 7: // com.zappay.app.data.repository.WalletRepository 
          return (T) new WalletRepository(singletonCImpl.provideZapPayApiProvider.get());

          case 8: // com.zappay.app.data.repository.TransactionRepository 
          return (T) new TransactionRepository(singletonCImpl.provideZapPayApiProvider.get());

          case 9: // com.zappay.app.data.repository.QRRepository 
          return (T) new QRRepository(singletonCImpl.provideZapPayApiProvider.get());

          case 10: // com.zappay.app.data.repository.UserRepository 
          return (T) new UserRepository(singletonCImpl.provideZapPayApiProvider.get());

          case 11: // com.zappay.app.data.repository.PumpRepository 
          return (T) new PumpRepository(singletonCImpl.provideZapPayApiProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
