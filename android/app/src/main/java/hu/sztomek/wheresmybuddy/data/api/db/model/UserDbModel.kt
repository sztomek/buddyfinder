package hu.sztomek.wheresmybuddy.data.api.db.model

data class UserDbModel(
        override val id: String?,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?
) : IdDbModel {

    // secondary constructor required by FireStore
    constructor() : this(null, null, null, null)

    companion object : IdCopyHelper<UserDbModel> {
        override fun copyWithId(id: String?, item: UserDbModel): UserDbModel {
            return item.copy(id = id)
        }
    }
}