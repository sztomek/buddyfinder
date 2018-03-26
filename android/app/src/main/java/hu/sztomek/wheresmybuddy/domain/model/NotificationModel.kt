package hu.sztomek.wheresmybuddy.domain.model

import hu.sztomek.wheresmybuddy.device.location.ResolvableLocationException
import hu.sztomek.wheresmybuddy.domain.NotificationAction

sealed class NotificationModel {

    abstract val title: String
    abstract val message: String
    abstract val actions: List<NotificationAction>

    data class GeneralNotificationModel(override val title: String, override val message: String, override val actions: List<NotificationAction>) : NotificationModel()
    data class UserImageNotificationModel(override val title: String, override val message: String, val photoUrl: String, override val actions: List<NotificationAction>) : NotificationModel()
    data class LocationSettingsErrorNotificationModel(override val title: String, override val message: String, override val actions: List<NotificationAction>, val exception: ResolvableLocationException, val originalAction: NotificationAction) : NotificationModel()
    data class LocationPermissionErrorNotificationModel(override val title: String, override val message: String, override val actions: List<NotificationAction>, val originalAction: NotificationAction) : NotificationModel()

}