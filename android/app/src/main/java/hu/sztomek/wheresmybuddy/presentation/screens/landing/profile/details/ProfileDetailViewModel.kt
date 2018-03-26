package hu.sztomek.wheresmybuddy.presentation.screens.landing.profile.details

import android.app.Application
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.common.ResultState
import hu.sztomek.wheresmybuddy.domain.model.ConnectionModel
import hu.sztomek.wheresmybuddy.domain.model.ConnectionRequestModel
import hu.sztomek.wheresmybuddy.domain.model.SearchResultItemModel
import hu.sztomek.wheresmybuddy.domain.usecase.*
import hu.sztomek.wheresmybuddy.presentation.common.BaseViewModel
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileDetailModel
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel
import hu.sztomek.wheresmybuddy.presentation.model.toUiModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import timber.log.Timber
import javax.inject.Inject

class ProfileDetailViewModel @Inject constructor(
        appSchedulers: AppSchedulers,
        application: Application,
        private val profileDetailsUseCase: GetProfileDetailsUseCase,
        private val sendRequestUseCase: SendRequestUseCase,
        private val cancelRequestUseCase: CancelRequestUseCase,
        private val acceptRequestUseCase: AcceptRequestUseCase,
        private val declineRequestUseCase: DeclineRequestUseCase,
        private val deleteConnectionUseCase: DeleteConnectionUseCase,
        private val locateUseCase: LocateUseCase,
        private val updateConnectionUseCase: UpdateConnectionUseCase
) : BaseViewModel<ProfileDetailModel>(
        appSchedulers,
        application,
        State.idleStateWithData(ProfileDetailModel(ProfileModel(null, null, null), null, null))
) {

    override fun updateState(current: State<ProfileDetailModel>, result: Result): State<ProfileDetailModel> {
        return current.copy(
                loading = result.state == ResultState.IN_PROGRESS,
                error = when {
                    result.error != null -> parseStreamError(result.error)
                    else -> null
                },
                data = if (result.state != ResultState.FINISHED || result.error != null) current.data else when(result.action) {
                    is Action.GetProfileDetailsAction -> {
                        val model = result.data as SearchResultItemModel
                        ProfileDetailModel(
                                model.user.toUiModel(),
                                model.connectionRequest?.toUiModel(),
                                model.connection?.toUiModel()
                        )
                    }
                    is Action.SendRequestAction -> {
                        val connectionReq = result.data as ConnectionRequestModel
                        current.data?.copy(
                                connectionRequestModel = connectionReq.toUiModel(),
                                connectionModel = null
                        )
                    }
                    is Action.CancelRequestAction, is Action.DeclineRequestAction, is Action.DeleteConnectionAction -> current.data?.copy(connectionRequestModel = null, connectionModel = null)
                    is Action.AcceptRequestAction -> {
                        val connection = result.data as ConnectionModel
                        current.data?.copy(
                                connectionRequestModel = null,
                                connectionModel = connection.toUiModel()
                        )
                    }
                    is Action.UpdateConnectionAction -> {
                        current.data?.copy(
                                connectionModel = current.data.connectionModel?.copy(
                                        trusted =  result.action.newLevel > 0
                                )
                        )
                    }
                    else -> {
                        Timber.d("Unhandled action [${result.action}]")
                        current.data
                    }
                }
        )
    }

    override fun invokeActions(): ObservableTransformer<Action, Result> {
        val transformer1: ObservableTransformer<Action, Result> = ObservableTransformer { upstream ->
            Observable.merge(
                    upstream.ofType(Action.GetProfileDetailsAction::class.java)
                            .flatMap { action ->
                                profileDetailsUseCase.execute(action)
                                        .onErrorReturn { Result.error(action, it) }
                                        .startWith(Result.inProgress(action))
                            },
                    upstream.ofType(Action.SendRequestAction::class.java)
                            .flatMap { action ->
                                sendRequestUseCase.execute(action)
                                        .toObservable()
                                        .onErrorReturn { Result.error(action, it) }
                                        .startWith(Result.inProgress(action))
                            },
                    upstream.ofType(Action.CancelRequestAction::class.java)
                            .flatMap { action ->
                                cancelRequestUseCase.execute(action)
                                        .toObservable()
                                        .onErrorReturn { Result.error(action, it) }
                                        .startWith(Result.inProgress(action))
                            },
                    upstream.ofType(Action.AcceptRequestAction::class.java)
                            .flatMap { action ->
                                acceptRequestUseCase.execute(action)
                                        .toObservable()
                                        .onErrorReturn { Result.error(action, it) }
                                        .startWith(Result.inProgress(action))
                            }
            )
        }
        val transformer2: ObservableTransformer<Action, Result> = ObservableTransformer { upstream ->
            Observable.merge(
                    upstream.ofType(Action.DeclineRequestAction::class.java)
                            .flatMap { action ->
                                declineRequestUseCase.execute(action)
                                        .toObservable()
                                        .onErrorReturn { Result.error(action, it) }
                                        .startWith(Result.inProgress(action))
                            },
                    upstream.ofType(Action.DeleteConnectionAction::class.java)
                            .flatMap { action ->
                                deleteConnectionUseCase.execute(action)
                                        .toObservable()
                                        .onErrorReturn { Result.error(action, it) }
                                        .startWith(Result.inProgress(action))
                            },
                    upstream.ofType(Action.LocateAction::class.java)
                            .flatMap { action ->
                                locateUseCase.execute(action)
                                        .toObservable()
                                        .onErrorReturn { Result.error(action, it) }
                                        .startWith(Result.inProgress(action))
                            },
                    upstream.ofType(Action.UpdateConnectionAction::class.java)
                            .flatMap { action ->
                                updateConnectionUseCase.execute(action)
                                        .toObservable()
                                        .onErrorReturn { Result.error(action, it) }
                                        .startWith(Result.inProgress(action))
                            }

            )
        }
        return ObservableTransformer { upstream ->
            Observable.merge(
                    upstream.compose(transformer1),
                    upstream.compose(transformer2)
            )
        }


    }

    fun getProfileDetails(userId: String) {
        actions.onNext(Action.GetProfileDetailsAction(userId))
    }

    fun sendRequest(userId: String) {
        actions.onNext(Action.SendRequestAction(userId))
    }

    fun cancelRequest(requestId: String) {
        actions.onNext(Action.CancelRequestAction(requestId))
    }

    fun acceptRequest(requestId: String) {
        actions.onNext(Action.AcceptRequestAction(requestId))
    }

    fun declineRequest(requestId: String) {
        actions.onNext(Action.DeclineRequestAction(requestId))
    }

    fun deleteConnection(userId: String) {
        actions.onNext(Action.DeleteConnectionAction(userId))
    }

    fun locate(userId: String) {
        actions.onNext(Action.LocateAction(userId))
    }

    fun updateTrustedState(trusted: Boolean, userId: String) {
        actions.onNext(Action.UpdateConnectionAction(
                userId,
                if (trusted) 1 else 0
        ))
    }
}