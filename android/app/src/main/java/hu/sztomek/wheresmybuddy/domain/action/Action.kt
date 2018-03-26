package hu.sztomek.wheresmybuddy.domain.action

import hu.sztomek.wheresmybuddy.domain.NotificationAction
import hu.sztomek.wheresmybuddy.domain.model.LocationModel

sealed class Action {

    private interface PagedListAction {
        val lastId: String?
        val fromLastId: Boolean
        val limit: Int?
        val loadMore: Boolean
    }

    data class SearchAction(val keyWord: String,
                            override val lastId: String?,
                            override val fromLastId: Boolean = true, // if it's false, then query ALL records until the lastId
                            override val limit: Int?,
                            override val loadMore: Boolean) : Action(), PagedListAction

    data class GetProfileAction(val userId: String?) : Action()
    data class GetProfileDetailsAction(val userId: String) : Action()
    data class UpdateProfileAction(val userId: String, val displayName: String?, val photoPath: String?) : Action()
    data class SetModelAction<M>(val model: M) : Action()
    data class SendRequestAction(val userId: String) : Action()
    data class CancelRequestAction(val requestId: String) : Action()
    data class AcceptRequestAction(val requestId: String) : Action()
    data class DeclineRequestAction(val requestId: String) : Action()
    data class LocateAction(val userId: String) : Action()
    class GetLocationAction : Action()
    class GetBroadcastStateAction : Action()
    class StartBroadcastLocationAction : Action()
    class StopBroadcastLocationAction : Action()
    class DiscoverAction: Action()
    data class UpdateSelfLocation(val longitude: Double, val latitude: Double) : Action()
    data class UploadLocationAction(val locationModel: LocationModel): Action()
    data class GetLocationByIdAction(val locationId: String): Action()
    data class DeleteConnectionAction(val userId: String) : Action()
    data class UpdateConnectionAction(val userId: String, val newLevel: Int) : Action()
    data class UpdateGcmTokenAction(val token: String) : Action()
    data class PresentNotificationAction(val title: String, val message: String, val photoUrl: String? = null, val actions: List<NotificationAction>? = null) : Action()
    class LogoutAction : Action()
    class HasActiveUserAction : Action()
    class CheckGcmTokenAction : Action()
    class DeleteAccountAction : Action()

    data class ListFriendsAction(
            val filterName: String?,
            override val lastId: String?,
            override val fromLastId: Boolean,  // if it's false, then query ALL records until the lastId
            val trustedOnly: Boolean,
            override val limit: Int?,
            override val loadMore: Boolean
    ) : Action(), PagedListAction

    data class ListIncomingRequestsAction(
            val filterName: String?,
            override val lastId: String?,
            override val fromLastId: Boolean,
            override val limit: Int?,
            override val loadMore: Boolean
    ) : Action(), PagedListAction

    data class ListOutgoingRequestsAction(
            val filterName: String?,
            override val lastId: String?,
            override val fromLastId: Boolean,
            override val limit: Int?,
            override val loadMore: Boolean
    ) : Action(), PagedListAction
}