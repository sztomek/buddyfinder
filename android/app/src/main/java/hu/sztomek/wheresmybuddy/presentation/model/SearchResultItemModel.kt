package hu.sztomek.wheresmybuddy.presentation.model

import hu.sztomek.wheresmybuddy.presentation.model.persistable.ProfileModel

private interface ProfileHolderItemModel : IdProvider{

    val profileModel: ProfileModel
    override val id: String?
        get() = profileModel.id

}

sealed class SearchResultItemModel : ProfileHolderItemModel {

    data class NotConnectedItemModel(override val profileModel: ProfileModel) : SearchResultItemModel()
    data class PendingOutgoingItemModel(override val profileModel: ProfileModel, val connectionRequestModel: ConnectionRequestModel) : SearchResultItemModel()
    data class PendingIncomingItemModel(override val profileModel: ProfileModel, val connectionRequestModel: ConnectionRequestModel) : SearchResultItemModel()
    data class ConnectedItemModel(override val profileModel: ProfileModel, val connectionModel: ConnectionModel) : SearchResultItemModel()
    data class TrustedItemModel(override val profileModel: ProfileModel, val connectionModel: ConnectionModel) : SearchResultItemModel()
    data class LoadingItemModel(override val profileModel: ProfileModel, val operationId: String) : SearchResultItemModel()

}