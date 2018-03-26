package hu.sztomek.wheresmybuddy.presentation.screens.landing

import android.support.v4.app.FragmentActivity
import dagger.Binds
import dagger.Module

@Module
interface LandingModule {

    @Binds
    fun provideFragmentActivityFromLanding(landing: LandingActivity): FragmentActivity


}