package hu.sztomek.wheresmybuddy.domain

import hu.sztomek.wheresmybuddy.domain.model.NotificationModel

interface INotifications {

    fun ensureChannel(): String
    fun show(id: Int, data: NotificationModel)
    fun dismiss(id: Int)

}