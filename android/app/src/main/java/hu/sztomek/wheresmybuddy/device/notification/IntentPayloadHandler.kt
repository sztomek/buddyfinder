package hu.sztomek.wheresmybuddy.device.notification

import android.content.Intent
import android.os.Parcelable
import hu.sztomek.wheresmybuddy.domain.NotificationAction
import hu.sztomek.wheresmybuddy.presentation.common.Helpers

class IntentPayloadHandler {

    companion object {
        const val ACTION_NEW_LOCATION = "buddyfinder.intent.action.newlocation"
        const val ACTION_NEW_BROADCAST_STOPPED = "buddyfinder.intent.action.broadcaststopped"
        private const val KEY_INTENT_ACTION = "buddyfinder.action"
        private const val KEY_LOCATION_ORIGINAL_ACTION = "buddyfinder.location.original"

        fun putActionToIntent(intent: Intent, action: NotificationAction) {
            Helpers.safeCastTo<Parcelable>(action)?.let {
                intent.putExtra(KEY_INTENT_ACTION, it)
            }
        }

        fun getActionFromIntent(intent: Intent?): NotificationAction? {
            val parcelableExtra = intent?.getParcelableExtra<Parcelable>(KEY_INTENT_ACTION)
            intent?.removeExtra(KEY_INTENT_ACTION)
            return Helpers.safeCastTo<NotificationAction>(parcelableExtra)
        }

        fun putOriginalActionToIntent(intent: Intent, original: NotificationAction) {
            Helpers.safeCastTo<Parcelable>(original)?.let {
                intent.putExtra(KEY_LOCATION_ORIGINAL_ACTION, it)
            }
        }

        fun getOriginalActionFromIntent(intent: Intent?): NotificationAction? {
            val parcelableExtra = intent?.getParcelableExtra<Parcelable>(KEY_LOCATION_ORIGINAL_ACTION)
            intent?.removeExtra(KEY_LOCATION_ORIGINAL_ACTION)
            return Helpers.safeCastTo<NotificationAction>(parcelableExtra)
        }

    }

}