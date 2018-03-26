package hu.sztomek.wheresmybuddy.data.api.db.model

import java.util.*

data class ConnectionDbModel(
        override val id: String?,
        val created: Date?,
        val level: Int?
) : IdDbModel {

    // secondary constructor required by FireStore
    constructor() : this(null, null, null)

    companion object : IdCopyHelper<ConnectionDbModel> {
        override fun copyWithId(id: String?, item: ConnectionDbModel): ConnectionDbModel {
            return item.copy(id = id)
        }
    }

}