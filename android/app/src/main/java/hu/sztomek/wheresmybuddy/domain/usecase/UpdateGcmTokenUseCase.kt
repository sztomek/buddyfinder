package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import io.reactivex.Completable
import javax.inject.Inject

class UpdateGcmTokenUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val datasource: IDatasource) {

    fun execute(action: Action.UpdateGcmTokenAction): Completable {
        return datasource.updateGcmToken(action.token)
    }

}