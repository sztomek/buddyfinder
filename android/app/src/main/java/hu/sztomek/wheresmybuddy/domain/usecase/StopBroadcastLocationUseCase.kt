package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.ILocation
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import io.reactivex.Single
import javax.inject.Inject

class StopBroadcastLocationUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val location: ILocation) {

    fun execute(action: Action.StopBroadcastLocationAction): Single<Result> {
        return location.stopLocationUpdates()
                .toSingleDefault(Result.complete(action, BooleanModel(true)))
    }

}