package hu.sztomek.wheresmybuddy.presentation.screens.splash

import android.support.v4.app.FragmentActivity
import dagger.Binds
import dagger.Module

@Module
interface SplashModule {
    @Binds
    fun provideFragmentActivityFromSplash(splash: SplashActivity): FragmentActivity
}