package hu.sztomek.wheresmybuddy.domain.parser

import android.content.res.Resources
import hu.sztomek.wheresmybuddy.R
import hu.sztomek.wheresmybuddy.device.notification.actions.*
import hu.sztomek.wheresmybuddy.domain.NotificationAction
import hu.sztomek.wheresmybuddy.domain.action.Action
import javax.inject.Inject

class NotificationParser @Inject constructor() {

    companion object {
        private const val KEY_TYPE = "type"

        private const val KEY_NAME = "displayName"
        private const val KEY_PHOTO = "photoUrl"

        private const val ACTION_TYPE_CONNECTION_REQUEST = "CONNECTION_REQUEST"
        private const val KEY_CONNECTION_REQUEST_ID = "connectionRequestId"

        private const val ACTION_TYPE_LOCATION_RESPONSE = "LOCATION_RESPONSE"
        private const val KEY_LOCATION_ID = "locationId"

        private const val ACTION_TYPE_LOCATION_REQUEST_NOT_TRUSTED = "LOCATION_REQUEST_NOT_TRUSTED"
        private const val ACTION_TYPE_LOCATION_REQUEST_TRUSTED = "LOCATION_REQUEST_TRUSTED"
        private const val KEY_FROM_ID = "fromId"

        private const val ACTION_TYPE_CONNECTION = "NEW_CONNECTION"
        private const val KEY_PROFILE_ID = "profileId"

    }

    // TODO should be domain entity only
    @Inject
    lateinit var resources: Resources

    fun parseActions(notificationData: Map<String, String>): List<NotificationAction> {
        val type = notificationData[KEY_TYPE]
        return when (type) {
            ACTION_TYPE_CONNECTION_REQUEST -> {
                val requestId = notificationData[KEY_CONNECTION_REQUEST_ID]
                if (requestId == null) {
                    throw NotRecognisedActionException("Failed to parse type [$type] : Missing key [${KEY_CONNECTION_REQUEST_ID}]")
                } else {
                    listOf<NotificationAction>(
                            AcceptConnectionRequest(requestId, 0),
                            DeclineNotTrustedLocationRequest(requestId, 0)
                    )
                }
            }
            ACTION_TYPE_LOCATION_REQUEST_NOT_TRUSTED -> {
                val fromId = notificationData[KEY_FROM_ID]
                if (fromId == null) {
                    throw NotRecognisedActionException("Failed to parse type [$type] : Missing key [$KEY_FROM_ID]")
                } else {
                    listOf<NotificationAction>(
                            AcceptNotTrustedLocationRequest(fromId, 0),
                            DeclineNotTrustedLocationRequest(fromId, 0)
                    )
                }
            }
            ACTION_TYPE_LOCATION_REQUEST_TRUSTED -> {
                val fromId = notificationData[KEY_FROM_ID]
                if (fromId == null) {
                    throw NotRecognisedActionException("Failed to parse type [$type] : Missing key [$KEY_FROM_ID]")
                } else {
                    listOf<NotificationAction>(
                            TrustedLocationRequest(fromId, 0)
                    )
                }
            }
            ACTION_TYPE_LOCATION_RESPONSE -> {
                val locationId = notificationData[KEY_LOCATION_ID]
                if (locationId == null) {
                    throw NotRecognisedActionException("Failed to parse type [$type] : Missing key [$KEY_LOCATION_ID]")
                } else {
                    listOf<NotificationAction>(
                            NewLocation(locationId, 0)
                    )
                }
            }
            ACTION_TYPE_CONNECTION -> {
                val profileId = notificationData[KEY_PROFILE_ID]
                if (profileId == null) {
                    throw NotRecognisedActionException("Failed to parse type [$type] : Missing key [$KEY_PROFILE_ID]")
                } else {
                    listOf<NotificationAction>(
                            NewConnection(profileId, 0)
                    )
                }
            }
            else -> throw NotRecognisedActionException("Failed to parse type [$type]")
        }
    }

    private fun getNameFromData(data: Map<String, String>): String? {
        return data[KEY_NAME]
    }

    private fun getTypeFromData(data: Map<String, String>): String? {
        return data[KEY_TYPE]
    }

    private fun getPhotoFromData(data: Map<String, String>): String? {
        return data[KEY_PHOTO]
    }

    fun shouldShowNotification(data: Map<String, String>): Boolean {
        return data[KEY_TYPE] != ACTION_TYPE_LOCATION_REQUEST_TRUSTED
    }

    fun createPresentAction(data: Map<String, String>): Action.PresentNotificationAction {
        val typeFromData = getTypeFromData(data)
        return Action.PresentNotificationAction(
                when (typeFromData) {
                    ACTION_TYPE_CONNECTION -> resources.getString(R.string.label_new_connection)
                    ACTION_TYPE_CONNECTION_REQUEST -> resources.getString(R.string.label_new_connection_request)
                    ACTION_TYPE_LOCATION_REQUEST_NOT_TRUSTED -> resources.getString(R.string.label_new_location_request)
                    ACTION_TYPE_LOCATION_RESPONSE -> resources.getString(R.string.label_location_acquired)
                    else -> resources.getString(R.string.label_new_notification)
                },
                when (typeFromData) {
                    ACTION_TYPE_CONNECTION -> {
                        resources.getString(R.string.format_new_connection, getNameFromData(data) ?: R.string.label_unknown)
                    }
                    ACTION_TYPE_CONNECTION_REQUEST -> {
                        resources.getString(R.string.format_new_connection_request, getNameFromData(data) ?: R.string.label_unknown)
                    }
                    ACTION_TYPE_LOCATION_REQUEST_NOT_TRUSTED -> {
                        resources.getString(R.string.format_new_location_request, getNameFromData(data) ?: R.string.label_unknown)
                    }
                    ACTION_TYPE_LOCATION_RESPONSE -> {
                        resources.getString(R.string.format_location_provided, getNameFromData(data) ?: R.string.label_unknown)
                    }
                    else -> resources.getString(R.string.label_something_happened)
                },
                getPhotoFromData(data),
                parseActions(data))
    }

}