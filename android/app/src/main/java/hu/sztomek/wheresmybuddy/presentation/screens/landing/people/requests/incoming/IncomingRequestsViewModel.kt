package hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.incoming

import android.app.Application
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.common.ResultState
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import hu.sztomek.wheresmybuddy.domain.model.ConnectionModel
import hu.sztomek.wheresmybuddy.domain.model.IncomingRequestsListModel
import hu.sztomek.wheresmybuddy.domain.usecase.AcceptRequestUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.DeclineRequestUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.ListIncomingRequestsUseCase
import hu.sztomek.wheresmybuddy.presentation.common.BaseViewModel
import hu.sztomek.wheresmybuddy.presentation.common.Helpers
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.common.adapter.ErrorRecyclerRowModel
import hu.sztomek.wheresmybuddy.presentation.common.adapter.LoadingRecyclerRowModel
import hu.sztomek.wheresmybuddy.presentation.common.adapter.RecyclerViewItem
import hu.sztomek.wheresmybuddy.presentation.model.IdProvider
import hu.sztomek.wheresmybuddy.presentation.model.persistable.PendingRequestsModel
import hu.sztomek.wheresmybuddy.presentation.model.toUiModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class IncomingRequestsViewModel @Inject constructor(
        appSchedulers: AppSchedulers,
        application: Application,
        private val listIncomingUseCase: ListIncomingRequestsUseCase,
        private val acceptRequestUseCase: AcceptRequestUseCase,
        private val declineRequestUseCase: DeclineRequestUseCase
) : BaseViewModel<PendingRequestsModel>(
        appSchedulers,
        application,
        State.idleStateWithData(PendingRequestsModel(
                null,
                null,
                emptyList()
        ))
) {
    override fun updateState(current: State<PendingRequestsModel>, result: Result): State<PendingRequestsModel> {
        return current.copy(
                loading = when (result.action) {
                    is Action.ListIncomingRequestsAction -> {
                        !result.action.loadMore && result.state == ResultState.IN_PROGRESS
                    }
                    else -> {
                        result.state == ResultState.IN_PROGRESS
                    }
                },
                error = when (result.action) {
                    is Action.ListIncomingRequestsAction -> {
                        if (result.action.loadMore) {
                            null // will be presented as a list row
                        } else {
                            parseStreamError(result.error)
                        }
                    }
                    else -> parseStreamError(result.error)
                },
                data = when (result.action) {
                    is Action.ListIncomingRequestsAction -> {
                        if (result.action.loadMore) {
                            val results = current.data?.results?.toMutableList()
                            results?.let {
                                if (result.state == ResultState.IN_PROGRESS) {
                                    it.remove(ErrorRecyclerRowModel())
                                    it.add(LoadingRecyclerRowModel())
                                } else {
                                    it.remove(LoadingRecyclerRowModel())
                                    if (result.error == null) {
                                        (result.data as? IncomingRequestsListModel)?.let {
                                            it.results.forEach {
                                                results.add(it.toUiModel())
                                            }
                                        }
                                    } else {
                                        results.add(ErrorRecyclerRowModel())
                                    }
                                }
                            }
                            PendingRequestsModel(result.action.filterName, (results?.lastOrNull { it is IdProvider } as? IdProvider)?.id, results?.toList() ?: emptyList())
                        } else {
                            if (result.state == ResultState.FINISHED) {
                                val results = mutableListOf<RecyclerViewItem>()
                                (result.data as? IncomingRequestsListModel)?.let {
                                    it.results.forEach {
                                        results.add(it.toUiModel())
                                    }
                                }
                                PendingRequestsModel(result.action.filterName, (results.lastOrNull { it is IdProvider } as? IdProvider)?.id, results)
                            } else {
                                current.data
                            }
                        }
                    }
                    is Action.DeclineRequestAction -> {
                        var data = current.data
                        if (result.state == ResultState.FINISHED && Helpers.safeCastTo<BooleanModel>(result.data)?.value == true) {
                            // trigger reload
                            current.data?.results?.let {
                                val lastItem = it.lastOrNull {
                                    it is IdProvider && it.id != result.action.requestId
                                }
                                if(lastItem != null) {
                                    triggerSearch(current.data.nameFilter, (lastItem as IdProvider).id, false)
                                } else {
                                    // empty list
                                    data = data?.copy(
                                            lastId = null,
                                            results = emptyList()
                                    )
                                }
                            }
                        }
                        data
                    }
                    is Action.AcceptRequestAction -> {
                        var data = current.data
                        if (result.state == ResultState.FINISHED && Helpers.safeCastTo<ConnectionModel>(result.data) != null) {
                            // trigger reload
                            current.data?.results?.let {
                                val lastItem = it.lastOrNull {
                                    it is IdProvider &&  it.id != result.action.requestId
                                }
                                if (lastItem != null) {
                                    triggerSearch(current.data.nameFilter, (lastItem as IdProvider).id, false)
                                } else {
                                    // empty list
                                    data = data?.copy(
                                            lastId = null,
                                            results = emptyList()
                                    )
                                }
                            }
                        }
                        data
                    }
                    else -> current.data
                }
        )
    }

    override fun invokeActions(): ObservableTransformer<Action, Result> {
        return ObservableTransformer { upstream ->
            Observable.merge(
                    upstream.ofType(Action.ListIncomingRequestsAction::class.java)
                            .flatMap {
                                listIncomingUseCase.execute(it)
                                        .toObservable()
                                        .startWith(Result.inProgress(it))
                                        .onErrorReturn { t -> Result.error(it, t) }
                            },
                    upstream.ofType(Action.AcceptRequestAction::class.java)
                            .flatMap {
                                acceptRequestUseCase.execute(it)
                                        .toObservable()
                                        .startWith(Result.inProgress(it))
                                        .onErrorReturn { t -> Result.error(it, t) }
                            },
                    upstream.ofType(Action.DeclineRequestAction::class.java)
                            .flatMap {
                                declineRequestUseCase.execute(it)
                                        .toObservable()
                                        .startWith(Result.inProgress(it))
                                        .onErrorReturn { t -> Result.error(it, t) }
                            }
            )
        }
    }

    fun triggerSearch(filter: String?, lastId: String?, fromLast: Boolean) {
        actions.onNext(Action.ListIncomingRequestsAction(filter, lastId, fromLast, 5, false))
    }

    fun acceptRequest(requestId: String) {
        actions.onNext(Action.AcceptRequestAction(requestId))
    }

    fun declineRequest(requestId: String) {
        actions.onNext(Action.DeclineRequestAction(requestId))
    }

    fun loadMore(nameFilter: String?, lastId: String?) {
        actions.onNext(Action.ListIncomingRequestsAction(nameFilter, lastId, true, 5, true))
    }
}