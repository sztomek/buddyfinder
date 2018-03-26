package hu.sztomek.wheresmybuddy.device.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.device.notification.actions.AcceptNotTrustedLocationRequest
import hu.sztomek.wheresmybuddy.device.notification.actions.DeclineNotTrustedLocationRequest
import hu.sztomek.wheresmybuddy.domain.NotificationAction
import hu.sztomek.wheresmybuddy.domain.model.NotificationModel
import hu.sztomek.wheresmybuddy.presentation.screens.splash.SplashActivity
import hu.sztomek.wheresmybuddy.presentation.service.LocationService
import timber.log.Timber

internal class NotificationFactory(private val context: Context, private val imageLoader: ImageLoader) {

    fun createNotification(id: Int, notificationModel: NotificationModel, channelId: String): Notification? {
        val builder = NotificationCompat.Builder(context)
        builder.setContentTitle(notificationModel.title)
        builder.setContentText(notificationModel.message)
        builder.setSmallIcon(R.drawable.ic_info)
        builder.setAutoCancel(true)
        builder.setChannelId(channelId)
        when (notificationModel) {
            is NotificationModel.GeneralNotificationModel -> builder.setSmallIcon(R.drawable.ic_info)
            is NotificationModel.UserImageNotificationModel -> {
                builder.setLargeIcon(imageLoader.loadImage(notificationModel.photoUrl).blockingGet()) // TODO offload
            }
        }
        when (notificationModel.actions.size) {
            0 -> {
                builder.setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, SplashActivity::class.java), PendingIntent.FLAG_CANCEL_CURRENT))
            }
            1 -> {
                val intent = Intent(context, SplashActivity::class.java)
                val notificationAction = notificationModel.actions[0]
                notificationAction.notificationId = id
                IntentPayloadHandler.putActionToIntent(intent, notificationAction)
                if (notificationModel is NotificationModel.LocationSettingsErrorNotificationModel) {
                    IntentPayloadHandler.putOriginalActionToIntent(intent, notificationModel.originalAction)
                    SplashActivity.pendingException = notificationModel.exception
                }
                if (notificationModel is NotificationModel.LocationPermissionErrorNotificationModel) {
                    IntentPayloadHandler.putOriginalActionToIntent(intent, notificationModel.originalAction)
                }
                builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            }
            2 -> {
                notificationModel.actions.forEachIndexed { idx, action ->
                    val target = when (action) {
                        is AcceptNotTrustedLocationRequest, is DeclineNotTrustedLocationRequest -> {
                            val intent = Intent(context, LocationService::class.java)
                            action.notificationId = id
                            IntentPayloadHandler.putActionToIntent(intent, action)
                            PendingIntent.getService(context, idx, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                        }
                        else -> {
                            val intent = Intent(context, SplashActivity::class.java)
                            action.notificationId = id
                            IntentPayloadHandler.putActionToIntent(intent, action)
                            PendingIntent.getActivity(context, idx, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                        }
                    }
                    val positive = isPositiveAction(action)
                    builder.addAction(
                            if (positive) R.drawable.ic_accept else R.drawable.ic_decline,
                            context.getString(if (positive) R.string.label_accept else R.string.label_decline),
                            target
                    )
                }
            }
            else -> Timber.d("Unhandled NotificationModel.actions size: [${notificationModel.actions.size}]")
        }
        return builder
                .build()
    }

    private fun isPositiveAction(action: NotificationAction): Boolean {
        return action.javaClass.simpleName.startsWith("Accept")
    }
}