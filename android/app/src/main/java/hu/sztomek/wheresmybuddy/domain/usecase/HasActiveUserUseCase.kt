package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import io.reactivex.Single
import javax.inject.Inject

class HasActiveUserUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val dataSource: IDatasource) {

    fun execute(action: Action.HasActiveUserAction): Single<Result> {
        return dataSource.hasActiveUser()
                .observeOn(appSchedulers.io)
                .map { Result.complete(action, BooleanModel(it)) }
    }

}