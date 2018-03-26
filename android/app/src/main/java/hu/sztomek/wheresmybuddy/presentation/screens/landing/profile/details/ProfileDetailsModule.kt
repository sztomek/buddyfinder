package hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.details

import android.support.v4.app.FragmentActivity
import dagger.Binds
import dagger.Module

@Module
interface ProfileDetailsModule {

    @Binds
    fun bindFragmentActivityFromProfileDetails(activity: ProfileDetailsActivity): FragmentActivity

}