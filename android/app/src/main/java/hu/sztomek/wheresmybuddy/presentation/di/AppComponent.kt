package hu.sztomek.wheresmybuddy.presentation.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import hu.sztomek.wheresmybuddy.presentation.app.BuddyApp
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(
        AppModule::class
        , AndroidSupportInjectionModule::class
        , ActivityModule::class
        , ServiceModule::class
))
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(app: BuddyApp): Builder
        fun build(): AppComponent
    }

    fun inject(app: BuddyApp)

}