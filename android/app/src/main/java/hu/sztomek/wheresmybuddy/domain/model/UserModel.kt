package hu.sztomek.wheresmybuddy.domain.model

import hu.sztomek.wheresmybuddy.domain.common.Entity

data class UserModel(
        override val id: String?,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?
) : Entity, IdModel