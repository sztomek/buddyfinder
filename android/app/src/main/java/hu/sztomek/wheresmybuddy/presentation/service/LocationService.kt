package hu.sztomek.wheresmybuddy.presentation.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.widget.Toast
import dagger.android.AndroidInjection
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.location.PermissionNotGrantedException
import hu.sztomek.wheresmybuddy.device.location.ResolvableLocationException
import hu.sztomek.wheresmybuddy.device.notification.IntentPayloadHandler
import hu.sztomek.wheresmybuddy.device.notification.actions.*
import hu.sztomek.wheresmybuddy.domain.INotifications
import hu.sztomek.wheresmybuddy.domain.NotificationAction
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.common.Result
import hu.sztomek.wheresmybuddy.domain.model.BooleanModel
import hu.sztomek.wheresmybuddy.domain.model.LocationModel
import hu.sztomek.wheresmybuddy.domain.model.NotificationModel
import hu.sztomek.wheresmybuddy.domain.usecase.BroadcastLocationUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.GetLocationUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.StopBroadcastLocationUseCase
import hu.sztomek.wheresmybuddy.domain.usecase.UploadLocationUseCase
import hu.sztomek.wheresmybuddy.presentation.common.UiError
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import javax.inject.Inject

class LocationService : Service() {

    companion object {
        const val ONETIME_NOTIFICATION_ID = 111
        private const val BROADCAST_NOTIFICATION_ID = 999

        fun startBroadcast(context: Context): Intent {
            val intent = Intent(context, LocationService::class.java)
            IntentPayloadHandler.putActionToIntent(intent, StartBroadcast(BROADCAST_NOTIFICATION_ID))
            return intent
        }

        fun stopBroadcast(context: Context): Intent {
            val intent = Intent(context, LocationService::class.java)
            IntentPayloadHandler.putActionToIntent(intent, StopBroadcast(BROADCAST_NOTIFICATION_ID))
            return intent
        }
    }

    @Inject
    lateinit var appSchedulers: AppSchedulers
    @Inject
    lateinit var getLocationUseCase: GetLocationUseCase
    @Inject
    lateinit var uploadLocationUseCase: UploadLocationUseCase
    @Inject
    lateinit var startBroadcastUseCase: BroadcastLocationUseCase
    @Inject
    lateinit var stopBroadcastLocationUseCase: StopBroadcastLocationUseCase
    @Inject
    lateinit var notifications: INotifications

    private var isRunningInForeground: Boolean = false
    private var locationUpdatesDisposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val actionFromIntent = IntentPayloadHandler.getActionFromIntent(intent)
            if (actionFromIntent != null) {
                notifications.dismiss(actionFromIntent.notificationId)
                when (actionFromIntent) {
                    is AcceptNotTrustedLocationRequest -> {
                        getOneTimeLocation(actionFromIntent.fromUserId, actionFromIntent)
                    }
                    is DeclineNotTrustedLocationRequest -> {
                        // TODO delete on server + start foreground while deleting
                        stopSelf()
                        return START_NOT_STICKY
                    }
                    is TrustedLocationRequest -> {
                        getOneTimeLocation(actionFromIntent.fromUserId, actionFromIntent)
                    }
                    is StartBroadcast -> {
                        stopForegroundService()
                        startLocationUpdates()
                    }
                    is StopBroadcast -> {
                        stopLocationUpdates()
                    }
                }
            } else {
                Timber.d("Failed to get action from intent: [$intent]")
                stopSelf()
            }
        } else {
            stopSelf()
        }

        return START_STICKY_COMPATIBILITY
    }

    private fun createNotificationBuilder(title: String, message: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_info)
                .setContentTitle(title)
                .setContentText(message)
                .setChannelId(notifications.ensureChannel())
    }

    private fun getOneTimeLocation(userId: String, originalAction: NotificationAction) {
        startForegroundService(
                ONETIME_NOTIFICATION_ID,
                createNotificationBuilder(
                        getString(R.string.default_loading_message),
                        getString(R.string.notification_acquiring_location)
                ).build()
        )

        val action = Action.GetLocationAction()
        getLocationUseCase.execute(action)
                .observeOn(appSchedulers.io)
                .toObservable()
                .flatMap {
                    when {
                        it.error != null -> Observable.just(it)
                        it.data is LocationModel -> {
                            val uploadAction = Action.UploadLocationAction(it.data.copy(toId = userId, public = false))
                            uploadLocationUseCase.execute(uploadAction)
                                    .toObservable()
                                    .onErrorReturn { t -> Result.error(uploadAction, t) }
                        }
                        else -> Observable.just(Result.error(action, UiError.GeneralUiError("Fail")))
                    }
                }
                .observeOn(appSchedulers.ui)
                .subscribe(
                        { result ->
                            Timber.d("onNext: $result")
                            if (result.data is BooleanModel) {
                                finishWithMessage(getString(R.string.message_ok_location))
                            }
                        },
                        { error ->
                            Timber.d("onError: $error")

                            when (error) {
                                is PermissionNotGrantedException -> {
                                    val id = 123
                                    notifications.show(id, NotificationModel.LocationPermissionErrorNotificationModel(
                                            getString(R.string.label_location_error),
                                            getString(R.string.message_location_permission),
                                            listOf(LocationPermissionRequired(id)),
                                            originalAction
                                    )
                                    )
                                    finishWithMessage(getString(R.string.error_unknown_error_location))
                                }
                                is ResolvableLocationException -> {
                                    val id = 234
                                    notifications.show(id, NotificationModel.LocationSettingsErrorNotificationModel(
                                            getString(R.string.label_location_error),
                                            getString(R.string.message_location_error_resolvable),
                                            listOf(LocationRecoverableError(id)),
                                            error,
                                            originalAction
                                    )
                                    )
                                    finishWithMessage(getString(R.string.error_unknown_error_location))
                                }
                                else -> {
                                    finishWithMessage(getString(R.string.error_unknown_error_location))
                                }
                            }

                            finishWithMessage(getString(R.string.error_unknown_error_location))
                        }
                )
    }

    private fun startForegroundService(notificationId: Int, notification: Notification) {
        if (!isRunningInForeground) {
            startForeground(notificationId, notification)
            isRunningInForeground = true
        }
    }

    private fun stopForegroundService() {
        stopForeground(true)
        isRunningInForeground = false
    }

    private fun startLocationUpdates() {
        startForegroundService(
                BROADCAST_NOTIFICATION_ID,
                createNotificationBuilder(
                        getString(R.string.notification_broadcast_title),
                        getString(R.string.notification_broadcast_message)
                ).addAction(
                        R.drawable.ic_decline,
                        getString(R.string.label_stop_broadcast),
                        PendingIntent.getService(this, BROADCAST_NOTIFICATION_ID, stopBroadcast(this), PendingIntent.FLAG_CANCEL_CURRENT)
                ).build()
        )

        if (locationUpdatesDisposable == null) {
            locationUpdatesDisposable = startBroadcastUseCase.execute(Action.StartBroadcastLocationAction())
                    .doOnNext {
                        uploadLocation(it.copy(public = true))
                        broadcastSelfLocationLocally(it)
                    }
                    .subscribeOn(appSchedulers.io)
                    .observeOn(appSchedulers.ui)
                    .subscribe({
                        Timber.d("New self location: [$it]")
                    }, {
                        stopLocationUpdates()
                        Timber.d("Failed to get self location: [$it]")
                    })
        }
    }

    private fun stopLocationUpdates() {
        if (locationUpdatesDisposable != null) {
            locationUpdatesDisposable?.dispose()
            locationUpdatesDisposable = null

            stopBroadcastLocationUseCase.execute(Action.StopBroadcastLocationAction())
                    .subscribeOn(appSchedulers.io)
                    .observeOn(appSchedulers.ui)
                    .subscribe({
                        Timber.d("Successfully stopped location updates")
                        finishWithMessage(getString(R.string.broadcast_stopped))
                        broadcastOff()
                    },{
                        Timber.d("Failed to stop location updates: [$it]")
                        finishWithMessage(getString(R.string.broadcast_stopped))
                    })
        }

    }

    private fun uploadLocation(location: LocationModel) {
        uploadLocationUseCase.execute(Action.UploadLocationAction(location))
                .subscribeOn(appSchedulers.io)
                .observeOn(appSchedulers.ui)
                .subscribe({
                    Timber.d("Successfully uploaded location [$it]")
                },{
                    Timber.d("Failed to upload location [$it]")
                })
    }

    private fun broadcastSelfLocationLocally(location: LocationModel) {
        if (location.longitude != null && location.latitude != null) {
            val intent = Intent(IntentPayloadHandler.ACTION_NEW_LOCATION)
            IntentPayloadHandler.putActionToIntent(intent, NewSelfLocation(0, location.longitude, location.latitude))
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(intent)
        }
    }

    private fun broadcastOff() {
        val intent = Intent(IntentPayloadHandler.ACTION_NEW_BROADCAST_STOPPED)
        IntentPayloadHandler.putActionToIntent(intent, BroadcastStopped(0))
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent)
    }

    private fun finishWithMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        stopForegroundService()
        stopSelf()
    }

}