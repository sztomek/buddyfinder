package hu.sztomek.wheresmybuddy.domain.model

import hu.sztomek.wheresmybuddy.domain.common.Entity

data class LocationModel(
        override val id: String?,
        val longitude: Double?,
        val latitude: Double?,
        val accuracy: Float?,
        val timestamp: Long?,
        val fromId: String?,
        val toId: String?,
        val public: Boolean?
) : IdModel, Entity