package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import io.reactivex.Single
import javax.inject.Inject

class DeleteConnectionUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val datasource: IDatasource) {

    fun execute(action: Action.DeleteConnectionAction): Single<Result> {
        return datasource.deleteConnection(action.userId)
                .toSingleDefault(Result.complete(action, BooleanModel(true)))
    }

}