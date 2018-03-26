package hu.sztomek.wheresmybuddy.presentation.screens.landing.people.friends

import android.app.Application
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.common.ResultState
import hu.sztomek.wheresmybuddy.domain.model.FriendsListModel
import hu.sztomek.wheresmybuddy.domain.usecase.ListFriendsUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.LocateUseCase
import hu.sztomek.wheresmybuddy.presentation.common.BaseViewModel
import hu.sztomek.wheresmybuddy.presentation.common.Helpers
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.common.adapter.ErrorRecyclerRowModel
import hu.sztomek.wheresmybuddy.presentation.common.adapter.LoadingRecyclerRowModel
import hu.sztomek.wheresmybuddy.presentation.common.adapter.RecyclerViewItem
import hu.sztomek.wheresmybuddy.presentation.model.IdProvider
import hu.sztomek.wheresmybuddy.presentation.model.persistable.FriendsModel
import hu.sztomek.wheresmybuddy.presentation.model.toUiModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class FriendsViewModel @Inject constructor(
        appSchedulers: AppSchedulers,
        application: Application,
        private val listUseCase: ListFriendsUseCase,
        private val locateUseCase: LocateUseCase
) : BaseViewModel<FriendsModel>(
        appSchedulers,
        application,
        State.idleStateWithData(FriendsModel(
                null,
                null,
                false,
                emptyList()
        ))
) {

    override fun updateState(current: State<FriendsModel>, result: Result): State<FriendsModel> {
        return current.copy(
                loading = when (result.action) {
                    is Action.ListFriendsAction -> {
                        !result.action.loadMore && result.state == ResultState.IN_PROGRESS
                    }
                    else -> {
                        result.state == ResultState.IN_PROGRESS
                    }
                },
                error = when(result.action) {
                    is Action.ListFriendsAction -> {
                        if (result.action.loadMore) {
                            null  // error will be displayed as a list row
                        } else {
                            parseStreamError(result.error)
                        }
                    }
                    else -> parseStreamError(result.error)
                },
                data = when (result.action) {
                    is Action.ListFriendsAction -> {
                        if (result.action.loadMore) {
                            val results = current.data?.results?.toMutableList()
                            results?.let {
                                if (result.state == ResultState.IN_PROGRESS) {
                                    // try to remove previous error row
                                    it.remove(ErrorRecyclerRowModel())
                                    // add loading row
                                    it.add(LoadingRecyclerRowModel())
                                } else {
                                    // remove previous loading row
                                    it.remove(LoadingRecyclerRowModel())
                                    if (result.error == null) {
                                        // if the request was successful, add new models
                                        Helpers.safeCastTo<FriendsListModel>(result.data)?.let {
                                            it.results.forEach {
                                                results.add(it.toUiModel())
                                            }
                                        }
                                    } else {
                                        // if the request failed, add error row
                                        it.add(ErrorRecyclerRowModel())
                                    }
                                }
                            }
                            FriendsModel(result.action.filterName, (results?.lastOrNull { it is IdProvider } as? IdProvider)?.id, result.action.trustedOnly, results?.toList() ?: emptyList())
                        } else {
                            if (result.error == null && result.state == ResultState.FINISHED){
                                val results = mutableListOf<RecyclerViewItem>()
                                Helpers.safeCastTo<FriendsListModel>(result.data)?.let {
                                    it.results.forEach {
                                        results.add(it.toUiModel())
                                    }
                                }
                                FriendsModel(result.action.filterName, (results.lastOrNull { it is IdProvider } as? IdProvider)?.id, result.action.trustedOnly, results.toList())
                            } else {
                                current.data?.copy(nameFilter = result.action.filterName, lastId = result.action.lastId, trustedOnly = result.action.trustedOnly)
                            }
                        }
                    }
                    else -> {
                        current.data
                    }
                }
        )
    }

    override fun invokeActions(): ObservableTransformer<Action, Result> {
        return ObservableTransformer { upstream ->
            Observable.merge(
                    upstream.ofType(Action.ListFriendsAction::class.java)
                            .flatMap {
                                listUseCase.execute(it)
                                        .toObservable()
                                        .onErrorReturn { t -> Result.error(it, t) }
                                        .startWith(Result.inProgress(it))
                            },
                    upstream.ofType(Action.LocateAction::class.java)
                            .flatMap {
                                locateUseCase.execute(it)
                                        .toObservable()
                                        .onErrorReturn { t -> Result.error(it, t) }
                                        .startWith(Result.inProgress(it))
                            }
            )
        }
    }

    fun listFriends(nameFilter: String?, lastId: String?, fromLastId: Boolean, trustedOnly: Boolean) {
        actions.onNext(Action.ListFriendsAction(nameFilter, lastId, fromLastId, trustedOnly, 5, false))
    }

    fun loadMore(nameFilter: String?, lastId: String?, trustedOnly: Boolean) {
        actions.onNext(Action.ListFriendsAction(nameFilter, lastId, true, trustedOnly, 5, true))
    }

    fun locate(userId: String) {
        actions.onNext(Action.LocateAction(userId))
    }
}