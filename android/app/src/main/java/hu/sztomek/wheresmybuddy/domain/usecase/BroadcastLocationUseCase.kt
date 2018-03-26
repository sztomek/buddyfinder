package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.IDatasource
import hu.sztomek.wheresmybuddy.domain.ILocation
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.model.LocationModel
import io.reactivex.Observable
import javax.inject.Inject

class BroadcastLocationUseCase @Inject constructor(private val appSchedulers: AppSchedulers, private val location: ILocation, private val datasource: IDatasource) {

    fun execute(action: Action.StartBroadcastLocationAction): Observable<LocationModel> {
        return location.startLocationUpdates()
    }

}