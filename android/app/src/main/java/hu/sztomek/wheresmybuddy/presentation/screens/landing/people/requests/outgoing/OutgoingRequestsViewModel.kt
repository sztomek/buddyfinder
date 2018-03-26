package hu.sztomek.wheresmybuddy.presentation.screens.landing.people.requests.outgoing

import android.app.Application
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.common.ResultState
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import hu.sztomek.wheresmybuddy.domain.model.OutgoingRequestsListModel
import hu.sztomek.wheresmybuddy.domain.usecase.CancelRequestUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.ListOutgoingRequestsUseCase
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

class OutgoingRequestsViewModel @Inject constructor(
        appSchedulers: AppSchedulers,
        application: Application,
        private val outgoingRequestsUseCase: ListOutgoingRequestsUseCase,
        private val cancelRequestUseCase: CancelRequestUseCase
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
                    is Action.ListOutgoingRequestsAction -> {
                        !result.action.loadMore && result.state == ResultState.IN_PROGRESS
                    }
                    else -> {
                        result.state == ResultState.IN_PROGRESS
                    }
                },
                error = when(result.action) {
                    is Action.ListOutgoingRequestsAction -> {
                        if (result.action.loadMore) {
                            null // will be presented as a list row
                        } else {
                            parseStreamError(result.error)
                        }
                    }
                    else -> parseStreamError(result.error)
                },
                data = when (result.action) {
                    is Action.ListOutgoingRequestsAction -> {
                        if (result.action.loadMore) {
                            val results = current.data?.results?.toMutableList()
                            results?.let {
                                if (result.state == ResultState.IN_PROGRESS) {
                                    it.remove(ErrorRecyclerRowModel())
                                    it.add(LoadingRecyclerRowModel())
                                } else {
                                    it.remove(LoadingRecyclerRowModel())
                                    if (result.error == null) {
                                        (result.data as? OutgoingRequestsListModel)?.let {
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
                                (result.data as? OutgoingRequestsListModel)?.let {
                                    it.results.forEach {
                                        results.add(it.toUiModel())
                                    }
                                }
                                PendingRequestsModel(result.action.filterName, (results.lastOrNull { it is IdProvider } as? IdProvider)?.id, results)
                            } else {
                                PendingRequestsModel(result.action.filterName, null, emptyList()) // empty data when the user hits search again
                            }
                        }
                    }
                    is Action.CancelRequestAction -> {
                        var data = current.data
                        if (result.state == ResultState.FINISHED && Helpers.safeCastTo<BooleanModel>(result.data)?.value == true) {
                            // trigger re-load
                            current.data?.results?.let {
                                val lastItem = it.lastOrNull {
                                    it is IdProvider && it.id != result.action.requestId
                                }
                                if (lastItem != null) {
                                    triggerSearch(current.data.nameFilter, (lastItem as IdProvider).id, false)
                                } else {
                                    // empty list
                                    data = data?.copy(results = emptyList())
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
                    upstream.ofType(Action.ListOutgoingRequestsAction::class.java)
                            .flatMap {
                                outgoingRequestsUseCase.execute(it)
                                        .toObservable()
                                        .startWith(Result.inProgress(it))
                                        .onErrorReturn { t -> Result.error(it, t) }
                            },
                    upstream.ofType(Action.CancelRequestAction::class.java)
                            .flatMap {
                                cancelRequestUseCase.execute(it)
                                        .toObservable()
                                        .startWith(Result.inProgress(it))
                                        .onErrorReturn { t -> Result.error(it, t) }
                            }
            )
        }
    }

    fun triggerSearch(filter: String?, lastId: String?, fromLast: Boolean) {
        actions.onNext(Action.ListOutgoingRequestsAction(filter, lastId, fromLast, 5, false))
    }

    fun cancelRequest(requestId: String) {
        actions.onNext(Action.CancelRequestAction(requestId))
    }

    fun loadMore(nameFilter: String?, lastId: String?) {
        actions.onNext(Action.ListOutgoingRequestsAction(nameFilter, lastId, true, 5, true))
    }
}