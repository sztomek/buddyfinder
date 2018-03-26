package hu.sztomek.wheresmybuddy.presentation.model.persistable

import android.os.Parcel
import hu.sztomek.wheresmybuddy.presentation.model.LocationModel
import hu.sztomek.wheresmybuddy.presentation.model.MarkerModel
import hu.sztomek.wheresmybuddy.presentation.util.KParcelable
import hu.sztomek.wheresmybuddy.presentation.util.parcelableCreator
import hu.sztomek.wheresmybuddy.presentation.util.readNullable
import hu.sztomek.wheresmybuddy.presentation.util.writeNullable

data class DiscoverModel(
        val targetMarker: MarkerModel?,
        val selfLongitude: Double?,
        val selfLatitude: Double?,
        val markers: List<MarkerModel> = emptyList()
) : KParcelable {

    constructor(locationId: String?) : this(MarkerModel(LocationModel(locationId, null, null, null, false, null), null, null), null, null)

    private constructor(p: Parcel) : this(
            locationId = p.readNullable { p.readString() }
    )

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::DiscoverModel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(dest) {
            writeNullable(targetMarker?.location?.id) { writeString(targetMarker?.location?.id!!) }
        }
    }

}