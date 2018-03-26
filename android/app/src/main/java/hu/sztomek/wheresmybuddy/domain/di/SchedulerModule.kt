package hu.sztomek.wheresmybuddy.domain.di

import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Named

@Module
class SchedulerModule {

    @Provides
    @Named("main")
    fun provideMainScheduler(): Scheduler = AndroidSchedulers.mainThread()

    @Provides
    @Named("io")
    fun provideIOScheduler(): Scheduler = Schedulers.io()

    @Provides
    @Named("computation")
    fun provideComputationScheduler(): Scheduler = Schedulers.computation()

    @Provides
    @Named("trampoline")
    fun provideTrampolineScheduler(): Scheduler = Schedulers.trampoline()



}