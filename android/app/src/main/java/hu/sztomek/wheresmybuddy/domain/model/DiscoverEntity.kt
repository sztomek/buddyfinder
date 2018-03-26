package hu.sztomek.wheresmybuddy.domain.model

import hu.sztomek.wheresmybuddy.domain.common.Entity

data class DiscoverEntity(val location: LocationModel, val user: UserModel): Entity