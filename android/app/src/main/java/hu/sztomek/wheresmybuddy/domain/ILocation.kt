package hu.sztomek.wheresmybuddy.domain

import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import hu.sztomek.wheresmybuddy.domain.model.LocationModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ILocation {

    fun getLastLocation(): Single<LocationModel>
    fun getCurrentLocation(): Single<LocationModel>
    fun isBroadcasting(): Single<BooleanModel>
    fun startLocationUpdates(): Observable<LocationModel>
    fun stopLocationUpdates(): Completable

}