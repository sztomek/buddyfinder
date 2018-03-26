package hu.sztomek.wheresmybuddy.device.notification.actions

import android.os.Parcel
import hu.sztomek.wheresmybuddy.domain.NotificationAction
import hu.sztomek.wheresmybuddy.presentation.util.KParcelable
import hu.sztomek.wheresmybuddy.presentation.util.parcelableCreator

data class NewSelfLocation(override var notificationId: Int, val longitude: Double, val latitude: Double) : NotificationAction, KParcelable {

    private constructor(p: Parcel) : this(
            notificationId = p.readInt(),
            longitude = p.readDouble(),
            latitude = p.readDouble()
    )

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::NewSelfLocation)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeInt(notificationId)
            writeDouble(longitude)
            writeDouble(latitude)
        }
    }

}