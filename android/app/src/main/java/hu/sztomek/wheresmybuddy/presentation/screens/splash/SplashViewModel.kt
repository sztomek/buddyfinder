    package hu.sztomek.wheresmybuddy.presentation.screens.splash

import android.app.Application
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.common.ResultState
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import hu.sztomek.wheresmybuddy.domain.usecase.AcceptRequestUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.CheckGcmTokenUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.DeclineRequestUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.HasActiveUserUseCase
import hu.sztomek.wheresmybuddy.presentation.common.BaseViewModel
import hu.sztomek.wheresmybuddy.presentation.common.Helpers
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.model.persistable.SplashModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class SplashViewModel @Inject constructor(
        application: Application,
        schedulers: AppSchedulers,
        private val hasUserUseCase: HasActiveUserUseCase,
        private val acceptRequestUseCase: AcceptRequestUseCase,
        private val declineRequestUseCase: DeclineRequestUseCase,
        private val checkGcmTokenUseCase: CheckGcmTokenUseCase
) : BaseViewModel<SplashModel>(
        schedulers,
        application,
        State.idleStateWithData(SplashModel(null, null))
) {

    override fun updateState(current: State<SplashModel>, result: Result): State<SplashModel> {
        return current.copy(
                loading = result.state == ResultState.IN_PROGRESS,
                data = current.data?.copy(
                        hasUser = when {
                            result.action is Action.HasActiveUserAction -> {
                                if (result.state == ResultState.FINISHED) {
                                    if (result.error != null) {
                                        false
                                    } else {
                                        Helpers.safeCastTo<BooleanModel>(result.data)?.value
                                    }
                                } else {
                                    current.data.hasUser
                                }
                            }
                            else -> current.data.hasUser
                        },
                        hasGcmToken = when {
                            result.action is Action.CheckGcmTokenAction -> {
                                if (result.state == ResultState.FINISHED) {
                                    if (result.error != null) {
                                        false
                                    } else {
                                        Helpers.safeCastTo<BooleanModel>(result.data)?.value
                                    }
                                } else {
                                    current.data.hasGcmToken
                                }
                            }
                            else -> current.data.hasGcmToken
                        }
                )
        )
    }

    override fun invokeActions(): ObservableTransformer<Action, Result> {
        return ObservableTransformer { upstream ->
            Observable.merge(
                    upstream.observeOn(appSchedulers.io)
                            .ofType(Action.HasActiveUserAction::class.java)
                            .flatMap {
                                hasUserUseCase.execute(it)
                                        .toObservable()
                                        .onErrorReturn { t -> Result.error(it, t) }
                                        .startWith(Result.inProgress(it))
                            },
                    upstream.ofType(Action.AcceptRequestAction::class.java)
                            .flatMap {
                                acceptRequestUseCase.execute(it)
                                        .toObservable()
                                        .onErrorReturn { t -> Result.error(it, t) }
                                        .startWith(Result.inProgress(it))
                            },
                    upstream.ofType(Action.DeclineRequestAction::class.java)
                            .flatMap {
                                declineRequestUseCase.execute(it)
                                        .toObservable()
                                        .onErrorReturn { t -> Result.error(it, t) }
                                        .startWith(Result.inProgress(it))
                            },
                    upstream.ofType(Action.CheckGcmTokenAction::class.java)
                            .flatMap {
                                checkGcmTokenUseCase.execute(it)
                                        .toObservable()
                                        .onErrorReturn { t -> Result.error(it, t) }
                                        .startWith(Result.inProgress(it))
                            }
            )
        }
    }

    fun checkUser() {
        actions.onNext(Action.HasActiveUserAction())
    }

    fun acceptRequest(requestId: String) {
        actions.onNext(Action.AcceptRequestAction(requestId))
    }

    fun declineRequest(requestId: String) {
        actions.onNext(Action.DeclineRequestAction(requestId))
    }

    fun checkGcmToken() {
        actions.onNext(Action.CheckGcmTokenAction())
    }

}