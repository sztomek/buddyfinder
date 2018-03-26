package hu.sztomek.wheresmybuddy.domain.model

import hu.sztomek.wheresmybuddy.domain.common.Entity

data class ConnectionRequestModel(
        override val id: String?,
        val from: String?,
        val to: String?,
        val created: Long?
) : Entity, IdModel