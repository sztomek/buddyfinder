package hu.sztomek.wheresmybuddy.presentation.model.persistable

import android.os.Parcel
import hu.sztomek.wheresmybuddy.presentation.common.ListData
import hu.sztomek.wheresmybuddy.presentation.common.adapter.RecyclerViewItem
import hu.sztomek.wheresmybuddy.presentation.util.KParcelable
import hu.sztomek.wheresmybuddy.presentation.util.parcelableCreator
import hu.sztomek.wheresmybuddy.presentation.util.readNullable
import hu.sztomek.wheresmybuddy.presentation.util.writeNullable

data class PendingRequestsModel(
        val nameFilter: String?,
        val lastId: String?,
        override val results: List<RecyclerViewItem>
) : KParcelable, ListData<RecyclerViewItem> {

    private constructor(p: Parcel) : this(
            nameFilter = p.readNullable { p.readString() },
            lastId = p.readNullable { p.readString() },
            results = listOf()
    )


    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::PendingRequestsModel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeNullable(nameFilter) { writeString(nameFilter!!) }
            writeNullable(lastId) { writeString(lastId!!) }
        }
    }
}