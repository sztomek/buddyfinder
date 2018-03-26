package hu.sztomek.wheresmybuddy.presentation.model.persistable

import android.os.Parcel
import hu.sztomek.wheresmybuddy.presentation.util.KParcelable
import hu.sztomek.wheresmybuddy.presentation.util.parcelableCreator
import hu.sztomek.wheresmybuddy.presentation.util.readNullable
import hu.sztomek.wheresmybuddy.presentation.util.writeNullable

data class LandingModel(val profileModel: ProfileModel?, val isBroadcasting: Boolean?) : KParcelable {


    private constructor(p: Parcel) : this(
            profileModel = p.readNullable { ProfileModel(p.readString(), null, null) },
            isBroadcasting = false
    )

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::LandingModel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeNullable(profileModel?.id) { writeString(profileModel?.id!!) }
        }
    }

}