package hu.sztomek.wheresmybuddy.presentation.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import hu.sztomek.wheresmybuddy.presentation.service.LocationService
import hu.sztomek.wheresmybuddy.presentation.service.MessagingService
import hu.sztomek.wheresmybuddy.presentation.service.TokenWatcherService

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun bindTokenWatcherService(): TokenWatcherService

    @ContributesAndroidInjector
    abstract fun bindTokenMessagingService(): MessagingService

    @ContributesAndroidInjector
    abstract fun bindLocationService(): LocationService

}