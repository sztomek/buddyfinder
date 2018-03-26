package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.ConnectionModel
import hu.sztomek.wheresmybuddy.domain.model.ConnectionRequestModel
import hu.sztomek.wheresmybuddy.domain.model.SearchResultItemModel
import hu.sztomek.wheresmybuddy.domain.model.UserModel
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class GetProfileDetailsUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val datasource: IDatasource) {

    fun execute(action: Action.GetProfileDetailsAction): Observable<Result> {
        val profileObservable = datasource.getProfile(action.userId)
                .toObservable()
        val activeUserIdObservable = datasource.getActiveUser()
                .toObservable()
                .map { it.id }
        val fakeConnectionRequest = ConnectionRequestModel(null, null, null, null)
        val fakeConnection = ConnectionModel(null, null, null, null)
        return profileObservable
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
                .map { Result.complete(action, it) }
    }

}