package hu.sztomek.wheresmybuddy.domain.model

import hu.sztomek.wheresmybuddy.domain.common.Entity


data class SearchResultItemModel(
        val user: UserModel,
        val connection: ConnectionModel?,
        val connectionRequest: ConnectionRequestModel?
): Entity