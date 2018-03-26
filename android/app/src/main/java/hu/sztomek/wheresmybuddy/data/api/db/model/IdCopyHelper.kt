package hu.sztomek.wheresmybuddy.data.api.db.model

interface IdCopyHelper<T> {

    fun copyWithId(id: String?, item: T): T

}