package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import io.reactivex.Single
import javax.inject.Inject

class SendRequestUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val datasource: IDatasource) {

    fun execute(action: Action.SendRequestAction): Single<Result> {
        return datasource.sendConnectionRequest(action.userId)
                .map { Result.complete(action, it) }
    }

}