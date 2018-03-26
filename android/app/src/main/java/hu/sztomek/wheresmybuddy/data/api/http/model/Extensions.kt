package hu.sztomek.wheresmybuddy.data.api.http.model

import hu.sztomek.wheresmybuddy.domain.model.ConnectionModel
import hu.sztomek.wheresmybuddy.domain.model.ConnectionRequestModel
import hu.sztomek.wheresmybuddy.domain.model.LocationModel

fun ConnectionApiModel.toDomainModel(created: Long? = null, level: Int? = 0) = ConnectionModel(user1, user2, level, created)

fun ConnectionRequestApiModel.toDomainModel() = ConnectionRequestModel(id, from, to, created)

fun LocationApiModel.toDomainModel(fromId: String?, toId: String?, public: Boolean?) = LocationModel(id, longitude, latitude, null, timestamp, fromId, toId, public) // FIXME