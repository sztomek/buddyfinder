package hu.sztomek.wheresmybuddy.domain.common

import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named

class AppSchedulers @Inject constructor(
        @Named("main") val ui: Scheduler
        , @Named("io") val io: Scheduler
        , @Named("computation") val computation: Scheduler
        , @Named("trampoline") val trampoline: Scheduler
)