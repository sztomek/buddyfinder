package hu.sztomek.wheresmybuddy.presentation.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import hu.sztomek.wheresmybuddy.device.notification.IntentPayloadHandler
import hu.sztomek.wheresmybuddy.device.notification.actions.TrustedLocationRequest
import hu.sztomek.wheresmybuddy.domain.common.AppSchedulers
import hu.sztomek.wheresmybuddy.domain.parser.NotificationParser
import hu.sztomek.wheresmybuddy.domain.usecase.ShowNotificationUseCase
import timber.log.Timber
import javax.inject.Inject

class MessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var showNotificationUseCase: ShowNotificationUseCase
    @Inject
    lateinit var appSchedulers: AppSchedulers
    @Inject
    lateinit var notificationParser: NotificationParser

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)
        Timber.d("onMessageReceived notification = [${message?.notification?.prettyPrint()}], data = [${message?.data}]")
        message?.let {
            if (notificationParser.shouldShowNotification(it.data)) {
                showNotificationUseCase.execute(
                        notificationParser.createPresentAction(it.data)
                ).subscribe(
                        { Timber.d("Successfully presented notification") },
                        { Timber.d("Failed to present notification : [$it]") }
                )
            } else {
                val parsedActions = notificationParser.parseActions(it.data)
                when {
                     parsedActions.size == 1 && parsedActions[0] is TrustedLocationRequest -> {
                        val intent = Intent(this, LocationService::class.java)
                        IntentPayloadHandler.putActionToIntent(intent, parsedActions[0])
                        startService(intent)
                    }
                    else -> {
                        Timber.d("Unhandled action type without showing notification: [$parsedActions]")
                    }
                }
            }
        }
    }

    private fun RemoteMessage.Notification.prettyPrint() = "Notification{title = $title, body = $body}"
}