package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class SearchUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val datasource: IDatasource) {

    // TODO optimize :|
    fun execute(action: Action.SearchAction): Single<Result> {
        val dbModels =
                if (action.lastId != null && !action.fromLastId) datasource.searchAllUntil(action.keyWord, action.lastId)
                else datasource.searchFrom(action.keyWord, action.lastId, action.limit ?: 10)
        val mappedUsersObservable = dbModels.toObservable()
                .flatMapIterable { it }
        val activeUserIdObservable = datasource.getActiveUser()
                .toObservable()
                .map { it.id }
        val fakeConnectionRequest = ConnectionRequestModel(null, null, null, null)
        val fakeConnection = ConnectionModel(null, null, null, null)
        return mappedUsersObservable
                .withLatestFrom(activeUserIdObservable, BiFunction { model: UserModel, id: String? -> Pair(model, id) })
                .observeOn(appSchedulers.io)
                .flatMap {
                    Observable.zip(
                            datasource.getConnectionRequestBetween(
                                    it.first.id!!, // if there userModel.id is null, that means i did something wrong during conversion -> just die
                                    it.second!! // if there is no active user, there's no point to continue this -> just die
                            ).defaultIfEmpty(fakeConnectionRequest)
                                    .toObservable(),
                            datasource.getConnectionBetween(
                                    it.second!!, // if there userModel.id is null, that means i did something wrong during conversion -> just die
                                    it.first.id!! // if there is no active user, there's no point to continue this -> just die
                            ).defaultIfEmpty(fakeConnection)
                                    .toObservable(),
                            BiFunction { connectionRequest: ConnectionRequestModel, connection: ConnectionModel ->
                                SearchResultItemModel(
                                        it.first,
                                        if (connection === fakeConnection) null
                                        else connection,
                                        if (connectionRequest === fakeConnectionRequest) null
                                        else connectionRequest
                                )
                            }
                    )
                }
                .observeOn(appSchedulers.io)
                .sorted({ item1, item2 ->
                    (item1.user.displayName ?: "N/A").compareTo(item2.user.displayName ?: "N/A")
                })
                .toList()
                .map { Result.complete(action, SearchResultModel(it)) }
    }

}