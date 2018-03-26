package hu.sztomek.wheresmybuddy.presentation.model.persistable

import android.os.Parcel
import hu.sztomek.wheresmybuddy.presentation.model.ConnectionModel
import hu.sztomek.wheresmybuddy.presentation.model.ConnectionRequestModel
import hu.sztomek.wheresmybuddy.presentation.util.KParcelable
import hu.sztomek.wheresmybuddy.presentation.util.parcelableCreator
import hu.sztomek.wheresmybuddy.presentation.util.readNullable
import hu.sztomek.wheresmybuddy.presentation.util.writeNullable

data class ProfileDetailModel(val profileModel: ProfileModel, val connectionRequestModel: ConnectionRequestModel?, val connectionModel: ConnectionModel?) : KParcelable {

    private constructor(p: Parcel) : this(
            ProfileModel(
                    id = p.readNullable { p.readString() },
                    displayName = p.readNullable { p.readString() },
                    profilePicture = p.readNullable { p.readString() }
            ), null, null)


    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::ProfileDetailModel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeNullable(profileModel.id) { writeString(profileModel.id!!) }
            writeNullable(profileModel.displayName) { writeString(profileModel.displayName!!) }
            writeNullable(profileModel.profilePicture) { writeString(profileModel.profilePicture!!) }
        }
    }
}