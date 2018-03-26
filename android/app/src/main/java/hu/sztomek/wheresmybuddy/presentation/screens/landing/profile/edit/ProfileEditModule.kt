package hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.edit

import android.support.v4.app.FragmentActivity
import dagger.Binds
import dagger.Module

@Module
interface ProfileEditModule {

    @Binds
    fun bindFragmentActivityFromProfileEdit(activity: ProfileEditActivity): FragmentActivity

}