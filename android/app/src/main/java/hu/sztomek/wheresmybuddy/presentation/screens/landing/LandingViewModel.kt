package hu.sztomek.wheresmybuddy.presentation.screens.landing

import android.app.Application
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.common.ResultState
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import hu.sztomek.wheresmybuddy.domain.model.UserModel
import hu.sztomek.wheresmybuddy.domain.usecase.GetBroadcastStateUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.GetProfileUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.LogoutUseCase
import hu.sztomek.wheresmybuddy.presentation.common.BaseViewModel
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.model.persistable.LandingModel
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel
import hu.sztomek.wheresmybuddy.presentation.model.toUiModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import timber.log.Timber
import javax.inject.Inject

class LandingViewModel @Inject constructor(
        appSchedulers: AppSchedulers,
        app: Application,
        private val useCase: GetProfileUseCase,
        private val logoutUseCase: LogoutUseCase,
        private val getBroadcastStateUseCase: GetBroadcastStateUseCase
) : BaseViewModel<LandingModel>(
        appSchedulers,
        app,
        State.idleStateWithData(data = LandingModel(ProfileModel(null, null, null), null))
) {

    override fun updateState(current: State<LandingModel>, result: Result): State<LandingModel> {
        return current.copy(
                loading = result.state == ResultState.IN_PROGRESS,
                error = parseStreamError(result.error),
                data = if (result.action is Action.LogoutAction && result.data is BooleanModel && result.data.value) {
                    null
                } else {
                    current.data?.copy(
                            profileModel = if (result.state == ResultState.FINISHED) {
                                when (result.data) {
                                    null -> current.data.profileModel
                                    is UserModel -> result.data.toUiModel()
                                    else -> {
                                        Timber.d("Unhandled Result.data type [${result.data}]")
                                        current.data.profileModel
                                    }
                                }
                            } else current.data.profileModel,
                            isBroadcasting = if (result.state == ResultState.FINISHED) {
                                when {
                                    result.action is Action.GetBroadcastStateAction && result.data is BooleanModel -> result.data.value
                                    result.action is Action.StartBroadcastLocationAction -> true
                                    result.action is Action.StopBroadcastLocationAction -> false
                                    else -> current.data.isBroadcasting
                                }
                            } else {
                                current.data.isBroadcasting
                            }
                    )
                }
        )
    }

    override fun invokeActions(): ObservableTransformer<Action, Result> {
        val locationTransformer: ObservableTransformer<Action, Result> = ObservableTransformer { upstream ->
            Observable.merge(
                    upstream.observeOn(appSchedulers.io)
                            .ofType(Action.GetBroadcastStateAction::class.java)
                            .flatMap {
                                getBroadcastStateUseCase.execute(it)
                                        .toObservable()
                                        .onErrorReturn { t -> Result.error(it, t) }
                            },
                    upstream.observeOn(appSchedulers.io)
                            .ofType(Action.StartBroadcastLocationAction::class.java)
                            .flatMap {
                                Observable.just(Result.complete(it, BooleanModel(true)))
                            },
                    upstream.observeOn(appSchedulers.io)
                            .ofType(Action.StopBroadcastLocationAction::class.java)
                            .flatMap {
                                Observable.just(Result.complete(it, BooleanModel(true)))
                            }
            )
        }
        return ObservableTransformer { upstream ->
            Observable.merge(
                    upstream.observeOn(appSchedulers.io)
                            .ofType(Action.GetProfileAction::class.java)
                            .flatMap({ action ->
                                useCase.execute(action)
                                        .toObservable()
                                        .onErrorReturn { Result.error(action, it) }
                                        .startWith(Result.inProgress(action))
                            }),
                    upstream.observeOn(appSchedulers.io)
                            .ofType(Action.LogoutAction::class.java)
                            .flatMap({ action ->
                                logoutUseCase.execute(action)
                                        .toObservable()
                                        .onErrorReturn { Result.error(action, it) }
                                        .startWith(Result.inProgress(action))
                            }),
                    upstream.compose(locationTransformer)
            )
        }
    }

    fun getProfile() {
        actions.onNext(Action.GetProfileAction(null))
    }

    fun logout() {
        actions.onNext(Action.LogoutAction())
    }

    fun getBroadcastState() {
        actions.onNext(Action.GetBroadcastStateAction())
    }

    fun startBroadcast() {
        actions.onNext(Action.StartBroadcastLocationAction())
    }

    fun stopBroadcast() {
        actions.onNext(Action.StopBroadcastLocationAction())
    }

}