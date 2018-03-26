package hu.sztomek.wheresmybuddy.device.notification.actions

import android.os.Parcel
import hu.sztomek.wheresmybuddy.domain.NotificationAction
import hu.sztomek.wheresmybuddy.presentation.util.KParcelable
import hu.sztomek.wheresmybuddy.presentation.util.parcelableCreator

data class NewLocation(val locationId: String, override var notificationId: Int) : NotificationAction, KParcelable {

    private constructor(p: Parcel) : this(
            locationId = p.readString(),
            notificationId = p.readInt()
    )

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::NewLocation)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeString(locationId)
            writeInt(notificationId)
        }
    }

}