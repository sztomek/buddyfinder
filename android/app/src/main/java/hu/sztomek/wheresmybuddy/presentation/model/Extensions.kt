package hu.sztomek.wheresmybuddy.presentation.model

import hu.sztomek.wheresmybuddy.domain.model.*
import hu.sztomek.wheresmybuddy.domain.model.ConnectionModel
import hu.sztomek.wheresmybuddy.domain.model.ConnectionRequestModel
import hu.sztomek.wheresmybuddy.domain.model.LocationModel
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel

fun UserModel.toUiModel() = ProfileModel(id, displayName, photoUrl)

fun ConnectionModel.toUiModel() = hu.sztomek.wheresmybuddy.presentation.model.ConnectionModel(otherUser, created, level ?:0 > 0)

fun ConnectionRequestModel.toUiModel() = hu.sztomek.wheresmybuddy.presentation.model.ConnectionRequestModel(id, created, from, to)

fun FriendsItemModel.toUiModel() = FriendItemModel(
        profile = this.userModel.toUiModel(),
        connectionModel = this.connectionModel.toUiModel(),
        statusInfo = StatusInfoModel(null, null, null)
)

fun PendingConnectionRequestItemModel.toUiModel() = PendingRequestItemModel(
        profile = this.userModel.toUiModel(),
        connectionRequestModel = this.connectionRequestModel.toUiModel(),
        infoModel = InfoModel(this.connectionRequestModel.created!!)
)

fun LocationModel.toUiModel() = hu.sztomek.wheresmybuddy.presentation.model.LocationModel(
        id = id,
        latitude = latitude,
        longitude = longitude,
        receiverId = toId,
        timestamp = timestamp,
        isPublic = public == true
)

fun DiscoverEntity.toUiModel(info: String?) = MarkerModel(location.toUiModel(), user.toUiModel(), info)