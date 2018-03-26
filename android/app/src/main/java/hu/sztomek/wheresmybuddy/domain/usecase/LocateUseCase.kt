package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import hu.sztomek.wheresmybuddy.domain.model.LocationModel
import io.reactivex.Single
import javax.inject.Inject

class LocateUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val datasource: IDatasource) {

    fun execute(action: Action.LocateAction): Single<Result> {
        return datasource.sendLocationRequest(action.userId)
                .toSingleDefault(Result.complete(action, BooleanModel(true)))
    }

}