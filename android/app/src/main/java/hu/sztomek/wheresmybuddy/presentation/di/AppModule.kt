package hu.sztomek.wheresmybuddy.presentation.di

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module
import hu.sztomek.wheresmybuddy.data.di.ApiModule
import hu.sztomek.wheresmybuddy.data.di.DatabaseModule
import hu.sztomek.wheresmybuddy.data.di.NetworkModule
import hu.sztomek.wheresmybuddy.data.di.UserModule
import hu.sztomek.wheresmybuddy.device.di.DeviceModule
import hu.sztomek.wheresmybuddy.domain.di.SchedulerModule
import hu.sztomek.wheresmybuddy.presentation.app.BuddyApp


@Module(includes = arrayOf(
        ViewModelModule::class,
        SchedulerModule::class,
        NetworkModule::class,
        UserModule::class,
        DatabaseModule::class,
        ApiModule::class,
        DeviceModule::class
))
interface AppModule {

    @Binds
    fun provideAppContext(app: BuddyApp): Context

    @Binds
    fun provideApp(app: BuddyApp): Application

}