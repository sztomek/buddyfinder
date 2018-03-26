package hu.sztomek.wheresmybuddy.device.notification.actions

import android.os.Parcel
import hu.sztomek.wheresmybuddy.domain.NotificationAction
import hu.sztomek.wheresmybuddy.presentation.util.KParcelable
import hu.sztomek.wheresmybuddy.presentation.util.parcelableCreator

class BroadcastStopped(override var notificationId: Int) : NotificationAction, KParcelable {

    private constructor(p: Parcel) : this(
            notificationId = p.readInt()
    )

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::BroadcastStopped)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeInt(notificationId)
        }
    }

}