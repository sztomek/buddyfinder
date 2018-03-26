package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import io.reactivex.Single
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val datasource: IDatasource) {

    fun execute(action: Action.GetProfileAction): Single<Result> {
        val userIdStream: Single<String> = if (action.userId == null) {
            datasource.getActiveUser()
                    .observeOn(appSchedulers.io)
                    .map {
                        it.id
                    }
        } else {
            Single.just(action.userId)
        }

        return userIdStream
                .flatMap { id ->
                    datasource.getProfile(id)
                            .observeOn(appSchedulers.io)
                            .map {
                                Result.complete(action, it.copy(id = id))
                            }
                }
    }

}