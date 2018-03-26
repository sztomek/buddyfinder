package hu.sztomek.wheresmybuddy.presentation.model.persistable

import android.os.Parcel
import hu.sztomek.wheresmybuddy.presentation.model.SearchResultItemModel
import hu.sztomek.wheresmybuddy.presentation.util.KParcelable
import hu.sztomek.wheresmybuddy.presentation.util.parcelableCreator
import hu.sztomek.wheresmybuddy.presentation.util.readNullable
import hu.sztomek.wheresmybuddy.presentation.util.writeNullable

data class SearchModel(
        var keyWord: String?,
        var lastId: String?,
        var results: List<SearchResultItemModel> = emptyList()
) : KParcelable {

    private constructor(p: Parcel) : this(
            keyWord = p.readNullable { p.readString() },
            lastId = p.readNullable { p.readString() }
    )

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::SearchModel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeNullable(keyWord) { writeString(keyWord!!) }
            writeNullable(lastId) { writeString(lastId!!) }
        }
    }

}