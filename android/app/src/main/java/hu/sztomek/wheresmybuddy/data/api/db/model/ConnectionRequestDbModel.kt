package hu.sztomek.wheresmybuddy.data.api.db.model

import java.util.*

data class ConnectionRequestDbModel(
        override val id: String?,
        val from: String?,
        val to: String?,
        val created: Date?
) : IdDbModel {

    // secondary constructor required by FireStore
    constructor() : this(null, null, null, null)

    companion object : IdCopyHelper<ConnectionRequestDbModel> {
        override fun copyWithId(id: String?, item: ConnectionRequestDbModel): ConnectionRequestDbModel {
            return item.copy(id = id)
        }
    }

}