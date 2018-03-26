package hu.sztomek.wheresmybuddy.data.api.db.model

import com.google.firebase.firestore.GeoPoint

data class LocationDbModel(
        override val id: String?,
        val latLng: GeoPoint?,
        val timestamp: Long?,
        val from: String?,
        val to: String?,
        val public: Boolean?
) : IdDbModel {

    // secondary constructor required by FireStore
    constructor() : this(null, null, null, null, null, null)

    companion object : IdCopyHelper<LocationDbModel> {

        override fun copyWithId(id: String?, item: LocationDbModel): LocationDbModel {
            return item.copy(id = id)
        }

    }

}