package hu.sztomek.wheresmybuddy.domain.usecase

import com.google.firebase.iid.FirebaseInstanceId
import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import io.reactivex.Single
import javax.inject.Inject

class CheckGcmTokenUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val datasource: IDatasource) {

    fun execute(action: Action.CheckGcmTokenAction): Single<Result> {
        return datasource.getActiveUser()
                .observeOn(appSchedulers.io)
                .flatMap {
                    val token = FirebaseInstanceId.getInstance().token
                    if (token == null) {
                        Single.just(BooleanModel(false))
                    } else {
                        datasource.updateGcmToken(token)
                                .toSingleDefault(BooleanModel(true))
                    }
                }
                .map { Result.complete(action, it) }
    }

}