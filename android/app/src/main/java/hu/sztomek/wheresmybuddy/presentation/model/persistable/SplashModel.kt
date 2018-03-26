package hu.sztomek.wheresmybuddy.presentation.model.persistable

import android.os.Parcel
import hu.sztomek.wheresmybuddy.presentation.util.*

data class SplashModel(val hasUser: Boolean?, val hasGcmToken: Boolean?) : KParcelable {

    private constructor(p: Parcel) : this(
            hasUser = p.readNullable { p.readBoolean() },
            hasGcmToken = p.readNullable { p.readBoolean() }
    )

    companion object {
        @JvmField val CREATOR = parcelableCreator(::SplashModel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeNullable(hasUser) { dest.writeBoolean(hasUser!!) }
        dest.writeNullable(hasGcmToken) { dest.writeBoolean(hasGcmToken!!) }
    }

}