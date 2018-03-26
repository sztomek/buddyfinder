package hu.sztomek.wheresmybuddy.presentation.model.persistable

import android.os.Parcel
import hu.sztomek.wheresmybuddy.presentation.common.ListData
import hu.sztomek.wheresmybuddy.presentation.common.adapter.RecyclerViewItem
import hu.sztomek.wheresmybuddy.presentation.util.*

data class FriendsModel(
        val nameFilter: String?,
        val lastId: String?,
        val trustedOnly: Boolean,
        override val results: List<RecyclerViewItem>
) : KParcelable, ListData<RecyclerViewItem> {

    private constructor(p: Parcel) : this(
            nameFilter = p.readNullable { p.readString() },
            lastId = p.readNullable { p.readString() },
            trustedOnly = p.readBoolean(),
            results = listOf()
    )


    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::FriendsModel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeNullable(nameFilter) { writeString(nameFilter!!) }
            writeNullable(lastId) { writeString(lastId!!) }
            writeBoolean(trustedOnly)
        }
    }

}