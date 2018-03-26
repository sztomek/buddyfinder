package hu.sztomek.wheresmybuddy.device.location

import android.Manifest
import android.content.Context
import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import hu.sztomek.wheresmybuddy.domain.ILocation
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import hu.sztomek.wheresmybuddy.domain.model.LocationModel
import io.reactivex.Completable
import io.reactivex.Emitter
import io.reactivex.Observable
import io.reactivex.Single
import pl.tajchert.nammu.Nammu
import timber.log.Timber

class Location(context: Context) : ILocation {

    private val locationAPI = LocationServices.getFusedLocationProviderClient(context)
    private val settingsClient = LocationServices.getSettingsClient(context)
    private var locationCallback: LocationCallback? = null
    private var locationEmitter: Emitter<LocationModel>? = null

    override fun getLastLocation(): Single<LocationModel> {
        return Single.create { emitter ->
            if (Nammu.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                locationAPI.lastLocation
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                if (it.result != null) {
                                    emitter.onSuccess(it.result.toDomainModel())
                                } else {
                                    emitter.onError(LocationException("Failed to get last location"))
                                }
                            } else {
                                emitter.onError(LocationException(it.exception?.message
                                        ?: "Failed to get last location"))
                            }
                        }
            } else {
                emitter.onError(PermissionNotGrantedException(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    override fun getCurrentLocation(): Single<LocationModel> {
        return Single.create { emitter ->
            if (Nammu.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                val locationRequest = LocationRequest()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.numUpdates = 1
                locationRequest.maxWaitTime = 15_000L
                val builder = LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                settingsClient.checkLocationSettings(builder.build())
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val callback = object : LocationCallback() {
                                    override fun onLocationResult(p0: LocationResult?) {
                                        val location = p0?.locations?.lastOrNull()
                                        if (location != null) {
                                            emitter.onSuccess(location.toDomainModel())
                                        } else {
                                            emitter.onError(LocationException("Failed to get location"))
                                        }
                                    }
                                }
                                locationAPI.requestLocationUpdates(locationRequest, callback, null)
                            } else {
                                emitter.onError(
                                        when (it.exception) {
                                            is ResolvableApiException -> ResolvableLocationException(it.exception as ResolvableApiException)
                                            else -> LocationException("Failed to get location")
                                        })
                            }
                        }
            } else {
                emitter.onError(PermissionNotGrantedException(Manifest.permission.ACCESS_FINE_LOCATION))
            }
        }
    }

    override fun isBroadcasting(): Single<BooleanModel> {
        return Single.just(BooleanModel(locationCallback != null))
    }

    override fun startLocationUpdates(): Observable<LocationModel> {
        return Observable.create { emitter ->
            locationEmitter = emitter
            if (Nammu.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                val locationRequest = LocationRequest()
                locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                locationRequest.fastestInterval = 20_000L
                locationRequest.interval = 45_000L
                val builder = LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                settingsClient.checkLocationSettings(builder.build())
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                locationCallback = object : LocationCallback() {
                                    override fun onLocationResult(p0: LocationResult?) {
                                        p0?.locations?.lastOrNull()?.let {
                                            emitter.onNext(it.toDomainModel())
                                        }
                                    }
                                }
                                locationAPI.requestLocationUpdates(locationRequest, locationCallback, null)
                            } else {
                                emitter.onError(
                                        when (it.exception) {
                                            is ResolvableApiException -> ResolvableLocationException(it.exception as ResolvableApiException)
                                            else -> LocationException("Failed to get location")
                                        })
                                emitter.onComplete()
                            }
                        }
            } else {
                emitter.onError(PermissionNotGrantedException(Manifest.permission.ACCESS_FINE_LOCATION))
                emitter.onComplete()
            }
        }
    }

    override fun stopLocationUpdates(): Completable {
        return Completable.create {
            emitter ->
            if (locationCallback != null) {
                locationAPI.removeLocationUpdates(locationCallback)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                locationEmitter?.onComplete()
                                locationCallback = null
                                locationEmitter?.onComplete()

                                emitter.onComplete()
                            } else {
                                Timber.d(it.exception)

                                emitter.onError(it.exception ?: LocationException("Failed to stop location updates"))
                            }
                        }
            }
        }
    }

    fun Location.toDomainModel() = LocationModel(null, longitude, latitude, accuracy, time, null, null, null)
}