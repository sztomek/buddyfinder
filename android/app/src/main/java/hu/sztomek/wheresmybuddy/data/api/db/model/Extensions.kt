package hu.sztomek.wheresmybuddy.data.api.db.model

import hu.sztomek.wheresmybuddy.domain.model.ConnectionModel
import hu.sztomek.wheresmybuddy.domain.model.ConnectionRequestModel
import hu.sztomek.wheresmybuddy.domain.model.LocationModel
import hu.sztomek.wheresmybuddy.domain.model.UserModel

fun ConnectionDbModel.toDomainModel(profileId: String?) = ConnectionModel(profileId, id, level, created?.time)

fun ConnectionRequestDbModel.toDomainModel() = ConnectionRequestModel(id, from, to, created?.time)

fun LocationDbModel.toDomainModel() = LocationModel(id, latLng?.longitude, latLng?.latitude, null, timestamp, from, to, public)

fun UserDbModel.toDomainModel() = UserModel(id, displayName, email, photoUrl)