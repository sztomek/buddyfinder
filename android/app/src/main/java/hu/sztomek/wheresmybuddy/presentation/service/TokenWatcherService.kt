package hu.sztomek.wheresmybuddy.presentation.service

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import dagger.android.AndroidInjection
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import hu.sztomek.wheresmybuddy.domain.usecase.HasActiveUserUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.UpdateGcmTokenUseCase
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TokenWatcherService : FirebaseInstanceIdService() {

    @Inject
    lateinit var useCase: UpdateGcmTokenUseCase
    @Inject
    lateinit var appSchedulers: AppSchedulers
    @Inject
    lateinit var hasActiveUserUseCase: HasActiveUserUseCase

    private var disposables: CompositeDisposable = CompositeDisposable()

    override fun onTokenRefresh() {
        val token = FirebaseInstanceId.getInstance().token
        Timber.d("onTokenRefresh - new token is [$token]")

        AndroidInjection.inject(this)

        hasActiveUserUseCase.execute(Action.HasActiveUserAction())
                .observeOn(appSchedulers.io)
                .subscribeOn(appSchedulers.ui)
                .toObservable()
                .flatMap {
                        if ((it.data as BooleanModel).value) {
                            useCase.execute(Action.UpdateGcmTokenAction(token!!)).toObservable()
                        } else {
                            Observable.error<BooleanModel>(NoSuchElementException())
                        }
                }.retryWhen {error ->
                    error.delay(1L, TimeUnit.MINUTES)
                    .flatMap {
                        if (it is NoSuchElementException) {
                            Observable.just(Unit)
                        } else {
                            Observable.error(it)
                        }
                    }
                }
                .subscribe(
                        { Timber.d("Successfully updated FCM token [$token]") },
                        { t ->
                            Timber.d("Failed to update FCM token [$token]: [$t]")
                        }
                )
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}