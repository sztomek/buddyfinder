package hu.sztomek.wheresmybuddy.device.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import hu.sztomek.wheresmybuddy.device.media.ImageLoader
import hu.sztomek.wheresmybuddy.domain.INotifications
import hu.sztomek.wheresmybuddy.domain.model.NotificationModel

internal class Notifications(context: Context, private val manager: NotificationManager?, imageLoader: ImageLoader) : INotifications {

    companion object {
        private const val CHANNEL_ID = "buddyfinder.notifications"
    }

    private val factory: NotificationFactory = NotificationFactory(context, imageLoader)

    override fun ensureChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager?.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Buddyfinder", NotificationManager.IMPORTANCE_DEFAULT))
        }
        return CHANNEL_ID
    }

    override fun show(id: Int, data: NotificationModel) {
        manager?.notify(id, factory.createNotification(id, data, ensureChannel()))
    }

    override fun dismiss(id: Int) {
        manager?.cancel(id)
    }

}