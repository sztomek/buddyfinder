package hu.sztomek.wheresmybuddy.presentation.screens.landing.search

import android.app.Application
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.common.ResultState
import hu.sztomek.wheresmybuddy.domain.model.ConnectionModel
import hu.sztomek.wheresmybuddy.domain.model.SearchResultModel
import hu.sztomek.wheresmybuddy.domain.model.UserModel
import hu.sztomek.wheresmybuddy.domain.usecase.*
import hu.sztomek.wheresmybuddy.presentation.common.BaseViewModel
import hu.sztomek.wheresmybuddy.presentation.common.State
import hu.sztomek.wheresmybuddy.presentation.common.UiError
import hu.sztomek.wheresmybuddy.presentation.model.ConnectionRequestModel
import hu.sztomek.wheresmybuddy.presentation.model.SearchResultItemModel
import hu.sztomek.wheresmybuddy.presentation.model.persistable.SearchModel
import hu.sztomek.wheresmybuddy.presentation.model.toUiModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import javax.inject.Inject

class SearchViewModel @Inject constructor(
        appSchedulers: AppSchedulers,
        app: Application,
        private val activeUserUseCase: GetProfileUseCase,
        private val searchUseCase: SearchUseCase,
        private val sendRequestUseCase: SendRequestUseCase,
        private val cancelRequestUseCase: CancelRequestUseCase,
        private val acceptRequestUseCase: AcceptRequestUseCase,
        private val declineRequestUseCase: DeclineRequestUseCase,
        private val locateUseCase: LocateUseCase
) : BaseViewModel<SearchModel>(
        appSchedulers,
        app,
        State.idleStateWithData(SearchModel(null, null))
) {

    // TODO refactor this state machine
    override fun updateState(current: State<SearchModel>, result: Result): State<SearchModel> {
        return when (result.action) {
            is Action.SearchAction -> {
                handleSearchResult(current, result)
            }
            is Action.SendRequestAction -> {
                handleSendRequest(current, result)
            }
            is Action.CancelRequestAction -> {
                handleCancelRequest(current, result)
            }
            is Action.AcceptRequestAction -> {
                handleAcceptRequest(current, result)
            }
            is Action.DeclineRequestAction -> {
                handleDeclineRequest(current, result)
            }
            is Action.LocateAction -> {
                handleLocate(current, result)
            }
            else -> current // TODO
        }
    }

    private fun handleLocate(current: State<SearchModel>, result: Result): State<SearchModel> {
        val currentDataCopy = current.data?.results?.toMutableList()
        val action: Action.LocateAction = result.action as Action.LocateAction
        val newData: List<SearchResultItemModel>? = when (result.state) {
            ResultState.IN_PROGRESS -> {
                currentDataCopy?.let {
                    currentDataCopy.map {
                        if (it.profileModel.id == action.userId) {
                            SearchResultItemModel.LoadingItemModel(it.profileModel, it.profileModel.id!!)
                        } else {
                            it
                        }
                    }
                }
            }
            ResultState.FINISHED -> {
                currentDataCopy?.let {
                    currentDataCopy.map {
                        if (it.profileModel.id == action.userId) {
                            SearchResultItemModel.ConnectedItemModel(
                                    it.profileModel,
                                    hu.sztomek.wheresmybuddy.presentation.model.ConnectionModel(it.profileModel.id, null, null) // TODO
                            )
                        } else {
                            it
                        }
                    }
                }
            }
        }
        return current.copy(
                error = if (result.error == null) null else UiError.GeneralUiError("Failed to locate user: ${result.error}"),
                data = current.data?.copy(results = newData?.toList()!!)
        )
    }

    private fun handleDeclineRequest(current: State<SearchModel>, result: Result): State<SearchModel> {
        val currentDataCopy = current.data?.results?.toMutableList()
        val action: Action.DeclineRequestAction = result.action as Action.DeclineRequestAction
        val newData: List<SearchResultItemModel>? = when (result.state) {
            ResultState.IN_PROGRESS -> {
                currentDataCopy?.let {
                    currentDataCopy.map {
                        if (it is SearchResultItemModel.PendingIncomingItemModel && it.connectionRequestModel.id == action.requestId) {
                            SearchResultItemModel.LoadingItemModel(it.profileModel, action.requestId)
                        } else {
                            it
                        }
                    }
                }
            }
            ResultState.FINISHED -> {
                currentDataCopy?.let {
                    currentDataCopy.map {
                        if (it is SearchResultItemModel.LoadingItemModel && it.operationId == action.requestId) {
                            if (result.error == null && result.data is ConnectionModel)
                                SearchResultItemModel.NotConnectedItemModel(it.profileModel)
                            else SearchResultItemModel.PendingIncomingItemModel(it.profileModel, ConnectionRequestModel(action.requestId, null, null, null)) // TODO
                        } else {
                            it
                        }
                    }
                }
            }
        }
        return current.copy(
                error = if (result.error == null) null else UiError.GeneralUiError("Failed to send request: ${result.error}"),
                data = current.data?.copy(results = newData?.toList()!!) // TODO fuck you
        )
    }

    private fun handleAcceptRequest(current: State<SearchModel>, result: Result): State<SearchModel> {
        val currentDataCopy = current.data?.results?.toMutableList()
        val action: Action.AcceptRequestAction = result.action as Action.AcceptRequestAction
        val newData: List<SearchResultItemModel>? = when (result.state) {
            ResultState.IN_PROGRESS -> {
                currentDataCopy?.let {
                    currentDataCopy.map {
                        if (it is SearchResultItemModel.PendingIncomingItemModel && it.connectionRequestModel.id == action.requestId) {
                            SearchResultItemModel.LoadingItemModel(it.profileModel, action.requestId)
                        } else {
                            it
                        }
                    }
                }
            }
            ResultState.FINISHED -> {
                currentDataCopy?.let {
                    currentDataCopy.map {
                        if (it is SearchResultItemModel.LoadingItemModel && it.operationId == action.requestId) {
                            if (result.error == null && result.data is ConnectionModel)
                                SearchResultItemModel.ConnectedItemModel(it.profileModel, result.data.toUiModel())
                            else SearchResultItemModel.PendingIncomingItemModel(it.profileModel, ConnectionRequestModel(action.requestId, null, null, null)) // TODO
                        } else {
                            it
                        }
                    }
                }
            }
        }
        return current.copy(
                error = if (result.error == null) null else UiError.GeneralUiError("Failed to send request: ${result.error}"),
                data = current.data?.copy(results = newData?.toList()!!) // TODO fuck you
        )
    }

    private fun handleCancelRequest(current: State<SearchModel>, result: Result): State<SearchModel> {
        val currentDataCopy = current.data?.results?.toMutableList()
        val action: Action.CancelRequestAction = result.action as Action.CancelRequestAction
        val newData: List<SearchResultItemModel>? = when (result.state) {
            ResultState.IN_PROGRESS -> {
                currentDataCopy?.let {
                    currentDataCopy.map {
                        if (it is SearchResultItemModel.PendingOutgoingItemModel && it.connectionRequestModel.id == action.requestId) {
                            SearchResultItemModel.LoadingItemModel(it.profileModel, action.requestId)
                        } else {
                            it
                        }
                    }
                }
            }
            ResultState.FINISHED -> {
                currentDataCopy?.let {
                    currentDataCopy.map {
                        if (it is SearchResultItemModel.LoadingItemModel && it.operationId == action.requestId) {
                            if (result.error == null)
                                SearchResultItemModel.NotConnectedItemModel(it.profileModel)
                            else SearchResultItemModel.PendingOutgoingItemModel(it.profileModel, ConnectionRequestModel(action.requestId, null, null, null)) // TODO
                        } else {
                            it
                        }
                    }
                }
            }
        }
        return current.copy(
                error = if (result.error == null) null else UiError.GeneralUiError("Failed to send request: ${result.error}"),
                data = current.data?.copy(results = newData?.toList()!!) // TODO fuck you
        )
    }

    private fun handleSendRequest(current: State<SearchModel>, result: Result): State<SearchModel> {
        val currentDataCopy = current.data?.results?.toMutableList()
        val action: Action.SendRequestAction = result.action as Action.SendRequestAction
        val newData: List<SearchResultItemModel>? = when (result.state) {
            ResultState.IN_PROGRESS -> {
                currentDataCopy?.let {
                    currentDataCopy.map {
                        if (it.profileModel.id == action.userId) {
                            SearchResultItemModel.LoadingItemModel(it.profileModel, it.profileModel.id!!)
                        } else {
                            it
                        }
                    }
                }
            }
            ResultState.FINISHED -> {
                currentDataCopy?.let {
                    currentDataCopy.map {
                        if (it.profileModel.id == action.userId) {
                            if (result.error == null && result.data is hu.sztomek.wheresmybuddy.domain.model.ConnectionRequestModel)
                                SearchResultItemModel.PendingOutgoingItemModel(
                                        it.profileModel,
                                        ConnectionRequestModel(
                                                result.data.id,
                                                result.data.created,
                                                result.data.from,
                                                result.data.to
                                        )
                                )
                            else SearchResultItemModel.NotConnectedItemModel(it.profileModel)
                        } else {
                            it
                        }
                    }
                }
            }
        }
        return current.copy(
                error = if (result.error == null) null else UiError.GeneralUiError("Failed to send request: ${result.error}"),
                data = current.data?.copy(results = newData?.toList()!!)
        )
    }

    private fun handleSearchResult(current: State<SearchModel>, result: Result): State<SearchModel> {
        val searchAction = result.action as Action.SearchAction
        var newData = current.data?.copy(keyWord = result.action.keyWord, lastId = result.action.lastId)
        if (result.state == ResultState.FINISHED) {
            val searchResultModel = result.data as SearchResultModel?
            searchResultModel?.let {
                val currentUserId = (activeUserUseCase.execute(Action.GetProfileAction(null)).blockingGet().data as UserModel).id
                var items: List<SearchResultItemModel>
                if (searchAction.loadMore) {
                    val results = current.data?.results?.toMutableList()
                    it.results.forEach {
                           results?.add(when {
                               it.connectionRequest != null && currentUserId == it.connectionRequest.to -> SearchResultItemModel.PendingIncomingItemModel(it.user.toUiModel(), it.connectionRequest.toUiModel())
                               it.connectionRequest != null && currentUserId == it.connectionRequest.from -> SearchResultItemModel.PendingOutgoingItemModel(it.user.toUiModel(), it.connectionRequest.toUiModel())
                               it.connection != null -> SearchResultItemModel.ConnectedItemModel(it.user.toUiModel(), it.connection.toUiModel())
                               else -> SearchResultItemModel.NotConnectedItemModel(it.user.toUiModel())
                           })
                    }
                    items = results?.toList() ?: emptyList()
                } else {
                    items = it.results.map {
                        return@map when {
                            it.connectionRequest != null && currentUserId == it.connectionRequest.to -> SearchResultItemModel.PendingIncomingItemModel(it.user.toUiModel(), it.connectionRequest.toUiModel())
                            it.connectionRequest != null && currentUserId == it.connectionRequest.from -> SearchResultItemModel.PendingOutgoingItemModel(it.user.toUiModel(), it.connectionRequest.toUiModel())
                            it.connection != null -> SearchResultItemModel.ConnectedItemModel(it.user.toUiModel(), it.connection.toUiModel())
                            else -> SearchResultItemModel.NotConnectedItemModel(it.user.toUiModel())
                        }
                    }
                }
                newData = newData?.copy(
                        lastId = items.lastOrNull()?.id,
                        results = items
                )
            }
        } else {
            if (!searchAction.loadMore) {
                newData = newData?.copy(
                        results = emptyList()
                )
            }
        }
        return current.copy(
                loading = result.state == ResultState.IN_PROGRESS,
                error = parseStreamError(result.error),
                data = newData)
    }

    override fun invokeActions(): ObservableTransformer<Action, Result> {
        val connectionRelated = ObservableTransformer<Action, Result> {
            upstream ->  Observable.merge(
                upstream.ofType(Action.SendRequestAction::class.java)
                        .flatMap({ action ->
                            sendRequestUseCase.execute(action)
                                    .toObservable()
                                    .onErrorReturn { Result.error(action, it) }
                                    .startWith(Result.inProgress(action))
                        }),
                upstream.ofType(Action.CancelRequestAction::class.java)
                        .flatMap({ action ->
                            cancelRequestUseCase.execute(action)
                                    .toObservable()
                                    .onErrorReturn { Result.error(action, it) }
                                    .startWith(Result.inProgress(action))
                        }),
                upstream.ofType(Action.AcceptRequestAction::class.java)
                        .flatMap({ action ->
                            acceptRequestUseCase.execute(action)
                                    .toObservable()
                                    .onErrorReturn { Result.error(action, it) }
                                    .startWith(Result.inProgress(action))
                        }),
                upstream.ofType(Action.DeclineRequestAction::class.java)
                        .flatMap({ action ->
                            declineRequestUseCase.execute(action)
                                    .toObservable()
                                    .onErrorReturn { Result.error(action, it) }
                                    .startWith(Result.inProgress(action))
                        }))
        }
        return ObservableTransformer { upstream ->
            Observable.merge(
                    upstream.ofType(Action.SearchAction::class.java)
                            .flatMap({ action ->
                                if (action.keyWord.length >= 2) {
                                    searchUseCase.execute(action)
                                            .toObservable()
                                            .onErrorReturn { Result.error(action, it) }
                                            .startWith(Result.inProgress(action))
                                } else {
                                    Observable.just(Result.error(action, UiError.FieldValidationError(String.format(getApplication<Application>().getString(R.string.error_format_search_keyword_length), 2))))
                                }
                            }),
                    upstream.ofType(Action.LocateAction::class.java)
                            .flatMap {
                                locateUseCase.execute(it)
                                        .toObservable()
                                        .startWith(Result.inProgress(it))
                                        .onErrorReturn { t -> Result.error(it, t) }
                            },
                    upstream.compose(connectionRelated)
            )
        }
    }

    fun triggerSearch(keyword: String, lastId: String?, refresh: Boolean) {
        actions.onNext(Action.SearchAction(keyword, lastId, !refresh, 10, false))
    }

    fun loadMore(keyword: String, lastId: String?) {
        actions.onNext(Action.SearchAction(keyword, lastId, true, 10, true))
    }

    fun sendRequest(userId: String) {
        actions.onNext(Action.SendRequestAction(userId))
    }

    fun cancelRequest(requestId: String) {
        actions.onNext(Action.CancelRequestAction(requestId))
    }

    fun handleRequest(requestId: String, accept: Boolean) {
        actions.onNext(if (accept) Action.AcceptRequestAction(requestId) else Action.DeclineRequestAction(requestId))
    }

    fun locate(userId: String) {
        actions.onNext(Action.LocateAction(userId))
    }
}