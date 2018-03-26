package hu.sztomek.wheresmybuddy.presentation.screens.landing.discover

import android.app.Application
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.common.ResultState
import hu.sztomek.wheresmybuddy.domain.model.DiscoverListModel
import hu.sztomek.wheresmybuddy.domain.model.LocationModel
import hu.sztomek.wheresmybuddy.domain.model.UserModel
import hu.sztomek.wheresmybuddy.domain.usecase.DiscoverUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.GetLocationUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.GetProfileUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.LoadLocationUseCase
import hu.sztomek.wheresmybuddy.presentation.common.BaseViewModel
import hu.sztomek.wheresmybuddy.presentation.common.Helpers
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.model.MarkerModel
import hu.sztomek.wheresmybuddy.presentation.model.persistable.DiscoverModel
import hu.sztomek.wheresmybuddy.presentation.model.toUiModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
        appSchedulers: AppSchedulers,
        application: Application,
        private val loadLocationUseCase: LoadLocationUseCase,
        private val getLocationUseCase: GetLocationUseCase,
        private val getProfileUseCase: GetProfileUseCase,
        private val discoverUseCase: DiscoverUseCase
) : BaseViewModel<DiscoverModel>(
        appSchedulers,
        application,
        State.idleStateWithData(DiscoverModel(null))
) {

    override fun updateState(current: State<DiscoverModel>, result: Result): State<DiscoverModel> {
        return current.copy(
                loading = result.state == ResultState.IN_PROGRESS,
                error = parseStreamError(result.error),
                data = when {
                    result.action is Action.UpdateSelfLocation -> {
                        if (result.state == ResultState.FINISHED && result.data is LocationModel) {
                            current.data?.copy(
                                    selfLongitude = result.data.longitude,
                                    selfLatitude = result.data.latitude
                            )
                        } else {
                            current.data?.copy()
                        }
                    }
                    result.action is Action.GetLocationByIdAction -> {
                        if (result.state == ResultState.IN_PROGRESS) {
                            DiscoverModel(result.action.locationId)
                        } else {
                            val model = Helpers.safeCastTo<LocationModel>(result.data)
                            getProfile(model?.fromId)
                            if (model != null) {
                                current.data?.copy(
                                        targetMarker = MarkerModel(
                                                model.toUiModel(),
                                                null,
                                                getMarkerInfo(model)
                                        )
                                )
                            } else {
                                current.data?.copy()
                            }
                        }
                    }
                    result.action is Action.GetLocationAction && result.state == ResultState.FINISHED -> {
                        val model = Helpers.safeCastTo<LocationModel>(result.data)
                        current.data?.copy(
                                selfLatitude = model?.latitude,
                                selfLongitude = model?.longitude
                        )
                    }
                    result.action is Action.GetProfileAction && result.state == ResultState.FINISHED -> {
                        val model = Helpers.safeCastTo<UserModel>(result.data)
                        if (model != null) {
                            current.data?.copy(
                                    targetMarker = current.data.targetMarker?.copy(
                                            profile = model.toUiModel()
                                    )
                            )
                        } else {
                            current.data?.copy()
                        }
                    }
                    result.action is Action.DiscoverAction && result.state == ResultState.FINISHED -> {
                        val models = Helpers.safeCastTo<DiscoverListModel>(result.data)
                        val markers = mutableListOf<MarkerModel>()
                        models?.let {
                            it.models.forEach {
                                markers.add(it.toUiModel(getMarkerInfo(it.location)))
                            }
                        }
                        current.data?.copy(
                                targetMarker = null,
                                markers = markers.toList()
                        )
                    }
                    else -> current.data?.copy()
                }
        )
    }

    private fun getMarkerInfo(model: LocationModel): String? {
        return getApplication<Application>().getString(R.string.format_last_seen, if (model.timestamp != null) SimpleDateFormat("MM/dd/yyyy kk:mm").format(Date(model.timestamp)) else "N/A")
    }

    override fun invokeActions(): ObservableTransformer<Action, Result> {
        val locationTransformers = ObservableTransformer<Action, Result> { upstream ->
            Observable.merge(
                    upstream.ofType(Action.GetLocationByIdAction::class.java)
                            .flatMap {
                                loadLocationUseCase.execute(it)
                                        .toObservable()
                                        .onErrorReturn { t -> Result.error(it, t) }
                                        .startWith(Result.inProgress(it))
                            },
                    upstream.ofType(Action.GetLocationAction::class.java)
                            .flatMap {
                                getLocationUseCase.execute(it)
                                        .toObservable()
                                        .onErrorReturn { t -> Result.error(it, t) }
                                        .startWith(Result.inProgress(it))
                            },
                    upstream.ofType(Action.UpdateSelfLocation::class.java)
                            .flatMap {
                                Observable.just(Result.complete(it, LocationModel(null, it.longitude, it.latitude, null, null, null, null, null)))
                            },
                    upstream.ofType(Action.DiscoverAction::class.java)
                            .flatMap {
                                discoverUseCase.execute(it)
                                        .toObservable()
                                        .startWith(Result.inProgress(it))
                                        .onErrorReturn { t -> Result.error(it, t) }
                            }
            )
        }
        return ObservableTransformer {
            upstream ->
                Observable.merge(
                        upstream.ofType(Action.GetProfileAction::class.java)
                                .flatMap {
                                    getProfileUseCase.execute(it)
                                            .toObservable()
                                            .onErrorReturn { t -> Result.error(it, t) }
                                },
                        upstream.compose(locationTransformers)
                )
        }
    }

    fun getLocation(locationId: String) {
        actions.onNext(Action.GetLocationByIdAction(locationId))
    }

    fun getSelfLocation() {
        actions.onNext(Action.GetLocationAction())
    }

    private fun getProfile(userId: String?) {
        actions.onNext(Action.GetProfileAction(userId))
    }

    fun discover() {
        actions.onNext(Action.DiscoverAction())
    }

    fun updateSelfLocation(longitude: Double, latitude: Double) {
        actions.onNext(Action.UpdateSelfLocation(longitude, latitude))
    }

}