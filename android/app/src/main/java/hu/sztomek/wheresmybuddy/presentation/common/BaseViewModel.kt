package hu.sztomek.wheresmybuddy.presentation.common

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

abstract class BaseViewModel<M>(protected val appSchedulers: AppSchedulers, app: Application, initialState: State<M>) : AndroidViewModel(app) {

    val state = MutableLiveData<State<M>>()
    protected val actions: PublishSubject<Action> = PublishSubject.create<Action>()
    private val disposables = CompositeDisposable()

    init {
        state.value = initialState
        disposables.add(
                actions
                        .doOnNext({ Timber.d("${javaClass.simpleName}: action [$it]") })
                        .compose(mapActionsToResults())
                        .doOnNext({ Timber.d("${javaClass.simpleName}: apiModel [$it]") })
                        .distinctUntilChanged()
                        .scan(state.value!!, accumulateState())
                        .observeOn(appSchedulers.ui)
                        .doOnSubscribe({ Timber.d("${javaClass.simpleName}: onSubscribe") })
                        .doOnNext({ Timber.d("${javaClass.simpleName}: onNext[$it]") })
                        .doOnError({ Timber.d("${javaClass.simpleName}: onError[$it]") })
                        .doOnComplete({ Timber.d("${javaClass.simpleName}: onComplete") })
                        .doOnDispose({ Timber.d("${javaClass.simpleName}: onDispose") })
                        .subscribe(
                                { state.value = it },
                                {
                                    state.value = state.value?.copy(
                                            loading = false,
                                            error = parseStreamError(it),
                                            data = state.value?.data)
                                }
                        )
        )
    }

    fun setModel(model: M) {
        actions.onNext(Action.SetModelAction(model))
    }

    private fun mapActionsToResults(): ObservableTransformer<Action, Result> {
        return ObservableTransformer {
            Timber.d("${javaClass.simpleName}: mapActionsToResults")
            Observable.merge(
                    it.ofType(Action.SetModelAction::class.java)
                            .flatMap({
                                action -> Observable.just(Result.complete(action, BooleanModel(true)))
                            }),
            it.observeOn(appSchedulers.io).compose(invokeActions())
            )
        }
    }

    protected open fun parseStreamError(it: Throwable?): UiError? {
        return it?.let { UiError.GeneralUiError("${it.javaClass.simpleName}: ${it.message}") }
    }

    private fun accumulateState(): BiFunction<State<M>, Result, State<M>> {
        return BiFunction { current, result ->
            Timber.d("${javaClass.simpleName}: accumulateState: current [$current], apiModel [$result]")
            if (result.action is Action.SetModelAction<*>) {
                current.copy(
                        data = result.action.model as M?
                )
            } else {
                updateState(current, result)
            }
        }
    }

    protected abstract fun updateState(current: State<M>, result: Result): State<M>
    protected abstract fun invokeActions(): ObservableTransformer<Action, Result>

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

}