package hu.sztomek.wheresmybuddy.device.notification.actions

import android.os.Parcel
import hu.sztomek.wheresmybuddy.domain.NotificationAction
import hu.sztomek.wheresmybuddy.presentation.util.KParcelable
import hu.sztomek.wheresmybuddy.presentation.util.parcelableCreator

data class NewConnection(val fromUserId: String, override var notificationId: Int) : NotificationAction, KParcelable {

    private constructor(p: Parcel) : this(
            fromUserId = p.readString(),
            notificationId = p.readInt()
    )

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::NewConnection)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeString(fromUserId)
            writeInt(notificationId)
        }
    }

}