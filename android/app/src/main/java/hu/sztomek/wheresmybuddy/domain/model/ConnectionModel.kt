package hu.sztomek.wheresmybuddy.domain.model

import hu.sztomek.wheresmybuddy.domain.common.Entity

data class ConnectionModel(
        override val id: String?,
        val otherUser: String?,
        val level: Int?,
        val created: Long?
) : Entity, IdModel