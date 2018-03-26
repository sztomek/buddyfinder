package hu.sztomek.wheresmybuddy.domain.usecase

import hu.sztomek.wheresmybuddy.domain.INotifications
import hu.sztomek.wheresmybuddy.domain.action.Action
import hu.sztomek.wheresmybuddy.domain.model.NotificationModel
import io.reactivex.Completable
import javax.inject.Inject

class ShowNotificationUseCase @Inject constructor(private val notifications: INotifications) {

    fun execute(action: Action.PresentNotificationAction): Completable {
        return Completable.fromAction {
            val model: NotificationModel = if (action.photoUrl == null) {
                NotificationModel.GeneralNotificationModel(action.title, action.message, action.actions ?: listOf())
            } else NotificationModel.UserImageNotificationModel(action.title, action.message, action.photoUrl, action.actions ?: listOf())
            notifications.show(1, model)
        }
    }

}