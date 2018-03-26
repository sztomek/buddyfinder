package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.ILocation
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.LocationModel
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import javax.inject.Inject

class GetLocationUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val location: ILocation, private val datasource: IDatasource) {

    fun execute(action: Action.GetLocationAction): Single<Result> {
        return Single.zip(
                datasource.getActiveUser(),
                location.getLastLocation()
                        .onErrorReturn {
                            LocationModel("fake", null, null, null, null, null, null, null)
                        }
                        .flatMap {
                            if (it.accuracy ?: 5_000f > 50f || (System.currentTimeMillis() - (it.timestamp ?: 0L) ) > 10 * 60 * 1000) {
                                location.getCurrentLocation()
                            } else {
                                Single.just(it)
                            }
                        },
                BiFunction {
                    user, location -> Result.complete(action, location.copy(fromId = user.id))
                }
        )
    }

}