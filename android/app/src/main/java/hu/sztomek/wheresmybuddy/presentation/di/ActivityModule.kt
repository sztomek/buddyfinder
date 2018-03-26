package hu.sztomek.wheresmybuddy.presentation.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import hu.sztomek.wheresmybuddy.presentation.screens.landing.LandingActivity
import hu.sztomek.wheresmybuddy.presentation.screens.landing.LandingModule
import hu.sztomek.wheresmybuddy.presentation.screens.landing.StatusUiModule
import hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.details.ProfileDetailsActivity
import hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.details.ProfileDetailsModule
import hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.edit.ProfileEditActivity
import hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.edit.ProfileEditModule
import hu.sztomek.wheresmybuddy.presentation.screens.splash.SplashActivity
import hu.sztomek.wheresmybuddy.presentation.screens.splash.SplashModule

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector(modules = arrayOf(
            SplashModule::class,
            RouterModule::class
    ))
    abstract fun bindSplash(): SplashActivity

    @ContributesAndroidInjector(modules = arrayOf(
            LandingModule::class,
            RouterModule::class,
            FragmentModule::class,
            StatusUiModule::class
    ))
    abstract fun bindLanding(): LandingActivity

    @ContributesAndroidInjector(modules = arrayOf(
            ProfileEditModule::class,
            RouterModule::class
    ))
    abstract fun bindProfileEdit(): ProfileEditActivity

    @ContributesAndroidInjector(modules = arrayOf(
            ProfileDetailsModule::class,
            RouterModule::class,
            DateFormatterModule::class
    ))
    abstract fun bindProfileDetails(): ProfileDetailsActivity

}