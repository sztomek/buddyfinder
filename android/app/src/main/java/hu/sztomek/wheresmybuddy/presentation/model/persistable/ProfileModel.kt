package hu.sztomek.wheresmybuddy.presentation.model.persistable

import android.os.Parcel
import hu.sztomek.wheresmybuddy.presentation.util.KParcelable
import hu.sztomek.wheresmybuddy.presentation.util.parcelableCreator
import hu.sztomek.wheresmybuddy.presentation.util.readNullable
import hu.sztomek.wheresmybuddy.presentation.util.writeNullable

data class ProfileModel(
        val id: String?,
        val displayName: String?,
        val profilePicture: String?
) : KParcelable {

    private constructor(p: Parcel) : this(
            id = p.readNullable { p.readString() },
            displayName = p.readNullable { p.readString() },
            profilePicture = p.readNullable { p.readString() }
    )

    companion object {
        @JvmField val CREATOR = parcelableCreator(::ProfileModel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeNullable(id) {writeString(id!!)}
            writeNullable(displayName) {writeString(displayName!!)}
            writeNullable(profilePicture) {writeString(profilePicture!!)}
        }
    }

}