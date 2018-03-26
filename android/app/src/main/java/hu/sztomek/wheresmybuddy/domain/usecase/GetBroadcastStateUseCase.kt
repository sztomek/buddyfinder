package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.ILocation
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import io.reactivex.Single
import javax.inject.Inject

class GetBroadcastStateUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val location: ILocation) {

    fun execute(action: Action.GetBroadcastStateAction): Single<Result> {
        return location.isBroadcasting()
                .map { Result.complete(action, it) }
    }

}