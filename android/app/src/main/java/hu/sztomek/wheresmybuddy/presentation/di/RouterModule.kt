package hu.sztomek.wheresmybuddy.presentation.di

import android.support.v4.app.FragmentActivity
import dagger.Module
import dagger.Provides
import hu.sztomek.wheresmybuddy.presentation.router.IRouter
import hu.sztomek.wheresmybuddy.presentation.router.Router

@Module
class RouterModule {

    @Provides
    fun provideRouter(activity: FragmentActivity): IRouter = Router(activity)

}