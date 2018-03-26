package hu.sztomek.wheresmybuddy.presentation.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import hu.sztomek.wheresmybuddy.presentation.screens.landing.LandingViewModel
import hu.sztomek.wheresmybuddy.presentation.screens.landing.discover.DiscoverViewModel
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.friends.FriendsViewModel
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.incoming.IncomingRequestsViewModel
import hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.outgoing.OutgoingRequestsViewModel
import hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.details.ProfileDetailViewModel
import hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.edit.ProfileEditViewModel
import hu.sztomek.wheresmybuddy.presentation.screens.landing.search.SearchViewModel
import hu.sztomek.wheresmybuddy.presentation.screens.splash.SplashViewModel

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    abstract fun bindSplashVM(viewModel: SplashViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LandingViewModel::class)
    abstract fun bindLandingVM(viewModel: LandingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchVM(viewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileEditViewModel::class)
    abstract fun bindProfileEditVM(viewModel: ProfileEditViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileDetailViewModel::class)
    abstract fun bindProfileDetailsVM(viewModel: ProfileDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FriendsViewModel::class)
    abstract fun bindFriendsVM(viewModel: FriendsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(IncomingRequestsViewModel::class)
    abstract fun bindIncomingRequestsVM(viewModel: IncomingRequestsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OutgoingRequestsViewModel::class)
    abstract fun bindOutgoingRequestsVM(viewModel: OutgoingRequestsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoverViewModel::class)
    abstract fun bindDiscoverVM(viewModel: DiscoverViewModel): ViewModel

    @Binds
    abstract fun bindVMFactory(factory: BuddyViewModelFactory): ViewModelProvider.Factory


}