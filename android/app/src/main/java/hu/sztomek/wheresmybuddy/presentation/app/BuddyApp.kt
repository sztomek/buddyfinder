package hu.sztomek.wheresmybuddy.presentation.app

import android.app.Activity
import android.app.Application
import android.app.Service
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import hu.sztomek.wheresmybuddy.BuildConfig
import hu.sztomek.wheresmybuddy.presentation.di.initialize
import pl.tajchert.nammu.Nammu
import timber.log.Timber
import javax.inject.Inject

class BuddyApp : Application(), HasActivityInjector, HasServiceInjector {

    @Inject
    lateinit var injector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Service>

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        initialize(this)
        Nammu.init(this)
    }

    override fun activityInjector() = injector

    override fun serviceInjector() = serviceInjector
}