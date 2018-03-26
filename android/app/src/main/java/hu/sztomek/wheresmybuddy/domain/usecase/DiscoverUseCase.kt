package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.DiscoverEntity
import hu.sztomek.wheresmybuddy.domain.model.DiscoverListModel
import io.reactivex.Single
import javax.inject.Inject

class DiscoverUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val datasource: IDatasource) {

    fun execute(action: Action.DiscoverAction): Single<Result> {
        return datasource.getActiveUser()
                .observeOn(appSchedulers.io)
                .flatMap { user ->
                    datasource.listConnections(user.id!!, null, null, false, 50)
                            .toObservable()
                            .flatMapIterable {
                                it
                            }
                            .flatMap {
                                datasource.getLastLocation(it.otherUser!!, user.id)
                                        .toObservable()
                                        .flatMap { location ->
                                            datasource.getProfile(location.fromId!!)
                                                    .toObservable()
                                                    .map { DiscoverEntity(location, it) }
                                        }
                            }
                            .toList()
                }
                .map { Result.complete(action, DiscoverListModel(it)) }
    }

}