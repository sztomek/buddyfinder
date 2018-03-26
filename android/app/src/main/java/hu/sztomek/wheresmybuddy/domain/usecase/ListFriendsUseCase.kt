package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.FriendsItemModel
import hu.sztomek.wheresmybuddy.domain.model.FriendsListModel
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class ListFriendsUseCase @Inject constructor(
        private val appSchedulers: AppSchedulers,
        private val datasource: IDatasource
) {

    fun execute(action: Action.ListFriendsAction): Single<Result> {
        return datasource.getActiveUser()
                .flatMap {
                    if (action.fromLastId) {
                        datasource.listConnections(it.id!!, action.filterName, action.lastId, action.trustedOnly, action.limit!!)
                    } else {
                        datasource.listAllConnectionsUntil(it.id!!, action.filterName, action.lastId!!, action.trustedOnly)
                    }
                }.flattenAsObservable { it }
                .flatMap { connectionModel ->
                    datasource.getProfile(connectionModel.otherUser!!)
                            .toObservable()
                            .flatMap {
                                Observable.just(FriendsItemModel(it, connectionModel))
                            }
                }
                .sorted({ item1, item2 ->
                    (item1.userModel.displayName ?: "N/A").compareTo(item2.userModel.displayName ?: "N/A")
                })
                .toList()
                .flatMap { Single.just(Result.complete(action, FriendsListModel(it))) }
    }

}