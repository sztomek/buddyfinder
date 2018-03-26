package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.IncomingRequestsListModel
import hu.sztomek.wheresmybuddy.domain.model.PendingConnectionRequestItemModel
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ListIncomingRequestsUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val datasource: IDatasource) {

    fun execute(action: Action.ListIncomingRequestsAction): Single<Result> {
        return datasource.getActiveUser()
                .flatMap {
                    if (action.fromLastId) {
                        datasource.listIncomingRequests(it.id!!, action.filterName, action.lastId, action.limit!!)
                    } else {
                        datasource.listAllIncomingRequestsUntil(it.id!!, action.filterName, action.lastId!!)
                    }
                }.flattenAsObservable { it }
                .flatMap { crModel ->
                    datasource.getProfile(crModel.from!!)
                            .toObservable()
                            .flatMap {
                                Observable.just(PendingConnectionRequestItemModel(it, crModel))
                            }
                }.sorted({ item1, item2 ->
                    (item1.userModel.displayName ?: "N/A").compareTo(item2.userModel.displayName ?: "N/A")
                })
                .toList()
                .flatMap { Single.just(Result.complete(action, IncomingRequestsListModel(it))) }
    }

}