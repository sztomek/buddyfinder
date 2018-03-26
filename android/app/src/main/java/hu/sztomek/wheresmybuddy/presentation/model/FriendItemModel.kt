package hu.sztomek.wheresmybuddy.presentation.model

import hu.sztomek.wheresmybuddy.presentation.common.adapter.RecyclerViewItem
import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel

data class FriendItemModel(val profile: ProfileModel, val connectionModel: ConnectionModel, val statusInfo: StatusInfoModel): IdProvider, RecyclerViewItem {

    override val id: String?
        get() = connectionModel.userId
}