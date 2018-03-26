package hu.sztomek.wheresmybuddy.data.api.user.model

import hu.sztomek.wheresmybuddy.domain.model.UserModel

fun UserAuthModel.toDomainModel() = UserModel(id, displayName, email, photoUrl)